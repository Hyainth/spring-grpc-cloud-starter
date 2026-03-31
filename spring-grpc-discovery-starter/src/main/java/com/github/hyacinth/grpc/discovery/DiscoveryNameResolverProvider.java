package com.github.hyacinth.grpc.discovery;

import io.grpc.EquivalentAddressGroup;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;
import io.grpc.Status;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.StringUtils;

/**
 * {@link NameResolverProvider} used to fill the current gap where Spring gRPC
 * does not provide a Spring Cloud Discovery based resolver out of the box.
 * <p>
 * The implementation is intentionally small and isolated so it can be promoted into a
 * reusable starter or used as reference material for an upstream issue or pull request.
 */
public class DiscoveryNameResolverProvider extends NameResolverProvider {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryNameResolverProvider.class);

    private final DiscoveryClient discoveryClient;
    private final DiscoveryResolverProperties properties;

    private final Map<String, CopyOnWriteArrayList<DiscoveryNameResolver>> activeResolvers = new ConcurrentHashMap<>();

    public DiscoveryNameResolverProvider(DiscoveryClient discoveryClient, DiscoveryResolverProperties properties) {
        this.discoveryClient = discoveryClient;
        this.properties = properties;
    }

    @Override
    public NameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (!GrpcDiscoveryConstants.DISCOVERY_SCHEME.equals(targetUri.getScheme())) {
            return null;
        }
        String serviceName = extractServiceName(targetUri);
        if (!StringUtils.hasText(serviceName)) {
            return null;
        }
        return new DiscoveryNameResolver(serviceName, args);
    }

    @Override
    public String getDefaultScheme() {
        return GrpcDiscoveryConstants.DISCOVERY_SCHEME;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 6;
    }

    public void refreshAll(String reason) {
        activeResolvers.values().forEach(resolvers -> resolvers.forEach(resolver -> resolver.resolveNow(reason)));
    }

    private static String extractServiceName(URI targetUri) {
        String path = targetUri.getPath();
        if (StringUtils.hasText(path)) {
            return path.startsWith("/") ? path.substring(1) : path;
        }
        return targetUri.getAuthority();
    }

    private void register(String serviceName, DiscoveryNameResolver resolver) {
        activeResolvers.computeIfAbsent(serviceName, key -> new CopyOnWriteArrayList<>()).add(resolver);
    }

    private void unregister(String serviceName, DiscoveryNameResolver resolver) {
        CopyOnWriteArrayList<DiscoveryNameResolver> resolvers = activeResolvers.get(serviceName);
        if (resolvers == null) {
            return;
        }
        resolvers.remove(resolver);
        if (resolvers.isEmpty()) {
            activeResolvers.remove(serviceName, resolvers);
        }
    }

    private final class DiscoveryNameResolver extends NameResolver {

        private final String serviceName;

        private final Args args;

        private volatile Listener2 listener;
        private volatile NameResolver.ResolutionResult lastResolutionResult;

        private DiscoveryNameResolver(String serviceName, Args args) {
            this.serviceName = serviceName;
            this.args = args;
        }

        @Override
        public String getServiceAuthority() {
            return serviceName;
        }

        @Override
        public void start(Listener2 listener) {
            this.listener = listener;
            register(serviceName, this);
            resolveNow("startup");
        }

        @Override
        public void refresh() {
            resolveNow("grpc-refresh");
        }

        @Override
        public void shutdown() {
            unregister(serviceName, this);
        }

        private void resolveNow(String reason) {
            if (listener == null) {
                return;
            }
            Executor executor = args.getOffloadExecutor();
            Runnable resolveTask = () -> {
                try {
                    List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                    if (instances == null || instances.isEmpty()) {
                        if (lastResolutionResult != null) {
                            log.warn("No instances found for discovery target, keeping previous resolution. serviceName={}, reason={}",
                                    serviceName, reason);
                            return;
                        }
                        log.warn("No instances found for discovery target. serviceName={}, reason={}", serviceName, reason);
                        args.getSynchronizationContext().execute(
                                () -> listener.onError(Status.UNAVAILABLE.withDescription("No instance for " + serviceName))
                        );
                        return;
                    }

                    List<EquivalentAddressGroup> addresses = instances.stream().map(this::toAddressGroup).toList();
                    if (log.isDebugEnabled()) {
                        log.debug("Discovery resolver refreshed. serviceName={}, reason={}, addressCount={}",
                                serviceName, reason, addresses.size());
                    }

                    NameResolver.ResolutionResult.Builder resultBuilder = NameResolver.ResolutionResult.newBuilder()
                            .setAddresses(addresses);
                    NameResolver.ConfigOrError serviceConfig = buildServiceConfig();
                    if (serviceConfig != null) {
                        resultBuilder.setServiceConfig(serviceConfig);
                    }
                    NameResolver.ResolutionResult result = resultBuilder.build();
                    lastResolutionResult = result;

                    args.getSynchronizationContext().execute(() -> listener.onResult(result));
                } catch (RuntimeException ex) {
                    log.error("Discovery resolver failed fast. serviceName={}, reason={}", serviceName, reason, ex);
                    throw new IllegalStateException(
                            "Failed to resolve discovery target '" + serviceName + "' for reason '" + reason + "'",
                            ex
                    );
                }
            };

            if (executor != null) {
                executor.execute(resolveTask);
                return;
            }
            resolveTask.run();
        }

        private NameResolver.ConfigOrError buildServiceConfig() {
            String policy = properties.getLoadBalancingPolicy();
            if (!StringUtils.hasText(policy)) {
                return null;
            }
            Map<String, ?> serviceConfig = Map.of(
                    "loadBalancingConfig",
                    List.of(Map.of(policy, Collections.emptyMap()))
            );
            return args.getServiceConfigParser().parseServiceConfig(serviceConfig);
        }

        private EquivalentAddressGroup toAddressGroup(ServiceInstance instance) {
            int grpcPort = resolveGrpcPort(instance);
            String host = instance.getHost();
            if (log.isDebugEnabled()) {
                log.debug("Discovery resolver selected instance. serviceName={}, host={}, grpcPort={}, metadata={}",
                        serviceName, host, grpcPort, instance.getMetadata());
            }
            return new EquivalentAddressGroup(new InetSocketAddress(host, grpcPort));
        }

        private int resolveGrpcPort(ServiceInstance instance) {
            String grpcPort = instance.getMetadata().get(GrpcDiscoveryConstants.GRPC_PORT_METADATA_KEY);
            if (!StringUtils.hasText(grpcPort)) {
                return instance.getPort();
            }
            try {
                return Integer.parseInt(grpcPort);
            } catch (NumberFormatException ex) {
                log.warn("Invalid metadata.gRPC_port detected, fallback to instance port. serviceName={}, host={}, value={}",
                        serviceName, instance.getHost(), grpcPort);
                return instance.getPort();
            }
        }
    }
}

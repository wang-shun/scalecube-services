package io.scalecube.services.gateway.rsocket;

import io.netty.util.concurrent.DefaultThreadFactory;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.ByteBufPayload;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.gateway.Gateway;
import io.scalecube.services.gateway.GatewayConfig;
import io.scalecube.services.gateway.GatewayMetrics;
import io.scalecube.services.gateway.GatewayTemplate;
import io.scalecube.services.metrics.Metrics;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.resources.LoopResources;

public class RSocketGateway extends GatewayTemplate {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSocketGateway.class);

  private static final DefaultThreadFactory BOSS_THREAD_FACTORY =
      new DefaultThreadFactory("rsws-boss", true);

  private static final Duration START_TIMEOUT = Duration.ofSeconds(30);

  private CloseableChannel server;

  @Override
  public Mono<Gateway> start(
      GatewayConfig config,
      Executor workerThreadPool,
      boolean preferNative,
      ServiceCall.Call call,
      Metrics metrics) {

    return Mono.defer(
        () -> {
          LOGGER.info("Starting gateway with {}", config);

          GatewayMetrics metrics1 = new GatewayMetrics(config.name(), metrics);
          RSocketGatewayAcceptor acceptor = new RSocketGatewayAcceptor(call.create(), metrics1);

          LoopResources loopResources =
              prepareLoopResources(preferNative, BOSS_THREAD_FACTORY, config, workerThreadPool);

          WebsocketServerTransport rsocketTransport =
              WebsocketServerTransport.create(
                  prepareHttpServer(loopResources, config.port(), metrics1));

          return RSocketFactory.receive()
              .frameDecoder(
                  frame ->
                      ByteBufPayload.create(
                          frame.sliceData().retain(), frame.sliceMetadata().retain()))
              .acceptor(acceptor)
              .transport(rsocketTransport)
              .start()
              .doOnSuccess(server -> this.server = server)
              .doOnSuccess(
                  server ->
                      LOGGER.info(
                          "Rsocket Gateway has been started successfully on {}", server.address()))
              .then(Mono.just(this));
        });
  }

  @Override
  public InetSocketAddress address() {
    return server.address();
  }

  @Override
  public Mono<Void> stop() {
    return Mono.defer(
        () -> {
          List<Mono<Void>> stopList = new ArrayList<>();
          stopList.add(shutdownBossGroup());
          if (server != null) {
            server.dispose();
            stopList.add(server.onClose());
          }
          return Mono.when(stopList);
        });
  }
}

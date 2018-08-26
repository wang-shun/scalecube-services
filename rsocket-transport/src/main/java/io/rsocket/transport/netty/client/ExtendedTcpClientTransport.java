package io.rsocket.transport.netty.client;

import io.rsocket.DuplexConnection;
import io.rsocket.transport.netty.RSocketLengthCodec;
import io.rsocket.transport.netty.TcpDuplexConnection;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.NettyPipeline.SendOptions;
import reactor.ipc.netty.tcp.TcpClient;

/**
 * Extended class for RSocket's {@link TcpClientTransport}. The point of it is to control channel
 * flushing behavior. See class {@link SendOptions} for more details.
 */
public class ExtendedTcpClientTransport implements io.rsocket.transport.ClientTransport {

  private final TcpClient client;

  /**
   * Constructor for this tcp server transport.
   *
   * @param client tcp client
   */
  public ExtendedTcpClientTransport(TcpClient client) {
    this.client = client;
  }

  @Override
  public Mono<DuplexConnection> connect() {
    return Mono.create(
        sink ->
            client
                .newHandler(
                    (in, out) -> {
                      in.context().addHandler(new RSocketLengthCodec());
                      out.options(SendOptions::flushOnEach);
                      // out.options(SendOptions::flushOnBoundary);
                      TcpDuplexConnection connection =
                          new TcpDuplexConnection(in, out, in.context());
                      sink.success(connection);
                      return connection.onClose();
                    })
                .doOnError(sink::error)
                .subscribe());
  }
}

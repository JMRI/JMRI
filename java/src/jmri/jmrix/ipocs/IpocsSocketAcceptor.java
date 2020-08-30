package jmri.jmrix.ipocs;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpocsSocketAcceptor implements CompletionHandler<AsynchronousSocketChannel, Object> {
  private final static Logger log = LoggerFactory.getLogger(IpocsSocketAcceptor.class);
  private final AsynchronousServerSocketChannel serverSocket;
  private final IpocsPortController portController;

  public IpocsSocketAcceptor(IpocsPortController portController, AsynchronousServerSocketChannel serverSocket) {
    this.serverSocket = serverSocket;
    this.portController = portController;
  }

  @Override
  public void completed(AsynchronousSocketChannel result, Object attachment) {
    serverSocket.accept(null, this);
    log.info("Client connected");
    IpocsClientHandler c = new IpocsClientHandler(result);
    c.addClientListener(portController);
  }

  @Override
  public void failed(Throwable exc, Object attachment) {
    log.error("Unable to accept socket");
  }
}

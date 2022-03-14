package jmri.jmrix.ipocs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class IpocsSocketAdapterTest {
  
  @Test
  public void constructorTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    AsynchronousServerSocketChannel serverSocket = mock(AsynchronousServerSocketChannel.class);
    assertNotNull(new IpocsSocketAcceptor(portController, serverSocket));
  }

  @Test
  public void completedTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    AsynchronousServerSocketChannel serverSocket = mock(AsynchronousServerSocketChannel.class);
    IpocsSocketAcceptor acceptor = new IpocsSocketAcceptor(portController, serverSocket);
    AsynchronousSocketChannel client = mock(AsynchronousSocketChannel.class);
    acceptor.completed(client, null);
    jmri.util.JUnitAppender.suppressErrorMessage("Unable to accept socket");
  }

  @Test
  public void failedTest() {
    IpocsPortController portController = mock(IpocsPortController.class);
    AsynchronousServerSocketChannel serverSocket = mock(AsynchronousServerSocketChannel.class);
    IpocsSocketAcceptor acceptor = new IpocsSocketAcceptor(portController, serverSocket);
    acceptor.failed(new Exception(), null);
    jmri.util.JUnitAppender.suppressErrorMessage("Unable to accept socket");
  }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

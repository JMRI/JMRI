package jmri.jmrix.ipocs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.HashMap;

import org.junit.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.util.JUnitUtil;
import jmri.util.zeroconf.ZeroConfService;

public class IpocsPortControllerTest {
  
  private final IpocsClientListener listener = new IpocsClientListener() {
    @Override
    public String getUserName() {
      return "Vx91";
    }

    @Override
    public void clientConnected(final IpocsClientHandler client) {
    }

    @Override
    public void clientDisconnected(final IpocsClientHandler client) {
    }

    @Override
    public void onMessage(final IpocsClientHandler client, final Message msg) {
    }
  };

  @Test
  public void constructorTest() throws IOException {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    assertNotNull(new IpocsPortController(memo));
  }

  @Test
  public void getSystemConnectionMemoTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    assertEquals(memo, pc.getSystemConnectionMemo());
  }

  @Test
  public void configureTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    pc.configure();
  }

  @Test
  public void connectTest() throws IOException {
    try (MockedStatic<AsynchronousServerSocketChannel> theMock = Mockito.mockStatic(AsynchronousServerSocketChannel.class)) {
      try (MockedStatic<ZeroConfService> theZcsMock = Mockito.mockStatic(ZeroConfService.class)) {
        AsynchronousServerSocketChannel serverSocket = mock(AsynchronousServerSocketChannel.class);
        when(serverSocket.getLocalAddress()).thenReturn(new InetSocketAddress("localhost", 0));
        theMock.when(AsynchronousServerSocketChannel::open).thenReturn(serverSocket);

        ZeroConfService mdnsService = mock(ZeroConfService.class);
        when(ZeroConfService.create("_ipocs._tcp.local.", "ipocs", 0, 0, 0, new HashMap<String, String>())).thenReturn(mdnsService);
      
        IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
        new IpocsPortController(memo).connect();
      }
    }
  }

  @Test
  public void getInputStreamTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    assertThrows(UnsupportedOperationException.class, () -> pc.getInputStream());
  }

  @Test
  public void getOutputStreamTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    assertThrows(UnsupportedOperationException.class, () -> pc.getOutputStream());
  }

  @Test
  public void getCurrentPortName() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    assertEquals("IPOCSMR", pc.getCurrentPortName());
  }

  @Test
  public void addListenerTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    pc.addListener(listener);
  }

  @Test
  public void removeListenerTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    pc.removeListener(listener);
  }

  @Test
  public void clientConnectedTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    pc.clientConnected(client);
  }

  @Test
  public void onMessageTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    pc.addListener(listener);
    Message msg = new Message();
    msg.setObjectName("Vx91");
    pc.onMessage(client, msg);
    msg.setObjectName("Vx92");
    pc.onMessage(client, msg);
  }

  @Test
  public void clientDisconnectedTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    pc.addListener(listener);
    pc.clientDisconnected(client);
    Message msg = new Message();
    msg.setObjectName("Vx91");
    pc.onMessage(client, msg);
    pc.clientDisconnected(client);
  }

  @Test
  public void sendTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    pc.addListener(listener);
    Message msg = new Message();
    msg.setObjectName("Vx91");
    pc.send(msg);
    pc.onMessage(client, msg);
    pc.send(msg);
  }
  
  @Test
  public void getLastStatusTest() {
    IpocsSystemConnectionMemo memo = mock(IpocsSystemConnectionMemo.class);
    IpocsPortController pc = new IpocsPortController(memo);
    IpocsClientHandler client = mock(IpocsClientHandler.class);
    pc.addListener(listener);
    Message msg = new Message();
    msg.setObjectName("Vx91");
    assertNull(pc.getLastStatus("Vx91"));
    pc.onMessage(client, msg);
    assertNotNull(pc.getLastStatus("Vx91"));
  }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

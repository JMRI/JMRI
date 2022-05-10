package jmri.jmrix.ipocs;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.packets.ConnectionRequestPacket;
import jmri.jmrix.ipocs.protocol.packets.SignOfLifePacket;

public class IpocsClientHandlerTest {

  private AsynchronousSocketChannel client;

  private final IpocsClientListener listener = new IpocsClientListener() {
    @Override
    public String getUserName() {
      return null;
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
  public void constructorTest() {
    assertNotNull(new IpocsClientHandler(client));
  }

  @Test
  public void getUnitIdTest() {
    assertEquals(null, new IpocsClientHandler(client).getUnitId());
  }

  @Test
  public void completedClosedTest() throws IOException {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    ch.completed(-1, null);

    doThrow(new IOException()).when(client).close();
    ch.completed(-1, null);
    
    JUnitAppender.assertErrorMessage("Unable to close client: null");
  }

  @Test
  public void completedReceived1Test() throws IOException {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    final Message msg = new Message();
    msg.setObjectName("testing");
    final ByteBuffer buff = msg.serialize();
    buff.position(buff.capacity());
    ch.completed(buff.capacity(), buff);
  }

  @Test
  public void completedReceived2Test() throws IOException {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    final Message msg = new Message();
    msg.setObjectName("testing");
    msg.getPackets().add(new SignOfLifePacket());
    final ByteBuffer buff = msg.serialize();
    buff.position(buff.capacity());
    ch.completed(buff.capacity(), buff);
  }

  @Test
  public void completedReceived3Test() throws IOException {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    final Message msg = new Message();
    msg.setObjectName("5");
    final ConnectionRequestPacket pkt = new ConnectionRequestPacket();
    pkt.setProtocolVersion((short) 0x0000);
    pkt.setSiteDataVersion("3df43d");
    msg.getPackets().add(pkt);
    final ByteBuffer buff = msg.serialize();
    buff.position(buff.capacity());
    ch.completed(buff.capacity(), buff);
  }

  @Test
  public void failedTest() throws IOException {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.failed(new Exception("Error"), null);
    jmri.util.JUnitAppender.suppressErrorMessage("Error closing connection");

    ch.addClientListener(listener);
    client.close();

    ch.failed(new Exception("Error"), null);
    jmri.util.JUnitAppender.suppressErrorMessage("Error closing connection");
  }

  @Test
  public void addClientListenerTest() {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
  }

  @Test
  public void removeClientListenerTest() {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.removeClientListener(listener);
  }

  @Test
  public void sendMessageTest() {
    final IpocsClientHandler ch = new IpocsClientHandler(client);
    final Message msg = mock(Message.class);
    when(msg.serialize()).thenReturn(ByteBuffer.wrap(new byte[] {}));
    ch.send(msg);
  }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        client = mock(AsynchronousSocketChannel.class);
    }

    @AfterEach
    public void tearDown() {
        client = null;
        JUnitUtil.tearDown();
    }

}

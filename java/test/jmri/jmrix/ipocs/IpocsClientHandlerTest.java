package jmri.jmrix.ipocs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jmri.jmrix.ipocs.protocol.Message;
import jmri.jmrix.ipocs.protocol.packets.ConnectionRequestPacket;
import jmri.jmrix.ipocs.protocol.packets.SignOfLifePacket;

public class IpocsClientHandlerTest {

  @Mock
  public AsynchronousSocketChannel client;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private IpocsClientListener listener = new IpocsClientListener() {
    @Override
    public String getUserName() {
      return null;
    }

    @Override
    public void clientConnected(IpocsClientHandler client) {
    }

    @Override
    public void clientDisconnected(IpocsClientHandler client) {
    }

    @Override
    public void onMessage(IpocsClientHandler client, Message msg) {
    }
  }; 

  @Test
  public void constructorTest() {
    assertNotNull(new IpocsClientHandler(client));
  }

  @Test
  public void getUnitIdTest() {
    assertEquals(0, new IpocsClientHandler(client).getUnitId());
  }

  @Test
  public void completedClosedTest() throws IOException {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    ch.completed(-1, null);
    doThrow(new IOException()).when(client).close();
    ch.completed(-1, null);
  }
  
  @Test
  public void completedReceived1Test() throws IOException {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    Message msg = new Message();
    msg.setObjectName("testing");
    ByteBuffer buff = msg.serialize();
    buff.position(buff.capacity());
    ch.completed(buff.capacity(), buff);
  }

  @Test
  public void completedReceived2Test() throws IOException {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    Message msg = new Message();
    msg.setObjectName("testing");
    msg.getPackets().add(new SignOfLifePacket());
    ByteBuffer buff = msg.serialize();
    buff.position(buff.capacity());
    ch.completed(buff.capacity(), buff);
  }

  @Test
  public void completedReceived3Test() throws IOException {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
    Message msg = new Message();
    msg.setObjectName("5");
    ConnectionRequestPacket pkt = new ConnectionRequestPacket();
    pkt.setProtocolVersion((short)0x0000);
    pkt.setSiteDataVersion("3df43d");
    msg.getPackets().add(pkt);
    ByteBuffer buff = msg.serialize();
    buff.position(buff.capacity());
    ch.completed(buff.capacity(), buff);
  }

  @Test
  public void failedTest() throws IOException {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.failed(new Exception("Error"), null);
    jmri.util.JUnitAppender.suppressErrorMessage("Error closing connection");
    ch.addClientListener(listener);
    doThrow(new IOException()).when(client).close();
    ch.failed(new Exception("Error"), null);
    jmri.util.JUnitAppender.suppressErrorMessage("Error closing connection");
  }

  @Test
  public void addClientListenerTest() {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.addClientListener(listener);
  }

  @Test
  public void removeClientListenerTest() {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    ch.removeClientListener(listener);
  }

  @Test
  public void sendMessageTest() {
    IpocsClientHandler ch = new IpocsClientHandler(client);
    Message msg = mock(Message.class);
    when(msg.serialize()).thenReturn(ByteBuffer.wrap(new byte[] {}));
    ch.send(msg);
  }
}

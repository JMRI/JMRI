package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ConnectionResponsePacketTest {
  private final byte[] testPacket = { 0x01, 0x23 };

  @Test
  public void getIdTest() {
    assertEquals(ConnectionResponsePacket.IDENT, new ConnectionResponsePacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ConnectionResponsePacket pkt = new ConnectionResponsePacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(0x0123, pkt.getProtocolVersion());
  }

  @Test
  public void serializeSpecificTest() {
    ConnectionResponsePacket pkt = new ConnectionResponsePacket();
    pkt.setProtocolVersion((short)0x0123);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

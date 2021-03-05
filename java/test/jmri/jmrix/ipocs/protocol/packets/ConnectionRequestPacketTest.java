package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ConnectionRequestPacketTest {
  private final byte[] testPacket = { 0x01, 0x23, 0x20, 0x00 };

  @Test
  public void getIdTest() {
    assertEquals(ConnectionRequestPacket.IDENT, new ConnectionRequestPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ConnectionRequestPacket pkt = new ConnectionRequestPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(0x0123, pkt.getProtocolVersion());
    assertEquals(" ", pkt.getSiteDataVersion());
  }

  @Test
  public void serializeSpecificTest() {
    ConnectionRequestPacket pkt = new ConnectionRequestPacket();
    pkt.setProtocolVersion((short)0x0123);
    pkt.setSiteDataVersion(" ");
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

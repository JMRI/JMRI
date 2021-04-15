package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqDisconnect;

public class DisconnectPacketTest {
  private final byte[] testPacket = { RqDisconnect.UnitClosingDown.value };

  @Test
  public void getIdTest() {
    assertEquals(DisconnectPacket.IDENT, new DisconnectPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    DisconnectPacket pkt = new DisconnectPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqDisconnect.UnitClosingDown, pkt.getReason());
  }

  @Test
  public void serializeSpecificTest() {
    DisconnectPacket pkt = new DisconnectPacket();
    pkt.setReason(RqDisconnect.UnitClosingDown);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

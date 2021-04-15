package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class SignOfLifeTimerPacketTest {
  private final byte[] testPacket = { 0x10, 0x11 };

  @Test
  public void getIdTest() {
    assertEquals(SignOfLifeTimerPacket.IDENT, new SignOfLifeTimerPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    SignOfLifeTimerPacket pkt = new SignOfLifeTimerPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(0x1011, pkt.getInterval());
  }

  @Test
  public void serializeSpecificTest() {
    SignOfLifeTimerPacket pkt = new SignOfLifeTimerPacket();
    pkt.setInterval((short)0x1011);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

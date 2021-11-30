package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ResetControllerPacketTest {
  private final byte[] testPacket = { };

  @Test
  public void getIdTest() {
    assertEquals(ResetControllerPacket.IDENT, new ResetControllerPacket().getId()); 
  }

  @Test
  public void parseSpecificTest() {
    ResetControllerPacket pkt = new ResetControllerPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
  }

  @Test
  public void serializeSpecificTest() {
    ResetControllerPacket pkt = new ResetControllerPacket();
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

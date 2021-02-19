package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;


public class AcknowledgementPacketTest {
  private byte[] testPacket = { 0x00, 0x02 };

  @Test
  public void getIdTest() {
    assertEquals(AcknowledgementPacket.IDENT, new AcknowledgementPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    AcknowledgementPacket pkt = new AcknowledgementPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(0x0002, pkt.getAckReason());
  }

  @Test
  public void serializeSpecificTest() {
    AcknowledgementPacket pkt = new AcknowledgementPacket();
    pkt.setAckReason((short)0x0002);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

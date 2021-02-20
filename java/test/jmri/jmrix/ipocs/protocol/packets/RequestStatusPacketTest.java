package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

public class RequestStatusPacketTest {
  private byte[] testPacket = { };

  @Test
  public void getIdTest() {
    assertEquals(RequestStatusPacket.IDENT, new RequestStatusPacket().getId()); 
  }

  @Test
  public void parseSpecificTest() {
    RequestStatusPacket pkt = new RequestStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
  }

  @Test
  public void serializeSpecificTest() {
    RequestStatusPacket pkt = new RequestStatusPacket();
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqPointsCommand;

public class ThrowPointsPacketTest {
  private final byte[] testPacket = { RqPointsCommand.Left.value };

  @Test
  public void getIdTest() {
    assertEquals(ThrowPointsPacket.IDENT, new ThrowPointsPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ThrowPointsPacket pkt = new ThrowPointsPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqPointsCommand.Left, pkt.getCommand());
  }

  @Test
  public void serializeSpecificTest() {
    ThrowPointsPacket pkt = new ThrowPointsPacket();
    pkt.setCommand(RqPointsCommand.Left);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

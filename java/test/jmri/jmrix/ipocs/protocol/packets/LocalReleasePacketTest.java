package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqReleaseCommand;

public class LocalReleasePacketTest {
  private final byte[] testPacket = { RqReleaseCommand.LocalControl.value };

  @Test
  public void getIdTest() {
    assertEquals(LocalReleasePacket.IDENT, new LocalReleasePacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    LocalReleasePacket pkt = new LocalReleasePacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqReleaseCommand.LocalControl, pkt.getCommand());
  }

  @Test
  public void serializeSpecificTest() {
    LocalReleasePacket pkt = new LocalReleasePacket();
    pkt.setCommand(RqReleaseCommand.LocalControl);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

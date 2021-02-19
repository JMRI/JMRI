package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqLevelCrossingState;
import jmri.jmrix.ipocs.protocol.enums.RqReleaseState;

public class LevelCrossingStatusPacketTest {
  private byte[] testPacket = { RqLevelCrossingState.Opening.value, RqReleaseState.LocalControl.value, 0x01, 0x23 };

  @Test
  public void getIdTest() {
    assertEquals(LevelCrossingStatusPacket.IDENT, new LevelCrossingStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    LevelCrossingStatusPacket pkt = new LevelCrossingStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqLevelCrossingState.Opening, pkt.getState());
    assertEquals(RqReleaseState.LocalControl, pkt.getReleaseState());
    assertEquals(0x0123, pkt.getOperationTime());
  }

  @Test
  public void serializeSpecificTest() {
    LevelCrossingStatusPacket pkt = new LevelCrossingStatusPacket();
    pkt.setState(RqLevelCrossingState.Opening);
    pkt.setReleaseState(RqReleaseState.LocalControl);
    pkt.setOperationTime((short)0x0123);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqDerailerState;
import jmri.jmrix.ipocs.protocol.enums.RqReleaseState;

public class DerailerStatusPacketTest {
  private final byte[] testPacket = { RqDerailerState.NonPassable.value, RqReleaseState.LocalControl.value, 0x01, 0x23 };

  @Test
  public void getIdTest() {
    assertEquals(DerailerStatusPacket.IDENT, new DerailerStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    DerailerStatusPacket pkt = new DerailerStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqDerailerState.NonPassable, pkt.getState());
    assertEquals(RqReleaseState.LocalControl, pkt.getReleaseState());
    assertEquals(0x0123, pkt.getOperationTime());
  }

  @Test
  public void serializeSpecificTest() {
    DerailerStatusPacket pkt = new DerailerStatusPacket();
    pkt.setState(RqDerailerState.NonPassable);
    pkt.setReleaseState(RqReleaseState.LocalControl);
    pkt.setOperationTime((short)0x0123);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

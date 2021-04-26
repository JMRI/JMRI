package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqPointsLockState;

public class ElectricalPointsLockStatusPacketTest {
  private final byte[] testPacket = { RqPointsLockState.LockedRight.value };

  @Test
  public void getIdTest() {
    assertEquals(ElectricalPointsLockStatusPacket.IDENT, new ElectricalPointsLockStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ElectricalPointsLockStatusPacket pkt = new ElectricalPointsLockStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqPointsLockState.LockedRight, pkt.getState());
  }

  @Test
  public void serializeSpecificTest() {
    ElectricalPointsLockStatusPacket pkt = new ElectricalPointsLockStatusPacket();
    pkt.setState(RqPointsLockState.LockedRight);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

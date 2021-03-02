package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqPointsState;
import jmri.jmrix.ipocs.protocol.enums.RqReleaseState;

public class PointsStatusPacketTest {
  private final byte[] testPacket = { RqPointsState.Left.value, RqReleaseState.LocalControl.value, 0x01, 0x23 };

  @Test
  public void getIdTest() {
    assertEquals(PointsStatusPacket.IDENT, new PointsStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    PointsStatusPacket pkt = new PointsStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqPointsState.Left, pkt.getState());
    assertEquals(RqReleaseState.LocalControl, pkt.getReleaseState());
    assertEquals(0x0123, pkt.getOperationTime());
  }

  @Test
  public void serializeSpecificTest() {
    PointsStatusPacket pkt = new PointsStatusPacket();
    pkt.setState(RqPointsState.Left);
    pkt.setReleaseState(RqReleaseState.LocalControl);
    pkt.setOperationTime((short)0x0123);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

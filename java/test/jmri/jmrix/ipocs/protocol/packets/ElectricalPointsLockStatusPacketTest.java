package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqPointsLockState;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

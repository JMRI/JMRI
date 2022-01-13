package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqPointsLockCommand;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class SetElectricalPointsLockPacketTest {
  private final byte[] testPacket = { RqPointsLockCommand.Lock.value };

  @Test
  public void getIdTest() {
    assertEquals(SetElectricalPointsLockPacket.IDENT, new SetElectricalPointsLockPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    SetElectricalPointsLockPacket pkt = new SetElectricalPointsLockPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqPointsLockCommand.Lock, pkt.getCommand());
  }

  @Test
  public void serializeSpecificTest() {
    SetElectricalPointsLockPacket pkt = new SetElectricalPointsLockPacket();
    pkt.setCommand(RqPointsLockCommand.Lock);
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

package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqPointsLockCommand;

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
}

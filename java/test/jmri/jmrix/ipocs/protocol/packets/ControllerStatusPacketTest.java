package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqControllerState;

public class ControllerStatusPacketTest {
  private final byte[] testPacket = { RqControllerState.Restarting.value };

  @Test
  public void getIdTest() {
    assertEquals(ControllerStatusPacket.IDENT, new ControllerStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ControllerStatusPacket pkt = new ControllerStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqControllerState.Restarting, pkt.getState());
  }

  @Test
  public void serializeSpecificTest() {
    ControllerStatusPacket pkt = new ControllerStatusPacket();
    pkt.setState(RqControllerState.Restarting);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

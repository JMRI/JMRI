package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.enums.RqInputState;

public class InputStatusPacketTest {
  private byte[] testPacket = { RqInputState.On.value };

  @Test
  public void getIdTest() {
    assertEquals(InputStatusPacket.IDENT, new InputStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    InputStatusPacket pkt = new InputStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqInputState.On, pkt.getState());
  }

  @Test
  public void serializeSpecificTest() {
    InputStatusPacket pkt = new InputStatusPacket();
    pkt.setState(RqInputState.On);
    assertArrayEquals(testPacket, pkt.serializeSpecific());
  }
}

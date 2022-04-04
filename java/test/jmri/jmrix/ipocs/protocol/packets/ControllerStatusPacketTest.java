package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqControllerState;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

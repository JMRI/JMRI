package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqPointsCommand;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class ThrowPointsPacketTest {
  private final byte[] testPacket = { RqPointsCommand.Left.value };

  @Test
  public void getIdTest() {
    assertEquals(ThrowPointsPacket.IDENT, new ThrowPointsPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ThrowPointsPacket pkt = new ThrowPointsPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqPointsCommand.Left, pkt.getCommand());
  }

  @Test
  public void serializeSpecificTest() {
    ThrowPointsPacket pkt = new ThrowPointsPacket();
    pkt.setCommand(RqPointsCommand.Left);
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

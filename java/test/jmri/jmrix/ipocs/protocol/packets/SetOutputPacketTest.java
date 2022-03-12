package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqOutputCommand;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class SetOutputPacketTest {
  private final byte[] testPacket = { RqOutputCommand.Off.value };

  @Test
  public void getIdTest() {
    assertEquals(SetOutputPacket.IDENT, new SetOutputPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    SetOutputPacket pkt = new SetOutputPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqOutputCommand.Off, pkt.getCommand());
  }

  @Test
  public void serializeSpecificTest() {
    SetOutputPacket pkt = new SetOutputPacket();
    pkt.setCommand(RqOutputCommand.Off);
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

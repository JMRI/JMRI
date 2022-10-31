package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqLevelCrossingCommand;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class SetLevelCrossingPacketTest {
  private final byte[] testPacket = {RqLevelCrossingCommand.Close.value, 0x00, 0x10, 0x01};

  @Test
  public void getIdTest() {
    assertEquals(SetLevelCrossingPacket.IDENT, new SetLevelCrossingPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    final SetLevelCrossingPacket pkt = new SetLevelCrossingPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqLevelCrossingCommand.Close, pkt.getCommand());
    assertEquals(0x0010, pkt.getDelay());
    assertEquals(0x01, pkt.getTrack());
  }

  @Test
  public void serializeSpecificTest() {
    final SetLevelCrossingPacket pkt = new SetLevelCrossingPacket();
    pkt.setCommand(RqLevelCrossingCommand.Close);
    pkt.setDelay((short)0x0010);
    pkt.setTrack((byte)0x01);
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

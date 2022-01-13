package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqDerailerCommand;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class SetDerailerPacketTest {
  private final byte[] testPacket = { RqDerailerCommand.Passable.value };

  @Test
  public void getIdTest() {
    assertEquals(SetDerailerPacket.IDENT, new SetDerailerPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    SetDerailerPacket pkt = new SetDerailerPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqDerailerCommand.Passable, pkt.getCommand());
  }

  @Test
  public void serializeSpecificTest() {
    SetDerailerPacket pkt = new SetDerailerPacket();
    pkt.setCommand(RqDerailerCommand.Passable);
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

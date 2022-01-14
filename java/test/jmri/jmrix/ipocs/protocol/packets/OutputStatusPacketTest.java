package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqOutputState;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class OutputStatusPacketTest {
  private final byte[] testPacket = { RqOutputState.On.value };

  @Test
  public void getIdTest() {
    assertEquals(OutputStatusPacket.IDENT, new OutputStatusPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    OutputStatusPacket pkt = new OutputStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(RqOutputState.On, pkt.getState());
  }

  @Test
  public void serializeSpecificTest() {
    OutputStatusPacket pkt = new OutputStatusPacket();
    pkt.setState(RqOutputState.On);
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

package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class RequestStatusPacketTest {
  private final byte[] testPacket = { };

  @Test
  public void getIdTest() {
    assertEquals(RequestStatusPacket.IDENT, new RequestStatusPacket().getId()); 
  }

  @Test
  public void parseSpecificTest() {
    RequestStatusPacket pkt = new RequestStatusPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
  }

  @Test
  public void serializeSpecificTest() {
    RequestStatusPacket pkt = new RequestStatusPacket();
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

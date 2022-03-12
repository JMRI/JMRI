package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class ApplicationDataPacketTest {
  private final byte[] testPacket = { 0x01, 0x23, 0x20, 0x00 };

  @Test
  public void getIdTest() {
    assertEquals(ApplicationDataPacket.IDENT, new ApplicationDataPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    ApplicationDataPacket pkt = new ApplicationDataPacket();
    pkt.setLength((byte)7);
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(0x0123, pkt.getxUser());
    assertEquals(ByteBuffer.wrap(new byte[] { 0x20, 0x00 }), pkt.getData());
  }

  @Test
  public void serializeSpecificTest() {
    ApplicationDataPacket pkt = new ApplicationDataPacket();
    pkt.setxUser((short)0x0123);
    pkt.setData(ByteBuffer.wrap(new byte[] { 0x20, 0x00 }));
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

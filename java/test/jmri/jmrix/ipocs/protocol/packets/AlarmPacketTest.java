package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqAlarmState;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

public class AlarmPacketTest {
  private final byte[] testPacket = { 0x01, 0x23, 0x45, RqAlarmState.Transient.value, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x02 };

  @Test
  public void getIdTest() {
    assertEquals(AlarmPacket.IDENT, new AlarmPacket().getId());
  }

  @Test
  public void parseSpecificTest() {
    AlarmPacket pkt = new AlarmPacket();
    pkt.parseSpecific(ByteBuffer.wrap(testPacket));
    assertEquals(0x0123, pkt.getAlarmCode());
    assertEquals(0x45, pkt.getAlarmLevel());
    assertEquals(RqAlarmState.Transient, pkt.getAlarmState());
    assertEquals(1, pkt.getParameter1());
    assertEquals(2, pkt.getParameter2());
  }

  @Test
  public void serializeSpecificTest() {
    AlarmPacket pkt = new AlarmPacket();
    pkt.setAlarmCode((short)0x0123);
    pkt.setAlarmLevel((byte)0x45);
    pkt.setAlarmState(RqAlarmState.Transient);
    pkt.setParameter1(1);
    pkt.setParameter2(2);
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

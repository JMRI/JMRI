package jmri.jmrix.ipocs.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;

import jmri.jmrix.ipocs.protocol.packets.SignOfLifePacket;

public class MessageTest {
  private final byte[] testMessage = { 0x09, 0x56, 0x78, 0x39, 0x31, 0x00, 0x17, 0x03, 0x00 };
  @Test
  public void testSerialize() {
    Message m = new Message();
    m.setObjectName("Vx91");
    m.getPackets().add(new SignOfLifePacket());
    ByteBuffer buffer = m.serialize();
    assertArrayEquals(testMessage, buffer.array(), "Message not as expected");
  }

  @Test
  public void testParse() {
    Message m = Message.parse(ByteBuffer.wrap(testMessage), testMessage.length);
    assertEquals("Vx91", m.getObjectName());
    assertEquals(1, m.getPackets().size());
    assertEquals(SignOfLifePacket.class, m.getPackets().get(0).getClass());
  }
}

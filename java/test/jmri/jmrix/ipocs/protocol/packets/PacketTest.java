package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

public class PacketTest {
  Packet pkt = null;
  private final byte[] testPacket = { 0x20, 0x03, 0x12 };

  @BeforeEach
  public void beforeEach() {
    pkt = new Packet() {
      @Override
      public byte getId() {
        return (char)0x20;
      }
  
      @Override
      protected void parseSpecific(ByteBuffer buffer) {
      }
  
      @Override
      protected byte[] serializeSpecific() {
        return new byte[0];
      }
    };
  }

  @Test
  public void ackTest() {
    if (pkt == null) {
      beforeEach();
    }
    byte[] bytes = new byte[1];
    new Random().nextBytes(bytes);
    pkt.setAck(bytes[0]);
    assertEquals(bytes[0], pkt.getAck());
  }

  @Test
  public void parseTest() {
    // This test is not a really good one - it relies on the actual ServiceLoader.
    // Could be solved by using PowerMockito:
    // stackoverflow.com/questions/21105403/mocking-static-methods-with-mockito
    byte[] successPacket = { ResetControllerPacket.IDENT, 0x03, 0x00 };
    assertNotNull(Packet.parse(ByteBuffer.wrap(successPacket)));
    //assertNull(Packet.parse(ByteBuffer.wrap(testPacket))); // throws IO Exception, packet=null
    //jmri.util.JUnitAppender.suppressErrorMessage("No packet found for identifier 32"); // better to fix method (or test input)
  }

  @Test
  public void serializeTest() {
    if (pkt == null) {
      beforeEach();
    }
    pkt.setAck((byte)0x12);
    assertEquals(ByteBuffer.wrap(testPacket), pkt.serialize());
    assertEquals(0x03, pkt.getLength());
  }

}

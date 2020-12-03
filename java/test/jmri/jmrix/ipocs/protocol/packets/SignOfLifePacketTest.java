package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class SignOfLifePacketTest {
  @Test
  public void getIdTest() {
    assertEquals(SignOfLifePacket.IDENT, new SignOfLifePacket().getId()); 
  }
}

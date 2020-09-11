package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class ResetControllerPacketTest {
  @Test
  public void getIdTest() {
    assertEquals(ResetControllerPacket.IDENT, new ResetControllerPacket().getId()); 
  }
}

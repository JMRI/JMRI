package jmri.jmrix.ipocs.protocol.packets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class RequestStatusPacketTest {
  @Test
  public void getIdTest() {
    assertEquals(RequestStatusPacket.IDENT, new RequestStatusPacket().getId()); 
  }
}

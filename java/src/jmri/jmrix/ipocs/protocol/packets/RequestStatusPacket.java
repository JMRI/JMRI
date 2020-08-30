package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class RequestStatusPacket extends Packet {
  public final static byte IDENT = 7;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
  }

  @Override
  protected byte[] serializeSpecific() {
    return new byte[] {};
  }
}

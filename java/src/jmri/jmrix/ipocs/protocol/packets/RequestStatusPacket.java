package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

/**
 * Request status from any object.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
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

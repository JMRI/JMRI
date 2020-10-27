package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

/**
 * Sign Of Life sent by IPOCS unit upon request by SignOfLifeTimerPacket.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SignOfLifePacket extends Packet {
  public final static byte IDENT = 23;
  
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

package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqDisconnect;

/**
 * Force a unit to disconnect and connect again, with a reason.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class DisconnectPacket extends Packet {
  public final static byte IDENT = 3;
  private RqDisconnect reason = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    reason = RqDisconnect.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(reason.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqDisconnect getReason() {
    return reason;
  }

  public void setReason(RqDisconnect reason) {
    this.reason = reason;
  }
}

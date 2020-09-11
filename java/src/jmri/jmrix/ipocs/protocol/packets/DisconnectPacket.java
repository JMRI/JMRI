package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqDisconnect;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class DisconnectPacket extends Packet {
  public final static byte IDENT = 3;
  private RqDisconnect reason;

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
    ByteBuffer buffer = ByteBuffer.allocate(2);
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

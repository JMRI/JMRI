package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class DisconnectPacket extends Packet {
  public final static byte IDENT = 3;
  private short reason;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    reason = buffer.getShort();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    buffer.putShort(reason);
    buffer.rewind();
    return buffer.array();
  }

  public short getReason() {
    return reason;
  }

  public void setReason(short reason) {
    this.reason = reason;
  }
}

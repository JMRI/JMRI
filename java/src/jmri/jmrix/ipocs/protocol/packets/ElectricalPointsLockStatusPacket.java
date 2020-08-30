package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class ElectricalPointsLockStatusPacket extends Packet {
  public final static byte IDENT = 21;
  private byte state;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = buffer.get();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(state);
    buffer.rewind();
    return buffer.array();
  }

  public byte getState() {
    return state;
  }

  public void setState(byte state) {
    this.state = state;
  }
}

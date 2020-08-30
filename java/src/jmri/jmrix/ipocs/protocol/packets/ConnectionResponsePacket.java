package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class ConnectionResponsePacket extends Packet {
  public final static byte IDENT = 2;
  private short protocolVersion;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    buffer.putShort(protocolVersion);
    buffer.rewind();
    return buffer.array();
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    protocolVersion = buffer.getShort();
  }

  public short getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(short protocolVersion) {
    this.protocolVersion = protocolVersion;
  }
}

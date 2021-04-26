package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

/**
 * Client sends this when initiating a connection.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class ConnectionRequestPacket extends Packet {
  public final static byte IDENT = 1;
  private short protocolVersion;
  private String siteDataVersion = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(2 + siteDataVersion.length() + 1);
    buffer.putShort(protocolVersion);
    buffer.put(siteDataVersion.getBytes());
    buffer.put((byte)0);
    buffer.rewind();
    return buffer.array();
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    protocolVersion = buffer.getShort();
    // Get object name
    StringBuilder sb = new StringBuilder();
    byte last;
    while ((last = buffer.get()) != 0x00) {
      sb.append((char) last);
    }
    siteDataVersion = sb.toString();
  }

  public short getProtocolVersion() {
    return protocolVersion;
  }

  public void setProtocolVersion(short protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  public String getSiteDataVersion() {
    return siteDataVersion;
  }

  public void setSiteDataVersion(String siteDataVersion) {
    this.siteDataVersion = siteDataVersion;
  }
}

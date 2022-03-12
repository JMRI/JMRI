package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

/**
 * Packet to send just about anything.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class ApplicationDataPacket extends Packet {
  public final static byte IDENT = 5;
  private short xUser;
  private ByteBuffer data = ByteBuffer.allocate(0);

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    xUser = buffer.getShort();
    data = ByteBuffer.allocate(getLength() - 5);
    data.put(buffer);
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(2 + data.capacity());
    buffer.putShort(xUser);
    buffer.put(data);
    buffer.rewind();
    return buffer.array();
  }

  public short getxUser() {
    return xUser;
  }

  public void setxUser(short xUser) {
    this.xUser = xUser;
  }

  public ByteBuffer getData() {
    data.rewind();
    return data;
  }

  public void setData(ByteBuffer data) {
    this.data = data;
  }
}

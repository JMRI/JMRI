package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

/**
 * Request IPOCS unit to periodically send sign of life notifications.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SignOfLifeTimerPacket extends Packet {
  public final static byte IDENT = 8;
  private short interval;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    interval = buffer.getShort();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    buffer.putShort(interval);
    buffer.rewind();
    return buffer.array();
  }

  public short getInterval() {
    return interval;
  }

  public void setInterval(short interval) {
    this.interval = interval;
  }
}

package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

/**
 * Packet transmitted as a response when requested by sender of a message and packet.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class AcknowledgementPacket extends Packet {
  public final static byte IDENT = 4;
  private short ackReason;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    ackReason = buffer.getShort();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(2);
    buffer.putShort(ackReason);
    buffer.rewind();
    return buffer.array();
  }

  public short getAckReason() {
    return ackReason;
  }

  public void setAckReason(short ackReason) {
    this.ackReason = ackReason;
  }
}

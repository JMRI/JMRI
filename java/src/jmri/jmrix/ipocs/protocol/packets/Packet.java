package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base packet functionality
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public abstract class Packet {
  private final static Logger log = LoggerFactory.getLogger(Packet.class);
  private byte length;
  private byte ack;

  public abstract byte getId();

  public byte getAck() {
    return ack;
  }

  public void setAck(byte ack) {
    this.ack = ack;
  }

  public byte getLength() {
      return length;
  }

  public void setLength(byte length) {
      this.length = length;
  }

  public static Packet parse(ByteBuffer buffer)  {
    Packet pkt = null;
    {
      byte id = buffer.get();
      // Get length
      byte length = buffer.get();
      // get if ack is requested
      byte ack = buffer.get();
      // Find packet type
      ServiceLoader<Packet> loader = ServiceLoader.load(Packet.class);
      for (Packet implClass : loader) {
        if (implClass.getId() == id) {
          pkt = implClass;
          pkt.ack = ack;
          pkt.length = length;
          break;
        }
      }
      if (pkt == null) {
        log.error("No packet found for identifier {}", id);
        buffer.position(buffer.position() + length - 3);
        return null;
      }
      log.debug("Found packet type {}", pkt.getClass().getSimpleName());
    }
    // parse the rest of the packet
    pkt.parseSpecific(buffer);
    return pkt;
  }

  public ByteBuffer serialize() {
    byte[] specific = serializeSpecific();
    length = (byte)(specific.length + 3);
    ByteBuffer buffer = ByteBuffer.allocate(length);
    buffer.put(getId());
    buffer.put(length);
    buffer.put(ack);
    buffer.put(specific);
    buffer.rewind();
    return buffer;
  }

  protected abstract void parseSpecific(ByteBuffer buffer);

  protected abstract byte[] serializeSpecific();
}

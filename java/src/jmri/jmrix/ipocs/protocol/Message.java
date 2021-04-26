package jmri.jmrix.ipocs.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.ipocs.protocol.packets.Packet;

/**
 * Represents a IPOCS Message.
 *
 * Protocol details can be found on the project website, https://ipocsmr.github.io
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class Message {
  private final static Logger log = LoggerFactory.getLogger(Message.class);
  private byte length;
  private String objectName;
  private final List<Packet> packets = new ArrayList<Packet>();

  public byte getLength() {
    return length;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
      this.objectName = objectName;
  }

  public List<Packet> getPackets() {
    return packets;
  }

  public static Message parse(ByteBuffer buffer, Integer contentSize) {
    int startPos = buffer.position();
    Message msg = new Message();
    // Get length
    msg.length = buffer.get();
    // Ensure that there are enough bytes in the buffer:
    if (msg.length > contentSize) {
      return null;
    }
    // Get object name
    StringBuilder sb = new StringBuilder();
    byte last;
    while ((last = buffer.get()) != 0x00) {
      sb.append((char) last);
    }
    msg.objectName = sb.toString();
    // Parse packets
    Packet packet;
    log.debug("Message for {}", msg.objectName);
    while (buffer.position() - startPos < msg.length && (packet = Packet.parse(buffer)) != null) {
      msg.packets.add(packet);
    }
    return msg;
  }

  public ByteBuffer serialize() {
    ByteBuffer buffer = ByteBuffer.allocate(1 + objectName.length() + 1);
    buffer.put(length);
    buffer.put(objectName.getBytes());
    buffer.put((byte)0);
    for (Packet packet : packets) {
      ByteBuffer serPacket = packet.serialize();
      ByteBuffer oldBuffer = buffer;
      oldBuffer.rewind();
      serPacket.rewind();
      buffer = ByteBuffer.allocate(buffer.capacity() + serPacket.capacity());
      buffer.put(oldBuffer);
      buffer.put(serPacket);
    }
    if (buffer.capacity() > 0xFF) {
      throw new SerializationException("Serialized message is longer than protocol allows for.");
    }
    buffer.put(0, (byte)buffer.capacity());

    buffer.rewind();
    return buffer;
  }
}

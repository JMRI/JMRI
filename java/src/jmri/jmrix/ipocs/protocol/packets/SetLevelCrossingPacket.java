package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SetLevelCrossingPacket extends Packet {
  public final static byte IDENT = 12;
  private byte command;
  private short delay;
  private byte track;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = buffer.get();
    delay = buffer.getShort();
    track = buffer.get();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.put(command);
    buffer.putShort(delay);
    buffer.put(track);
    buffer.rewind();
    return buffer.array();
  }

  public byte getCommand() {
    return command;
  }

  public void setCommand(byte command) {
    this.command = command;
  }

  public short getDelay() {
    return delay;
  }

  public void setDelay(short delay) {
    this.delay = delay;
  }

  public byte getTrack() {
    return track;
  }

  public void setTrack(byte track) {
    this.track = track;
  }
}

package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqLevelCrossingCommand;

/**
 * Level Crossing Order
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SetLevelCrossingPacket extends Packet {
  public final static byte IDENT = 12;
  private RqLevelCrossingCommand command = null;
  private short delay;
  private byte track;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = RqLevelCrossingCommand.valueOf(buffer.get());
    delay = buffer.getShort();
    track = buffer.get();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.put(command.value);
    buffer.putShort(delay);
    buffer.put(track);
    buffer.rewind();
    return buffer.array();
  }

  public RqLevelCrossingCommand getCommand() {
    return command;
  }

  public void setCommand(RqLevelCrossingCommand command) {
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

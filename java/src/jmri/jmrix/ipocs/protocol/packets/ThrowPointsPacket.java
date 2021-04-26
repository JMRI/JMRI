package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqPointsCommand;

/**
 * Points/Turnout Order
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class ThrowPointsPacket extends Packet {
  public final static byte IDENT = 10;
  private RqPointsCommand command = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = RqPointsCommand.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(command.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqPointsCommand getCommand() {
    return command;
  }

  public void setCommand(RqPointsCommand command) {
    this.command = command;
  }
}

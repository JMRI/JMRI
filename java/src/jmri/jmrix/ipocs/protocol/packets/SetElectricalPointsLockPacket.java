package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqPointsLockCommand;

/**
 * Points lock order
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SetElectricalPointsLockPacket extends Packet {
  public final static byte IDENT = 14;
  private RqPointsLockCommand command = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = RqPointsLockCommand.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(command.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqPointsLockCommand getCommand() {
    return command;
  }

  public void setCommand(RqPointsLockCommand command) {
    this.command = command;
  }
}

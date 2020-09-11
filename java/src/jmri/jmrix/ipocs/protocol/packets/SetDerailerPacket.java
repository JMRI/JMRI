package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqDerailerCommand;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SetDerailerPacket extends Packet {
  public final static byte IDENT = 4;
  private RqDerailerCommand command;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = RqDerailerCommand.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(command.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqDerailerCommand getCommand() {
    return command;
  }

  public void setCommand(RqDerailerCommand command) {
    this.command = command;
  }
}

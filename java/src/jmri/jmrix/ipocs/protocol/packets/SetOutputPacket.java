package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqOutputCommand;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SetOutputPacket extends Packet {
  public final static byte IDENT = 13;
  private RqOutputCommand command;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = RqOutputCommand.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(command.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqOutputCommand getCommand() {
    return command;
  }

  public void setCommand(RqOutputCommand command) {
    this.command = command;
  }
}

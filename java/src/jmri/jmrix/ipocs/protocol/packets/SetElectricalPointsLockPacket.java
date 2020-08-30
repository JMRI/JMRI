package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class SetElectricalPointsLockPacket extends Packet {
  public final static byte IDENT = 14;
  private byte command;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    command = buffer.get();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(command);
    buffer.rewind();
    return buffer.array();
  }

  public byte getCommand() {
    return command;
  }

  public void setCommand(byte command) {
    this.command = command;
  }
}

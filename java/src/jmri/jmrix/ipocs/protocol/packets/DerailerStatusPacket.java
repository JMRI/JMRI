package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class DerailerStatusPacket extends Packet {
  public final static byte IDENT = 18;
  private byte state;
  private byte releaseState;
  private short operationTime;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = buffer.get();
    releaseState = buffer.get();
    operationTime = buffer.getShort();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.put(state);
    buffer.put(releaseState);
    buffer.putShort(operationTime);
    buffer.rewind();
    return buffer.array();
  }

  public byte getState() {
    return state;
  }

  public void setState(byte state) {
    this.state = state;
  }

  public byte getReleaseState() {
    return releaseState;
  }

  public void setReleaseState(byte releaseState) {
    this.releaseState = releaseState;
  }

  public short getOperationTime() {
    return operationTime;
  }

  public void setOperationTime(short operationTime) {
    this.operationTime = operationTime;
  }
}

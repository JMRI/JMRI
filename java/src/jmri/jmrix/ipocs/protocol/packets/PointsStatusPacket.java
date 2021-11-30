package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqPointsState;
import jmri.jmrix.ipocs.protocol.enums.RqReleaseState;

/**
 * Points/Turnout status
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class PointsStatusPacket extends Packet {
  public final static byte IDENT = 17;
  private RqPointsState state = null;
  private RqReleaseState releaseState = null;
  private short operationTime;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = RqPointsState.valueOf(buffer.get());
    releaseState = RqReleaseState.valueOf(buffer.get());
    operationTime = buffer.getShort();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.put(state.value);
    buffer.put(releaseState.value);
    buffer.putShort(operationTime);
    buffer.rewind();
    return buffer.array();
  }

  public RqPointsState getState() {
    return state;
  }

  public void setState(RqPointsState state) {
    this.state = state;
  }

  public RqReleaseState getReleaseState() {
    return releaseState;
  }

  public void setReleaseState(RqReleaseState releaseState) {
    this.releaseState = releaseState;
  }

  public short getOperationTime() {
    return operationTime;
  }

  public void setOperationTime(short operationTime) {
    this.operationTime = operationTime;
  }
}

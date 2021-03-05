package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqLevelCrossingState;
import jmri.jmrix.ipocs.protocol.enums.RqReleaseState;

/**
 * Level Crossing Status
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class LevelCrossingStatusPacket extends Packet {
  public final static byte IDENT = 19;
  private RqLevelCrossingState state = null;
  private RqReleaseState releaseState = null;
  private short operationTime;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = RqLevelCrossingState.valueOf(buffer.get());
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

  public RqLevelCrossingState getState() {
    return state;
  }

  public void setState(RqLevelCrossingState state) {
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

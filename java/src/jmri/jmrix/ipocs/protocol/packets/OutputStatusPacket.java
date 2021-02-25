package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqOutputState;

/**
 * Output status
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class OutputStatusPacket extends Packet {
  public final static byte IDENT = 22;
  private RqOutputState state = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = RqOutputState.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(state.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqOutputState getState() {
    return state;
  }

  public void setState(RqOutputState state) {
    this.state = state;
  }
}

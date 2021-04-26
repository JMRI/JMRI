package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqInputState;

/**
 * Input Status
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class InputStatusPacket extends Packet {
  public final static byte IDENT = 20;
  private RqInputState state = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = RqInputState.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(state.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqInputState getState() {
    return state;
  }

  public void setState(RqInputState state) {
    this.state = state;
  }
}

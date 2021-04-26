package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqControllerState;

/**
 * An IPOCS unit can send this as a status message.
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class ControllerStatusPacket extends Packet {
  public final static byte IDENT = 15;
  private RqControllerState state = null;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    state = RqControllerState.valueOf(buffer.get());
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(1);
    buffer.put(state.value);
    buffer.rewind();
    return buffer.array();
  }

  public RqControllerState getState() {
    return state;
  }

  public void setState(RqControllerState state) {
    this.state = state;
  }
}

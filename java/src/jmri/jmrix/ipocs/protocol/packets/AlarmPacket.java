package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

import jmri.jmrix.ipocs.protocol.enums.RqAlarmState;

/**
 * An alarm packet that can be sent by any party when something goes wrong
 * which is not covered by an Acknowledgement Packet (or one wasn't requested).
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class AlarmPacket extends Packet {
  public final static byte IDENT = 16;
  private short alarmCode;
  private byte alarmLevel;
  private RqAlarmState alarmState = null;
  private int parameter1;
  private int parameter2;

  @Override
  public byte getId() {
    return IDENT;
  }

  @Override
  protected void parseSpecific(ByteBuffer buffer) {
    alarmCode = buffer.getShort();
    alarmLevel = buffer.get();
    alarmState = RqAlarmState.valueOf(buffer.get());
    parameter1 = buffer.getInt();
    parameter2 = buffer.getInt();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(12);
    buffer.putShort(alarmCode);
    buffer.put(alarmLevel);
    buffer.put(alarmState.value);
    buffer.putInt(parameter1);
    buffer.putInt(parameter2);
    buffer.rewind();
    return buffer.array();
  }

  public short getAlarmCode() {
    return alarmCode;
  }

  public void setAlarmCode(short alarmCode) {
    this.alarmCode = alarmCode;
  }

  public byte getAlarmLevel() {
    return alarmLevel;
  }

  public void setAlarmLevel(byte alarmLevel) {
    this.alarmLevel = alarmLevel;
  }

  public RqAlarmState getAlarmState() {
    return alarmState;
  }

  public void setAlarmState(RqAlarmState alarmState) {
    this.alarmState = alarmState;
  }

  public int getParameter1() {
    return parameter1;
  }

  public void setParameter1(int parameter1) {
      this.parameter1 = parameter1;
  }

  public int getParameter2() {
    return parameter2;
  }

  public void setParameter2(int parameter2) {
      this.parameter2 = parameter2;
  }
}

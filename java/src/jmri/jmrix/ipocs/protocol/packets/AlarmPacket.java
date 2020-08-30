package jmri.jmrix.ipocs.protocol.packets;

import java.nio.ByteBuffer;

@org.openide.util.lookup.ServiceProvider(service = Packet.class)
public class AlarmPacket extends Packet {
  public final static byte IDENT = 16;
  private short alarmCode;
  private byte alarmLevel;
  private byte alarmState;
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
    alarmState = buffer.get();
    parameter1 = buffer.getInt();
    parameter2 = buffer.getInt();
  }

  @Override
  protected byte[] serializeSpecific() {
    ByteBuffer buffer = ByteBuffer.allocate(12);
    buffer.putShort(alarmCode);
    buffer.put(alarmLevel);
    buffer.put(alarmState);
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

  public byte getAlarmState() {
    return alarmState;
  }

  public void setAlarmState(byte alarmState) {
    this.alarmState = alarmState;
  }

  public int getParameter1() {
    return parameter1;
  }

  public int getParameter2() {
    return parameter2;
  }
}

package jmri.jmrix.ipocs.protocol.enums;

public enum RqAlarmState {
  Active(1),
  NotPresent(2),
  Transient(3);

  public final byte value;

  private RqAlarmState(int value) {
    this.value = (byte)value;
  }

  public static RqAlarmState valueOf(byte value) {
    for (RqAlarmState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}
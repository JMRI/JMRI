package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqPointsLockState {
  LockedRight(1),
  LockedLeft(2),
  Unlocked(3),
  OutOfControl(4);

  public final byte value;

  private RqPointsLockState(int value) {
    this.value = (byte)value;
  }

  public static RqPointsLockState valueOf(byte value) {
    for (RqPointsLockState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

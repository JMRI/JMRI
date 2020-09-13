package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqPointsLockCommand {
  Unlock(1),
  Lock(2);

  public final byte value;

  private RqPointsLockCommand(int value) {
    this.value = (byte)value;
  }

  public static RqPointsLockCommand valueOf(byte value) {
    for (RqPointsLockCommand e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqControllerState {
  Unavailable(1),
  Restarting(2),
  Operational(3);

  public final byte value;

  private RqControllerState(int value) {
    this.value = (byte)value;
  }

  public static RqControllerState valueOf(byte value) {
    for (RqControllerState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

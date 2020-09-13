package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqOutputState {
  On(1),
  Off(2);

  public final byte value;

  private RqOutputState(int value) {
    this.value = (byte)value;
  }

  public static RqOutputState valueOf(byte value) {
    for (RqOutputState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

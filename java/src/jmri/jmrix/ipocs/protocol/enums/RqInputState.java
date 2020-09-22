package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqInputState {
  On(1),
  Off(2),
  Undefined(3);

  public final byte value;

  private RqInputState(int value) {
    this.value = (byte)value;
  }

  public static RqInputState valueOf(byte value) {
    for (RqInputState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

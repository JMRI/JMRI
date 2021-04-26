package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqOutputCommand {
  On(1),
  Off(2);

  public final byte value;

  private RqOutputCommand(int value) {
    this.value = (byte)value;
  }

  public static RqOutputCommand valueOf(byte value) {
    for (RqOutputCommand e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

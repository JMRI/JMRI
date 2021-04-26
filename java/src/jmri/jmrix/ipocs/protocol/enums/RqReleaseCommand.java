package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqReleaseCommand {
  LocalControl(1),
  CentralControl(2);

  public final byte value;

  private RqReleaseCommand(int value) {
    this.value = (byte)value;
  }

  public static RqReleaseCommand valueOf(byte value) {
    for (RqReleaseCommand e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

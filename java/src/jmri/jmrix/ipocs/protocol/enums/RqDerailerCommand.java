package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqDerailerCommand {
  Passable(1),
  NonPassable(2);

  public final byte value;

  private RqDerailerCommand(int value) {
    this.value = (byte)value;
  }

  public static RqDerailerCommand valueOf(byte value) {
    for (RqDerailerCommand e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

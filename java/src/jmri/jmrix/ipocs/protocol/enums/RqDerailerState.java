package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqDerailerState {
  Passable(1),
  NonPassable(2),
  Moving(3),
  OutOfControl(4);

  public final byte value;

  private RqDerailerState(int value) {
    this.value = (byte)value;
  }

  public static RqDerailerState valueOf(byte value) {
    for (RqDerailerState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

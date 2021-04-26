package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqPointsCommand {
  Right(1),
  Left(2);

  public final byte value;

  private RqPointsCommand(int value) {
    this.value = (byte)value;
  }

  public static RqPointsCommand valueOf(byte value) {
    for (RqPointsCommand e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }

}

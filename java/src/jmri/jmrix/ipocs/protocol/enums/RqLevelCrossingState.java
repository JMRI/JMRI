package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqLevelCrossingState {
    Open(1),
    PreparedForActivation(2),
    Closing(3),
    Closed(4),
    Opening(5),
    OutOfControl(6);

    public final byte value;

    private RqLevelCrossingState(int value) {
      this.value = (byte)value;
    }
  
    public static RqLevelCrossingState valueOf(byte value) {
      for (RqLevelCrossingState e : values()) {
        if (e.value == value) {
          return e;
        }
      }
      return null;
    }
}

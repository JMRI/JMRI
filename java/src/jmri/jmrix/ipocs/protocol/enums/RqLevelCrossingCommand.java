package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqLevelCrossingCommand {
    OpenNow(1),
    OpenAfterPassage(2),
    Close(3),
    ActivateReducedAutomation(4),
    DeactiveReducedAutomation(5);

    public final byte value;

    private RqLevelCrossingCommand(int value) {
      this.value = (byte)value;
    }
  
    public static RqLevelCrossingCommand valueOf(byte value) {
      for (RqLevelCrossingCommand e : values()) {
        if (e.value == value) {
          return e;
        }
      }
      return null;
    }
}

package jmri.jmrix.ipocs.protocol.enums;

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

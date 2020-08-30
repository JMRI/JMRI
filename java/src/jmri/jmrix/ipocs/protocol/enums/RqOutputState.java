package jmri.jmrix.ipocs.protocol.enums;

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

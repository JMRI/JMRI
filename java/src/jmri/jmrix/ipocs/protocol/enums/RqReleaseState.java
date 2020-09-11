package jmri.jmrix.ipocs.protocol.enums;

public enum RqReleaseState {
  LocalControl(1),
  CentralControl(2),
  Unknown(3);

  public final byte value;

  private RqReleaseState(int value) {
    this.value = (byte)value;
  }

  public static RqReleaseState valueOf(byte value) {
    for (RqReleaseState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

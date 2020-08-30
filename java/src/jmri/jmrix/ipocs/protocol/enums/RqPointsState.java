package jmri.jmrix.ipocs.protocol.enums;

public enum RqPointsState {
  Right(1),
  Left(2),
  Moving(3),
  OutOfControl(4);

  public final byte value;

  private RqPointsState(int value) {
    this.value = (byte)value;
  }

  public static RqPointsState valueOf(byte value) {
    for (RqPointsState e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}
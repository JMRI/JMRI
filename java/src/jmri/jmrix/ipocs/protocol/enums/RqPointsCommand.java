package jmri.jmrix.ipocs.protocol.enums;

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

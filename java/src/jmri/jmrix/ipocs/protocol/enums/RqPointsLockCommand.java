package jmri.jmrix.ipocs.protocol.enums;

public enum RqPointsLockCommand {
  Unlock(1),
  Lock(2);

  public final byte value;

  private RqPointsLockCommand(int value) {
    this.value = (byte)value;
  }

  public static RqPointsLockCommand valueOf(byte value) {
    for (RqPointsLockCommand e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

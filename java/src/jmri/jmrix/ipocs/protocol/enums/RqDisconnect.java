package jmri.jmrix.ipocs.protocol.enums;

/**
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public enum RqDisconnect {
  WrongSiteDataVersion(1),
  WrongProtocolVersion(2),
  WrongSenderIdentity(3),
  WrongReceiverIdentity(4),
  UnitClosingDown(5);

  public final byte value;

  private RqDisconnect(int value) {
    this.value = (byte)value;
  }

  public static RqDisconnect valueOf(byte value) {
    for (RqDisconnect e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return null;
  }
}

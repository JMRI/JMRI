package jmri.jmrix.marklin.mcan.cdb;

/** Discriminant for CdB configuration-bus frame direction and kind. */
public enum CdBFrameType {
    PING,
    PING_REPLY,
    READ,
    READ_REPLY,
    WRITE,
    UNKNOWN
}

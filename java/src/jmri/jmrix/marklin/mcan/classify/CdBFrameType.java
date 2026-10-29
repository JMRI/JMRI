package jmri.jmrix.marklin.mcan.classify;

/** Discriminant for CdB configuration-bus frame direction and kind. */
public enum CdBFrameType {
    PING,
    PING_REPLY,
    READ,
    READ_REPLY,
    WRITE,
    UNKNOWN
}

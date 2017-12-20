package jmri;

/**
 * Allow notification of the completion of programming operations.
 * <P>
 * This allows a {@link Programmer} object to return delayed status, including
 * the CV value from a read operation.
 * For simplicity, expect these to be returned to be on the
 * <a href="http://jmri.org/help/en/html/doc/Technical/Threads.shtml">GUI thread</a>.
 * See the discussion in the {@link Programmer#readCV(String CV, ProgListener p) Programmer.readCV(...)}, 
 * {@link Programmer#writeCV(String CV, int val, ProgListener p) Programmer.writeCV(...)} and
 * {@link Programmer#confirmCV(String CV, int val, ProgListener p) Programmer.confirmCV(...)} methods.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface ProgListener extends java.util.EventListener {

    /**
     * Receive a callback at the end of a programming operation.
     *
     * @param value  Value from a read operation, or value written on a write
     * @param status Denotes the completion code. Note that this is a bitwise
     *               combination of the various status coded defined in this
     *               interface.
     */
    public void programmingOpReply(int value, int status);

    /**
     * Constant denoting that the request completed correctly. Note this is a
     * specific value; all others are bitwise combinations
     */
    public final int OK = 0;

    /**
     * Constant denoting the request failed, but no specific reason is known
     */
    public final int UnknownError = 1;

    /**
     * Constant denoting that no decoder was detected on the programming track
     */
    public final int NoLocoDetected = 2;

    /**
     * Constant denoting that the request failed because the decoding hardware
     * was already busy
     */
    public final int ProgrammerBusy = 4;

    /**
     * Constant denoting that the request failed because it requested some
     * unimplemented capability. Note that this can also result in an exception
     * during the original request; which happens is implementation dependent
     */
    public final int NotImplemented = 8;

    /**
     * Constant denoting that the user (human or software) aborted the request
     * before completion
     */
    public final int UserAborted = 0x10;

    /**
     * Constant denoting there was no acknowledge from the locomotive, so the CV
     * may or may not have been written on a write. No value was read.
     */
    public final int NoAck = 0x20;

    /**
     * Constant denoting that confirm failed, likely due to another value being
     * present
     */
    public final int ConfirmFailed = 0x40;

    /**
     * Constant denoting that the programming operation timed out
     */
    public final int FailedTimeout = 0x80;

    /**
     * Constant denoting that a short circuit occurred while programming
     */
    public final int ProgrammingShort = 0x100;

    /**
     * Constant denoting that there was an error with the programming sequence
     * (such as early exit)
     */
    public final int SequenceError = 0x200;

    /**
     * Constant denoting that a communications error occurred between the command
     * station and the PC durring programming
     */
    public final int CommError = 0x400;
}


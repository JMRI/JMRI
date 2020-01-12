package jmri;

import java.util.EventListener;

/**
 * Allow notification of delayed consisting errors.
 * <p>
 * This allows a {@link Consist} object to return delayed status.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Paul Bender Copyright (C) 2004
 */
public interface ConsistListener extends EventListener {

    /**
     * Receive notification at the end of a consisting operation.
     *
     * @param locoaddress Address of specific locomotive involved, if error is
     *                    locomotive specific.
     * @param status      Denotes the completion code. Note that this is a
     *                    bitwise combination of the various status coded
     *                    defined in this interface.
     */
    public void consistReply(LocoAddress locoaddress, int status);
    /**
     * Constant denoting that the request completed correctly. Note this is a
     * specific value; all others are bitwise combinations
     */
    public final int OK = 0;
    /**
     * Constant denoting that the request failed because it requested some
     * unimplemented capability. Note that this can also result in an exception
     * during the original request; which happens is implementation dependent
     */
    public final int NotImplemented = 0x01;
    /**
     * the Operation Completed successfully.
     */
    public final int OPERATION_SUCCESS = 0x02;
    /**
     * An Error Occurred.
     */
    public final int CONSIST_ERROR = 0x04;
    /**
     * All of the slots available for the consist are full Note: This may not be
     * an error. If the last locomotive added to the consist caused the number
     * of units in the consist to equal the size limit, the value returned to
     * the listeners should be OPERATION_SUCCESS | CONSIST_FULL. To indicate an
     * error, send CONSIST_ERROR | CONSIST_FULL, and to send an information
     * message, just send CONSIST_FULL
     */
    public final int CONSIST_FULL = 0x08;
    /**
     * The requested locomotive has not been operated by this device, or is
     * currently being operated by another device
     */
    public final int LOCO_NOT_OPERATED = 0x10;
    /**
     * An add request is not valid for this address because the locomotive is
     * already in a consist.
     */
    public final int ALREADY_CONSISTED = 0x20;
    /**
     * A remove request is not valid for this address because the locomotive is
     * not in a consist.
     */
    public final int NOT_CONSISTED = 0x40;
    /**
     * The operation is not valid because the locomotive's speed is not zero.
     */
    public final int NONZERO_SPEED = 0x80;
    /**
     * The operation is not valid because the specified address is not a consist
     * base address
     */
    public final int NOT_CONSIST_ADDR = 0x100;
    /**
     * The operation failed because it is not possible to delete the locomotive
     */
    public final int DELETE_ERROR = 0x200;
    /**
     * The operation failed because the command station stack is full
     */
    public final int STACK_FULL = 0x400;
}

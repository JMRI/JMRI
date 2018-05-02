/* ProgListenerScaffold.java */
package jmri;

/**
 * Scaffold implementation of Programmer Listener interface for testing.
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
 * @see jmri.Programmer
 * @author	Paul Bender Copyright (C) 2013
 */
public class ProgListenerScaffold implements ProgListener {

    public ProgListenerScaffold() {
        rcvdInvoked = 0;
        rcvdValue = -1;
        rcvdStatus = -1;
        wasOnRightThread = false;
    }

    @Override
    public void programmingOpReply(int value, int status) {
        rcvdValue = value;
        rcvdStatus = status;
        rcvdInvoked++;
        wasOnRightThread = jmri.util.ThreadingUtil.isGUIThread();
    }

    private int rcvdValue;
    private int rcvdStatus;
    private int rcvdInvoked;
    private boolean wasOnRightThread;

    public int getRcvdValue() {
        return rcvdValue;
    }

    public int getRcvdStatus() {
        return rcvdStatus;
    }

    public int getRcvdInvoked() {
        return rcvdInvoked;
    }

    public boolean wasRightThread() {
        return wasOnRightThread;
    }
}




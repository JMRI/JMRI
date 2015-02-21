/* ProgrammerScaffold.java */
package jmri;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

/**
 * Scaffold implementation of Programmer interface for testing.
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
 * @author	Bob Jacobsen Copyright (C) 2008, 2014
 * @version	$Revision$
 */
public class ProgrammerScaffold implements Programmer {

    ProgrammingMode matchesMode;
    ProgrammingMode lastSeenMode;

    public ProgrammerScaffold(ProgrammingMode matchesMode) {
        this.matchesMode = matchesMode;
        this.lastSeenMode = matchesMode;
    }

    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
    }

    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        writeCV(Integer.parseInt(CV), val, p);
    }

    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        readCV(Integer.parseInt(CV), p);
    }

    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(Integer.parseInt(CV), val, p);
    }

    public void setMode(ProgrammingMode p) {
        // temporary implementation, have to remove the "mode" int value eventually
        lastSeenMode = p;
    }

    public ProgrammingMode getMode() {
        return lastSeenMode;
    }

    public List<ProgrammingMode> getSupportedModes() {
        return Arrays.asList(
                new ProgrammingMode[]{matchesMode}
        );
    }

    public boolean getCanRead() {
        return true;
    }

    public boolean getCanRead(String addr) {
        return Integer.parseInt(addr) <= 1024;
    }

    public boolean getCanWrite() {
        return true;
    }

    public boolean getCanWrite(String addr) {
        return Integer.parseInt(addr) <= 1024;
    }

    public void addPropertyChangeListener(PropertyChangeListener p) {
    }

    public void removePropertyChangeListener(PropertyChangeListener p) {
    }

    public String decodeErrorCode(int i) {
        return null;
    }

}


/* @(#)ProgrammerScaffold.java */

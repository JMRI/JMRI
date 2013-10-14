/* ProgrammerScaffold.java */

package jmri;

import jmri.ProgListener;
import java.beans.PropertyChangeListener;

/**
 * Scaffold implementation of Programmer interface for testing.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @see         jmri.Programmer
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version	$Revision$
 */
public class ProgrammerScaffold implements Programmer  {

    int matchesMode = -1;
    int lastSeenMode = -1;
    
    public ProgrammerScaffold(int matchesMode) {
        this.matchesMode = matchesMode;
        this.lastSeenMode = matchesMode;
    }
    
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {}

    public void readCV(int CV, ProgListener p) throws ProgrammerException {}

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {}
    
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {
        writeCV(Integer.parseInt(CV), val, p);
    }
    public void readCV(String CV, ProgListener p) throws ProgrammerException {
        readCV(Integer.parseInt(CV), p);
    }
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        confirmCV(Integer.parseInt(CV), val, p);
    }
    
    public void setMode(int mode) {
        lastSeenMode = mode;
    }

    public int getMode() { return lastSeenMode; }

    public boolean hasMode(int mode) {
        return mode == matchesMode;
    }

    public boolean getCanRead() { return true; }
    public boolean getCanRead(String addr) { return Integer.parseInt(addr)<=2048; }
    public boolean getCanRead(int mode, String addr) { return getCanRead(addr); }
    
    public boolean getCanWrite()  { return true; }
    public boolean getCanWrite(String addr) { return Integer.parseInt(addr)<=2048; }
    public boolean getCanWrite(int mode, String addr)  { return getCanWrite(addr); }

    public void addPropertyChangeListener(PropertyChangeListener p) {}
    public void removePropertyChangeListener(PropertyChangeListener p) {}

    public String decodeErrorCode(int i) { return null; }

}


/* @(#)ProgrammerScaffold.java */

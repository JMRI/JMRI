package jmri;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;


/**
 * Scaffold implementation of Programmer interface for testing.
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
 * @see jmri.Programmer
 * @author	Bob Jacobsen Copyright (C) 2008, 2014
 */
public class ProgrammerScaffold implements Programmer {

    ProgrammingMode matchesMode;
    ProgrammingMode lastSeenMode;

    public ProgrammerScaffold(ProgrammingMode matchesMode) {
        this.matchesMode = matchesMode;
        this.lastSeenMode = matchesMode;
    }

    @Override
    public void writeCV(String CV, int val, ProgListener p) throws ProgrammerException {}

    @Override
    public void readCV(String CV, ProgListener p) throws ProgrammerException {}

    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {}

    @Override
    public void setMode(ProgrammingMode p) {
        // temporary implementation, have to remove the "mode" int value eventually
        lastSeenMode = p;
    }

    @Override
    public ProgrammingMode getMode() {
        return lastSeenMode;
    }

    @Override
    public List<ProgrammingMode> getSupportedModes() {
        return Arrays.asList(
                new ProgrammingMode[]{matchesMode}
        );
    }

    @Override
    public boolean getCanRead() {
        return true;
    }

    @Override
    public boolean getCanRead(String addr) {
        return Integer.parseInt(addr) <= 1024;
    }

    @Override
    public boolean getCanWrite() {
        return true;
    }

    @Override
    public boolean getCanWrite(String addr) {
        return Integer.parseInt(addr) <= 1024;
    }

    @Override
    @Nonnull
    public Programmer.WriteConfirmMode getWriteConfirmMode(String addr) { return WriteConfirmMode.NotVerified; }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener p) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener p) {
    }

    @Override
    public String decodeErrorCode(int i) {
        return null;
    }

}

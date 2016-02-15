package jmri.util.datatransfer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Transfer roster entries either via drag-and-drop or via the clipboard.
 * <p>
 * Note that roster entries can only be transfered within a single JVM instance,
 * and cannot be shared between programs via this mechanism.
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
 * @author	Randall Wood Copyright (C) 2011
 * @version	$Revision: $
 */
public class RosterEntrySelection implements Transferable, ClipboardOwner {

    private ArrayList<String> Ids;

    public static final DataFlavor rosterEntryFlavor = new DataFlavor(ArrayList.class, "RosterEntryIDs");

    static final DataFlavor[] flavors = {
        RosterEntrySelection.rosterEntryFlavor
    };

    private static final List<DataFlavor> flavorList = Arrays.asList(flavors);

    /**
     * Create the transferable.
     * <P>
     * Takes as a parameter an ArrayList containing Strings representing
     * RosterEntry Ids.
     *
     * @param rosterEntries - an ArrayList of RosterEntry Ids
     */
    public RosterEntrySelection(ArrayList<String> rosterEntries) {
        this.Ids = rosterEntries;
    }

    /**
     * Create a transferable with a list of RosterEntries.
     *
     * @param rosterEntries - an ArrayList of RosterEntries
     */
    public static RosterEntrySelection createRosterEntrySelection(ArrayList<RosterEntry> rosterEntries) {
        ArrayList<String> Ids = new ArrayList<String>(rosterEntries.size());
        for (RosterEntry re : rosterEntries) {
            Ids.add(re.getId());
        }
        return new RosterEntrySelection(Ids);
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return java.util.Arrays.copyOf(flavors, flavors.length);
    }

    public boolean isDataFlavorSupported(DataFlavor df) {
        return (flavorList.contains(df));
    }

    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if (df.equals(rosterEntryFlavor)) {
            return Ids;
        }
        throw new UnsupportedFlavorException(df);
    }

    public void lostOwnership(Clipboard clpbrd, Transferable t) {
        // if we need to take an action when something else if posted on the
        // clipboard, we would do so now.
    }

    /**
     * Get an ArrayList of RosterEntries from a RosterEntrySelection.
     *
     * @param t - a Transferable object. This should be a RosterEntrySelection,
     *          but for simplicity, will accept any Transferable object.
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    public static ArrayList<RosterEntry> getRosterEntries(Transferable t) throws UnsupportedFlavorException, IOException {
        if (t.isDataFlavorSupported(rosterEntryFlavor)) {
            @SuppressWarnings("unchecked")
            ArrayList<String> Ids = (ArrayList<String>) t.getTransferData(rosterEntryFlavor);
            ArrayList<RosterEntry> REs = new ArrayList<RosterEntry>(Ids.size());
            for (String Id : Ids) {
                RosterEntry re = Roster.instance().entryFromTitle(Id);
                if (re != null) {
                    REs.add(re);
                }
            }
            return REs;
        }
        throw new UnsupportedFlavorException(t.getTransferDataFlavors()[0]);
    }

}

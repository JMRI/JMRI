package jmri.jmrit.ctc.editor.code;

import java.util.HashSet;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * The purpose of this class is to internally manage the sensors needed by the
 * CTC system.  All routines that modify either "OtherData" or
 * "CodeButtonHandlerData" in any way will ultimately call this routine, so that
 * it can determine if any internal sensors need to be deleted and/or created
 * dynamically.  This is for integration into JMRI internally.
 *
 * This also includes the "search and replace" function.
 *
 * Right now this routine just makes two "delete" and "create" lists, but does
 * nothing since there is no integration with JMRI at this time (12/6/18).
 *
 */

public class InternalSensorManager {
    private final HashSet<String> _mInternalSensorsToDelete;

    public InternalSensorManager(CTCSerialData ctcSerialData) {
        _mInternalSensorsToDelete = ctcSerialData.getAllInternalSensors();
    }

    public void checkForChanges(CTCSerialData ctcSerialData) {
        HashSet<String> internalSensorsToCreate = ctcSerialData.getAllInternalSensors();
        process(internalSensorsToCreate, _mInternalSensorsToDelete);
    }

    static public void doDialog(javax.swing.JFrame dialog, CTCSerialData ctcSerialData) {
//  Get "before" image (anything left in here after analysis will be deleted):
        HashSet<String> internalSensorsToDelete = ctcSerialData.getAllInternalSensors();
//  Let the dialog hack the data any way it wants, without regard for us:
        dialog.setVisible(true);
//  Get "after" image (anything left in here after analysis will be created):
        HashSet<String> internalSensorsToCreate = ctcSerialData.getAllInternalSensors();
        process(internalSensorsToCreate, internalSensorsToDelete);
    }

    static private void process(HashSet<String> internalSensorsToCreate, HashSet<String> internalSensorsToDelete) {
//  Now, find out what was modified ANYWHERE:
        HashSet<String> intersection = new HashSet<>(internalSensorsToDelete);
        intersection.retainAll(internalSensorsToCreate);

        internalSensorsToDelete.removeAll(intersection);
        internalSensorsToCreate.removeAll(intersection);
/*
        PrintWriter printWriter;
        try {
            printWriter = new PrintWriter(new FileWriter("junkit.txt"));
        } catch (IOException e) { return; }
        for (String line : internalSensorsToDelete) {
            printWriter.println("Del: " + line);
        }
        for (String line : internalSensorsToCreate) {
            printWriter.println("Ins: " + line);
        }
        printWriter.close();
*/
    }
}

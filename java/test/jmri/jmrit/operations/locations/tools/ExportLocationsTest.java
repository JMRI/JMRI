package jmri.jmrit.operations.locations.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportLocationsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportLocations t = new ExportLocations();
        Assert.assertNotNull("exists", t);
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCreateFile() {

        ExportLocations exportLoc = new ExportLocations();
        Assert.assertNotNull("exists", exportLoc);

        JUnitOperationsUtil.loadFiveLocations(); //only Test Loc E has a track

        // should cause export complete dialog to appear
        Thread export = new Thread(exportLoc::writeOperationsLocationFile);
        export.setName("Export Locations"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !export.isAlive(), "wait for export to complete");

        java.io.File file = new java.io.File(ExportLocations.defaultOperationsFilename());   
        Assert.assertTrue("Confirm file creation", file.exists());
    }
}

package jmri.jmrit.operations.rollingstock.engines.tools;

import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportEngines t = new ExportEngines();
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCreateFile() {
        JUnitOperationsUtil.initOperationsData();
        ExportEngines exportEngines = new ExportEngines();

        // should cause export complete dialog to appear
        Thread export = new Thread(exportEngines::writeOperationsEngineFile);
        export.setName("Export Engines"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        jmri.util.JUnitUtil.waitFor(() -> !export.isAlive(), "wait for export to complete");

        java.io.File file = new java.io.File(ExportEngines.defaultOperationsFilename());
        Assertions.assertTrue(file.exists(), "Confirm file creation");
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportEnginesTest.class);

}

package jmri.jmrit.operations.rollingstock.engines.tools;

import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportEngines t = new ExportEngines();
        assertThat(t).withFailMessage("exists").isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCreateFile() {
        JUnitOperationsUtil.initOperationsData();
        ExportEngines exportEngines = new ExportEngines();

        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportEngines.writeOperationsEngineFile();
            }
        });
        export.setName("Export Engines"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        java.io.File file = new java.io.File(ExportEngines.defaultOperationsFilename());
        assertThat(file.exists()).withFailMessage("Confirm file creation").isTrue();
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportEnginesTest.class);

}

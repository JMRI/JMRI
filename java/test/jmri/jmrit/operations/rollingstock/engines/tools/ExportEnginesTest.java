package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ExportEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ExportEngines t = new ExportEngines();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreateFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        ExportEngines exportEngines = new ExportEngines();
        Assert.assertNotNull("exists", exportEngines);

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
        Assert.assertTrue("Confirm file creation", file.exists());
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportEnginesTest.class);

}

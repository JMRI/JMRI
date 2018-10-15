package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.regex.Pattern;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFileChooserOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ImportEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportEngines t = new ImportEngines();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testReadFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        JUnitOperationsUtil.initOperationsData();
        // check number of engines in operations data
        Assert.assertEquals("engines", 4, emanager.getNumEntries());

        // export engines to create file
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

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), "OK");

        java.io.File file = new java.io.File(ExportEngines.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all engines
        emanager.deleteAll();
        Assert.assertEquals("engines", 0, emanager.getNumEntries());
        
        // do import
        
        Thread mb = new ImportEngines();
        mb.setName("Test Import Engines"); // NOI18N
        mb.start();
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.WAITING);
        }, "wait for file chooser");
        
        // opens file chooser path "operations" "JUnitTest"
        JFileChooserOperator fco = new JFileChooserOperator();
        String[] path = OperationsXml.getOperationsDirectoryName().split(Pattern.quote(File.separator));  
        fco.chooseFile(path[0]);
        fco.chooseFile(path[1]);
        fco.chooseFile(ExportEngines.getOperationsFileName());
        
        // dialog windows should now open asking to add 2 models
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), "Yes");
        JemmyUtil.pressDialogButton(Bundle.getMessage("engineAddModel"), "Yes");
        
        // import complete 
        JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), "OK");
        
        jmri.util.JUnitUtil.waitFor(() -> {
            return mb.getState().equals(Thread.State.TERMINATED);
        }, "wait for import complete");
        
        // confirm import successful
        Assert.assertEquals("engines", 4, emanager.getNumEntries()); 
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ImportEnginesTest.class);

}

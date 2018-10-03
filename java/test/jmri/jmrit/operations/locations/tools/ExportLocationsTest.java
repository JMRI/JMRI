package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ExportLocationsTest extends OperationsSwingTestCase {

    @Test
    public void testCTor() {
        ExportLocations t = new ExportLocations();
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testCreateFile() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ExportLocations exportLoc = new ExportLocations();
        Assert.assertNotNull("exists", exportLoc);
        
        loadLocations(); //only Test Loc E has a track
        
        // should cause export complete dialog to appear
        Thread export = new Thread(new Runnable() {
            @Override
            public void run() {
                exportLoc.writeOperationsLocationFile();
            }
        });
        export.setName("Export Locations"); // NOI18N
        export.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return export.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), "OK");
        
        java.io.File file = new java.io.File(ExportLocations.defaultOperationsFilename());   
        Assert.assertTrue("Confirm file creation", file.exists());        
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

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class);
}

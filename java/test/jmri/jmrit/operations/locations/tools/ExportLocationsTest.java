package jmri.jmrit.operations.locations.tools;

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
public class ExportLocationsTest extends OperationsTestCase {

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
        
        JUnitOperationsUtil.loadFiveLocations(); //only Test Loc E has a track
        
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
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));
        
        java.io.File file = new java.io.File(ExportLocations.defaultOperationsFilename());   
        Assert.assertTrue("Confirm file creation", file.exists());        
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportCarsTest.class);
}

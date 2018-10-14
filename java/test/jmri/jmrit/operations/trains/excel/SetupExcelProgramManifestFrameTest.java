package jmri.jmrit.operations.trains.excel;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
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
public class SetupExcelProgramManifestFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramManifestFrame t = new SetupExcelProgramManifestFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrameAddButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramManifestFrame f = new SetupExcelProgramManifestFrame();
        Assert.assertNotNull("exists", f);

        f.initComponents();
        Assert.assertTrue(f.isShowing());

        JemmyUtil.enterClickAndLeave(f.addButton);

        // abort find file
        JemmyUtil.pressDialogButton(Bundle.getMessage("FindDesiredExcelFile"), "Cancel");

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameTestButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramManifestFrame f = new SetupExcelProgramManifestFrame();
        Assert.assertNotNull("exists", f);

        f.initComponents();
        Assert.assertTrue(f.isShowing());

        JemmyUtil.enterClickAndLeave(f.testButton);

        // kill dialog
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ManifestCreatorNotFound"), "OK");

        JUnitUtil.dispose(f);
    }

    @Test
    public void testFrameSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramManifestFrame f = new SetupExcelProgramManifestFrame();
        Assert.assertNotNull("exists", f);

        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.fileNameTextField.setText("Test File Name");
        //toggle create manifest checkbox
        JemmyUtil.enterClickAndLeave(f.generateCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // confirm save
        Assert.assertTrue(Setup.isGenerateCsvManifestEnabled());
        Assert.assertEquals("", "Test File Name", InstanceManager.getDefault(TrainCustomManifest.class).getFileName());

        JUnitUtil.dispose(f);
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

    // private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramManifestFrameTest.class);

}

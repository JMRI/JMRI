package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

import java.awt.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020, 2021
 */
public class BlockPathEditFrameTest {

    @Test
    public void testCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlock ob = new OBlock("OB1");
        BlockPathEditFrame bpef = new BlockPathEditFrame(
                "Test BPEF",
                ob,
                null,
                new TableFrames.PathTurnoutJPanel(null),
                null,
                null);
        Assertions.assertNotNull(bpef, "exists");
    }

    @Test
    public void testPathCTor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlock ob = new OBlock("OB1");
        OPath path = new OPath(ob, "path");
        TableFrames tf = new TableFrames();
        BlockPathTableModel bptm = new BlockPathTableModel(ob, tf);
        BlockPathEditFrame bpef = new BlockPathEditFrame(
                "Test BPEF",
                ob,
                path,
                new TableFrames.PathTurnoutJPanel(null),
                bptm,
                tf);
        Assertions.assertNotNull(bpef, "exists");
    }


    @Test
    public void testOkPressed() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        // repeat Ctor
        OBlock ob1 = new OBlock("OB1");
        OBlock ob2 = new OBlock("OB2");
        OPath path1 = new OPath(ob2, "path");
        // add more stuff
        PortalManager pm = InstanceManager.getDefault(PortalManager.class);
        Portal po1 = pm.providePortal("Po-1-2-CW");
        po1.setFromBlock(ob1, true);
        po1.setToBlock(ob2, true);
        Portal po2 = pm.providePortal("Po-1-2-CCW");
        po2.setFromBlock(ob1, true);
        po2.setToBlock(ob2, true); // assume that's a very small oval track
        TableFrames tf = new TableFrames();
        BlockPathTableModel bptm = new BlockPathTableModel(ob1, tf);

        Thread dialog_thread1 = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator(Bundle.getMessage("ErrorTitle") );
            new JButtonOperator(jfo, Bundle.getMessage("ButtonOK")).doClick();
        });
        dialog_thread1.setName("Path error warning message clicked");
        dialog_thread1.start();

        BlockPathEditFrame bpef = new BlockPathEditFrame(
                "Test invalid BPEF",
                ob1,
                path1,
                new TableFrames.PathTurnoutJPanel(null),
                bptm,
                tf);
        bpef.setVisible(true);
        JUnitUtil.waitFor(()-> !(dialog_thread1.isAlive()), "Path error warning message clicked");
        jmri.util.JUnitAppender.assertErrorMessage("BlockPathEditFrame for OPath path, but it is not part of OBlock OB1");

        // valid parameter values
        bpef = new BlockPathEditFrame(
                "Test BPEF",
                ob1,
                null,
                new TableFrames.PathTurnoutJPanel(null),
                bptm,
                tf);
        bpef.setVisible(true);
        // Set up is ready
        Assertions.assertEquals(0, ob1.getPaths().size(), "initially: 0 paths in oblock 1");
        Assertions.assertEquals("", bpef.fromPortalComboBox.getSelectedItem(), "initial empty toCombo");
        Assertions.assertEquals("", bpef.toPortalComboBox.getSelectedItem(), "initial empty fromCombo");
        Assertions.assertEquals(3, bpef.fromPortalComboBox.getItemCount(), "initial fromCombo 4 items");
        Assertions.assertEquals(3, bpef.toPortalComboBox.getItemCount(), "initial toCombo 4 items");
        bpef.okPressed(null);
        Assertions.assertTrue(bpef.isVisible(), "pane will not close with empty path name");
        // set new values in edit pane
        bpef.pathUserName.setText("PATH12CW");
        bpef.fromPortalComboBox.setSelectedItem("Po-1-2-CW");
        bpef.toPortalComboBox.setSelectedItem("Po-1-2-CCW");
        // see what happens
        bpef.okPressed(null);
        Assertions.assertFalse(bpef.isVisible(), "pane closed with valid entries");
        // check portals are stored in oblock
        Assertions.assertEquals(1, ob1.getPaths().size(), "after edit: 1 path in oblock 1");
        Assertions.assertEquals("Po-1-2-CW", ((OPath)ob1.getPaths().get(0)).getFromPortal().getName(), "portal on path");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockPathEditFrameTest.class);

}

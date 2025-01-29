package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.io.File;

import javax.swing.JFrame;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test simple functioning of CbusNodeBackupsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusNodeBackupsPaneTest {

    @Test
    public void testCtor() {

        t = new CbusNodeBackupsPane(null);

        assertNotNull(t);
        assertNotNull(nodeToEdit);

    }

    @Test
    public void testTableData() {

        Assertions.assertNotNull(nodeToEdit);
        nodeToEdit.getNodeBackupManager().doLoad();

        t = new CbusNodeBackupsPane(null);
        t.initComponents();

        t.setNode(nodeToEdit);

        // check pane has loaded something
        JFrame f = new JFrame("CBUS Node Backups Pane");
        f.add(t);
        jmri.util.ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(f);

        JTableOperator tbl = new JTableOperator(new JFrameOperator(f), 0);

        assertEquals(0, nodeToEdit.getNodeBackupManager().getBackups().size(),"0 entry in node xml");

        assertEquals( 0, tbl.getRowCount(), "No Rows at Startup");
        assertEquals( 5, tbl.getColumnCount(), "column count");

        JemmyUtil.pressButton(jfo,("Create New Backup"));

        assertEquals(1, nodeToEdit.getNodeBackupManager().getBackups().size(),"1 entry in node xml");

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();
        t.dispose();
        // JemmyUtil.pressButton(frame,("Pause Test"));

    }

    private CbusNodeBackupsPane t;

    private CbusNodeTableDataModel nodeModel = null;
    private CbusNode nodeToEdit = null;
    private CanSystemConnectionMemo memo = null;

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);

        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);
        nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});

    }

    @AfterEach
    public void tearDown() {
        assertNotNull(nodeModel);
        nodeModel.dispose();
        assertNotNull(nodeToEdit);
        nodeToEdit.dispose();
        assertNotNull(memo);
        memo.dispose();
        memo = null;

        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePaneTest.class);

}

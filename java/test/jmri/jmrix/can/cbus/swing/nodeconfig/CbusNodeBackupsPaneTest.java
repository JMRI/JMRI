package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusNodeBackupsPane
 *
 * @author	Paul Bender Copyright (C) 2016
 * @author	Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupsPaneTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CbusNodeBackupsPane t = new CbusNodeBackupsPane(null);
        
        Assert.assertNotNull("exists",t);
        Assert.assertNotNull("exists",nodeToEdit);
        
    }
    
    @Test
    public void testTableData() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        nodeToEdit.getNodeBackupManager().doLoad();
        
        CbusNodeBackupsPane t = new CbusNodeBackupsPane(null);
        t.initComponents();
        
        t.setNode(nodeToEdit);
        
        // check pane has loaded something
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle("CBUS Node Backups Pane");
        f.pack();
        f.setVisible(true);
        
        JFrameOperator frame = new JFrameOperator(f);

        JTableOperator tbl = new JTableOperator(new JFrameOperator(f), 0);
        
        Assert.assertEquals("0 entry in node xml",0, nodeToEdit.getNodeBackupManager().getBackups().size());
        
        Assert.assertTrue("Initially empty table",tbl.getRowCount()==0);
        Assert.assertEquals("column count 5",5,tbl.getColumnCount());
        
        JemmyUtil.pressButton(frame,("Create New Backup"));
        
        Assert.assertEquals("1 entry in node xml",1, nodeToEdit.getNodeBackupManager().getBackups().size());
        
        f.dispose();
        t.dispose();
        // JemmyUtil.pressButton(frame,("Pause Test"));
    
    }
    
    
    private CbusNodeTableDataModel nodeModel;
    private CbusNode nodeToEdit;
    private CanSystemConnectionMemo memo;
    // private TrafficControllerScaffold tcis;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE)));
        memo = new CanSystemConnectionMemo();
        
        nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
    }

    @After
    public void tearDown() {
        nodeModel.dispose();
        nodeToEdit.dispose();
        memo.dispose();
        memo = null;
       
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePaneTest.class);

}

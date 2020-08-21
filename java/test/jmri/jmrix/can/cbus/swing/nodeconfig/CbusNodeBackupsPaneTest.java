package jmri.jmrix.can.cbus.swing.nodeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusNodeBackupsPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupsPaneTest {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCtor() {

        t = new CbusNodeBackupsPane(null);
        
        assertThat(t).isNotNull();
        assertThat(nodeToEdit).isNotNull();
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testTableData() {

        nodeToEdit.getNodeBackupManager().doLoad();
        
        t = new CbusNodeBackupsPane(null);
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
        
        assertEquals(0, nodeToEdit.getNodeBackupManager().getBackups().size(),"0 entry in node xml");
        
        assertThat(tbl.getRowCount()).withFailMessage("No Rows at Startup").isEqualTo(0);
        assertThat(tbl.getColumnCount()).withFailMessage("column count").isEqualTo(5);
        
        JemmyUtil.pressButton(frame,("Create New Backup"));
        
        assertEquals(1, nodeToEdit.getNodeBackupManager().getBackups().size(),"1 entry in node xml");
        
        f.dispose();
        t.dispose();
        // JemmyUtil.pressButton(frame,("Pause Test"));
    
    }
    
    private CbusNodeBackupsPane t;
    
    private CbusNodeTableDataModel nodeModel;
    private CbusNode nodeToEdit;
    private CanSystemConnectionMemo memo;
    // private TrafficControllerScaffold tcis;
    
    @TempDir 
    protected Path tempDir;
    
    @BeforeEach
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir.toFile()));
        memo = new CanSystemConnectionMemo();
        
        nodeModel = new CbusNodeTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
        jmri.InstanceManager.setDefault(CbusNodeTableDataModel.class,nodeModel );
        nodeToEdit = nodeModel.provideNodeByNodeNum(256);
        // set node to 3 node vars , param6
        nodeToEdit.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,3,7,8});
        
    }

    @AfterEach
    public void tearDown() {
        nodeModel.dispose();
        nodeToEdit.dispose();
        memo.dispose();
        memo = null;
       
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTablePaneTest.class);

}

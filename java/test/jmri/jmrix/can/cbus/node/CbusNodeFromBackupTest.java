package jmri.jmrix.can.cbus.node;

import java.util.Date;

import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeFromBackupTest {

    @Test
    public void testCTor() {
        
        CbusNodeFromBackup t = new CbusNodeFromBackup(null,256);
        Assert.assertNotNull("exists",t);
        t.dispose();
        
    }
    
    @Test
    public void testCTorNodeDate() {
        
        CbusNode existingNode = new CbusNode(null,257);
       // existingNode.setParameters(new int[]{8,1,2,3,4,5,6,7,8});
        
        CbusNodeFromBackup t = new CbusNodeFromBackup(existingNode,new Date());
        Assert.assertNotNull("exists",t);
        Assert.assertTrue("Backup No Params",t.getBackupResult() == BackupType.COMPLETEDWITHERROR);
        
        existingNode.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,6,7,8});
        t = new CbusNodeFromBackup(existingNode,new Date());
        Assert.assertTrue("Backup No NVs",t.getBackupResult() == BackupType.COMPLETEDWITHERROR);
        
        existingNode.getNodeNvManager().setNVs(new int[]{6,1,2,3,4,5,6});
        t = new CbusNodeFromBackup(existingNode,new Date());
        Assert.assertTrue("Backup No EVs",t.getBackupResult() == BackupType.COMPLETEDWITHERROR);
        
        existingNode.getNodeEventManager().resetNodeEventsToZero();
        t = new CbusNodeFromBackup(existingNode,new Date());
        Assert.assertTrue("Backup No EVs",t.getBackupResult() == BackupType.COMPLETE);
        Assert.assertNotNull("Backup Time Set by Constructor",t.getBackupTimeStamp());
        
        existingNode.dispose();
        t.dispose();
    }
    
    @Test
    public void testBasicSetGets() {
        
        CbusNodeFromBackup t = new CbusNodeFromBackup(null,258);
        Assert.assertNull("Backup Time Unset",t.getBackupTimeStamp());
        t.setBackupTimeStamp(new Date());
        Assert.assertNotNull("Backup Time Set",t.getBackupTimeStamp());
        
        Assert.assertNull("Backup result unset",t.getBackupResult());
        t.setBackupResult(BackupType.NOTONNETWORK);
        Assert.assertNotNull("Backup result set",t.getBackupResult());
        
        Assert.assertTrue("Comment unset",t.getBackupComment().isEmpty());
        t.setBackupComment("My Comment 123");
        Assert.assertEquals("Comment set","My Comment 123",t.getBackupComment());
        
        t.dispose();
    }
    
    @Test
    public void testAddEvent() {
        
        CbusNodeFromBackup t = new CbusNodeFromBackup(null,258);
        
        // set node to 5 ev vars per event, para 5, 6NV's, param 6
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,6,7,8});
        t.addBupEvent(0,1,"01020304FF");
        
        Assert.assertNull("Random event not found",t.getNodeEventManager().getNodeEvent(123,123));
        
        CbusNodeEvent ndEv = t.getNodeEventManager().getNodeEvent(0,1);
        Assert.assertNotNull("Event Found",ndEv);
        Assert.assertEquals("Event 0,1 Ev Var 1",1,ndEv.getEvVar(1));
        Assert.assertEquals("Event 0,1 Ev Var 2",2,ndEv.getEvVar(2));
        Assert.assertEquals("Event 0,1 Ev Var 5",255,ndEv.getEvVar(5));
        
        t.dispose();
    }
    
    @Test
    @SuppressWarnings("unlikely-arg-type") // Makes sures different objects do not pass
    public void testEquals() {
    
        CbusNodeFromBackup t = new CbusNodeFromBackup(null,259);
        CbusNodeFromBackup tt = new CbusNodeFromBackup(null,259);
        
        Assert.assertEquals("node t 259",259,t.getNodeNumber());
        Assert.assertTrue("Same node t 259",t.equals(t));
        Assert.assertFalse("Not Equals String",t.equals("t"));
        Assert.assertTrue("Same node t tt 259",t.equals(tt));
        Assert.assertTrue("node t tt hash",tt.hashCode()==t.hashCode());
        
        t.setNodeNumber(258);
        Assert.assertFalse("Node 258 / 259",t.equals(tt));
        Assert.assertFalse("Node hash 258 / 259",t.hashCode()==tt.hashCode());
        t.setNodeNumber(259);
        
        t.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,6,7,8});
        Assert.assertFalse("No Params eq",t.equals(tt));
        Assert.assertFalse("No params hash",t.hashCode()==tt.hashCode());
        tt.getNodeParamManager().setParameters(new int[]{8,1,2,3,4,5,6,7,9});
        Assert.assertFalse("diff Params eq",t.equals(tt));
        Assert.assertFalse("diff params hash",t.hashCode()==tt.hashCode());
        tt.getNodeParamManager().setParameter(8,8);
        Assert.assertTrue("Same params eq",t.equals(tt));
        Assert.assertTrue("Same params hash",tt.hashCode()==t.hashCode());
        
        t.getNodeNvManager().setNVs(new int[]{6,1,2,3,4,5,6});
        Assert.assertFalse("No nv eq",t.equals(tt));
        Assert.assertFalse("No nv hash",t.hashCode()==tt.hashCode());
        tt.getNodeNvManager().setNVs(new int[]{6,1,2,3,4,5,7});
        Assert.assertFalse("diff nv eq",t.equals(tt));
        Assert.assertFalse("diff nv hash",t.hashCode()==tt.hashCode());
        tt.getNodeNvManager().setNV(6,6);
        Assert.assertTrue("Same nv eq",t.equals(tt));
        Assert.assertTrue("Same nv hash",tt.hashCode()==t.hashCode());
        
        t.addBupEvent(0,1,"01020304FF");
        Assert.assertFalse("No ev eq",t.equals(tt));
        Assert.assertFalse("No ev hash",t.hashCode()==tt.hashCode());
        tt.addBupEvent(0,1,"01020304FE");
        Assert.assertFalse("diff ev eq",t.equals(tt));
        Assert.assertFalse("diff ev hash",t.hashCode()==tt.hashCode());
        
        CbusNodeEvent ndEv = tt.getNodeEventManager().getNodeEvent(0,1);
        Assert.assertNotNull(ndEv);
        ndEv.setEvVar(5,255);
        Assert.assertTrue("Same ev eq",t.equals(tt));
        Assert.assertTrue("Same ev hash",tt.hashCode()==t.hashCode());
        
        t.addBupEvent(0,2,"0102030405");
        tt.addBupEvent(0,3,"0102030405");
        Assert.assertFalse("diff event eq",t.equals(tt));
        Assert.assertFalse("diff event hash",t.hashCode()==tt.hashCode());
        
        t.addBupEvent(0,3,"0102030405");
        tt.addBupEvent(0,2,"0102030405");
        Assert.assertTrue("Same ev added in different order eq",t.equals(tt));
        Assert.assertTrue("Same ev added in different order hash",tt.hashCode()==t.hashCode());
        
        t.addBupEvent(0,7,"0102030405");
        tt.addBupEvent(7,7,"0102030405");
        Assert.assertFalse("diff ev node eq",t.equals(tt));
        Assert.assertFalse("diff ev node hash",t.hashCode()==tt.hashCode());
        
    }
    
    @Test
    public void testCompareWithString() {
        
        CbusNodeFromBackup t = new CbusNodeFromBackup(null,259);
        CbusNodeFromBackup tt = new CbusNodeFromBackup(null,259);
        
        Assert.assertEquals("Same Nodes",Bundle.getMessage("NoChanges"),t.compareWithString(tt));
        
    }
    
    @Test
    public void testIgnoredFunctions() {
        
        jmri.jmrix.can.CanSystemConnectionMemo memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        jmri.jmrix.can.TrafficControllerScaffold tcis = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        CbusNodeFromBackup t = new CbusNodeFromBackup(memo,1234);
        
        jmri.jmrix.can.CanReply r = new jmri.jmrix.can.CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0,17);
        t.getCanListener().reply(r);
        t.getCanListener().message(new jmri.jmrix.can.CanMessage(r));
        
        Assert.assertEquals("default getNodeInLearnMode ",false,t.getNodeInLearnMode() );
        
        Assert.assertTrue("No Traffic Controler",(t.getCanListener() instanceof CbusNodeFromBackup.DoNothingCanListener));
        
        tcis.terminateThreads();
        memo.dispose();
        
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeFromBackupTest.class);

}

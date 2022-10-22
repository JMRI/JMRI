package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEventTableDataModelTest {

    @Test
    public void testCTor() {

        CbusNodeEventTableDataModel t = new CbusNodeEventTableDataModel( null,
            memo, 3,CbusNodeEventTableDataModel.MAX_COLUMN);
        Assert.assertNotNull("exists",t);

    }

    @Test
    public void testNodeNoEv() {

        CbusNodeEventTableDataModel t = new CbusNodeEventTableDataModel( null,
            memo, 3,CbusNodeEventTableDataModel.MAX_COLUMN);

        Assert.assertTrue("default rowcount", t.getRowCount() == 0 );
        Assert.assertTrue("getValueAt no node null", t.getValueAt(0,1) == null );


        CbusNode myNode = new CbusNode(memo,12345);

        t.setNode(myNode);

      //  Assert.assertEquals("starting 0 rowcount",0,t.getRowCount() );

        Assert.assertTrue( t.getRowCount()== 0 );
        Assert.assertTrue( t.getColumnCount()== 7 );

        for (int i = 0; i <t.getColumnCount(); i++) {
            Assert.assertFalse("column has name", t.getColumnName(i).isEmpty() );
        }

        Assert.assertTrue("column has NO name", t.getColumnName(999).equals("unknown 999") );


        myNode.dispose();

    }


    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testNodeWithNewEv() {

        // not headless as setValueAt triggers window open

        jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane mainpane = new
            jmri.jmrix.can.cbus.swing.nodeconfig.NodeConfigToolPane();

        CbusNodeEventTableDataModel t = new CbusNodeEventTableDataModel( mainpane,
            memo, 3,CbusNodeEventTableDataModel.MAX_COLUMN);

        CbusNode myNode = new CbusNode(memo,12345);
        CbusNodeEvent myNodeEvent = new CbusNodeEvent(memo,3011,7,12345,-1,4);
        myNodeEvent.setEvArr(new int[]{1,2,3,4});

        myNode.getNodeEventManager().addNewEvent(myNodeEvent);
        t.setNode(myNode);

        Assert.assertTrue( t.getRowCount()== 1 );

        Assert.assertTrue("cell not editable",
            t.isCellEditable(0,CbusNodeEventTableDataModel.NODE_NAME_COLUMN) == false );
        Assert.assertTrue("cell editable",
            t.isCellEditable(0,CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN) == true );

        Assert.assertTrue("column class int",
            t.getColumnClass(CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN) == Integer.class );
        Assert.assertTrue("column class string",
            t.getColumnClass(CbusNodeEventTableDataModel.EVENT_NAME_COLUMN) == String.class );
        Assert.assertTrue("button class string",
            t.getColumnClass(CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN) == javax.swing.JButton.class );
        Assert.assertTrue("class unknown", t.getColumnClass(999) == null );

        Assert.assertTrue("getValueAt NODE_NUMBER_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN)== 3011 );

        Assert.assertTrue("getValueAt EVENT_NUMBER_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN)== 7 );

        Assert.assertTrue("getValueAt NODE_EDIT_BUTTON_COLUMN number", (String)t.getValueAt(
            0,CbusNodeEventTableDataModel.NODE_EDIT_BUTTON_COLUMN) != null );

        Assert.assertTrue("getValueAt NODE_NAME_COLUMN number", ((String)t.getValueAt(
            0,CbusNodeEventTableDataModel.NODE_NAME_COLUMN)).isEmpty());

        Assert.assertTrue("getValueAt EVENT_NAME_COLUMN number", ((String)t.getValueAt(
            0,CbusNodeEventTableDataModel.EVENT_NAME_COLUMN)).isEmpty() );

        Assert.assertTrue("getValueAt EVENT_NAME_COLUMN number", (Integer)t.getValueAt(
            0,CbusNodeEventTableDataModel.EV_INDEX_COLUMN) == -1 );

        Assert.assertEquals("starting ev vars","1, 2, 3, 4",t.getValueAt(
            0,CbusNodeEventTableDataModel.EV_VARS_COLUMN) );

        Assert.assertTrue("getValueAt nac", (String)t.getValueAt(0,999) == null );

        mainpane.dispose();
        myNode.dispose();


    }

    private CbusNodeTableDataModel nodeModel = null;
    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);

        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.SPROGCBUS);

        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(nodeModel);
        nodeModel.dispose();
        nodeModel = null;

        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        tcis = null;

        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeEventTableDataModelTest.class);

}

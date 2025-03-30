package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeNVTableDataModelTest {

    @Test
    public void testCTor() {

        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);

        assertNotNull(t);

    }

    @Test
    public void testNodeNoNv() {

        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);

        assertEquals( 8, t.getColumnCount());

        CbusNode myNode = new CbusNode(memo,12345);

        t.setNode(myNode);

        assertEquals( 0, t.getRowCount());
        assertEquals( 8, t.getColumnCount());

        for (int i = 0; i <t.getColumnCount(); i++) {
            assertFalse(t.getColumnName(i).isEmpty());
        }

        assertEquals( "unknown 999", t.getColumnName(999));

        t.dispose();
        myNode.dispose();

    }

    @Test
    public void testNodeWithNv() {

        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);

        CbusNode myNode = new CbusNode(memo,12345);

        // set node to 3 node vars , param6
        myNode.getNodeParamManager().setParameters(new int[]{7,1,2,3,4,5,3,7});

        t.setNode(myNode);

        assertEquals( 3, t.getRowCount());

        assertFalse(t.isCellEditable(0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));

        assertTrue(t.isCellEditable(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        assertEquals( Integer.class, t.getColumnClass(CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));
        assertEquals( String.class, t.getColumnClass(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN));


        assertEquals( 1, t.getValueAt( 0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));
        assertEquals( 2, t.getValueAt( 1,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));
        assertEquals( 3, t.getValueAt( 2,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));

        assertEquals( "", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN));
        assertEquals( "", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN));
        assertEquals( -1, t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));
        assertEquals( "", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN));
        assertEquals( "", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN));


        assertNull(t.getValueAt(0,999));
        assertFalse(t.isTableDirty());
        assertEquals( 0, t.getCountDirty());

        t.setValueAt(0,0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN);
        assertFalse(t.isTableDirty());

        t.setValueAt(0,0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        t.setValueAt(122,1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);

        assertTrue( t.isTableDirty());
        assertEquals( 2, t.getCountDirty());

        t.setValueAt(255,2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);

        assertTrue( t.isTableDirty());

        assertEquals( 1, t.getValueAt( 0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));
        assertEquals( 2, t.getValueAt( 1,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));
        assertEquals( 3, t.getValueAt( 2,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN));

        assertEquals( "", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN));
        assertEquals( "", t.getValueAt( 1,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN));
        assertEquals( "", t.getValueAt( 2,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN));

        assertEquals( "", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN));
        assertEquals( "", t.getValueAt( 1,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN));
        assertEquals( "", t.getValueAt( 2,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN));

        assertEquals( 0, t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));
        assertEquals( 122, t.getValueAt( 1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));
        assertEquals( 255, t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        assertEquals( "00", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN));
        assertEquals( "7A", t.getValueAt( 1,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN));
        assertEquals( "FF", t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN));

        assertEquals("0000 0000", t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN));
        assertEquals("0111 1010", t.getValueAt( 1,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN));
        assertEquals("1111 1111", t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN));


        t.resetNewNvs();
        assertFalse(t.isTableDirty());

        assertEquals( -1, t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.setValueAt(255,0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);

        t.dispose();
        myNode.dispose();

    }

    @Test
    public void testHexBinSets() {

        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);

        CbusNode myNode = new CbusNode(memo,12345);

        // set node to 3 node vars , param6
        myNode.getNodeParamManager().setParameters(new int[]{7,1,2,3,4,5,3,7});

        t.setNode(myNode);

        // set all 3 NVs to 0
        myNode.getNodeNvManager().setNVs(new int[]{3,0,0,0});

        assertEquals( 0, t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.setValueAt("01", 0, CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN);
        assertEquals( 1, t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.setValueAt("FF", 0, CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN);
        assertEquals( 255, t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.setValueAt("0000 0000", 0, CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN);
        assertEquals( 0, t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.setValueAt("1100 0011", 0, CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN);
        assertEquals( 195, t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.setValueAt("not binary", 0, CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN);
        assertEquals( 195, t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN));

        t.dispose();
        myNode.dispose();
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    private CbusNodeNVTableDataModel t;


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);

    }

    @AfterEach
    public void tearDown() {
        t = null;
        assertNotNull(tcis);
        tcis.terminateThreads();
        tcis = null;
        assertNotNull(memo);
        memo.dispose();
        memo = null;

        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeNVTableDataModelTest.class);

}

package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeSingleEventTableDataModelTest {

    @Test
    public void testCTor() {

        CbusNodeEvent ev = new CbusNodeEvent(null,0,1,0,0,0);
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,ev);

        assertNotNull(t);

    }

    @Test
    public void testNoVars() {

        CbusNodeEvent evNoRows = new CbusNodeEvent(null,0,1,0,0,0);

        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evNoRows);

        assertEquals( 0, t.getRowCount());
        assertEquals( 7, t.getColumnCount());
        assertFalse( t.isTableLoaded());

        for (int i = 0; i <t.getColumnCount(); i++) {
            assertFalse(t.getColumnName(i).isEmpty());
            assertTrue(CbusNodeSingleEventTableDataModel.getPreferredWidth(i) > 0);
        }

        assertEquals( "unknown 999", t.getColumnName(999));
        assertTrue(CbusNodeSingleEventTableDataModel.getPreferredWidth(999) > 0);

    }

    @Test
    public void testThreeVars() {

        CbusNodeEvent evThreeRows = new CbusNodeEvent(null,0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{0,2,255});

        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);

        assertEquals( 3, t.getRowCount());
        assertTrue(t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));
        assertFalse(t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN));
        assertTrue(t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        assertEquals( Integer.class, t.getColumnClass(CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));
        assertEquals( String.class, t.getColumnClass(CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));

        assertEquals( 1, t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN));
        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN));
        assertEquals( 3, t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN));

        assertEquals( 0, t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN));
        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN));
        assertEquals( 255, t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN));

        assertEquals( "00", t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        assertEquals( "02", t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        assertEquals( "FF", t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));

        assertEquals( "0000 0000", t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
        assertEquals( "0000 0010", t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
        assertEquals( "1111 1111", t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));

        assertEquals( 0, t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));
        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));
        assertEquals( 255, t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        assertEquals( "", t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        assertEquals( "", t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        assertEquals( "", t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));

        assertEquals( "", t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));
        assertEquals( "", t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));
        assertEquals( "", t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));

        assertFalse(t.isTableDirty());
        assertTrue(t.isTableLoaded());
        assertEquals( 0, t.getCountDirty());

        t.setValueAt(211,1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN);

        assertEquals( 211, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));
        assertEquals( "D3", t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN));
        assertEquals( "1101 0011", t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN));

        assertFalse(t.isSingleEvDirty(0));
        assertTrue(t.isSingleEvDirty(1));

        assertEquals( 1, t.getCountDirty());
        assertTrue(t.isTableDirty());

        t.resetnewEVs();

        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));
        assertFalse(t.isTableDirty());

        t.setValueAt(211,1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN);

        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN));
        assertNull(t.getValueAt(0,999));

    }

    @Test
    public void testLoading() {

        CbusNodeEvent evThreeRows = new CbusNodeEvent(null,0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{-1,-1,-1});

        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);

        assertFalse(t.isTableLoaded());

        assertEquals( -1, t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN));
        assertEquals( -1, t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN));
        assertEquals( -1, t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

    }

    @Test
    public void testSetGetValues(){
        CbusNodeEvent evThreeRows = new CbusNodeEvent(null,0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{0,2,255});

        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);

        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN));
        assertEquals( 2, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("EE", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN);
        assertEquals( 238, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("00", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN);
        assertEquals( 0, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("FF", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN);
        assertEquals( 255, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("FF", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertEquals( 255, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("0000 0000", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertEquals( 0, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("1111 1111", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertEquals( 255, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("1100 0011", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertEquals( 195, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("not binary", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertEquals( 195, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

        t.setValueAt("111111111", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN); // too big
        assertEquals( 195, t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN));

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeSingleEventTableDataModelTest.class);

}

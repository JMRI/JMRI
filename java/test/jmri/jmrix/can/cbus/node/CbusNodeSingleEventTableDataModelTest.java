package jmri.jmrix.can.cbus.node;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.swing.edt.GuiActionRunner;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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
        
        assertThat(t).isNotNull();
        
    }
    
    @Test
    public void testNoVars() {
        
        CbusNodeEvent evNoRows = new CbusNodeEvent(null,0,1,0,0,0);
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evNoRows);
        
        assertThat(t.getRowCount()).isEqualTo(0);
        assertThat(t.getColumnCount()).isEqualTo(7);

        assertThat(t.isTableLoaded()).isFalse();
        
        GuiActionRunner.execute(() -> {
            for (int i = 0; i <t.getColumnCount(); i++) {
                assertThat(t.getColumnName(i)).isNotEmpty();
                assertThat(CbusNodeSingleEventTableDataModel.getPreferredWidth(i)).isGreaterThan(0);
            }
            
            assertThat(t.getColumnName(999)).isEqualTo("unknown 999");
            
            assertThat(CbusNodeSingleEventTableDataModel.getPreferredWidth(999)).isGreaterThan(0);
        });
    }

    @Test
    public void testThreeVars() {
        
        CbusNodeEvent evThreeRows = new CbusNodeEvent(null,0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{0,2,255});
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);
    
        assertThat(t.getRowCount()).isEqualTo(3);
        assertThat(t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN)).isTrue();
        assertThat(t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)).isFalse();
        assertThat(t.isCellEditable(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isTrue();
        
        assertThat(t.getColumnClass(CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(Integer.class);
        assertThat(t.getColumnClass(CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN)).isEqualTo(String.class);
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)).isEqualTo(1);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_NUMBER_COLUMN)).isEqualTo(3);
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)).isEqualTo(0);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)).isEqualTo(255);
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN)).isEqualTo("00");
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN)).isEqualTo("02");
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN)).isEqualTo("FF");
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN)).isEqualTo("0000 0000");
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN)).isEqualTo("0000 0010");
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN)).isEqualTo("1111 1111");
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(0);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(255);
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN)).isEqualTo("");
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt(2,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN)).isEqualTo("");
        
        assertThat(t.isTableDirty()).isFalse();
        assertThat(t.isTableLoaded()).isTrue();
        assertThat(t.getCountDirty()).isEqualTo(0);
        
        t.setValueAt(211,1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN);
        
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(211);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN)).isEqualTo("D3");
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN)).isEqualTo("1101 0011");
        
        assertThat(t.isSingleEvDirty(0)).isFalse();
        assertThat(t.isSingleEvDirty(1)).isTrue();
        
        assertThat(t.getCountDirty()).isEqualTo(1);
        assertThat(t.isTableDirty()).isTrue();
        
        t.resetnewEVs();
        
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(2);
        assertThat(t.isTableDirty()).isFalse();
        
        t.setValueAt(211,1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN);
        
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt(0,999)).isNull();
            
    }
    
    @Test
    public void testLoading() {
        
        CbusNodeEvent evThreeRows = new CbusNodeEvent(null,0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{-1,-1,-1});
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            new CanSystemConnectionMemo(), 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);    
    
        assertThat(t.isTableLoaded()).isFalse();
        
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_HEX_COLUMN)).isEqualTo(-1);
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_CURRENT_BIT_COLUMN)).isEqualTo(-1);
        assertThat(t.getValueAt(0,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(-1);
        
    }
    
    @Test
    public void testSetGetValues(){
        CbusNodeEvent evThreeRows = new CbusNodeEvent(null,0,1,0,0,3);
        evThreeRows.setEvArr( new int[]{0,2,255});
        
        CbusNodeSingleEventTableDataModel t = new CbusNodeSingleEventTableDataModel(
            null, 3,CbusNodeSingleEventTableDataModel.MAX_COLUMN,evThreeRows);
        
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_CURRENT_VAL_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(2);
        
        t.setValueAt("EE", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(238);
        
        t.setValueAt("00", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(0);
        
        t.setValueAt("FF", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_HEX_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(255);
        
        t.setValueAt("FF", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(255);
        
        t.setValueAt("0000 0000", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(0);
        
        t.setValueAt("1111 1111", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(255);
        
        t.setValueAt("1100 0011", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(195);
        
        t.setValueAt("not binary", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(195);
        
        t.setValueAt("111111111", 1, CbusNodeSingleEventTableDataModel.EV_SELECT_BIT_COLUMN); // too big
        assertThat(t.getValueAt(1,CbusNodeSingleEventTableDataModel.EV_SELECT_COLUMN)).isEqualTo(195);
        
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

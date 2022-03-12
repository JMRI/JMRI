package jmri.jmrix.can.cbus.node;

import static org.assertj.core.api.Assertions.assertThat;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


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
        
        assertThat(t).isNotNull();
        
    }
    
    @Test
    public void testNodeNoNv() {
        
        t = new CbusNodeNVTableDataModel(
            memo, 3,CbusNodeNVTableDataModel.MAX_COLUMN);
        
        assertThat(t.getColumnCount()).isEqualTo(8);
        
        CbusNode myNode = new CbusNode(memo,12345);
        
        t.setNode(myNode);
        
        assertThat(t.getRowCount()).isEqualTo(0);
        assertThat(t.getColumnCount()).isEqualTo(8);
        
        for (int i = 0; i <t.getColumnCount(); i++) {
            assertThat(t.getColumnName(i)).isNotEmpty();
        }
        
        assertThat(t.getColumnName(999)).isEqualTo("unknown 999");
        
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
        
        assertThat(t.getRowCount()).isEqualTo(3);
        
        assertThat(t.isCellEditable(0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isFalse();
        
        assertThat(t.isCellEditable(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isTrue();
        
        assertThat(t.getColumnClass(CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(Integer.class);
        assertThat(t.getColumnClass(CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN)).isEqualTo(String.class);
        
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(1);
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(3);
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(-1);
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN)).isEqualTo("");
        
        
        assertThat(t.getValueAt(0,999)).isNull();
        assertThat(t.isTableDirty()).isFalse();
        assertThat(t.getCountDirty()).isEqualTo(0);
        
        t.setValueAt(0,0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN);
        assertThat(t.isTableDirty()).isFalse();
        
        t.setValueAt(0,0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        t.setValueAt(122,1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        
        assertThat(t.isTableDirty()).isTrue();
        assertThat(t.getCountDirty()).isEqualTo(2);
        
        t.setValueAt(255,2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        
        assertThat(t.isTableDirty()).isTrue();
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(1);
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(2);
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_NUMBER_COLUMN)).isEqualTo(3);
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_CURRENT_HEX_COLUMN)).isEqualTo("");
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN)).isEqualTo("");
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_CURRENT_BIT_COLUMN)).isEqualTo("");

        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(0);
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(122);
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(255);
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN)).isEqualTo("00");
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN)).isEqualTo("7A");
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN)).isEqualTo("FF");
        
        assertThat(t.getValueAt( 0,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN)).isEqualTo("0000 0000");
        assertThat(t.getValueAt( 1,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN)).isEqualTo("0111 1010");
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN)).isEqualTo("1111 1111");
        
        
        t.resetNewNvs();
        assertThat(t.isTableDirty()).isFalse();
        
        assertThat(t.getValueAt( 2,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(-1);
        
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
        
        assertThat(t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(0);
        
        t.setValueAt("01", 0, CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN);
        assertThat(t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(1);
        
        t.setValueAt("FF", 0, CbusNodeNVTableDataModel.NV_SELECT_HEX_COLUMN);
        assertThat(t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(255);
        
        t.setValueAt("0000 0000", 0, CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(0);
        
        t.setValueAt("1100 0011", 0, CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(195);
        
        t.setValueAt("not binary", 0, CbusNodeNVTableDataModel.NV_SELECT_BIT_COLUMN);
        assertThat(t.getValueAt(0,CbusNodeNVTableDataModel.NV_SELECT_COLUMN)).isEqualTo(195);
        
        t.dispose();
        myNode.dispose();
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
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
        tcis.terminateThreads();
        tcis = null;
        memo.dispose();
        memo = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeNVTableDataModelTest.class);

}

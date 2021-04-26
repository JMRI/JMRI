package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JTextField;
import jmri.jmrix.can.cbus.CbusEventDataElements.EvState;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of CbusCommonSwing
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusCommonSwingTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        assertThat(new CbusCommonSwing()).isNotNull();
    }
    
    @Test
    public void testJTextFieldFromCbusEvState(){
    
        JTextField t = new JTextField();
    
        CbusCommonSwing.setCellFromCbusEventEnum(EvState.ON, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("CbusEventOn"));
        
        CbusCommonSwing.setCellFromCbusEventEnum(EvState.OFF, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("CbusEventOff"));
        
        CbusCommonSwing.setCellFromCbusEventEnum(EvState.UNKNOWN, t);
        assertThat(t.getText()).isEmpty();
        
        CbusCommonSwing.setCellFromCbusEventEnum(EvState.REQUEST, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("CbusEventRequest"));
        
    }
    
    @Test
    public void testJTextFieldFromCbusNodeBackupState(){
    
        JTextField t = new JTextField();
    
        CbusCommonSwing.setCellFromBackupEnum(BackupType.COMPLETE, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("BackupComplete"));
        
        CbusCommonSwing.setCellFromBackupEnum(BackupType.COMPLETEDWITHERROR, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("BackupCompleteError"));
        
        CbusCommonSwing.setCellFromBackupEnum(BackupType.INCOMPLETE, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("BackupIncomplete"));
        
        CbusCommonSwing.setCellFromBackupEnum(BackupType.NOTONNETWORK, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("BackupNotOnNetwork"));
        
        CbusCommonSwing.setCellFromBackupEnum(BackupType.OUTSTANDING, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("BackupOutstanding"));
        
        CbusCommonSwing.setCellFromBackupEnum(BackupType.SLIM, t);
        assertThat(t.getText()).isEqualTo(Bundle.getMessage("NodeInSlim"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

package jmri.jmrix.can.cbus.swing;

import javax.swing.JTextField;

import jmri.jmrix.can.cbus.CbusEventDataElements.EvState;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of CbusCommonSwing
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusCommonSwingTest  {

    // class only provides static methods, no need for constructor test

    @Test
    public void testJTextFieldFromCbusEvState(){

        JTextField t = new JTextField();

        CbusCommonSwing.setCellFromCbusEventEnum(EvState.ON, t);
        assertEquals( Bundle.getMessage("CbusEventOn"), t.getText());

        CbusCommonSwing.setCellFromCbusEventEnum(EvState.OFF, t);
        assertEquals( Bundle.getMessage("CbusEventOff"), t.getText());

        CbusCommonSwing.setCellFromCbusEventEnum(EvState.UNKNOWN, t);
        assertTrue( t.getText().isEmpty());

        CbusCommonSwing.setCellFromCbusEventEnum(EvState.REQUEST, t);
        assertEquals( Bundle.getMessage("CbusEventRequest"), t.getText());

    }

    @Test
    public void testJTextFieldFromCbusNodeBackupState(){

        JTextField t = new JTextField();

        CbusCommonSwing.setCellFromBackupEnum(BackupType.COMPLETE, t);
        assertEquals( Bundle.getMessage("BackupComplete"), t.getText());

        CbusCommonSwing.setCellFromBackupEnum(BackupType.COMPLETEDWITHERROR, t);
        assertEquals( Bundle.getMessage("BackupCompleteError"), t.getText());

        CbusCommonSwing.setCellFromBackupEnum(BackupType.INCOMPLETE, t);
        assertEquals( Bundle.getMessage("BackupIncomplete"), t.getText());

        CbusCommonSwing.setCellFromBackupEnum(BackupType.NOTONNETWORK, t);
        assertEquals( Bundle.getMessage("BackupNotOnNetwork"), t.getText());

        CbusCommonSwing.setCellFromBackupEnum(BackupType.OUTSTANDING, t);
        assertEquals( Bundle.getMessage("BackupOutstanding"), t.getText());

        CbusCommonSwing.setCellFromBackupEnum(BackupType.SLIM, t);
        assertEquals( Bundle.getMessage("NodeInSlim"), t.getText());
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

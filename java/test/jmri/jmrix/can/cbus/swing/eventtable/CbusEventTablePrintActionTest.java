package jmri.jmrix.can.cbus.swing.eventtable;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventTablePrintActionTest {

    @Test
    public void testCTor() {
        
        CbusEventTableDataModel eventModel = new CbusEventTableDataModel(memo,0);
        
        CbusEventTablePrintAction t = new CbusEventTablePrintAction("PreviewTable",
        eventModel,"CBUS Event Table Print Preview Test",true);
        
        assertNotNull(t);
        
        eventModel.skipSaveOnDispose();
        eventModel.dispose();
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testPreview() {
        
        CbusEventTableDataModel eventModel = new CbusEventTableDataModel(memo,0);
        
        eventModel.provideEvent(0, 7);
        eventModel.provideEvent(256, 77).setName("Event Name");
        
        CbusEventTablePrintAction t = new CbusEventTablePrintAction("PreviewTable",
        eventModel,"CBUS Event Table Print Preview Test",true);
        
        Thread dialog_thread = new Thread(() -> {
            JFrameOperator jfo = new JFrameOperator( "CBUS Event Table Print Preview Test" );
            new JButtonOperator(jfo,"Close").doClick();
        });
        dialog_thread.setName("Close Preview Window Thread");
        dialog_thread.start();
        
        t.actionPerformed(null);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "Close Preview Window Thread closed");
        
        eventModel.skipSaveOnDispose();
        eventModel.dispose();
        
    }

    private CanSystemConnectionMemo memo = null;
    
    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        } catch ( java.io.IOException e) {
            fail("Exception creating temp. user folder");
        }
        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTablePrintActionTest.class);

}

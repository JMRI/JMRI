package jmri.jmrix.can.cbus.swing.eventtable;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventTablePrintActionTest {

    @Test
    public void testCTor() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        CbusEventTableDataModel eventModel = new CbusEventTableDataModel(memo,0);
        
        CbusEventTablePrintAction t = new CbusEventTablePrintAction("PreviewTable",
        eventModel,"CBUS Event Table Print Preview Test",true);
        
        assertThat(t).isNotNull();
        
        eventModel.skipSaveOnDispose();
        eventModel.dispose();
        memo.dispose();
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testPreview() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        CbusEventTableDataModel eventModel = new CbusEventTableDataModel(memo, 2);
        // assertThat(t).isNotNull();
        
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
        memo.dispose();
        
    }
    
    @TempDir 
    protected Path tempDir;
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        try {
            JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        } catch ( java.io.IOException e) {
            fail("Exception creating temp. user folder");
        }
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTablePrintActionTest.class);

}

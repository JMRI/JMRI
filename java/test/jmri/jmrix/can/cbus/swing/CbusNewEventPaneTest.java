package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.swing.eventtable.CbusEventTablePane;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;

/**
 * Test simple functioning of CbusNewEventPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusNewEventPaneTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        CbusNewEventPane t = new CbusNewEventPane(mainPanel);
        assertThat(t).isNotNull();
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tc;
    private CbusEventTablePane mainPanel;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
            jmri.InstanceManager.setDefault(CbusPreferences.class,new CbusPreferences() );
            memo = new CanSystemConnectionMemo();
            tc = new TrafficControllerScaffold();
            memo.setTrafficController(tc);
            mainPanel = new CbusEventTablePane();
            mainPanel.initComponents(memo);
        }
    }

    @AfterEach
    public void tearDown() {
        CbusEventTableDataModel dm = InstanceManager.getNullableDefault(CbusEventTableDataModel.class);
        if ( dm !=null ){
            dm.skipSaveOnDispose();
            dm.dispose();
        }
        if(!GraphicsEnvironment.isHeadless()){
            mainPanel.dispose();
            tc.terminateThreads();
            memo.dispose();
            tc = null;
            memo = null;
        }
        JUnitUtil.tearDown();
    }

}

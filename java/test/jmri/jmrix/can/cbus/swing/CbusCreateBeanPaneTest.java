package jmri.jmrix.can.cbus.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Transferable;
import javax.swing.JLabel;
import javax.swing.JTable;
import jmri.NamedBean;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.swing.CanPanel;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test simple functioning of CbusCreateBeanPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class CbusCreateBeanPaneTest  {

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        assertThat(new CbusCreateBeanPane(mainPane)).isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCreateBean() {
        // for now, just makes sure there isn't an exception.
        
        CbusCreateBeanPane t = new CbusCreateBeanPane(mainPane);
        
        JmriJFrame f = new JmriJFrame();
        f.add(t);
        f.setTitle("Test CBUS Create Bean");
        f.pack();
        f.setVisible(true);
        JFrameOperator jfo = new JFrameOperator( "Test CBUS Create Bean" );
        
        dm = new CbusEventTableDataModel(memo,0,0);
        dm.provideEvent(123, 456).setName("TestEvent1");
        dm.provideEvent(0, 7);
        JTable table = new JTable(dm);
        table.setName("jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel");
        CbusTableRowEventDnDHandler dh = new CbusTableRowEventDnDHandler(memo,table);
        table.setRowSelectionInterval(0, 0);
        Transferable trnfr = dh.createTransferable(table);

        Thread dialog_thread = new Thread(() -> {
            JDialogOperator jdo = new JDialogOperator( Bundle.getMessage("ReminderTitle") );
            new JCheckBoxOperator(jdo,Bundle.getMessage("HideFurtherDialog")).setSelected(true);
            new JButtonOperator(jdo,"OK").doClick();
        });
        dialog_thread.setName("Reminder Dialog Close Thread");
        dialog_thread.start();
        
        t.transferArray[0].importData(new JLabel("Turnout"), trnfr);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "Reminder Dialog closed");

        t.transferArray[0].importData(new JLabel("NotABeanType"), trnfr);
        
        NamedBean bean = ((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class)).getBySystemName("MT+N123E456"); 
        NamedBean notAbean = ((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class)).getBySystemName("NotABean");
        
        assertThat(bean).isNotNull();
        assertThat(notAbean).isNull();
        
        
        table.setRowSelectionInterval(1, 1);
        trnfr = dh.createTransferable(table);
        
        t.transferArray[2].importData(new JLabel("Light"), trnfr);
        t.transferArray[2].importData(new JLabel("Light"), trnfr);
        
        bean = ((jmri.LightManager) memo.get(jmri.LightManager.class)).getBySystemName("ML+7"); 
        notAbean = ((jmri.LightManager) memo.get(jmri.LightManager.class)).getBySystemName("NotABean");
        
        assertThat(bean).isNotNull();
        assertThat(notAbean).isNull();
        
        
        new JTextFieldOperator(jfo,0).setText("NewName");
        new JButtonOperator(jfo,0).doClick();
        assertEquals("NewName",bean.getUserName());
        
        dh.dispose();
        
    }
    
    private class TestPane extends CanPanel{
    }
    
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    
    private TestPane mainPane;
    private CbusEventTableDataModel dm;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        if(!GraphicsEnvironment.isHeadless()){
            memo = new CanSystemConnectionMemo();
            memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
            memo.configureManagers();
            tcis = new TrafficControllerScaffold();
            memo.setTrafficController(tcis);
            mainPane = new TestPane();
            mainPane.initComponents(memo);
        }
    }

    @AfterEach
    public void tearDown() {
        if(!GraphicsEnvironment.isHeadless()){
            memo.dispose();
            memo = null;
            tcis.terminateThreads();
            tcis = null;
            mainPane.dispose();
            mainPane = null;
        }
        if (dm!=null){
            dm.skipSaveOnDispose();
            dm.dispose();
        }
        JUnitUtil.tearDown();
    }

}

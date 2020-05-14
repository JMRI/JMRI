package jmri.jmrix.can.cbus.eventtable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import jmri.InstanceManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.*;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableActionTest {

    @Test
    public void testCTor() {
        CbusEventTableAction t = new CbusEventTableAction(model);
        assertThat(t).isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDeleteConfirm() {
        
        assertThat(model.getRowCount()).isEqualTo(0);
        
        model.provideEvent(111, 222);
        model.provideEvent(333, 444);
        model.provideEvent(555, 666);
        
        assertThat(model.getRowCount()).isEqualTo(3);
    
        
        Thread cancel_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( Bundle.getMessage("DelEvPopTitle") );
            new JButtonOperator(jfo,"Cancel").doClick();
        });
        cancel_thread.setName("CBUS Cancel Delete Event Dialog Table Close Thread");
        cancel_thread.start();
        
        model.ta.buttonDeleteClicked(1);
        
        JUnitUtil.waitFor(()->{return !(cancel_thread.isAlive());}, "CBUS Cancel Delete Event Dialog closed");
        assertThat(model.getRowCount()).isEqualTo(3);
        
        
        Thread dialog_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( Bundle.getMessage("DelEvPopTitle") );
            new JButtonOperator(jfo,"OK").doClick();
        });
        dialog_thread.setName("CBUS Delete Event Dialog Table Close Thread");
        dialog_thread.start();
        
        Thread dialog_thread2 = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( Bundle.getMessage("DelEvPopTitle") );
            new JCheckBoxOperator(jfo,Bundle.getMessage("PopupSessionConfirmDel")).doClick();
            new JButtonOperator(jfo,"OK").doClick();
        });
        dialog_thread2.setName("CBUS Delete Event Dialog Table Close Thread");
        
        
        model.ta.buttonDeleteClicked(1);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "CBUS Delete Event Dialog closed");
        
        assertThat(model.getRowCount()).isEqualTo(2);
        // String events = model._mainArray.toString();
        assertEquals("[NN:111 EN:222 , NN:555 EN:666 ]",model._mainArray.toString());
        
        dialog_thread2.start();
        
        model.ta.buttonDeleteClicked(0);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread2.isAlive());}, "CBUS Delete Event 2nd Dialog closed");
        
        assertThat(model.getRowCount()).isEqualTo(1);
        assertEquals("[NN:555 EN:666 ]",model._mainArray.toString());
        
        model.ta.buttonDeleteClicked(0);
        assertThat(model.getRowCount()).isEqualTo(0);
        
    }
    
    @Test
    public void testAddBeanToEvent(){
    
        assertThat(model.getRowCount()).isEqualTo(0);
        
        CbusTableEvent evA = model.provideEvent(111, 222);
        assertThat(model.getRowCount()).isEqualTo(1);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.ON).getActionA().size()).isEqualTo(0);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.ON).getActionB().size()).isEqualTo(0);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.OFF).getActionA().size()).isEqualTo(0);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.OFF).getActionB().size()).isEqualTo(0);
        
        sm.provideSensor("+N111E222").setUserName("SensorA");
        assertThat(model.getRowCount()).isEqualTo(1);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.ON).getActionA().size()).isEqualTo(1);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.ON).getActionB().size()).isEqualTo(0);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.OFF).getActionA().size()).isEqualTo(0);
        assertThat(evA.getBeans(CbusEventDataElements.EvState.OFF).getActionB().size()).isEqualTo(1);
        assertEquals("Sensor Active: SensorA",evA.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Sensor Inactive: SensorA",evA.getBeans(CbusEventDataElements.EvState.OFF).toString());
        
        sm.provideSensor("+N111E222").setInverted(true);
        assertEquals("Sensor Inactive: SensorA",evA.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Sensor Active: SensorA",evA.getBeans(CbusEventDataElements.EvState.OFF).toString());
        
        sm.provideSensor("+N111E222").setInverted(false);
        assertEquals("Sensor Active: SensorA",evA.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Sensor Inactive: SensorA",evA.getBeans(CbusEventDataElements.EvState.OFF).toString());
        
        lm.provideLight("-N333E444").setUserName("LightA");
        assertThat(model.getRowCount()).isEqualTo(2);
        CbusTableEvent lE = model.provideEvent(333, 444);
        assertEquals("Light Off: LightA",lE.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Light On: LightA",lE.getBeans(CbusEventDataElements.EvState.OFF).toString());
                
        tm.provideTurnout("+123;+456").setUserName("TurnoutA");
        assertThat(model.getRowCount()).isEqualTo(4);
        CbusTableEvent toEv1 = model.provideEvent(0, 123);
        CbusTableEvent toEv2 = model.provideEvent(0, 456);
        assertThat(model.getRowCount()).isEqualTo(4);
        
        assertEquals("Turnout Thrown: TurnoutA",toEv1.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("",toEv1.getBeans(CbusEventDataElements.EvState.OFF).toString());
        assertEquals("Turnout Closed: TurnoutA",toEv2.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("",toEv2.getBeans(CbusEventDataElements.EvState.OFF).toString());
        
        tm.provideTurnout("+123;+456").setInverted(true);
        assertEquals("Turnout Closed: TurnoutA",toEv1.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("",toEv1.getBeans(CbusEventDataElements.EvState.OFF).toString());
        assertEquals("Turnout Thrown: TurnoutA",toEv2.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("",toEv2.getBeans(CbusEventDataElements.EvState.OFF).toString());
        
        lm.provideLight("+789;X0A").setUserName("LightB");
        assertThat(model.getRowCount()).isEqualTo(5);
        CbusTableEvent lsE = model.provideEvent(0, 789);
        assertEquals("Light On: LightB",lsE.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("",lsE.getBeans(CbusEventDataElements.EvState.OFF).toString());

        sm.provideSensor("+N111E222").dispose();
        lm.provideLight("-N333E444").dispose();
        tm.provideTurnout("+123;+456").dispose();
        lm.provideLight("+789;X0A").dispose();
    
    }
    
    @Test
    public void testResetSession(){
    
        
        model.provideEvent(0, 1).bumpDirection(CbusConstants.EVENT_DIR_OUT);
        model.provideEvent(0, 2).bumpDirection(CbusConstants.EVENT_DIR_IN);
        model.provideEvent(0, 2).bumpDirection(CbusConstants.EVENT_DIR_IN);
        
        model.provideEvent(0, 1).setState(CbusEventDataElements.EvState.ON);
        model.provideEvent(0, 1).setState(CbusEventDataElements.EvState.OFF);
        model.provideEvent(0, 2).setState(CbusEventDataElements.EvState.ON);
        
        assertThat(model.provideEvent(0, 1).getSessionInOut(false)).isEqualTo(1);
        assertThat(model.provideEvent(0, 2).getSessionInOut(true)).isEqualTo(2);
        assertThat(model.provideEvent(0, 1).getSessionOnOff(true)).isEqualTo(1);
        assertThat(model.provideEvent(0, 1).getSessionOnOff(false)).isEqualTo(1);
        assertThat(model.provideEvent(0, 2).getSessionOnOff(true)).isEqualTo(1);
        
        model.ta.resetAllSessionTotals();

        assertThat(model.provideEvent(0, 1).getSessionInOut(false)).isEqualTo(0);
        assertThat(model.provideEvent(0, 2).getSessionInOut(true)).isEqualTo(0);
        assertThat(model.provideEvent(0, 1).getSessionOnOff(true)).isEqualTo(0);
        assertThat(model.provideEvent(0, 1).getSessionOnOff(false)).isEqualTo(0);
        assertThat(model.provideEvent(0, 2).getSessionOnOff(true)).isEqualTo(0);
        
    }
    
    
    @TempDir 
    protected Path tempDir;
    
    private CbusEventTableDataModel model;
    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    
    private CbusTurnoutManager tm;
    private CbusLightManager lm;
    private CbusSensorManager sm;
    
    // InstanceManager.setDefault(jmri.SensorManager.class,new CbusSensorManager(memo));

    @BeforeEach
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        
        tm = new CbusTurnoutManager(memo);
        sm = new CbusSensorManager(memo);
        lm = new CbusLightManager(memo);
        InstanceManager.setDefault(jmri.TurnoutManager.class,tm);
        InstanceManager.setDefault(jmri.SensorManager.class,sm);
        InstanceManager.setDefault(jmri.LightManager.class,lm);
        
        InstanceManager.store(new CbusPreferences(),CbusPreferences.class );
        model = new CbusEventTableDataModel( memo,4,CbusEventTableDataModel.MAX_COLUMN);
      
    }

    @AfterEach
    public void tearDown() {
        
        model.skipSaveOnDispose();
        model.dispose();
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        lm.dispose();
        tm.dispose();
        sm.dispose();
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}

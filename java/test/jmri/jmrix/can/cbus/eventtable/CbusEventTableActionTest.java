package jmri.jmrix.can.cbus.eventtable;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
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
        assertNotNull(t);
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testDeleteConfirm() {

        assertEquals(0, model.getRowCount());

        model.provideEvent(111, 222);
        model.provideEvent(333, 444);
        model.provideEvent(555, 666);

        assertEquals(3, model.getRowCount());


        Thread cancel_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( Bundle.getMessage("DelEvPopTitle") );
            new JButtonOperator(jfo,"Cancel").doClick();
        });
        cancel_thread.setName("CBUS Cancel Delete Event Dialog Table Close Thread");
        cancel_thread.start();

        model.ta.buttonDeleteClicked(1);

        JUnitUtil.waitFor(()->{return !(cancel_thread.isAlive());}, "CBUS Cancel Delete Event Dialog closed");
        assertEquals(3, model.getRowCount());


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

        assertEquals(2, model.getRowCount());
        // String events = model._mainArray.toString();
        assertEquals("[NN:111 EN:222 , NN:555 EN:666 ]",model._mainArray.toString());

        dialog_thread2.start();

        model.ta.buttonDeleteClicked(0);

        JUnitUtil.waitFor(()->{return !(dialog_thread2.isAlive());}, "CBUS Delete Event 2nd Dialog closed");

        assertEquals(1, model.getRowCount());
        assertEquals("[NN:555 EN:666 ]",model._mainArray.toString());

        model.ta.buttonDeleteClicked(0);
        assertEquals(0, model.getRowCount());

    }

    @Test
    public void testAddBeanToEvent(){

        assertEquals(0, model.getRowCount());

        CbusTableEvent evA = model.provideEvent(111, 222);
        assertEquals(1, model.getRowCount());
        assertEquals(0, evA.getBeans(CbusEventDataElements.EvState.ON).getActionA().size());
        assertEquals(0, evA.getBeans(CbusEventDataElements.EvState.ON).getActionB().size());
        assertEquals(0, evA.getBeans(CbusEventDataElements.EvState.OFF).getActionA().size());
        assertEquals(0, evA.getBeans(CbusEventDataElements.EvState.OFF).getActionB().size());

        sm.provideSensor("+N111E222").setUserName("SensorA");
        assertEquals(1, model.getRowCount());
        assertEquals(1, evA.getBeans(CbusEventDataElements.EvState.ON).getActionA().size());
        assertEquals(0, evA.getBeans(CbusEventDataElements.EvState.ON).getActionB().size());
        assertEquals(0, evA.getBeans(CbusEventDataElements.EvState.OFF).getActionA().size());
        assertEquals(1, evA.getBeans(CbusEventDataElements.EvState.OFF).getActionB().size());
        assertEquals("Sensor Active: SensorA",evA.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Sensor Inactive: SensorA",evA.getBeans(CbusEventDataElements.EvState.OFF).toString());

        sm.provideSensor("+N111E222").setInverted(true);
        assertEquals("Sensor Inactive: SensorA",evA.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Sensor Active: SensorA",evA.getBeans(CbusEventDataElements.EvState.OFF).toString());

        sm.provideSensor("+N111E222").setInverted(false);
        assertEquals("Sensor Active: SensorA",evA.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Sensor Inactive: SensorA",evA.getBeans(CbusEventDataElements.EvState.OFF).toString());

        lm.provideLight("-N333E444").setUserName("LightA");
        assertEquals(2, model.getRowCount());
        CbusTableEvent lE = model.provideEvent(333, 444);
        assertEquals("Light Off: LightA",lE.getBeans(CbusEventDataElements.EvState.ON).toString());
        assertEquals("Light On: LightA",lE.getBeans(CbusEventDataElements.EvState.OFF).toString());

        tm.provideTurnout("+123;+456").setUserName("TurnoutA");
        assertEquals(4, model.getRowCount());
        CbusTableEvent toEv1 = model.provideEvent(0, 123);
        CbusTableEvent toEv2 = model.provideEvent(0, 456);
        assertEquals(4, model.getRowCount());

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
        assertEquals(5, model.getRowCount());
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

        assertEquals(1, model.provideEvent(0, 1).getSessionInOut(false));
        assertEquals(2, model.provideEvent(0, 2).getSessionInOut(true));
        assertEquals(1, model.provideEvent(0, 1).getSessionOnOff(true));
        assertEquals(1, model.provideEvent(0, 1).getSessionOnOff(false));
        assertEquals(1, model.provideEvent(0, 2).getSessionOnOff(true));

        model.ta.resetAllSessionTotals();

        assertEquals(0, model.provideEvent(0, 1).getSessionInOut(false));
        assertEquals(0, model.provideEvent(0, 2).getSessionInOut(true));
        assertEquals(0, model.provideEvent(0, 1).getSessionOnOff(true));
        assertEquals(0, model.provideEvent(0, 1).getSessionOnOff(false));
        assertEquals(0, model.provideEvent(0, 2).getSessionOnOff(true));

    }

    private CbusEventTableDataModel model;
    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;

    private CbusTurnoutManager tm;
    private CbusLightManager lm;
    private CbusSensorManager sm;

    // InstanceManager.setDefault(jmri.SensorManager.class,new CbusSensorManager(memo));

    @BeforeEach
    public void setUp( @TempDir File tempDir) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));
        JUnitUtil.resetInstanceManager();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);

        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();

        tm = (CbusTurnoutManager) memo.get(jmri.TurnoutManager.class);
        sm = (CbusSensorManager) memo.get(jmri.SensorManager.class);
        lm = (CbusLightManager) memo.get(jmri.LightManager.class);

        model = memo.get(CbusConfigurationManager.class)
            .provide(CbusEventTableDataModel.class);

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

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusEventTableActionTest.class);

}

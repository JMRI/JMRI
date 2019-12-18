package jmri.jmrix.openlcb;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;

import org.mockito.Mockito;
import org.openlcb.protocols.TimeBroadcastGenerator;
import org.openlcb.protocols.TimeProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.ClockControl;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.jmrit.simpleclock.configurexml.SimpleTimebaseXml;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import static jmri.Timebase.ClockInitialRunState.DO_NOTHING;
import static jmri.Timebase.ClockInitialRunState.DO_START;
import static jmri.Timebase.ClockInitialRunState.DO_STOP;
import static org.junit.Assert.*;
import static jmri.jmrix.openlcb.OlcbConfigurationManager.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for OpenLCB clock interfacing with JMRI.
 *
 * Created by bracz on 11/27/18.
 */
public class OlcbClockControlTest {
    OlcbTestInterface iface = null;
    ClockControl clock;

    interface MockInterface {
        void onChange(String property, Object newValue);
    }

    private class MockRateChangeListener implements PropertyChangeListener {
        public MockInterface m;

        public MockRateChangeListener() {
            m = mock(MockInterface.class);
        }

        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (propertyChangeEvent.getPropertyName().equals("rate")) {
                m.onChange(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
            }
        }
    }


    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        if (iface != null) iface.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    private void initializeWithClockMaster() {
        iface = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager() {
            @Override
            void setOptions(CanSystemConnectionMemo memo) {
                super.setOptions(memo);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ENABLE, OPT_FASTCLOCK_ENABLE_GENERATOR);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ID, OPT_FASTCLOCK_ID_ALT_2);
            }
        });
        clock = iface.systemConnectionMemo.get(ClockControl.class);
        assertNotNull(clock);
    }

    private void initializeWithClockSlave() {
        iface = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager() {
            @Override
            void setOptions(CanSystemConnectionMemo memo) {
                super.setOptions(memo);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ENABLE,
                        OPT_FASTCLOCK_ENABLE_CONSUMER);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ID,
                        OPT_FASTCLOCK_ID_ALT_1);
            }
        });
        clock = iface.systemConnectionMemo.get(ClockControl.class);
        assertNotNull(clock);
    }

    @Test
    public void getHardwareClockNameMaster() throws Exception {
        initializeWithClockMaster();
        assertEquals("OpenLCB Clock Generator for Alternate Clock 2", clock.getHardwareClockName
                ());
    }

    @Test
    public void getHardwareClockNameSlave() throws Exception {
        initializeWithClockSlave();
        assertEquals("OpenLCB Clock Consumer for Alternate Clock 1", clock.getHardwareClockName
                ());
    }

    @Test
    public void stopHardwareClock() throws Exception {
        initializeWithClockMaster();
        clock.stopHardwareClock();
        iface.assertSentMessage(":X195B4C4CN010100000103F001;");
        clock.startHardwareClock(clock.getTime());
        iface.assertSentMessage(":X195B4C4CN010100000103F002;");
    }

    @Test
    public void stopHardwareClockSlave() throws Exception {
        initializeWithClockSlave();
        clock.stopHardwareClock();
        iface.assertSentMessage(":X195B4C4CN010100000102F001;");
        clock.startHardwareClock(clock.getTime());
        iface.assertSentMessage(":X195B4C4CN010100000102F002;");
    }

    @Test
    public void customClockId() throws Exception {
        iface = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager() {
            @Override
            void setOptions(CanSystemConnectionMemo memo) {
                super.setOptions(memo);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ENABLE,
                        OPT_FASTCLOCK_ENABLE_CONSUMER);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ID,
                        OPT_FASTCLOCK_ID_CUSTOM);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_CUSTOM_ID,
                        "05.01.01.01.14.de");
            }
        });
        clock = iface.systemConnectionMemo.get(ClockControl.class);
        assertNotNull(clock);
        assertEquals("OpenLCB Clock Consumer for Custom Clock 05.01.01.01.14.DE",
                clock.getHardwareClockName());
        clock.stopHardwareClock();
        iface.assertSentMessage(":X195B4C4CN0501010114DEF001;");
    }

    @Test
    public void noClockNeeded() throws Exception {
        iface = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager() {
            @Override
            void setOptions(CanSystemConnectionMemo memo) {
                super.setOptions(memo);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ENABLE,
                        OPT_FASTCLOCK_ENABLE_OFF);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_ID,
                        OPT_FASTCLOCK_ID_CUSTOM);
                memo.setProtocolOption(OPT_PROTOCOL_FASTCLOCK, OPT_FASTCLOCK_CUSTOM_ID,
                        "05.01.01.01.14.de");
            }
        });
        clock = iface.systemConnectionMemo.get(ClockControl.class);
        assertNull(clock);
    }

    @Test
    public void defaultNoClock() throws Exception {
        iface = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        clock = iface.systemConnectionMemo.get(ClockControl.class);
        assertNull(clock);
    }

    @Test
    public void setRate() throws Exception {
        initializeWithClockSlave();
        clock.setRate(13.25);
        iface.assertSentMessage(":X195B4C4CN010100000102C035;");
    }

    @Test
    public void setRateMaster() throws Exception {
        initializeWithClockMaster();
        clock.setRate(13.25);
        iface.assertSentMessage(":X195B4C4CN0101000001034035;");
    }

    @Test
    public void rateListenerSlave() throws Exception {
        initializeWithClockSlave();
        Timebase tb = InstanceManager.getDefault(Timebase.class);
        MockRateChangeListener ml = new MockRateChangeListener();
        tb.addPropertyChangeListener(ml);

        clock.setRate(13.25);
        iface.assertSentMessage(":X195B4C4CN010100000102C035;");

        Mockito.verifyNoMoreInteractions(ml.m);

        iface.sendMessage(":X195B4123N0101000001024010;");
        Mockito.verify(ml.m).onChange("rate", 4.0d);
    }

    @Test
    public void rateListenerMaster() throws Exception {
        initializeWithClockMaster();
        Timebase tb = InstanceManager.getDefault(Timebase.class);
        MockRateChangeListener ml = new MockRateChangeListener();
        tb.addPropertyChangeListener(ml);

        clock.setRate(13.25);
        iface.assertSentMessage(":X195B4C4CN0101000001034035;");

        Mockito.verify(ml.m).onChange("rate", 13.25d);

        Mockito.verifyNoMoreInteractions(ml.m);

        iface.sendMessage(":X195B4123N010100000103C010;");
        iface.assertSentMessage(":X195B4C4CN0101000001034010;");
        Mockito.verify(ml.m).onChange("rate", 4.0d);
    }

    @Test
    public void rateListenerMasterRounding() throws Exception {
        initializeWithClockMaster();
        Timebase tb = InstanceManager.getDefault(Timebase.class);
        MockRateChangeListener ml = new MockRateChangeListener();
        tb.addPropertyChangeListener(ml);

        clock.setRate(13.33);
        iface.assertSentMessage(":X195B4C4CN0101000001034035;");

        Mockito.verify(ml.m).onChange("rate", 13.25d);

        Mockito.verifyNoMoreInteractions(ml.m);
    }

    @Test
    public void rateListenerMasterRoundingAtZero() throws Exception {
        initializeWithClockMaster();
        Timebase tb = InstanceManager.getDefault(Timebase.class);
        MockRateChangeListener ml = new MockRateChangeListener();
        tb.addPropertyChangeListener(ml);

        // A small but positive rate will be rounded up to 0.25.
        clock.setRate(0.001);
        iface.assertSentMessage(":X195B4C4CN0101000001034001;");

        Mockito.verify(ml.m).onChange("rate", 0.25d);

        Mockito.verifyNoMoreInteractions(ml.m);
    }

    @Test
    public void rateListenerSlaveRounding() throws Exception {
        initializeWithClockSlave();
        Timebase tb = InstanceManager.getDefault(Timebase.class);
        MockRateChangeListener ml = new MockRateChangeListener();
        tb.addPropertyChangeListener(ml);

        clock.setRate(13.33);
        iface.assertSentMessage(":X195B4C4CN010100000102C035;");

        Mockito.verifyNoMoreInteractions(ml.m);

        iface.sendMessage(":X195B4123N0101000001024035;");
        Mockito.verify(ml.m).onChange("rate", 13.25d);
    }

    @Test
    public void setTime() throws Exception {
        initializeWithClockSlave();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 35);
        clock.setTime(c.getTime());
        iface.assertSentMessage(":X195B4C4CN0101000001028223;");
    }

    @Test
    public void setTimeMaster() throws Exception {
        initializeWithClockMaster();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 2);
        c.set(Calendar.MINUTE, 35);
        clock.setTime(c.getTime());
        iface.assertSentMessage(":X195B4C4CN0101000001030223;");
    }

    @Test
    public void loadAndRestartLeavesRate() throws Exception {
        log.trace("loadandrestart start");
        initializeWithClockSlave();

        iface.sendMessage(":X195B4123N0101000001024010;");
        assertEquals(4.0d, clock.getRate(), 0.1);

        Timebase tb = InstanceManager.getDefault(Timebase.class);
        assertEquals(4.0d, tb.getRate(), 0.1);

        // Fills in setup fast clock dialog and saves it to XML.
        tb.setMasterName(clock.getHardwareClockName());
        tb.setCorrectHardware(false, false);
        tb.setInternalMaster(false, false);
        tb.setStartClockOption(Timebase.NONE);
        tb.setSetRateAtStart(false);
        assertEquals(DO_START, tb.getClockInitialRunState());
        Element store = new SimpleTimebaseXml().store(null);

        // Simulates a new start of JMRI.
        JUnitUtil.resetInstanceManager();
        Timebase tb2 = InstanceManager.getDefault(Timebase.class);
        iface.dispose();
        iface = null;
        initializeWithClockSlave();
        TimeBroadcastGenerator gen = new TimeBroadcastGenerator(iface.iface, TimeProtocol
                .ALT_CLOCK_1);
        gen.requestStop();
        gen.requestSetRate(2.0);
        iface.flush();
        assertEquals(false, tb2.getRun());
        assertEquals(false, gen.isRunning());

        iface.sendMessage(":X195B4123N010100000102F001;"); //clock is stopped
        iface.clearSentMessages();
        assertEquals(2.0d, tb2.getRate(), 0.1);

        new SimpleTimebaseXml().load(store, new Element("foo"));
        iface.flush();
        assertEquals(2.0d, tb2.getRate(), 0.1);
        assertEquals(true, tb2.getRun());
        assertEquals(true, gen.isRunning());

        log.trace("loadandrestart end");
    }

    @Test
    public void loadAndRestartLeavesRunState() throws Exception {
        log.trace("loadandrestart start");
        initializeWithClockSlave();

        iface.sendMessage(":X195B4123N0101000001024010;");
        assertEquals(4.0d, clock.getRate(), 0.1);

        Timebase tb = InstanceManager.getDefault(Timebase.class);
        assertEquals(4.0d, tb.getRate(), 0.1);

        // Fills in setup fast clock dialog and saves it to XML.
        tb.setMasterName(clock.getHardwareClockName());
        tb.setCorrectHardware(false, false);
        tb.setInternalMaster(false, false);
        tb.setStartClockOption(Timebase.NONE);
        tb.setSetRateAtStart(false);
        tb.setClockInitialRunState(Timebase.ClockInitialRunState.DO_NOTHING);
        Element store = new SimpleTimebaseXml().store(null);

        // Simulates a new start of JMRI.
        JUnitUtil.resetInstanceManager();
        Timebase tb2 = InstanceManager.getDefault(Timebase.class);
        iface.dispose();
        iface = null;

        initializeWithClockSlave();
        TimeBroadcastGenerator gen = new TimeBroadcastGenerator(iface.iface, TimeProtocol
                .ALT_CLOCK_1);
        gen.requestStop();
        gen.requestSetRate(2.0);
        iface.flush();

        assertEquals(false, tb2.getRun());
        assertEquals(2.0d, tb2.getRate(), 0.1);
        new SimpleTimebaseXml().load(store, new Element("foo"));
        assertEquals(2.0d, tb2.getRate(), 0.1);
        assertEquals(false, tb2.getRun());
        assertEquals(false, gen.isRunning());

        log.trace("loadandrestart end");
    }

    private interface LoadRestartModule {
        void setTimebaseOptions(Timebase tb);
        void setGeneratorStateBeforeLoad(TimeBroadcastGenerator gen, Timebase tb2);
        void checkFinalState(TimeBroadcastGenerator gen, Timebase tb2);
    }

    private void runLoadRestartTest(LoadRestartModule m) {
        initializeWithClockSlave();
        Timebase tb = InstanceManager.getDefault(Timebase.class);
        tb.setMasterName(clock.getHardwareClockName());
        tb.setCorrectHardware(false, false);
        tb.setInternalMaster(false, false);
        tb.setStartClockOption(Timebase.NONE);

        m.setTimebaseOptions(tb);

        Element store = new SimpleTimebaseXml().store(null);
        // Simulates a new start of JMRI.
        JUnitUtil.resetInstanceManager();
        Timebase tb2 = InstanceManager.getDefault(Timebase.class);
        iface.dispose();
        iface = null;

        initializeWithClockSlave();
        TimeBroadcastGenerator gen = new TimeBroadcastGenerator(iface.iface, TimeProtocol
                .ALT_CLOCK_1);
        gen.requestStop();
        gen.requestSetRate(2.0);

        m.setGeneratorStateBeforeLoad(gen, tb2);

        iface.flush();
        new SimpleTimebaseXml().load(store, new Element("foo"));
        iface.flush();

        m.checkFinalState(gen, tb2);
    }

    @Test
    public void loadAndRestartWithStopAndRate() throws Exception {
        runLoadRestartTest(new LoadRestartModule() {
            @Override
            public void setTimebaseOptions(Timebase tb) {
                tb.setClockInitialRunState(DO_STOP);
                tb.setSetRateAtStart(true);
                tb.setStartRate(13.0);
            }

            @Override
            public void setGeneratorStateBeforeLoad(TimeBroadcastGenerator gen, Timebase tb2) {
                gen.requestSetRate(2.0);
                gen.requestStart();
            }

            @Override
            public void checkFinalState(TimeBroadcastGenerator gen, Timebase tb2) {
                assertEquals(false, gen.isRunning());
                assertEquals(gen.getRate(), 13.0, 0.01);
            }
        });
    }

    @Test
    public void loadAndRestartWithStopAndNoRate() throws Exception {
        runLoadRestartTest(new LoadRestartModule() {
            @Override
            public void setTimebaseOptions(Timebase tb) {
                tb.setClockInitialRunState(DO_STOP);
                tb.setSetRateAtStart(false);
                tb.setStartRate(13.0);
            }

            @Override
            public void setGeneratorStateBeforeLoad(TimeBroadcastGenerator gen, Timebase tb2) {
                gen.requestSetRate(2.0);
                gen.requestStart();
            }

            @Override
            public void checkFinalState(TimeBroadcastGenerator gen, Timebase tb2) {
                assertEquals(false, gen.isRunning());
                assertEquals(gen.getRate(), 2.0, 0.01);
            }
        });
    }

    @Test
    public void loadAndRestartWithNoRunAndNoRate() throws Exception {
        runLoadRestartTest(new LoadRestartModule() {
            @Override
            public void setTimebaseOptions(Timebase tb) {
                tb.setClockInitialRunState(DO_NOTHING);
                tb.setSetRateAtStart(false);
                tb.setStartRate(13.0);
            }

            @Override
            public void setGeneratorStateBeforeLoad(TimeBroadcastGenerator gen, Timebase tb2) {
                gen.requestSetRate(2.0);
                gen.requestStop();
                iface.flush();
                gen.requestStart();
                iface.flush();
                assertEquals(true, gen.isRunning());
                assertEquals(true, tb2.getRun());
            }

            @Override
            public void checkFinalState(TimeBroadcastGenerator gen, Timebase tb2) {
                assertEquals(true, gen.isRunning());
                assertEquals(gen.getRate(), 2.0, 0.01);
            }
        });
    }

    @Test
    public void loadAndRestartWithNoRunAndNoRateStopped() throws Exception {
        runLoadRestartTest(new LoadRestartModule() {
            @Override
            public void setTimebaseOptions(Timebase tb) {
                tb.setClockInitialRunState(DO_NOTHING);
                tb.setSetRateAtStart(false);
                tb.setStartRate(13.0);
            }

            @Override
            public void setGeneratorStateBeforeLoad(TimeBroadcastGenerator gen, Timebase tb2) {
                gen.requestSetRate(2.0);
                gen.requestStop();
                iface.flush();
                assertEquals(false, gen.isRunning());
                assertEquals(false, tb2.getRun());
            }

            @Override
            public void checkFinalState(TimeBroadcastGenerator gen, Timebase tb2) {
                assertEquals(false, gen.isRunning());
                assertEquals(gen.getRate(), 2.0, 0.01);
            }
        });
    }

    @Test(timeout=1000)
    public void thisTestDidNotKillJemmy() throws Exception {
        new org.netbeans.jemmy.QueueTool().waitEmpty();  // using 100 as argument has a high fail rate 2018-12-15
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbClockControlTest.class);
}
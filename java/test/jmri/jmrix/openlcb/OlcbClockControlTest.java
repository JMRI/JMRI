package jmri.jmrix.openlcb;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.ClockControl;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

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

    private class MockPropertyChangeListener implements PropertyChangeListener {
        public MockInterface m;

        public MockPropertyChangeListener() {
            m = mock(MockInterface.class);
        }

        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            m.onChange(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
        }
    }


    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        if (iface != null) iface.dispose();
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
        MockPropertyChangeListener ml = new MockPropertyChangeListener();
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
        MockPropertyChangeListener ml = new MockPropertyChangeListener();
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

    // private final static Logger log = LoggerFactory.getLogger(OlcbClockControlTest.class);
}
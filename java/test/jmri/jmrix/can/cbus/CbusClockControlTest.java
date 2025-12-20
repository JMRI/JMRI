package jmri.jmrix.can.cbus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import jmri.ClockControl;
import jmri.InstanceManager;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.Timebase;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @since 4.19.6
 * @author Steve Young Copyright (C) 2020
 */
public class CbusClockControlTest {

    @Test
    public void testCTor() {
        t = new CbusClockControl(memo);
        assertNotNull(t);
        t.dispose();
    }

    @Test
    public void testgetHardwareClockName() {
        t = new CbusClockControl(memo);
        assertEquals( "CAN CBUS Fast Clock", t.getHardwareClockName() );
        t.dispose();
    }

    @Test
    public void testGetSetTemperature() {
        t = new CbusClockControl(memo);

        t.setTemp(15);
        assertEquals( 15, t.getTemp());

        t.setTemp(333);
        assertEquals( 15, t.getTemp(), "333 has no effect");
        JUnitAppender.assertWarnMessage("Temperature 333 out of range -128 to 127");

        t.setTemp(-129);
        assertEquals( 15, t.getTemp(), "-129 has no effect");
        JUnitAppender.assertWarnMessage("Temperature -129 out of range -128 to 127");

        t.dispose();
    }

    @Test
    public void testSendTimeFromInternal() throws jmri.TimebaseRateException {
        
        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);
        
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday
        
        t = new CbusClockControl(memo);
        InstanceManager.setDefault(ClockControl.class, t);
        
        tb.setInternalMaster(true, false);
        tb.setSynchronize(true, true);
        Assertions.assertNotNull(tcis);
        assertEquals( 1, tcis.outbound.size() );
        assertEquals( "[5f8] CF 39 11 46 00 18 00", tcis.outbound.get(0).toString());

        tb.setRate(13.789);
        JUnitAppender.assertWarnMessage("Non Integer Speed rate set, DIV values sent will not be accurate.");
        assertEquals( 1, tcis.outbound.size(), "clock not updated as not running");

        tb.setRun(true);
        assertEquals( 2, tcis.outbound.size() );
        tb.setRun(false);
        assertEquals( 3, tcis.outbound.size() );

        // started
        assertEquals( "[5f8] CF 39 11 46 0D 18 00", tcis.outbound.get(1).toString() );
        assertEquals( "[5f8] CF 39 11 46 00 18 00", tcis.outbound.get(2).toString() );

        specificDate = LocalDateTime.of(2020, 11, 22, 19, 02, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Sunday

        assertEquals( 4, tcis.outbound.size() );
        assertEquals( "[5f8] CF 02 13 B1 00 16 00", tcis.outbound.get(3).toString() );

        t.setTemp(37); // should not send canmessage
        tb.setRate(19); // sends
        assertEquals( 5, tcis.outbound.size() );
        assertEquals( "[5f8] CF 02 13 B1 00 16 25", tcis.outbound.get(4).toString() );


        tb.setRun(true);
        assertEquals( 6, tcis.outbound.size() );
        assertEquals( "[5f8] CF 02 13 B1 13 16 25", tcis.outbound.get(5).toString() );

        tb.setRun(false);
        assertEquals( 7, tcis.outbound.size() );
        assertEquals( "[5f8] CF 02 13 B1 00 16 25", tcis.outbound.get(6).toString() );

        tb.setInternalMaster(false, false);
        tb.setRun(true);
        tb.setRun(false);
        assertEquals( 7, tcis.outbound.size() );
        tb.setInternalMaster(true, false);

        tb.setSynchronize(false, false);
        tb.setRun(true);
        tb.setRun(false);
        assertEquals( 7, tcis.outbound.size() );
        tb.setSynchronize(true, false);

        specificDate = LocalDateTime.of(2020, 04, 24, 0, 0, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday

        assertEquals( 8, tcis.outbound.size() );
        assertEquals( "[5f8] CF 00 00 46 00 18 25", tcis.outbound.get(7).toString() );

        t.dispose();

    }

    @Test
    public void testIncomingDoesNotSetDate() {

        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);

        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday

        t = new CbusClockControl(memo);
        Assertions.assertNotNull(t);
        InstanceManager.setDefault(ClockControl.class, t);

        tb.setInternalMaster(false, false);
        tb.setSynchronize(true, true);
        String clockName = t.getHardwareClockName();
        Assertions.assertNotNull(clockName);
        tb.setMasterName(clockName);

        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );

        // base message
        CanReply send = new CanReply(memo.getTrafficController().getCanid());
        send.setNumDataElements(7);
        send.setElement(0, CbusConstants.CBUS_FCLK);
        send.setElement(1, 41 ); // mins
        send.setElement(2, 13 ); // hrs
        send.setElement(3, 0b001000000 ); // month 4
        send.setElement(4,  2); // time divider, 0 is stpeed, 1 is real time, 2 twice real, 3 thrice real
        send.setElement(5, 24); // day of month, 0-31
        send.setElement(6, 0xDB ); // Temperature as twos complement -127 to +127
        CbusMessage.setPri(send, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);

        send.setExtended(true);

        t.message(new CanMessage(send));
        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );

        t.reply(send);
        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );
        send.setExtended(false);

        send.setElement(0, 1);
        t.reply(send);
        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );
        send.setElement(0, CbusConstants.CBUS_FCLK);

        tb.setInternalMaster(true, false);
        t.reply(send);
        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );
        tb.setInternalMaster(false, false);

        tb.setSynchronize(false, true);
        t.reply(send);
        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );

        t.dispose();

    }

    @Test
    public void testIncomingSetDate() {

        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);

        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday

        t = new CbusClockControl(memo);
        Assertions.assertNotNull(t);
        InstanceManager.setDefault(ClockControl.class, t);

        tb.setInternalMaster(false, false);
        tb.setSynchronize(true, true);
        String clockName = t.getHardwareClockName();
        Assertions.assertNotNull(clockName);
        tb.setMasterName(clockName);

        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );

        // base message
        CanReply send = new CanReply(memo.getTrafficController().getCanid());
        send.setNumDataElements(7);
        send.setElement(0, CbusConstants.CBUS_FCLK);
        send.setElement(1, 41 ); // mins
        send.setElement(2, 13 ); // hrs
        send.setElement(3, 0b001010000 ); // month 5
        send.setElement(4,  2); // time divider, 0 is stpeed, 1 is real time, 2 twice real, 3 thrice real
        send.setElement(5, 27); // day of month, 0-31
        send.setElement(6, 0xDB ); // Temperature as twos complement -127 to +127
        CbusMessage.setPri(send, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);

        assertFalse(tb.getRun());
        assertEquals( 1d, t.getRate() );

        t.reply(send);
        assertTrue( tb.getRun());
        assertEquals( 2d, t.getRate() );
        assertEquals( 2d, tb.getRate() );
        assertEquals( -37, t.getTemp() );
        assertTrue(tb.getTime().toString().contains("May 27 13:41") );

        send.setElement(4,  1); // time divider,
        send.setElement(6, 0x5e ); // Temperature as twos complement -127 to +127
        t.reply(send);
        assertTrue( tb.getRun());
        assertEquals( 1d, tb.getRate() );
        assertEquals( 94, t.getTemp() );

        send.setElement(4, 0); // time divider,
        t.reply(send);
        assertFalse( tb.getRun() );
        assertEquals( 1d, t.getRate() );

        t.dispose();

    }

    @Test
    public void testBadIncomingSetDate() {

        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);

        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday

        t = new CbusClockControl(memo);
        Assertions.assertNotNull(t);
        InstanceManager.setDefault(ClockControl.class, t);

        tb.setInternalMaster(false, false);
        tb.setSynchronize(true, true);
        String clockName = t.getHardwareClockName();
        Assertions.assertNotNull(clockName);
        tb.setMasterName(clockName);     

        assertTrue(tb.getTime().toString().contains("Apr 24 17:57") );

        // base message
        CanReply send = new CanReply(memo.getTrafficController().getCanid());
        send.setNumDataElements(7);
        send.setElement(0, CbusConstants.CBUS_FCLK);
        send.setElement(1, 0xff ); // mins
        send.setElement(2, 13 ); // hrs
        send.setElement(3, 0b001010000 ); // month 5
        send.setElement(4,  2); // time divider, 0 is stpeed, 1 is real time, 2 twice real, 3 thrice real
        send.setElement(5, 27); // day of month, 0-31
        send.setElement(6, 0xDB ); // Temperature as twos complement -127 to +127
        CbusMessage.setPri(send, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);

        t.reply(send);
        assertTrue(tb.getTime().toString().contains("Apr 24 17:57"), "unchanged as 0xff mins" );
        JUnitAppender.assertWarnMessageStartingWith("Unable to process FastClock");

        send.setElement(1, 0x05 ); // mins
        send.setElement(3, 0xff ); // weekday month
        t.reply(send);
        assertTrue(tb.getTime().toString().contains("Apr 24 13:05"), "updated as valid mins" );

        t.dispose();

    }

    @Test
    public void testStringFromCanFrame() {

        CanReply send = new CanReply(memo.getTrafficController().getCanid());
        send.setNumDataElements(7);
        send.setElement(0, CbusConstants.CBUS_FCLK);
        send.setElement(1, 0x0c ); // mins
        send.setElement(2, 0x11 ); // hrs
        send.setElement(3, 0b001010001 ); // month 5 day 1
        send.setElement(4,  2); // time divider, 0 is stpeed, 1 is real time, 2 twice real, 3 thrice real
        send.setElement(5, 27); // day of month, 0-31
        send.setElement(6, 0xDB ); // Temperature as twos complement -127 to +127

        assertEquals( "Speed: x2 17:12 Sunday 27 May Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(4, 0);
        assertEquals( "Stopped 17:12 Sunday 27 May Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(1, 0xed);
        assertEquals( "Stopped 17:237 Sunday 27 May Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(2, 0x33);
        assertEquals( "Stopped 51:237 Sunday 27 May Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(3, 0x00);
        assertEquals( "Stopped 51:237 Saturday 27 Incorrect month (0) Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(3, 0xff);
        assertEquals( "Stopped 51:237 Incorrect weekday (15) 27 Incorrect month (15) Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(3, 0b001010101 ); // month 5 Day 3 Wednesday
        send.setElement(5, 0xd0);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: -37",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(6, 0x00);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: 0",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(6, 0x13);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: 19",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(6, 0x7f);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: 127",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(6, 0xff);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: -1",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(6, 0x8f);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: -113",
            CbusClockControl.dateFromCanFrame(send) );

        send.setElement(6, 0xa0);
        assertEquals( "Stopped 51:237 Thursday 208 May Temp: -96",
            CbusClockControl.dateFromCanFrame(send) );

        send = new CanReply(new int[] {0xCF,0x1C,0x15,0x76,0x01,0x09,0xA3});
        assertEquals( "Speed: x1 21:28 Friday 9 July Temp: -93",
            CbusClockControl.dateFromCanFrame(send) );

        send = new CanReply(new int[] {0xCF,0x2C,0x02,0x70,0x0A,0x0A,0x23});
        assertEquals( "Speed: x10 02:44 Saturday 10 July Temp: 35",
            CbusClockControl.dateFromCanFrame(send) );

    }

    private CbusClockControl t = null;
    private Timebase tb;
    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
    }

    @AfterEach
    public void tearDown() {

        tb = jmri.InstanceManager.getNullableDefault(Timebase.class);
        if (tb !=null){
            tb.dispose();
        }
        t = null;
        assertNotNull(memo);
        memo.dispose();
        memo = null;
        assertNotNull(tcis);
        tcis.terminateThreads();
        tcis = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusClockControlTest.class);

}

package jmri.jmrix.can.cbus;

import static org.assertj.core.api.Assertions.assertThat;

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
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @since 4.19.6
 * @author Steve Young Copyright (C) 2020
 */
public class CbusClockControlTest {

    @Test
    public void testCTor() {
        t = new CbusClockControl(null);
        assertThat(t).isNotNull();
        t.dispose();
    }
    
    @Test
    public void testCTorMemo() {
        t = new CbusClockControl(memo);
        assertThat(t).isNotNull();
        t.dispose();
    }
    
    @Test
    public void testgetHardwareClockName() {
        t = new CbusClockControl(memo);
        assertThat(t.getHardwareClockName()).isEqualTo("CAN CBUS Fast Clock");
        t.dispose();
    }
    
    @Test
    public void testGetSetTemperature() {
        t = new CbusClockControl(memo);
        
        t.setTemp(15);
        assertThat(t.getTemp()).isEqualTo(15);
        
        t.setTemp(333);
        assertThat(t.getTemp()).isEqualTo(15); // has no effect
        jmri.util.JUnitAppender.assertWarnMessage("Temperature 333 out of range -128 to 127");
        
        t.setTemp(-129);
        assertThat(t.getTemp()).isEqualTo(15); // has no effect
        jmri.util.JUnitAppender.assertWarnMessage("Temperature -129 out of range -128 to 127");
        
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
        
        assertThat(tcis.outbound.size()).isEqualTo(1);
        assertThat(tcis.outbound.get(0).toString()).isEqualTo("[5f8] CF 39 11 46 00 18 00");

        tb.setRate(13.789);
        jmri.util.JUnitAppender.assertWarnMessage("Non Integer Speed rate set, DIV values sent will not be accurate.");
        assertThat(tcis.outbound.size()).isEqualTo(1); // no update as not running
        
        tb.setRun(true);
        assertThat(tcis.outbound.size()).isEqualTo(2);
        tb.setRun(false);
        assertThat(tcis.outbound.size()).isEqualTo(3);
        
        // started
        assertThat(tcis.outbound.get(1).toString()).isEqualTo("[5f8] CF 39 11 46 0D 18 00");
        assertThat(tcis.outbound.get(2).toString()).isEqualTo("[5f8] CF 39 11 46 00 18 00");
        
        specificDate = LocalDateTime.of(2020, 11, 22, 19, 02, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Sunday
        
        assertThat(tcis.outbound.size()).isEqualTo(4); // ?
        assertThat(tcis.outbound.get(3).toString()).isEqualTo("[5f8] CF 02 13 B1 00 16 00");
        
        t.setTemp(37); // should not send canmessage
        tb.setRate(19); // sends
        assertThat(tcis.outbound.size()).isEqualTo(5);
        assertThat(tcis.outbound.get(4).toString()).isEqualTo("[5f8] CF 02 13 B1 00 16 25");
        
        
        tb.setRun(true);
        assertThat(tcis.outbound.size()).isEqualTo(6);
        assertThat(tcis.outbound.get(5).toString()).isEqualTo("[5f8] CF 02 13 B1 13 16 25");

        tb.setRun(false);
        assertThat(tcis.outbound.size()).isEqualTo(7);
        assertThat(tcis.outbound.get(6).toString()).isEqualTo("[5f8] CF 02 13 B1 00 16 25");
        
        tb.setInternalMaster(false, false);
        tb.setRun(true);
        tb.setRun(false);
        assertThat(tcis.outbound.size()).isEqualTo(7);
        tb.setInternalMaster(true, false);
        
        tb.setSynchronize(false, false);
        tb.setRun(true);
        tb.setRun(false);
        assertThat(tcis.outbound.size()).isEqualTo(7);
        tb.setSynchronize(true, false);
        
        specificDate = LocalDateTime.of(2020, 04, 24, 0, 0, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday
        
        assertThat(tcis.outbound.size()).isEqualTo(8);
        assertThat(tcis.outbound.get(7).toString()).isEqualTo("[5f8] CF 00 00 46 00 18 25");
        
        t.dispose();
        
    }
    
    @Test
    public void testIncomingDoesNotSetDate() {
        
        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);
        
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday
        
        t = new CbusClockControl(memo);
        InstanceManager.setDefault(ClockControl.class, t);
        
        tb.setInternalMaster(false, false);
        tb.setSynchronize(true, true);
        tb.setMasterName(t.getHardwareClockName());     
        
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        
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
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        
        t.reply(send);
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        send.setExtended(false);
        
        send.setElement(0, 1);
        t.reply(send);
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        send.setElement(0, CbusConstants.CBUS_FCLK);
        
        tb.setInternalMaster(true, false);
        t.reply(send);
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        tb.setInternalMaster(false, false);
        
        tb.setSynchronize(false, true);
        t.reply(send);
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        
        t.dispose();
        
    }
    
    @Test
    public void testIncomingSetDate() {
        
        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);
        
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday
        
        t = new CbusClockControl(memo);
        InstanceManager.setDefault(ClockControl.class, t);
        
        tb.setInternalMaster(false, false);
        tb.setSynchronize(true, true);
        tb.setMasterName(t.getHardwareClockName());     
        
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        
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
        
        assertThat(tb.getRun()).isFalse();
        assertThat(t.getRate()).isEqualTo(1d);
        
        t.reply(send);
        assertThat(tb.getRun()).isTrue();
        assertThat(t.getRate()).isEqualTo(2d);
        assertThat(tb.getRate()).isEqualTo(2d);
        assertThat(t.getTemp()).isEqualTo(-37);
        assertThat(tb.getTime().toString()).contains("May 27 13:41");
        
        send.setElement(4,  1); // time divider,
        send.setElement(6, 0x5e ); // Temperature as twos complement -127 to +127
        t.reply(send);
        assertThat(tb.getRun()).isTrue();
        assertThat(t.getRate()).isEqualTo(1d);
        assertThat(t.getTemp()).isEqualTo(94);
        
        send.setElement(4, 0); // time divider,
        t.reply(send);
        assertThat(tb.getRun()).isFalse();
        assertThat(t.getRate()).isEqualTo(1d);
        
        t.dispose();
        
    }
    
    @Test
    public void testBadIncomingSetDate() {
        
        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);
        
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 0);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday
        
        t = new CbusClockControl(memo);
        InstanceManager.setDefault(ClockControl.class, t);
        
        tb.setInternalMaster(false, false);
        tb.setSynchronize(true, true);
        tb.setMasterName(t.getHardwareClockName());     
        
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57");
        
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
        assertThat(tb.getTime().toString()).contains("Apr 24 17:57"); // unchanged as 0xff mins
        jmri.util.JUnitAppender.assertWarnMessageStartingWith("Unable to process FastClock");
        
        send.setElement(1, 0x05 ); // mins
        send.setElement(3, 0xff ); // weekday month
        t.reply(send);
        assertThat(tb.getTime().toString()).contains("Apr 24 13:05"); // unpdate as valid mins
        
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
        
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Speed: x2 17:12 Sunday 27 May Temp: -37");
        
        send.setElement(4, 0);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 17:12 Sunday 27 May Temp: -37");
        
        send.setElement(1, 0xed);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 17:237 Sunday 27 May Temp: -37");
        
        send.setElement(2, 0x33);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Sunday 27 May Temp: -37");
        
        
        send.setElement(3, 0x00);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Saturday 27 Incorrect month (0) Temp: -37");
        
        send.setElement(3, 0xff);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Incorrect weekday (15) 27 Incorrect month (15) Temp: -37");
        
        send.setElement(3, 0b001010101 ); // month 5 Day 3 Wednesday
        send.setElement(5, 0xd0);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: -37");
        
        send.setElement(6, 0x00);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: 0");
        
        send.setElement(6, 0x13);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: 19");
        
        send.setElement(6, 0x7f);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: 127");
        
        send.setElement(6, 0xff);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: -1");
        
        send.setElement(6, 0x8f);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: -113");
        
        send.setElement(6, 0xa0);
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Stopped 51:237 Thursday 208 May Temp: -96");
        
        send = new CanReply(new int[] {0xCF,0x1C,0x15,0x76,0x01,0x09,0xA3});
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Speed: x1 21:28 Friday 9 July Temp: -93");
        
        send = new CanReply(new int[] {0xCF,0x2C,0x02,0x70,0x0A,0x0A,0x23});
        assertThat(CbusClockControl.dateFromCanFrame(send)).
            isEqualTo("Speed: x10 02:44 Saturday 10 July Temp: 35");
        
    }
    
    private CbusClockControl t;
    private Timebase tb;
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        
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
        memo.dispose();
        tcis.terminateThreads();
        memo = null;
        tcis = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusClockControlTest.class);

}

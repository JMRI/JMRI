package jmri.jmrix.can.cbus;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import jmri.TimebaseRateException;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to CBUS Clock Network Functions.
 * @since 4.19.6
 * @author Steve Young (C) 2020
 */
public class CbusClockControl extends jmri.implementation.DefaultClockControl implements CanListener {

    private boolean isRunning;
    private int _cbusTemp = 0;
    private final jmri.Timebase clock;
    private CanMessage _lastSent;
    
    private final SimpleDateFormat minuteFormat;
    private final SimpleDateFormat hourFormat;        
    private final SimpleDateFormat dayofWeek;
    private final SimpleDateFormat dayInMonth;
    private final SimpleDateFormat monthFormat;
    private final SimpleDateFormat yearFormat;
    
    private final CanSystemConnectionMemo _memo;
    
    public CbusClockControl(CanSystemConnectionMemo memo) {
        super();
        
        minuteFormat = new java.text.SimpleDateFormat("mm");
        hourFormat = new java.text.SimpleDateFormat("H");        
        dayofWeek = new java.text.SimpleDateFormat("u");
        dayInMonth = new java.text.SimpleDateFormat("d");
        monthFormat = new java.text.SimpleDateFormat("MM");
        yearFormat = new java.text.SimpleDateFormat("YYYY");
        
        _memo = memo;
        this.addTc(memo);
        // Get internal timebase
        clock = jmri.InstanceManager.getDefault(jmri.Timebase.class);
        // Create a Timebase listener for Minute change events from the internal clock
        clock.addMinuteChangeListener(this::newMinute);
    }
    
    private void newMinute(java.beans.PropertyChangeEvent e){
        sendToLayout();
    }
    
    /**
     * Get current Temperature.
     * Int format, not twos complement.
     * @return -128 to 127
     */
    public int getTemp() {
        return _cbusTemp;
    }
    
    /**
     * Set current Temperature.
     * Calling this method does not send to layout, is just for setting the value.
     * Int format, not twos complement.
     * @param newTemp -128 to 127
     */
    public void setTemp(int newTemp) {
        if (newTemp>-128 && newTemp<127) {
            _cbusTemp = newTemp;
        }
        else {
            log.warn("Temperature {} out of range -128 to 127",newTemp);
        }
    }
    
    /**
     * System Connection + Clock Name, e.g. MERG CBUS Fast Clock.
     * {@inheritDoc}
     */
    @Override
    public String getHardwareClockName() {
        return(_memo.getUserName() + " CBUS Fast Clock");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(Date now) {
        sendToLayout();
    }
    
    /**
     * 
     * {@inheritDoc} 
     */
    @Override
    public void setRate(double newRate) {
        // log.info("set rate to {}",newRate);
        int newRatio = (int) newRate;
        if ((newRate % 1) != 0){
            log.warn("Non Integer Speed rate set, DIV values sent will not be accurate.");
        }
        if (newRatio < -255 || newRatio > 255) { // not happening at present as checked by Timebase.
            log.error(("ClockRatioRangeError"));
        } else {
            sendToLayout();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeHardwareClock(double rate, Date now, boolean getTime) {
        // on startup, rate already set
        isRunning = clock.getRun();
        setRate(rate);
        setTime(now);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stopHardwareClock() {
        isRunning = false;
        sendToLayout();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void startHardwareClock(Date now) {
        isRunning = true;
        setTime(now); // also sends to layout
    }
    
    private void sendToLayout(){
        if (!clock.getInternalMaster()  || !clock.getSynchronize()){
            return;
        }
        
        int day = Integer.parseInt(dayofWeek.format(clock.getTime()))+1;
        if (day==8){
            day = 1;
        }
        int bstot=(Integer.parseInt(monthFormat.format(clock.getTime())) << 4)+day;
        // weekday month, bits 0-3 are the weekday (1=Sun, 2=Mon etc)
        // bits 4-7 are the month (1=Jan, 2=Feb etc)
    
        CanMessage send = getCanMessage(bstot);
        if (!(send.equals(_lastSent))) {
            _memo.getTrafficController().sendCanMessage(send, this);
            _lastSent = send;
        }
    }
    
    private CanMessage getCanMessage(int bstot){
    
        CanMessage send = new CanMessage(_memo.getTrafficController().getCanid());
        send.setNumDataElements(7);
        send.setElement(0, CbusConstants.CBUS_FCLK);
        send.setElement(1, Integer.parseInt(minuteFormat.format(clock.getTime())) ); // mins
        send.setElement(2, Integer.parseInt(hourFormat.format(clock.getTime())) ); // hrs
        send.setElement(3, bstot);
        send.setElement(4,  ( isRunning ? (int) getRate() : 0)); // time divider, 0 is stpeed, 1 is real time, 2 twice real, 3 thrice real
        send.setElement(5, Integer.parseInt(dayInMonth.format(clock.getTime()))); // day of month, 0-31
        send.setElement(6, _cbusTemp); // Temperature as twos complement -127 to +127
        CbusMessage.setPri(send, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        
        return send;
    
    }
    
    /**
     * Listen for CAN Frames sent by external CBUS FC source.
     * Typically sent every fast minute.
     *
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply r) {
        if ( r.extendedOrRtr()
            || CbusMessage.getOpcode(r) != CbusConstants.CBUS_FCLK
            || !clock.getSynchronize()
            || clock.getInternalMaster())
            return;
        
        setRateFromReply( r.getElement(4) & 0xff);
        setTimeFromReply(r);
        setTemp(tempFromTwos(r.getElement(6) & 0xff));
    }
    
    private static int tempFromTwos(int twosTemp){
        return (twosTemp > 127 ? twosTemp - 256 : twosTemp);
    }
    
    private void setTimeFromReply(CanReply r) {
        int min = r.getElement(1) & 0xff;
        int hour = r.getElement(2) & 0xff;
        int day = r.getElement(5) & 0xff;
        int month = (r.getElement(3) >>> 4);
        LocalDateTime specificDate = null;
        try {
            specificDate = LocalDateTime.of(Integer.parseInt(yearFormat.format(clock.getTime()))
                , month, day, hour, min, 0);
        }
        catch( java.time.DateTimeException e){
            log.debug ("Unable to process FastClock date. Incoming: {}", r, e);
        }
        if (specificDate==null) { // if unset, try just the times.
            try {
                specificDate = LocalDateTime.of(Integer.parseInt(yearFormat.format(clock.getTime()))
                    , Integer.parseInt(monthFormat.format(clock.getTime())),
                    Integer.parseInt(dayInMonth.format(clock.getTime())), 
                    hour, min, 0);
            }
            catch( java.time.DateTimeException e){
                log.warn ("Unable to process FastClock time hrs:{} mins:{} error:{} CanFrame:{}",
                    hour,min,e.getLocalizedMessage(),r);
            }
        }
        if (specificDate!=null) {
            Date newnewdate = Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant());
            clock.setTime(newnewdate);
        }
    }
    
    /**
     * Set Clock Rate, Running, Paused from incoming network.
     * @param rate new fast clock speed multiplier.
     */
    private void setRateFromReply(int rate){
        if ( clock.getRun() && rate==0 ){
            clock.setRun(false);
        }
        if ( !clock.getRun() && rate!=0 ){
            clock.setRun(true);
        }
        double oldRate = clock.getRate();
        if ( (Math.abs(rate - oldRate) > 0.0001) && rate!=0 ) {
            try {
                clock.userSetRate(rate);
            }
            catch (TimebaseRateException ex) {}  // error message logged by clock.
        }
    }
    
    /**
     * Outgoing CAN Frames ignored.
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {
    }
    
    /**
     * String representation of time / date from a CanMessage or CanReply.
     * Does not check for FastClock OPC.
     * @param r FastClock Message to translate.
     * @return String format of Message, e.g. 
     */
    public static String dateFromCanFrame(jmri.jmrix.AbstractMessage r) {
    
        // not converting to a Java Date in case the data is incorrect
        // and we don't know 100% what year it is ( leap years ).
        
        StringBuilder sb = new StringBuilder();
        int speed = r.getElement(4) & 0xff;
        int month = (r.getElement(3) >>> 4);
        int weekday = r.getElement(3) - ( month << 4);
        
        log.debug("bs tot   {}",Integer.toBinaryString(r.getElement(3)));
        log.debug("bs day       {} {}",Integer.toBinaryString(weekday),weekday);
        log.debug("bs month {} {}",Integer.toBinaryString(month),month);
        // weekday month, bits 0-3 are the weekday (1=Sun,  2=Mon  3=Tues 4+Weds 5=Thurs 6=Fri 7=Sat
        // bits 4-7 are the month (1=Jan, 2=Feb etc)
        
        if (speed>0) {
            sb.append("Speed: x").append(speed).append(" ");
        } else {
            sb.append("Stopped ");
        }
        sb.append(String.format("%02d",(r.getElement(2) & 0xff))).append(":")
            .append(String.format("%02d",r.getElement(1) & 0xff)).append(" ");
        try {
            sb.append(DateFormatSymbols.getInstance().getWeekdays()[weekday]).append(" ");
        } catch ( ArrayIndexOutOfBoundsException ex ){
            sb.append("Incorrect weekday (").append(weekday).append(") ");
        }
        sb.append(r.getElement(5) & 0xff).append(" ");
        try {
            sb.append(DateFormatSymbols.getInstance().getMonths()[month-1]).append(" ");
        } catch ( ArrayIndexOutOfBoundsException ex ){
            sb.append("Incorrect month (").append(month).append(") ");
        }
        sb.append("Temp: ").append(tempFromTwos(r.getElement(6) & 0xff));
        return sb.toString();
    }
    
    /**
     * Stops listening for updates from network and main time base.
     *
     */
    public void dispose() {
        clock.removeMinuteChangeListener(this::newMinute);
        this.removeTc(_memo);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusClockControl.class);

}

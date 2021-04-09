package jmri.jmrit.logixng.tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.lang.reflect.Field;

import jmri.*;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.simpleclock.SimpleTimebase;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression signalMast
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionClockTest extends ImportExpressionComplexTestBase {

    Timebase fastClock;
    ConditionalVariable cv;
    
    private enum ClockEnum {
        Clock__start10_20__end_13_10(10,20,13,10),
        Clock__start18_15__end_12_25(18,15,12,25);
        
        private final int _start;
        private final int _end;
        
        private ClockEnum(int startHour, int startMin, int endHour, int endMin) {
            this._start = startHour * 60 + startMin;
            this._end = endHour * 60 + endMin;
        }
    }
    
    @Override
    public Enum[] getEnums() {
        return ClockEnum.values();
    }
    
    private boolean isLogixActivated() {
        try {
            Field privateStringField = logix.getClass().getDeclaredField("_isActivated");
            privateStringField.setAccessible(true);
            return (Boolean) privateStringField.get(logix);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException("Cannot read field _isActivated", e);
        }
    }
    
    @Override
    public void setNamedBeanState(Enum e, Setup setup) throws JmriException {
        
        ClockEnum ce = ClockEnum.valueOf(e.name());
        
        SimpleTimebase timeBase = new SimpleTimebase(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        PropertyChangeEvent evt = new PropertyChangeEvent(timeBase, "something", null, "new");
        
        cv.setType(Conditional.Type.FAST_CLOCK_RANGE);
        cv.setNum1(ce._start);  // start time
        cv.setNum2(ce._end);    // end time
        
        switch (ce) {
            case Clock__start10_20__end_13_10:
                switch (setup) {
                    case Init:
                        fastClock.setTime(new Date(0,0,0,9,20));    // 9:20
                        break;
                    case Fail1:
                        fastClock.setTime(new Date(0,0,0,9,30));    // 9:30
                        break;
                    case Fail2:
                        fastClock.setTime(new Date(0,0,0,21,20));   // 21:20
                        break;
                    case Fail3:
                        fastClock.setTime(new Date(0,0,0,17,30));   // 17:30
                        break;
                    case Succeed1:
                        fastClock.setTime(new Date(0,0,0,10,28));   // 10:28
                        fastClock.setTime(new Date(0,0,0,10,30));   // 10:30
                        break;
                    case Succeed2:
                        fastClock.setTime(new Date(0,0,0,12,30));   // 12:30
                        break;
                    case Succeed3:
                        fastClock.setTime(new Date(0,0,0,13,10));   // 13:10
                        break;
                    case Succeed4:
                        fastClock.setTime(new Date(0,0,0,11,05));   // 11:05
                        break;
                    default: throw new RuntimeException("Unknown enum: "+ce.name());
                }
                break;
            
            case Clock__start18_15__end_12_25:
                switch (setup) {
                    case Init:
                        fastClock.setTime(new Date(0,0,0,14,20));   // 14:20
                        break;
                    case Fail1:
                        fastClock.setTime(new Date(0,0,0,13,30));   // 13:30
                        break;
                    case Fail2:
                        fastClock.setTime(new Date(0,0,0,17,20));   // 17:20
                        break;
                    case Fail3:
                        fastClock.setTime(new Date(0,0,0,18,10));   // 18:10
                        break;
                    case Succeed1:
                        fastClock.setTime(new Date(0,0,0,10,30));   // 10:30
                        break;
                    case Succeed2:
                        fastClock.setTime(new Date(0,0,0,3,30));    // 3:30
                        break;
                    case Succeed3:
                        fastClock.setTime(new Date(0,0,0,7,30));    // 7:30
                        break;
                    case Succeed4:
                        fastClock.setTime(new Date(0,0,0,11,30));   // 11:30
                        break;
                    default: throw new RuntimeException("Unknown enum: "+ce.name());
                }
                break;
            
            default:
                throw new RuntimeException("Unknown enum: "+e.name());
        }
        if ((isLogixActivated()) && (conditional != null)) {
            conditional.calculate(true, evt);   // Logix expression Clock doesn't listen on the fast clock
        }
    }
    
    @Override
    public ConditionalVariable newConditionalVariable() {
        fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        cv = new ConditionalVariable();
        cv.setType(Conditional.Type.FAST_CLOCK_RANGE);
        return cv;
    }
    
}

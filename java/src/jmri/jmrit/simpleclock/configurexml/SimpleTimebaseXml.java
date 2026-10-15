package jmri.jmrit.simpleclock.configurexml;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import jmri.InstanceManager;
// import jmri.Timebase;
import jmri.time.FakeTimebase;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jmri.time.FakeTimebase.ClockInitialRunState.DO_NOTHING;
import static jmri.time.FakeTimebase.ClockInitialRunState.DO_START;
import static jmri.time.FakeTimebase.ClockInitialRunState.DO_STOP;

import jmri.time.*;

/**
 * Handle XML persistance of SimpleTimebase objects.
 *
 * @author Bob Jacobsen Copyright (c) 2003, 2008, 2017
 */
public class SimpleTimebaseXml extends jmri.configurexml.AbstractXmlAdapter {

    public SimpleTimebaseXml() {
    }

    /**
     * Default implementation for storing the contents of a SimpleTimebase.
     *
     * @param o Object to start process, but not actually used
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;

        FakeTimebase clock = InstanceManager.getDefault(TimeProviderManager.class).getFakeTimebase();

        TimeProvider timeProvider = InstanceManager.getDefault(TimeProviderManager.class).getCurrentTimeProvider();
        TimeSetter timeSetter = null;
        RateSetter rateSetter = null;
        StartStopTimeProvider startStopTimeProvider = null;

        if (timeProvider instanceof TimeSetter) {
            timeSetter = (TimeSetter) timeProvider;
        }
        if (timeProvider instanceof RateSetter) {
            rateSetter = (RateSetter) timeProvider;
        }
        if (timeProvider instanceof StartStopTimeProvider && ((StartStopTimeProvider) timeProvider).canStartAndStop()) {
            startStopTimeProvider = (StartStopTimeProvider) timeProvider;
        }

        String val, val2;
        if (shared.getAttribute("master") != null) {
            val = shared.getAttributeValue("master");
            if (val.equals("yes")) {
                clock.setInternalMaster(true, false);
            }
            if (val.equals("no")) {
                clock.setInternalMaster(false, false);
                if (shared.getAttribute("mastername") != null) {
                    clock.setMasterName(shared.getAttributeValue("mastername"));
                }
            }
        }
        if (shared.getAttribute("sync") != null) {
            val = shared.getAttributeValue("sync");
            if (val.equals("yes")) {
                clock.setSynchronize(true, false);
            }
            if (val.equals("no")) {
                clock.setSynchronize(false, false);
            }
        }
        if (shared.getAttribute("correct") != null) {
            val = shared.getAttributeValue("correct");
            if (val.equals("yes")) {
                clock.setCorrectHardware(true, false);
            }
            if (val.equals("no")) {
                clock.setCorrectHardware(false, false);
            }
        }
        if (shared.getAttribute("display") != null) {
            val = shared.getAttributeValue("display");
            if (val.equals("yes")) {
                clock.set12HourDisplay(true, false);
            }
            if (val.equals("no")) {
                clock.set12HourDisplay(false, false);
            }
        }
        if (shared.getAttribute("showbutton") != null) {
            val = shared.getAttributeValue("showbutton");
            if (val.equals("yes")) {
                clock.setShowStopButton(true);
            }
            if (val.equals("no")) {
                clock.setShowStopButton(false);
            }
        }
        if ("yes".equals(shared.getAttributeValue("startrunning"))) {
            if (startStopTimeProvider != null) {
                startStopTimeProvider.start();
            }
            clock.setClockInitialRunState(DO_START);
        } else if ("yes".equals(shared.getAttributeValue("startstopped"))) {
            if (startStopTimeProvider != null) {
                startStopTimeProvider.stop();
            }
            clock.setClockInitialRunState(DO_STOP);
        } else if (shared.getAttribute("startrunning") != null){
            clock.setClockInitialRunState(DO_NOTHING);
        } else {
            // legacy XML
            if (shared.getAttribute("run") != null) {
                val = shared.getAttributeValue("run");
                if (val.equals("yes")) {
                    if (startStopTimeProvider != null) {
                        startStopTimeProvider.start();
                    }
                    clock.setClockInitialRunState(DO_START);
                }
                if (val.equals("no")) {
                    if (startStopTimeProvider != null) {
                        startStopTimeProvider.stop();
                    }
                    clock.setClockInitialRunState(DO_STOP);
                }
            }
        }
        if (shared.getAttribute("startsetrate") != null) {
            val = shared.getAttributeValue("startsetrate");
            clock.setSetRateAtStart(!val.equals("no"));
        }
        boolean hasRate = false;
        if (shared.getAttribute("startrate") != null) {
            try {
                double r = shared.getAttribute("startrate").getDoubleValue();
                clock.setStartRate(r);
                if (rateSetter != null) {
                    rateSetter.setRate(r);
                }
                hasRate = true;
            } catch (org.jdom2.DataConversionException e2) {
                log.error("Cannot convert start rate", e2);
                result = false;
            }
        }
        if (!hasRate && shared.getAttribute("rate") != null) {
            try {
                double r = shared.getAttribute("rate").getDoubleValue();
                clock.setStartRate(r);
                if (rateSetter != null) {
                    rateSetter.setRate(r);
                }
                hasRate = true;
            } catch (org.jdom2.DataConversionException e2) {
                log.error("Cannot convert rate", e2);
                result = false;
            }
        }
        if (clock.getSetRateAtStart() && hasRate) {
            clock.userSetRate(clock.getStartRate());
        }
/*        if (clock.getSetRateAtStart() && hasRate) {
            try {
                clock.userSetRate(clock.getStartRate());
            } catch (jmri.TimebaseRateException e1) {
                log.error("Cannot restore rate: {}", clock.getStartRate(), e1);
                result = false;
            }
        }
*/
        if (shared.getAttribute("startsettime") != null) {
            val = shared.getAttributeValue("startsettime");
            if (val.equals("yes")) {
                if (shared.getAttribute("time") != null) {
                    val2 = shared.getAttributeValue("time");
                    clock.setStartSetTime(true, ZonedDateTime.parse(val2,TimeProvider.LEGACY_XML_DATETIME_FORMAT));
                    clock.setTime(ZonedDateTime.parse(val2,TimeProvider.LEGACY_XML_DATETIME_FORMAT));
/*                    try {
                        clock.setStartSetTime(true, format.parse(val2));
                        clock.setTime(format.parse(val2));
                    } catch (ParseException e) {
                        // if non-invertable date format, just skip
                        log.warn("Cannot set date using value stored in file: {}", val2);
                        result = false;
                    }
*/
                }
            } else if (val.equals("no")) {
                if (shared.getAttribute("time") != null) {
                    val2 = shared.getAttributeValue("time");
                    clock.setStartSetTime(false, ZonedDateTime.parse(val2,TimeProvider.LEGACY_XML_DATETIME_FORMAT));
                    try {
                        if (timeSetter != null) {
                            ZonedDateTime zdt = ZonedDateTime.parse(
                                    val2,
                                    TimeProvider.LEGACY_XML_DATETIME_FORMAT);
                            timeSetter.setDateTime(zdt);
                        }
                    } catch (DateTimeParseException e) {
                        // if non-invertable date format, just skip
                        log.warn("Cannot set date using value stored in file: {}", val2);
                        result = false;
                    }
                }
            }
        } else if (shared.getAttribute("time") != null) {
            // this only to preserve previous behavior for preexisting files
            val2 = shared.getAttributeValue("time");
            try {
                clock.setStartSetTime(true, ZonedDateTime.parse(val2,TimeProvider.LEGACY_XML_DATETIME_FORMAT));
                clock.setTime(ZonedDateTime.parse(val2,TimeProvider.LEGACY_XML_DATETIME_FORMAT));
//                clock.setStartSetTime(true, format.parse(val2));
//                clock.setTime(format.parse(val2));
                if (timeSetter != null) {
                    ZonedDateTime ldt = ZonedDateTime.parse(
                            val2,
                            TimeProvider.LEGACY_XML_DATETIME_FORMAT);
                    timeSetter.setDateTime(ldt);
                }
            } catch (DateTimeParseException e) {
                // if non-invertable date format, just skip
                log.warn("Cannot set date using value stored in file: {}", val2);
                result = false;
            }

        }
        if (shared.getAttribute("startclockoption") != null) {
            int option = Integer.parseInt(shared.getAttribute(
                    "startclockoption").getValue());
            clock.setStartClockOption(option);
            clock.initializeClock();
//            clock.setStartClockOption(option);
//            clock.initializeClock();
        }
        clock.initializeHardwareClock();
//        clock.initializeHardwareClock();
        return result;
    }

    // Conversion format for dates created by Java Date.toString().
    // The Locale needs to be always US, irrelevant from computer's and program's settings!
    final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    @Override
    public int loadOrder() {
        return jmri.Manager.TIMEBASE;
    }

    private static final Logger log = LoggerFactory.getLogger(SimpleTimebaseXml.class);

}

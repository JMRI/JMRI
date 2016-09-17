package jmri.jmrit.simpleclock.configurexml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Timebase;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of SimpleTimebase objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 */
public class SimpleTimebaseXml extends jmri.configurexml.AbstractXmlAdapter {

    public SimpleTimebaseXml() {
    }

    /**
     * Default implementation for storing the contents of a SimpleTimebase.
     * <P>
     *
     * @param o Object to start process, but not actually used
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        Timebase clock = InstanceManager.getDefault(jmri.Timebase.class);

        Element elem = new Element("timebase");
        elem.setAttribute("class", this.getClass().getName());

        elem.setAttribute("time", clock.getStartTime().toString());
        elem.setAttribute("rate", "" + clock.userGetRate());
        elem.setAttribute("run", (!clock.getStartStopped() ? "yes" : "no"));
        elem.setAttribute("master", (clock.getInternalMaster() ? "yes" : "no"));
        if (!clock.getInternalMaster()) {
            elem.setAttribute("mastername", clock.getMasterName());
        }
        elem.setAttribute("sync", (clock.getSynchronize() ? "yes" : "no"));
        elem.setAttribute("correct", (clock.getCorrectHardware() ? "yes" : "no"));
        elem.setAttribute("display", (clock.use12HourDisplay() ? "yes" : "no"));
        elem.setAttribute("startstopped", (clock.getStartStopped() ? "yes" : "no"));
        elem.setAttribute("startsettime", (clock.getStartSetTime() ? "yes" : "no"));
        elem.setAttribute("startclockoption", Integer.toString(
                clock.getStartClockOption()));

        return elem;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        Timebase clock = InstanceManager.getDefault(jmri.Timebase.class);
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
        if (shared.getAttribute("run") != null) {
            val = shared.getAttributeValue("run");
            if (val.equals("yes")) {
                clock.setRun(true);
                clock.setStartStopped(false);
            }
            if (val.equals("no")) {
                clock.setRun(false);
                clock.setStartStopped(true);
            }
        }
        if (shared.getAttribute("rate") != null) {
            try {
                double r = shared.getAttribute("rate").getDoubleValue();
                try {
                    clock.userSetRate(r);
                } catch (jmri.TimebaseRateException e1) {
                    log.error("Cannot restore rate: " + r + " " + e1);
                    result = false;
                }
            } catch (org.jdom2.DataConversionException e2) {
                log.error("Cannot convert rate: " + e2);
                result = false;
            }
        }
        if (shared.getAttribute("startsettime") != null) {
            val = shared.getAttributeValue("startsettime");
            if (val.equals("yes")) {
                if (shared.getAttribute("time") != null) {
                    val2 = shared.getAttributeValue("time");
                    try {
                        clock.setStartSetTime(true, format.parse(val2));
                        clock.setTime(format.parse(val2));
                    } catch (ParseException e) {
                        // if non-invertable date format, just skip
                        log.warn("Cannot set date using value stored in file: " + val2);
                        result = false;
                    }
                }
            } else if (val.equals("no")) {
                if (shared.getAttribute("time") != null) {
                    val2 = shared.getAttributeValue("time");
                    try {
                        clock.setStartSetTime(false, format.parse(val2));
                    } catch (ParseException e) {
                        // if non-invertable date format, just skip
                        log.warn("Cannot set date using value stored in file: " + val2);
                        result = false;
                    }
                }
            }
        } else if (shared.getAttribute("time") != null) {
            // this only to preserve previous behavior for preexisting files
            val2 = shared.getAttributeValue("time");
            try {
                clock.setStartSetTime(true, format.parse(val2));
                clock.setTime(format.parse(val2));
            } catch (ParseException e) {
                // if non-invertable date format, just skip
                log.warn("Cannot set date using value stored in file: " + val2);
                result = false;
            }
        }
        if (shared.getAttribute("startclockoption") != null) {
            int option = Integer.parseInt(shared.getAttribute(
                    "startclockoption").getValue());
            clock.setStartClockOption(option);
            clock.initializeClock();
        }
        clock.initializeHardwareClock();
        return result;
    }

    // Conversion format for dates created by Java Date.toString().
    // The Locale needs to be always US, irrelevant from computer's and program's settings!
    final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    public void load(Element element, Object o) {
        log.error("load(Element, Object) called unexpectedly");
    }

    public int loadOrder() {
        return jmri.Manager.TIMEBASE;
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleTimebaseXml.class.getName());

}

package jmri.time.implementation.configurexml;


import jmri.configurexml.JmriConfigureXmlException;

import jmri.InstanceManager;
import jmri.time.*;

import org.jdom2.Element;

// import static jmri.time.FakeTimebase.ClockInitialRunState.DO_NOTHING;
import static jmri.time.FakeTimebase.ClockInitialRunState.DO_START;
import static jmri.time.FakeTimebase.ClockInitialRunState.DO_STOP;

/**
 * Store and load a DefaultTimebase.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class DefaultTimebaseXml extends jmri.configurexml.AbstractXmlAdapter {

    @Override
    public Element store(Object o) {

        FakeTimebase clock = InstanceManager.getDefault(TimeProviderManager.class).getFakeTimebase();



//        Timebase clock = InstanceManager.getDefault(jmri.Timebase.class);
        TimeProvider timeProvider = InstanceManager.getDefault(TimeProviderManager.class).getCurrentTimeProvider();

        Element elem = new Element("timebase");
        if (1==1) return elem;

//        elem.setAttribute("class", this.getClass().getName());
        elem.setAttribute("class", "jmri.jmrit.simpleclock.configurexml.SimpleTimebaseXml");

        var loadAndStorePreferences = InstanceManager.getDefault(jmri.configurexml.LoadAndStorePreferences.class);
        if (! loadAndStorePreferences.isExcludeTimebase() ) {
//            elem.setAttribute("time", timeProvider.getTime().format(DateTimeFormatter.ISO_DATE));
            elem.setAttribute("time", timeProvider.getTime().format(TimeProvider.LEGACY_XML_DATETIME_FORMAT));
        }
/*DANIEL*/
        elem.setAttribute("rate", "" + clock.userGetRate());
        elem.setAttribute("startrate", "" + clock.getStartRate());
        elem.setAttribute("run", (clock.getClockInitialRunState() == DO_START ? "yes" : "no"));
        elem.setAttribute("master", (clock.getInternalMaster() ? "yes" : "no"));
        if (!clock.getInternalMaster()) {
            elem.setAttribute("mastername", clock.getMasterName());
        }
        elem.setAttribute("sync", (clock.getSynchronize() ? "yes" : "no"));
        elem.setAttribute("correct", (clock.getCorrectHardware() ? "yes" : "no"));
        elem.setAttribute("display", (clock.use12HourDisplay() ? "yes" : "no"));
        elem.setAttribute("startstopped", (clock.getClockInitialRunState() == DO_STOP ? "yes" : "no"));
        elem.setAttribute("startrunning", ((clock.getClockInitialRunState() == DO_START) ? "yes" : "no"));
        elem.setAttribute("startsettime", (clock.getStartSetTime() ? "yes" : "no"));
        elem.setAttribute("startclockoption", Integer.toString(
                clock.getStartClockOption()));
        elem.setAttribute("showbutton", (clock.getShowStopButton() ? "yes" : "no"));
        elem.setAttribute("startsetrate", (clock.getSetRateAtStart() ? "yes" : "no"));
/**/
        return elem;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        return true;
    }

}

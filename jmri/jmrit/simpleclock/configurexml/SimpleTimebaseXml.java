package jmri.jmrit.simpleclock.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.Timebase;
import jmri.InstanceManager;
import jmri.jmrit.simpleclock.*;

import java.util.Date;

import org.jdom.Element;
import org.jdom.Attribute;

/**
 * Handle XML persistance of SimpleTimebase objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class SimpleTimebaseXml implements XmlAdapter {

    public SimpleTimebaseXml() {
    }

    /**
     * Default implementation for storing the contents of
     * a SimpleTimebase.
     * <P>
     *
     * @param o Object to start process, but not actually used
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        Timebase clock = InstanceManager.timebaseInstance();
        
        Element elem = new Element("timebase");
        elem.addAttribute("class", this.getClass().getName());

        elem.addAttribute("time", clock.getTime().toString());
        elem.addAttribute("rate", ""+clock.getRate());
        elem.addAttribute("run", (clock.getRun()?"yes":"no"));
        
        return elem;
    }

    /**
     * Update static data from XML file
     * @param element Top level blocks Element to unpack.
      */
    public void load(Element element) {

        Timebase clock = InstanceManager.timebaseInstance();
        String val;

        if (element.getAttribute("run")!=null) {
            val = element.getAttributeValue("run");
            if (val.equals("yes")) clock.setRun(true);
            if (val.equals("no")) clock.setRun(false);
        }

        if (element.getAttribute("rate")!=null) {
            try {
                double r = element.getAttribute("rate").getDoubleValue();
                try {
                    clock.setRate(r);
                } catch (jmri.TimebaseRateException e1) {
                    log.error("Cannot restore rate: "+r+" "+e1);
                }
            } catch (org.jdom.DataConversionException e2) {
                log.error("Cannot convert rate: "+e2);
            }
        }

        if (element.getAttribute("time")!=null) {
            val = element.getAttributeValue("time");
            clock.setTime(new Date(val));
        }
    }


    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("load(Element, Object) called unexpectedly");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SimpleTimebaseXml.class.getName());

}
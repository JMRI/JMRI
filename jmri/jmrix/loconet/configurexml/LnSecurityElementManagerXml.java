package jmri.jmrix.loconet.configurexml;

import org.jdom.*;
import jmri.InstanceManager;
import jmri.configurexml.*;
import jmri.jmrix.loconet.*;
import java.util.Enumeration;
import com.sun.java.util.collections.List;

/**
 * Provides load and store functionality for
 * configuring LnSecurityElementManager.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.7 $
 */
public class LnSecurityElementManagerXml implements XmlAdapter {

    public LnSecurityElementManagerXml() {
        super();
    }

    public Element store(Object o) {
        Element elements = new Element("securityelements");
        elements.addAttribute("class","jmri.jmrix.loconet.configurexml.LnSecurityElementManagerXml");
        LnSecurityElementManager sem = (LnSecurityElementManager) o;
        if (sem!=null) {
            com.sun.java.util.collections.Iterator iter =
                                    sem.getSecurityElementList().iterator();

            while (iter.hasNext()) {
                SecurityElement se = (SecurityElement)iter.next();
                // we do a brute force store here eventually
                Element elem = new Element("securityelement");
                elem.addAttribute("number", String.valueOf(se.mNumber));     // own SE number

                elem.addAttribute("dsSensor", String.valueOf(se.dsSensor));
                elem.addAttribute("turnout", String.valueOf(se.turnout));
                elem.addAttribute("auxInput", String.valueOf(se.auxInput));

                String makeReservation = "";
                if (se.makeAReservation) makeReservation+="A";
                if (se.makeBReservation) makeReservation+="B";
                if (se.makeCReservation) makeReservation+="C";
                if (makeReservation.equals("")) makeReservation = "none";
                elem.addAttribute("makeReservation", makeReservation);
                String onAXReservation;
                if (se.onAXReservation == SecurityElement.STOPOPPOSITE)
                    onAXReservation = "stopOpposite";
                else if (se.onAXReservation == SecurityElement.STOPUNRESERVED)
                    onAXReservation = "stopUnreserved";
                else
                    onAXReservation = "none";
                elem.addAttribute("onAXReservation", onAXReservation);
                String onXAReservation;
                if (se.onXAReservation == SecurityElement.STOPOPPOSITE)
                    onXAReservation = "stopOpposite";
                else if (se.onXAReservation == SecurityElement.STOPUNRESERVED)
                    onXAReservation = "stopUnreserved";
                else
                    onXAReservation = "none";
                elem.addAttribute("onXAReservation", onXAReservation);

                elem.addAttribute("attachAnum", String.valueOf(se.attachAnum));
                elem.addAttribute("attachAleg", String.valueOf(se.attachAleg));

                elem.addAttribute("attachBnum", String.valueOf(se.attachBnum));
                elem.addAttribute("attachBleg", String.valueOf(se.attachBleg));

                elem.addAttribute("attachCnum", String.valueOf(se.attachCnum));
                elem.addAttribute("attachCleg", String.valueOf(se.attachCleg));

                elem.addAttribute("maxSpeedAC", String.valueOf(se.maxSpeedAC));
                elem.addAttribute("maxSpeedCA", String.valueOf(se.maxSpeedCA));
                elem.addAttribute("maxSpeedAB", String.valueOf(se.maxSpeedAB));
                elem.addAttribute("maxSpeedBA", String.valueOf(se.maxSpeedBA));

                elem.addAttribute("maxBrakingAC", String.valueOf(se.maxBrakingAC));
                elem.addAttribute("maxBrakingCA", String.valueOf(se.maxBrakingCA));
                elem.addAttribute("maxBrakingAB", String.valueOf(se.maxBrakingAB));
                elem.addAttribute("maxBrakingBA", String.valueOf(se.maxBrakingBA));

                elements.addContent(elem);
            }
        }
        return elements;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element elements) {
        // create the master object
        LnSecurityElementManager mgr = new LnSecurityElementManager();

        // register it for configuration
        InstanceManager.configureManagerInstance().registerConfig(mgr);
        // load individual security elements
        loadElements(elements);
    }

    /**
     * Utility method to load the individual SecurityElement objects.
     * @param elements Element containing the securityelement elements to load.
     */
    public void loadElements(Element elements) {
        List elementList = elements.getChildren("securityelement");
        if (log.isDebugEnabled()) log.debug("Found "+elementList.size()+" securityelement s");
        LnSecurityElementManager sem = LnSecurityElementManager.instance();

        for (int i=0; i<elementList.size(); i++) {
            // create SecurityElement & aspect generator
            Element current = (Element)(elementList.get(i));
            int number = Integer.parseInt(current.getAttribute("number").getValue());
            SecurityElement se = sem.getSecurityElement(number);
            AspectGenerator ag = sem.getAspectGenerator(number);

            // brute force the parameters
            Attribute a;
            if ((a = current.getAttribute("attachAnum")) != null)
                se.attachAnum = getIntValue(a);
            if ((a = current.getAttribute("attachAleg")) != null)
                se.attachAleg = getIntValue(a);
            if ((a = current.getAttribute("attachBnum")) != null)
                se.attachBnum = getIntValue(a);
            if ((a = current.getAttribute("attachBleg")) != null)
                se.attachBleg = getIntValue(a);
            if ((a = current.getAttribute("attachCnum")) !=null)
                se.attachCnum = getIntValue(a);
            if ((a = current.getAttribute("attachCleg")) !=null)
                se.attachCleg = getIntValue(a);

            if ((a = current.getAttribute("dsSensor")) !=null)
                se.dsSensor = getIntValue(a);
            if ((a = current.getAttribute("turnout")) !=null)
                se.turnout = getIntValue(a);
            if ((a = current.getAttribute("auxInput")) !=null)
                se.auxInput = getIntValue(a);

            if ((a = current.getAttribute("maxSpeedAC")) !=null)
                se.maxSpeedAC = getIntValue(a);
            if ((a = current.getAttribute("maxSpeedAB")) !=null)
                se.maxSpeedAB = getIntValue(a);
            if ((a = current.getAttribute("maxSpeedCA")) !=null)
                se.maxSpeedCA = getIntValue(a);
            if ((a = current.getAttribute("maxSpeedBA")) !=null)
                se.maxSpeedBA = getIntValue(a);
            if ((a = current.getAttribute("maxBrakingAC")) !=null)
                se.maxBrakingAC = getIntValue(a);
            if ((a = current.getAttribute("maxBrakingAB")) !=null)
                se.maxBrakingAB = getIntValue(a);
            if ((a = current.getAttribute("maxBrakingCA")) !=null)
                se.maxBrakingCA = getIntValue(a);
            if ((a = current.getAttribute("maxBrakingBA")) !=null)
                se.maxBrakingBA = getIntValue(a);

            se.onAXReservation = SecurityElement.NONE;
            if ((a = current.getAttribute("onAXReservation")) !=null) {
                if (a.getValue().equals("stopOpposite"))
                    se.onAXReservation = SecurityElement.STOPOPPOSITE;
                else if (a.getValue().equals("stopUnreserved"))
                    se.onAXReservation = SecurityElement.STOPUNRESERVED;
                else
                    se.onAXReservation = SecurityElement.NONE;
            }

            se.onXAReservation = SecurityElement.NONE;
            if ((a = current.getAttribute("onXAReservation")) !=null) {
                if (a.getValue().equals("stopOpposite"))
                    se.onXAReservation = SecurityElement.STOPOPPOSITE;
                else if (a.getValue().equals("stopUnreserved"))
                    se.onXAReservation = SecurityElement.STOPUNRESERVED;
                else
                    se.onXAReservation = SecurityElement.NONE;
            }

            se.makeAReservation = false;
            se.makeBReservation = false;
            se.makeCReservation = false;
            if ((a = current.getAttribute("makeReservation")) !=null) {
                if (a.getValue().indexOf("A")!=-1)
                    se.makeAReservation = true;
                if (a.getValue().indexOf("B")!=-1)
                    se.makeBReservation = true;
                if (a.getValue().indexOf("C")!=-1)
                    se.makeCReservation = true;
            }

            // do mode last, as it overwrites the specific details
            if ((a= current.getAttribute("mode")) !=null) {
                log.warn("SE definition uses deprecated 'mode' attribute");
                String mode = a.getValue();
                if (mode.equals("ABS")) { /* nothing needed */ }
                else if (mode.equals("APB")) {
                    se.onXAReservation = SecurityElement.STOPOPPOSITE;
                    se.onAXReservation = SecurityElement.STOPOPPOSITE;
                    se.makeBReservation = true;
                    se.makeAReservation = true;
                }
                else if (mode.equals("head")) {
                    se.onXAReservation = SecurityElement.STOPOPPOSITE;
                    se.onAXReservation = SecurityElement.STOPOPPOSITE;
                    se.makeBReservation = true;
                }
                else if (!mode.equals("none"))
                    log.error("Unrecognized mode: "+mode);
            }
        }
    }

    /**
     * Service routine to handle attribute parsing for int values
     * @param a Attribute with int value
     * @return -1 if error, hence value
     */
    int getIntValue(Attribute a) {
        try {
            return a.getIntValue();
        } catch (org.jdom.DataConversionException e) {
            log.error("improper formatted int "+a.getValue()+" in Attribute "+a.getName());
            return -1;
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSecurityElementManagerXml.class.getName());

}
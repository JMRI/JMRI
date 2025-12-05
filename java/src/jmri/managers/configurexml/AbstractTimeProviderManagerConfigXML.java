package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.InstanceManager;
import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * TimeProviderManagers, working with AbstractTimeProviderManagers.
 * <p>
 * Typically, a subclass will just implement the load(Element timeProviders) class,
 * relying on implementation here to load the individual timeProviders. Note that
 * these are stored explicitly, so the resolution mechanism doesn't need to see
 * *Xml classes for each specific TimeProvider or AbstractTimeProvider subclass at store
 * time.
 *
 * @author Bob Jacobsen      Copyright (C) 2002
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public abstract class AbstractTimeProviderManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractTimeProviderManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a TimeProviderManager and
     * associated TimeProviderOperations.
     *
     * @param o Object to store, of type TimeProviderManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element timeProviders = new Element("timeProviders");
        setStoreElementClass(timeProviders);
        TimeProviderManager tm = (TimeProviderManager) o;
        if (tm != null) {
            SortedSet<TimeProvider> tList = tm.getNamedBeanSet();
            // don't return an element if there are no timeProviders to include
            if (tList.isEmpty()) {
                return null;
            }
            for (TimeProvider t : tList) {
                // store the timeProviders
                String tName = t.getSystemName();
                log.debug("system name is {}", tName);

                Element elem = jmri.configurexml.ConfigXmlManager.elementFromObject(t);
                if (elem == null) {
                    throw new RuntimeException("Cannot load xml configurator for " + t.getClass().getName());
                }
/*
                // include feedback info
                elem.setAttribute("feedback", t.getFeedbackModeName());
                NamedBeanHandle<Sensor> s = t.getFirstNamedSensor();
                if (s != null) {
                    elem.setAttribute("sensor1", s.getName());
                }
                s = t.getSecondNamedSensor();
                if (s != null) {
                    elem.setAttribute("sensor2", s.getName());
                }

                // include timeProvider inverted
                elem.setAttribute("inverted", t.getInverted() ? "true" : "false");

                if (t.canLock(TimeProvider.CABLOCKOUT | TimeProvider.PUSHBUTTONLOCKOUT)) {
                    // include timeProvider locked
                    elem.setAttribute("locked", t.getLocked(TimeProvider.CABLOCKOUT + TimeProvider.PUSHBUTTONLOCKOUT) ? "true" : "false");
                    // include timeProvider lock mode
                    String lockOpr;
                    if (t.canLock(TimeProvider.CABLOCKOUT) && t.canLock(TimeProvider.PUSHBUTTONLOCKOUT)) {
                        lockOpr = "both";
                    } else if (t.canLock(TimeProvider.CABLOCKOUT)) {
                        lockOpr = "cab";
                    } else if (t.canLock(TimeProvider.PUSHBUTTONLOCKOUT)) {
                        lockOpr = "pushbutton";
                    } else {
                        lockOpr = "none";
                    }
                    elem.setAttribute("lockMode", lockOpr);
                    // include timeProvider decoder
                    elem.setAttribute("decoder", t.getDecoderName());
                }

                // include number of control bits, if different from one
                int iNum = t.getNumberControlBits();
                if (iNum != 1) {
                    elem.setAttribute("numBits", "" + iNum);
                }

                // include timeProvider control type, if different from 0
                int iType = t.getControlType();
                if (iType != 0) {
                    elem.setAttribute("controlType", "" + iType);
                }

                // add operation stuff
                String opstr = null;
                TimeProviderOperation op = t.getTimeProviderOperation();
                if (t.getInhibitOperation()) {
                    opstr = "Off";
                } else if (op == null) {
                    opstr = "Default";
                } else if (op.isNonce()) { // nonce operation appears as subelement
                    TimeProviderOperationXml adapter = TimeProviderOperationXml.getAdapter(op);
                    if (adapter != null) {
                        Element nonceOpElem = adapter.store(op);
                        elem.addContent(nonceOpElem);
                    }
                } else {
                    opstr = op.getName();
                }
                if (opstr != null) {
                    elem.setAttribute("automate", opstr);
                }
                if ((t.getDivergingSpeed() != null) && (!t.getDivergingSpeed().isEmpty()) && !t.getDivergingSpeed().contains("Global")) {
                    elem.addContent(new Element("divergingSpeed").addContent(t.getDivergingSpeed()));
                }
                if ((t.getStraightSpeed() != null) && (!t.getStraightSpeed().isEmpty()) && !t.getStraightSpeed().contains("Global")) {
                    elem.addContent(new Element("straightSpeed").addContent(t.getStraightSpeed()));
                }
*/
                // add element
                timeProviders.addContent(elem);
            }
        }
        return timeProviders;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param timeProviders The top-level element being created
     */
    abstract public void setStoreElementClass(Element timeProviders);

    @Override
    public abstract boolean load(Element shared, Element perNode);

    /**
     * Utility method to load the individual TimeProvider objects. If there's no
     * additional info needed for a specific timeProvider type, invoke this with the
     * parent of the set of TimeProvider elements.
     *
     * @param shared Element containing the TimeProvider elements to load.
     * @param perNode Element containing per-node TimeProvider data.
     * @return true if succeeded
     */
    public boolean loadTimeProviders(Element shared, Element perNode) {
        boolean result = true;
        List<Element> timeProviderList = shared.getChildren("timeProvider");
        log.debug("Found {} timeProviders", timeProviderList.size());
        TimeProviderManager tm = InstanceManager.getDefault(TimeProviderManager.class);
        tm.setPropertyChangesSilenced("beans", true);
/*
        try {
            if (shared.getChild("defaultclosedspeed") != null) {
                String closedSpeed = shared.getChild("defaultclosedspeed").getText();
                if (closedSpeed != null && !closedSpeed.isEmpty()) {
                    tm.setDefaultClosedSpeed(closedSpeed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error("JmriException {}", ex.getMessage() );
        }

        try {
            if (shared.getChild("defaultthrownspeed") != null) {
                String thrownSpeed = shared.getChild("defaultthrownspeed").getText();
                if (thrownSpeed != null && !thrownSpeed.isEmpty()) {
                    tm.setDefaultThrownSpeed(thrownSpeed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error("JmriException {}", ex.getMessage() );
        }
*/
        for (Element elem : timeProviderList) {
            String sysName = getSystemName(elem);
            if (sysName == null) {
                log.error("unexpected null in systemName {}", elem);
                result = false;
                break;
            }
            String userName = getUserName(elem);

            checkNameNormalization(sysName, userName, tm);

            log.debug("create timeProvider: ({})({})", sysName, (userName == null ? "<null>" : userName));
            TimeProvider t = tm.getBySystemName(sysName);
            if (t == null) {
                throw new RuntimeException("Not implemented yet");
//                t = tm.newTimeProvider(sysName, userName);
                // nothing is logged in the console window as the newTimeProviderFunction already does this.
            } else if (userName != null) {
                t.setUserName(userName);
            }

            // Load common parts
            loadCommon(t, elem);
/*
            // now configure feedback if needed
            Attribute a;
            a = elem.getAttribute("sensor1");
            if (a != null) {
                try {
                    t.provideFirstFeedbackSensor(a.getValue());
                } catch (jmri.JmriException e) {
                    result = false;
                }
            }
            a = elem.getAttribute("sensor2");
            if (a != null) {
                try {
                    t.provideSecondFeedbackSensor(a.getValue());
                } catch (jmri.JmriException e) {
                    result = false;
                }
            }
            a = elem.getAttribute("feedback");
            if (a != null) {
                try {
                    t.setFeedbackMode(a.getValue());
                } catch (IllegalArgumentException e) {
                    log.error("Can not set feedback mode: '{}' for timeProvider: '{}' user name: '{}'",
                            a.getValue(), sysName, (userName == null ? "" : userName));
                    result = false;
                }
            }

            // check for timeProvider inverted
            t.setInverted(getAttributeBool(elem, "inverted", false));

            // check for timeProvider decoder
            a = elem.getAttribute("decoder");
            if (a != null) {
                t.setDecoderName(a.getValue());
            }

            // check for timeProvider lock mode
            a = elem.getAttribute("lockMode");
            if (a != null) {
                if (a.getValue().equals("both")) {
                    t.enableLockOperation(TimeProvider.CABLOCKOUT + TimeProvider.PUSHBUTTONLOCKOUT, true);
                }
                if (a.getValue().equals("cab")) {
                    t.enableLockOperation(TimeProvider.CABLOCKOUT, true);
                    t.enableLockOperation(TimeProvider.PUSHBUTTONLOCKOUT, false);
                }
                if (a.getValue().equals("pushbutton")) {
                    t.enableLockOperation(TimeProvider.PUSHBUTTONLOCKOUT, true);
                    t.enableLockOperation(TimeProvider.CABLOCKOUT, false);
                }
            }

            // check for timeProvider locked
            a = elem.getAttribute("locked");
            if (a != null) {
                t.setLocked(TimeProvider.CABLOCKOUT + TimeProvider.PUSHBUTTONLOCKOUT, a.getValue().equals("true"));
            }

            // number of bits, if present - if not, defaults to 1
            a = elem.getAttribute("numBits");
            if (a == null) {
                t.setNumberControlBits(1);
            } else {
                int iNum = Integer.parseInt(a.getValue());
                if ((iNum == 1) || (iNum == 2)) {
                    t.setNumberControlBits(iNum);
                } else {
                    log.warn("illegal number of output bits for control of timeProvider {}", sysName);
                    t.setNumberControlBits(1);
                    result = false;
                }
            }

            // control type, if present - if not, defaults to 0
            a = elem.getAttribute("controlType");
            if (a == null) {
                t.setControlType(0);
            } else {
                int iType = Integer.parseInt(a.getValue());
                if (iType >= 0) {
                    t.setControlType(iType);
                } else {
                    log.warn("illegal control type for control of timeProvider {}", sysName);
                    t.setControlType(0);
                    result = false;
                }
            }

            // operation stuff
            List<Element> myOpList = elem.getChildren("operation");
            if (!myOpList.isEmpty()) {
                if (myOpList.size() > 1) {
                    log.warn("unexpected extra elements found in timeProvider-specific operations");
                    result = false;
                }
                TimeProviderOperation toper = TimeProviderOperationXml.loadOperation(myOpList.get(0));
                t.setTimeProviderOperation(toper);
            } else {
                a = elem.getAttribute("automate");
                if (a != null) {
                    String str = a.getValue();
                    if (str.equals("Off")) {
                        t.setInhibitOperation(true);
                    } else if (!str.equals("Default")) {
                        t.setInhibitOperation(false);
                        TimeProviderOperation toper
                                = InstanceManager.getDefault(TimeProviderOperationManager.class).getOperation(str);
                        t.setTimeProviderOperation(toper);
                    } else {
                        t.setInhibitOperation(false);
                    }
                }
            }

            //  set initial state from sensor feedback if appropriate
            t.setInitialKnownStateFromFeedback();
            try {
                t.setDivergingSpeed("Global");
                if (elem.getChild("divergingSpeed") != null) {
                    String speed = elem.getChild("divergingSpeed").getText();
                    if (speed != null && !speed.isEmpty() && !speed.contains("Global")) {
                        t.setDivergingSpeed(speed);
                    }
                }
            } catch (jmri.JmriException ex) {
                log.error("TimeProvider {} : {}", t, ex.getMessage());
            }

            try {
                t.setStraightSpeed("Global");
                if (elem.getChild("straightSpeed") != null) {
                    String speed = elem.getChild("straightSpeed").getText();
                    if (speed != null && !speed.isEmpty() && !speed.contains("Global")) {
                        t.setStraightSpeed(speed);
                    }
                }
            } catch (jmri.JmriException ex) {
                log.error("TimeProvider {} : {}", t, ex.getMessage());
            }
*/
        }

        tm.setPropertyChangesSilenced("beans", false);

        return result;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(TimeProviderManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTimeProviderManagerConfigXML.class);

}

package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.configurexml.TurnoutOperationManagerXml;
import jmri.configurexml.turnoutoperations.TurnoutOperationXml;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the abstract base and store functionality for configuring
 * TurnoutManagers, working with AbstractTurnoutManagers.
 * <p>
 * Typically, a subclass will just implement the load(Element turnouts) class,
 * relying on implementation here to load the individual turnouts. Note that
 * these are stored explicitly, so the resolution mechanism doesn't need to see
 * *Xml classes for each specific Turnout or AbstractTurnout subclass at store
 * time.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public abstract class AbstractTurnoutManagerConfigXML extends AbstractNamedBeanManagerConfigXML {

    public AbstractTurnoutManagerConfigXML() {
    }

    /**
     * Default implementation for storing the contents of a TurnoutManager and
     * associated TurnoutOperations.
     *
     * @param o Object to store, of type TurnoutManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element turnouts = new Element("turnouts");
        setStoreElementClass(turnouts);
        TurnoutManager tm = (TurnoutManager) o;
        if (tm != null) {
            TurnoutOperationManagerXml tomx = new TurnoutOperationManagerXml();
            Element opElem = tomx.store(InstanceManager.getDefault(TurnoutOperationManager.class));
            turnouts.addContent(opElem);
            SortedSet<Turnout> tList = tm.getNamedBeanSet();
            // don't return an element if there are no turnouts to include
            if (tList.isEmpty()) {
                return null;
            }
            String defaultclosed = tm.getDefaultClosedSpeed();
            String defaultthrown = tm.getDefaultThrownSpeed();
            turnouts.addContent(new Element("defaultclosedspeed").addContent(defaultclosed));
            turnouts.addContent(new Element("defaultthrownspeed").addContent(defaultthrown));
            for (Turnout t : tList) {
                // store the turnouts
                String tName = t.getSystemName();
                log.debug("system name is {}", tName);

                Element elem = new Element("turnout");
                elem.addContent(new Element("systemName").addContent(tName));
                log.debug("store Turnout {}", tName);

                storeCommon(t, elem);

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

                // include turnout inverted
                elem.setAttribute("inverted", t.getInverted() ? "true" : "false");

                if (t.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT)) {
                    // include turnout locked
                    elem.setAttribute("locked", t.getLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT) ? "true" : "false");
                    // include turnout lock mode
                    String lockOpr;
                    if (t.canLock(Turnout.CABLOCKOUT) && t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        lockOpr = "both";
                    } else if (t.canLock(Turnout.CABLOCKOUT)) {
                        lockOpr = "cab";
                    } else if (t.canLock(Turnout.PUSHBUTTONLOCKOUT)) {
                        lockOpr = "pushbutton";
                    } else {
                        lockOpr = "none";
                    }
                    elem.setAttribute("lockMode", lockOpr);
                    // include turnout decoder
                    elem.setAttribute("decoder", t.getDecoderName());
                }

                // include number of control bits, if different from one
                int iNum = t.getNumberOutputBits();
                if (iNum != 1) {
                    elem.setAttribute("numBits", "" + iNum);
                }

                // include turnout control type, if different from 0
                int iType = t.getControlType();
                if (iType != 0) {
                    elem.setAttribute("controlType", "" + iType);
                }

                // add operation stuff
                String opstr = null;
                TurnoutOperation op = t.getTurnoutOperation();
                if (t.getInhibitOperation()) {
                    opstr = "Off";
                } else if (op == null) {
                    opstr = "Default";
                } else if (op.isNonce()) {	// nonce operation appears as subelement
                    TurnoutOperationXml adapter = TurnoutOperationXml.getAdapter(op);
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
                if ((t.getDivergingSpeed() != null) && (!t.getDivergingSpeed().equals("")) && !t.getDivergingSpeed().contains("Global")) {
                    elem.addContent(new Element("divergingSpeed").addContent(t.getDivergingSpeed()));
                }
                if ((t.getStraightSpeed() != null) && (!t.getStraightSpeed().equals("")) && !t.getStraightSpeed().contains("Global")) {
                    elem.addContent(new Element("straightSpeed").addContent(t.getStraightSpeed()));
                }

                // add element
                turnouts.addContent(elem);
            }
        }
        return turnouts;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param turnouts The top-level element being created
     */
    abstract public void setStoreElementClass(Element turnouts);

    @Override
    public abstract boolean load(Element shared, Element perNode);

    /**
     * Utility method to load the individual Turnout objects. If there's no
     * additional info needed for a specific turnout type, invoke this with the
     * parent of the set of Turnout elements.
     *
     * @param shared Element containing the Turnout elements to load.
     * @param perNode Element containing per-node Turnout data.
     * @return true if succeeded
     */
    public boolean loadTurnouts(Element shared, Element perNode) {
        boolean result = true;
        List<Element> operationList = shared.getChildren("operations");
        if (operationList.size() > 1) {
            log.warn("unexpected extra elements found in turnout operations list");
            result = false;
        }
        if (operationList.size() > 0) {
            TurnoutOperationManagerXml tomx = new TurnoutOperationManagerXml();
            tomx.load(operationList.get(0), null);
        }
        List<Element> turnoutList = shared.getChildren("turnout");
        log.debug("Found {} turnouts", turnoutList.size());
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        tm.setDataListenerMute(true);

        try {
            if (shared.getChild("defaultclosedspeed") != null) {
                String closedSpeed = shared.getChild("defaultclosedspeed").getText();
                if (closedSpeed != null && !closedSpeed.equals("")) {
                    tm.setDefaultClosedSpeed(closedSpeed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }

        try {
            if (shared.getChild("defaultthrownspeed") != null) {
                String thrownSpeed = shared.getChild("defaultthrownspeed").getText();
                if (thrownSpeed != null && !thrownSpeed.equals("")) {
                    tm.setDefaultThrownSpeed(thrownSpeed);
                }
            }
        } catch (jmri.JmriException ex) {
            log.error(ex.toString());
        }

        for (Element elem : turnoutList) {
            String sysName = getSystemName(elem);
            if (sysName == null) {
                log.error("unexpected null in systemName {}", elem);
                result = false;
                break;
            }
            String userName = getUserName(elem);

            checkNameNormalization(sysName, userName, tm);

            log.debug("create turnout: ({})({})", sysName, (userName == null ? "<null>" : userName));
            Turnout t = tm.getBySystemName(sysName);
            if (t == null) {
                t = tm.newTurnout(sysName, userName);
                // nothing is logged in the console window as the newTurnoutFunction already does this.
            } else if (userName != null) {
                t.setUserName(userName);
            }

            // Load common parts
            loadCommon(t, elem);

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
                    log.error("Can not set feedback mode: '{}' for turnout: '{}' user name: '{}'",
                            a.getValue(), sysName, (userName == null ? "" : userName));
                    result = false;
                }
            }

            // check for turnout inverted
            t.setInverted(getAttributeBool(elem, "inverted", false));

            // check for turnout decoder
            a = elem.getAttribute("decoder");
            if (a != null) {
                t.setDecoderName(a.getValue());
            }

            // check for turnout lock mode
            a = elem.getAttribute("lockMode");
            if (a != null) {
                if (a.getValue().equals("both")) {
                    t.enableLockOperation(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
                }
                if (a.getValue().equals("cab")) {
                    t.enableLockOperation(Turnout.CABLOCKOUT, true);
                    t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, false);
                }
                if (a.getValue().equals("pushbutton")) {
                    t.enableLockOperation(Turnout.PUSHBUTTONLOCKOUT, true);
                    t.enableLockOperation(Turnout.CABLOCKOUT, false);
                }
            }

            // check for turnout locked
            a = elem.getAttribute("locked");
            if (a != null) {
                t.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, a.getValue().equals("true"));
            }

            // number of bits, if present - if not, defaults to 1
            a = elem.getAttribute("numBits");
            if (a == null) {
                t.setNumberOutputBits(1);
            } else {
                int iNum = Integer.parseInt(a.getValue());
                if ((iNum == 1) || (iNum == 2)) {
                    t.setNumberOutputBits(iNum);
                } else {
                    log.warn("illegal number of output bits for control of turnout {}", sysName);
                    t.setNumberOutputBits(1);
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
                    log.warn("illegal control type for control of turnout {}", sysName);
                    t.setControlType(0);
                    result = false;
                }
            }

            // operation stuff
            List<Element> myOpList = elem.getChildren("operation");
            if (myOpList.size() > 0) {
                if (myOpList.size() > 1) {
                    log.warn("unexpected extra elements found in turnout-specific operations");
                    result = false;
                }
                TurnoutOperation toper = TurnoutOperationXml.loadOperation(myOpList.get(0));
                t.setTurnoutOperation(toper);
            } else {
                a = elem.getAttribute("automate");
                if (a != null) {
                    String str = a.getValue();
                    if (str.equals("Off")) {
                        t.setInhibitOperation(true);
                    } else if (!str.equals("Default")) {
                        t.setInhibitOperation(false);
                        TurnoutOperation toper
                                = InstanceManager.getDefault(TurnoutOperationManager.class).getOperation(str);
                        t.setTurnoutOperation(toper);
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
                    if (speed != null && !speed.equals("") && !speed.contains("Global")) {
                        t.setDivergingSpeed(speed);
                    }
                }
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
            }

            try {
                t.setStraightSpeed("Global");
                if (elem.getChild("straightSpeed") != null) {
                    String speed = elem.getChild("straightSpeed").getText();
                    if (speed != null && !speed.equals("") && !speed.contains("Global")) {
                        t.setStraightSpeed(speed);
                    }
                }
            } catch (jmri.JmriException ex) {
                log.error(ex.toString());
            }
        }

        tm.setDataListenerMute(false);

        return result;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.turnoutManagerInstance().getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractTurnoutManagerConfigXML.class);

}

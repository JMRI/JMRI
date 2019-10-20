package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;

import jmri.InstanceManager;
import jmri.SignalGroup;
import jmri.SignalGroupManager;
import jmri.SignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for a DefaultSignalGroupManager objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 */
public class DefaultSignalGroupManagerXml
        extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultSignalGroupManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * DefaultSignalGroupManager.
     *
     * @param o Object to store, of type SignalGroup
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element groups = new Element("signalgroups");
        groups.setAttribute("class", this.getClass().getName());
        SignalGroupManager sgm = (SignalGroupManager) o;
        if (sgm != null) {
            SortedSet<SignalGroup> sgList = sgm.getNamedBeanSet();
            // don't return an element if there are no SignalGroups to include
            if (sgList.isEmpty()) {
                return null;
            }
            for (SignalGroup sg : sgList) {
                // store the signalgroups
                String sgName = sg.getSystemName();
                log.debug("SignalGroup system name is {}", sgName);  // NOI18N

                Element e = new Element("signalgroup");
                e.addContent(new Element("systemName").addContent(sgName));
                e.addContent(new Element("userName").addContent(sg.getUserName()));
                // storeCommon(sg, e); previously would store comment, now a separate element
                storeComment(sg, e);
                groups.addContent(e);
                for (int x = 0; x < sg.getNumSignalMastAspects(); x++) {
                    Element app = new Element("aspect").setAttribute("valid", sg.getSignalMastAspectByIndex(x));
                    e.addContent(app);
                }
                e.setAttribute("signalMast", sg.getSignalMastName());

                for (int x = 0; x < sg.getNumHeadItems(); x++) {
                    storeSignalHead(e, sg, x);
                }
            }
        }
        return groups;
    }

    private void storeSignalHead(Element element, SignalGroup _group, int x) {
        Element group = new Element("signalHead");
        String name = _group.getHeadItemNameByIndex(x);
        group.setAttribute("name", name);
        group.setAttribute("onAppearance", getSignalColour(_group.getHeadOnStateByIndex(x)));
        group.setAttribute("offAppearance", getSignalColour(_group.getHeadOffStateByIndex(x)));
        if (_group.getSensorTurnoutOperByIndex(x)) {
            group.setAttribute("sensorTurnoutLogic", "AND");
        } else {
            group.setAttribute("sensorTurnoutLogic", "OR");
        }

        for (int i = 0; i < _group.getNumHeadTurnoutsByIndex(x); i++) {
            storeTurnout(group, _group, x, i);
        }
        for (int i = 0; i < _group.getNumHeadSensorsByIndex(x); i++) {
            storeSensor(group, _group, x, i);
        }

        element.addContent(group);
    }

    private void storeTurnout(Element element, SignalGroup _group, int x, int turn) {
        Element turnout = new Element("turnout").setAttribute("name", _group.getTurnoutNameByIndex(x, turn));
        turnout.setAttribute("state", "" + _group.getTurnoutStateByIndex(x, turn));
        element.addContent(turnout);
    }

    private void storeSensor(Element element, SignalGroup _group, int x, int sensor) {
        Element Sensor = new Element("sensor").setAttribute("name", _group.getSensorNameByIndex(x, sensor));
        Sensor.setAttribute("state", "" + _group.getSensorStateByIndex(x, sensor));
        element.addContent(Sensor);
    }

    public void setStoreElementClass(Element signalGroup) {
        signalGroup.setAttribute("class", this.getClass().getName());
    }

    private String getSignalColour(int mAppearance) {
        switch (mAppearance) {
            case SignalHead.RED:
                return "RED";
            case SignalHead.FLASHRED:
                return "FLASHRED";
            case SignalHead.YELLOW:
                return "YELLOW";
            case SignalHead.FLASHYELLOW:
                return "FLASHYELLOW";
            case SignalHead.GREEN:
                return "GREEN";
            case SignalHead.FLASHGREEN:
                return "FLASHGREEN";
            case SignalHead.LUNAR:
                return "LUNAR";
            case SignalHead.FLASHLUNAR:
                return "FLASHLUNAR";
            case SignalHead.DARK:
                return "DARK";
            default:
                log.warn("Unexpected appearance: {}", mAppearance);
                // go dark
                return "DARK";
        }
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // loop over contained signalgroup elements
        List<Element> list = shared.getChildren("signalgroup");

        SignalGroupManager sgm = InstanceManager.getDefault(jmri.SignalGroupManager.class);

        for (Element e : list) {
            SignalGroup sg;
            String primary;
            String yesno;
            boolean inverse = false;
            int state = 0x00;

            String sys = getSystemName(e);

            sg = sgm.provideSignalGroup(sys, getUserName(e));

            //loadCommon(sg, e); // would store comment, now a separate element
            loadComment(sg, e);

            primary = e.getAttribute("signalMast").getValue();
            sg.setSignalMast(primary);

            List<Element> appList = e.getChildren("appearance"); // deprecated 4.7.2 for aspect
            for (Element app : appList) {
                String value = app.getAttribute("valid").getValue();
                sg.addSignalMastAspect(value);
            }
            List<Element> aspList = e.getChildren("aspect");
            for (Element asp : aspList) {
                String value = asp.getAttribute("valid").getValue();
                sg.addSignalMastAspect(value);
            }

            List<Element> signalHeadList = e.getChildren("signalHead");
            for (Element sh : signalHeadList) {
                String head = sh.getAttribute("name").getValue();
                SignalHead sigHead = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(head);
                sg.addSignalHead(sigHead);
                yesno = sh.getAttribute("sensorTurnoutLogic").getValue();
                inverse = false;
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("AND")) {
                        inverse = true;
                    // } else if (yesno.equals("OR")) {
                    //     inverse = false; // value already assigned as default
                    }
                }
                sg.setSensorTurnoutOper(sigHead, inverse);

                try {
                    sg.setHeadOnState(sigHead, getIntFromColour(sh.getAttribute("onAppearance").getValue()));
                } catch (NullPointerException ex) {  // considered normal if the attributes are not present
                }
                try {
                    sg.setHeadOffState(sigHead, getIntFromColour(sh.getAttribute("offAppearance").getValue()));
                } catch (NullPointerException ex) {  // considered normal if the attributes are not present
                }
                List<Element> signalTurnoutList = sh.getChildren("turnout");
                for (Element sgt : signalTurnoutList) {
                    String tName = sgt.getAttribute("name").getValue();
                    jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(tName);
                    state = 0;
                    try {
                        state = sgt.getAttribute("state").getIntValue();
                    } catch (org.jdom2.DataConversionException ex) {
                        log.warn("invalid state attribute value");
                    }
                    sg.setHeadAlignTurnout(sigHead, turnout, state);
                }
                List<Element> signalSensorList = sh.getChildren("sensor");
                for (Element sgs: signalSensorList) {
                    String sName = sgs.getAttribute("name").getValue();
                    jmri.Sensor sensor = jmri.InstanceManager.sensorManagerInstance().getSensor(sName);
                    state = 0;
                    try {
                        state = sgs.getAttribute("state").getIntValue();
                    } catch (org.jdom2.DataConversionException ex) {
                        log.warn("invalid style attribute value");
                    }
                    sg.setHeadAlignSensor(sigHead, sensor, state);
                }
            }
        }
        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private int getIntFromColour(String color) {
        switch (color) {
            case "RED":
                return SignalHead.RED;
            case "YELLOW":
                return SignalHead.YELLOW;
            case "GREEN":
                return SignalHead.GREEN;
            case "LUNAR":
                return SignalHead.LUNAR;
            case "DARK":
                return SignalHead.DARK;
            case "FLASHRED":
                return SignalHead.FLASHRED;
            case "FLASHYELLOW":
                return SignalHead.FLASHYELLOW;
            case "FLASHGREEN":
                return SignalHead.FLASHGREEN;
            case "FLASHLUNAR":
                return SignalHead.FLASHLUNAR;
            default:
                log.warn("Unexpected appearance: {}", color);
                return SignalHead.DARK;
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.SignalGroupManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalGroupManagerXml.class);

}

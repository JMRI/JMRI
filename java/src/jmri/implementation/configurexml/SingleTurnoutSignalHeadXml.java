package jmri.implementation.configurexml;

import java.util.List;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.SingleTurnoutSignalHead;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for SingleTurnoutSignalHead objects. Based Upon
 * DoubleTurnoutSignalHeadXML by Bob Jacobsen
 *
 * @author Kevin Dickerson: Copyright (c) 2010
 */
public class SingleTurnoutSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SingleTurnoutSignalHeadXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SingleTurnoutSignalHead.
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SingleTurnoutSignalHead p = (SingleTurnoutSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Element el = new Element("appearance");
        el.setAttribute("defines", "thrown");
        el.addContent(getSignalColour(p.getOnAppearance()));
        element.addContent(el);

        el = new Element("appearance");
        el.setAttribute("defines", "closed");
        el.addContent(getSignalColour(p.getOffAppearance()));
        element.addContent(el);

        el = new Element("turnoutname");
        el.setAttribute("defines", "aspect");
        el.addContent(p.getOutput().getName());
        element.addContent(el);
        //element.addContent(addTurnoutElement("aspect"));

        return element;
    }

    private String getSignalColour(int mAppearance) {
        switch (mAppearance) {
            case SignalHead.RED:
                return "red";
            case SignalHead.FLASHRED:
                return "flashred";
            case SignalHead.YELLOW:
                return "yellow";
            case SignalHead.FLASHYELLOW:
                return "flashyellow";
            case SignalHead.GREEN:
                return "green";
            case SignalHead.FLASHGREEN:
                return "flashgreen";
            case SignalHead.LUNAR:
                return "lunar";
            case SignalHead.FLASHLUNAR:
                return "flashlunar";
            case SignalHead.DARK:
                return "dark";
            default:
                log.warn("Unexpected appearance: {}", mAppearance);
                // go dark
                return "dark";
        }
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        List<Element> l = shared.getChildren("turnoutname");
        if (l.size() == 0) {
            l = shared.getChildren("turnout");
        }
        NamedBeanHandle<Turnout> lit = loadTurnout(l.get(0));

        int off = loadAppearance(shared.getChildren("appearance"), "closed");
        int on = loadAppearance(shared.getChildren("appearance"), "thrown");

        // put it together
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        SignalHead h;
        if (uname == null) {
            h = new SingleTurnoutSignalHead(sys, lit, on, off);
        } else {
            h = new SingleTurnoutSignalHead(sys, uname, lit, on, off);
        }

        loadCommon(h, shared);

        SignalHead existingBean =
                InstanceManager.getDefault(jmri.SignalHeadManager.class)
                        .getBeanBySystemName(sys);

        if ((existingBean != null) && (existingBean != h)) {
            log.error("systemName is already registered: {}", sys);
        } else {
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        }

        return true;
    }

    private int loadAppearance(List<Element> l, String state) {
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).getAttribute("defines").getValue().equals(state)) {
                return getIntFromColour(l.get(i).getText());
            }
        }
        return 0x00;
    }

    /**
     * Load a turnout.
     * Needs to handle two types of element: turnoutname is new form, turnout is
     * old form.
     *
     * @param o Object read from storage, of type Turnout
     * @return Turnout bean
     */
    NamedBeanHandle<Turnout> loadTurnout(Object o) {
        Element e = (Element) o;

        String name = e.getText();
        try {
            Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
            return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to provide Turnout \"{}\" in loadTurnout", name);
            return null;
        }
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private int getIntFromColour(String colour) {
        switch (colour.toLowerCase()) {
            case "red":
                return SignalHead.RED;
            case "yellow":
                return SignalHead.YELLOW;
            case "green":
                return SignalHead.GREEN;
            case "lunar":
                return SignalHead.LUNAR;
            case "dark":
                return SignalHead.DARK;
            case "flashred":
                return SignalHead.FLASHRED;
            case "flashyellow":
                return SignalHead.FLASHYELLOW;
            case "flashgreen":
                return SignalHead.FLASHGREEN;
            case "flashlunar":
                return SignalHead.FLASHLUNAR;
            default:
                log.warn("Unexpected appearance: {}", colour);
                break;
        }
        return SignalHead.DARK;

    }

    private final static Logger log = LoggerFactory.getLogger(SingleTurnoutSignalHeadXml.class);
}

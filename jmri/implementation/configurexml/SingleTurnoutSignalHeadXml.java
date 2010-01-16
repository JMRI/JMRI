package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.SingleTurnoutSignalHead;
import jmri.Turnout;
import jmri.util.NamedBeanHandle;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for SingleTurnoutSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008
 * @version $Revision: 1.2 $
 */
public class SingleTurnoutSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public SingleTurnoutSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * SingleTurnoutSignalHead
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        SingleTurnoutSignalHead p = (SingleTurnoutSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());

        storeCommon(p, element);
        element.setAttribute("thrownAppearance", getSignalColour(p.getOnAppearance()));
        element.setAttribute("closedAppearance", getSignalColour(p.getOffAppearance()));
        
        Element el = new Element("turnoutname");
        el.setAttribute("defines", "aspect");
        el.addContent(p.getOutput().getName());
        element.addContent(el);
        //element.addContent(addTurnoutElement("aspect"));

        return element;
    }
    @SuppressWarnings("fallthrough")
    private String getSignalColour(int mAppearance){
        switch(mAppearance){
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
        	default:
                    log.warn("Unexpected appearance: "+mAppearance);
                // go dark
        	case SignalHead.DARK:
                    return "DARK";
        }
    }


    /**
     * Create a SingleTurnoutSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnoutname");
        if (l.size() == 0) l = element.getChildren("turnout");
        NamedBeanHandle<Turnout> lit = loadTurnout(l.get(0));
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        int on = 0x00;
        int off = 0x00;
        on = getIntFromColour(element.getAttribute("closedAppearance").getValue());
        
        off = getIntFromColour(element.getAttribute("thrownAppearance").getValue());
        SignalHead h;
        if (a == null)
            h = new SingleTurnoutSignalHead(sys, lit, on, off);
        else
            h = new SingleTurnoutSignalHead(sys, a.getValue(), lit, on, off);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    /**
     * Needs to handle two types of element:
     *    turnoutname is new form
     *    turnout is old form
     */
    NamedBeanHandle<Turnout> loadTurnout(Object o) {
        Element e = (Element)o;
        
        String name = e.getText();
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout(name);
        return new NamedBeanHandle<Turnout>(name, t);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    @SuppressWarnings("fallthrough")
    private int getIntFromColour(String colour){
        if (colour.equals("RED")) return SignalHead.RED;
        else if (colour.equals("YELLOW")) return SignalHead.YELLOW;
        else if (colour.equals("GREEN")) return SignalHead.GREEN;
        else if (colour.equals("LUNAR")) return SignalHead.LUNAR;
        else if (colour.equals("DARK")) return SignalHead.DARK;
        else if (colour.equals("FLASHRED")) return SignalHead.FLASHRED;
        else if (colour.equals("FLASHYELLOW")) return SignalHead.FLASHYELLOW;
        else if (colour.equals("FLASHGREEN")) return SignalHead.FLASHGREEN;
        else if (colour.equals("FLASHLUNAR")) return SignalHead.FLASHLUNAR;
        log.warn("Unexpected appearance: "+colour);
        return SignalHead.DARK;
    
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleTurnoutSignalHeadXml.class.getName());
}
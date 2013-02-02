package jmri.implementation.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.SingleTurnoutSignalHead;
import jmri.Turnout;
import jmri.NamedBeanHandle;

import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for SingleTurnoutSignalHead objects.
 * Based Upon DoubleTurnoutSignalHeadXML by Bob Jacobsen
 * @author Kevin Dickerson: Copyright (c) 2010
 * @version $Revision$
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
    private String getSignalColour(int mAppearance){
        switch(mAppearance){
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
                    log.warn("Unexpected appearance: "+mAppearance);
                // go dark
                    return "dark";
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
        
        int off = loadAppearance(element.getChildren("appearance"), "closed");
        int on = loadAppearance(element.getChildren("appearance"), "thrown");

        // put it together
        String sys = getSystemName(element);
        String uname = getUserName(element);

        SignalHead h;
        if (uname == null)
            h = new SingleTurnoutSignalHead(sys, lit, on, off);
        else
            h = new SingleTurnoutSignalHead(sys, uname, lit, on, off);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }
    
    private int loadAppearance(List<Element> l, String state){
        for (int i = 0; i <l.size(); i++){
            if(l.get(i).getAttribute("defines").getValue().equals(state))
                return getIntFromColour(l.get(i).getText());
        }
        return 0x00;
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
        return jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, t);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }
    
    private int getIntFromColour(String colour){
        String c = colour.toLowerCase();
        if (c.equals("red")) return SignalHead.RED;
        else if (c.equals("yellow")) return SignalHead.YELLOW;
        else if (c.equals("green")) return SignalHead.GREEN;
        else if (c.equals("lunar")) return SignalHead.LUNAR;
        else if (c.equals("dark")) return SignalHead.DARK;
        else if (c.equals("flashred")) return SignalHead.FLASHRED;
        else if (c.equals("flashyellow")) return SignalHead.FLASHYELLOW;
        else if (c.equals("flashgreen")) return SignalHead.FLASHGREEN;
        else if (c.equals("flashlunar")) return SignalHead.FLASHLUNAR;
        else log.warn("Unexpected appearance: "+colour);
        return SignalHead.DARK;
    
    }

    static Logger log = Logger.getLogger(SingleTurnoutSignalHeadXml.class.getName());
}

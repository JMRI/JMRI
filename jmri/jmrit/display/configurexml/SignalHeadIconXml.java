package jmri.jmrit.display.configurexml;

import org.jdom.Element;

import jmri.InstanceManager;
import jmri.jmrit.display.PositionableLabel;
import jmri.configurexml.XmlAdapter;

import jmri.jmrit.display.SignalHeadIcon;
import jmri.jmrit.display.PanelEditor;

import javax.swing.*;

/**
 * Handle configuration for display.SignalHeadIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class SignalHeadIconXml implements XmlAdapter {

    public SignalHeadIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalHeadIcon
     * @param o Object to store, of type SignalHeadIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element element = new Element("signalheadicon");

        // include contents
        SignalHeadIcon p = (SignalHeadIcon)o;
        element.addAttribute("aspectgenerator", p.getAspectGenerator().getSEName());
        element.addAttribute("head", ""+p.getHeadNumber());
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("red", p.getRedIcon().getName());
        element.addAttribute("yellow", p.getYellowIcon().getName());
        element.addAttribute("flashyellow", p.getFlashYellowIcon().getName());
        element.addAttribute("green", p.getGreenIcon().getName());

        element.addAttribute("class", "jmri.jmrit.display.configurexml.SignalHeadIconXml");

        return element;
    }


    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;
        String name;

        SignalHeadIcon l = new SignalHeadIcon();
        l.setSignalHead(element.getAttribute("aspectgenerator").getValue(),
                    Integer.parseInt(element.getAttribute("head").getValue())
                    );

        name = element.getAttribute("red").getValue();
        l.setRedIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("yellow").getValue();
        l.setYellowIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("flashyellow").getValue();
        l.setFlashYellowIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("green").getValue();
        l.setGreenIcon(p.catalog.getIconByName(name));

        l.displayState(l.headState());

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);

        // no putSignalHead exists, so code is here
        p.target.add(l, PanelEditor.SIGNALS);
        p.contents.add(l);
        p.target.revalidate();

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadIconXml.class.getName());

}
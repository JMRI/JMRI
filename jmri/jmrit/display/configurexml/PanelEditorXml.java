package jmri.jmrit.display.configurexml;

import org.jdom.Element;
import com.sun.java.util.collections.List;

import jmri.InstanceManager;
import jmri.jmrit.display.PanelEditor;
import jmri.configurexml.XmlAdapter;
import javax.swing.*;

/**
 * Handle configuration for display.PanelEditor panes.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.6 $
 */
public class PanelEditorXml implements XmlAdapter {

    public PanelEditorXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PanelEditor
     * @param o Object to store, of type PanelEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PanelEditor p = (PanelEditor)o;
        Element panel = new Element("paneleditor");

        panel.addAttribute("class", "jmri.jmrit.display.configurexml.PanelEditorXml");
        panel.addAttribute("name", ""+p.getFrame().getTitle());
        panel.addAttribute("x", ""+p.getFrame().getX());
        panel.addAttribute("y", ""+p.getFrame().getY());
        panel.addAttribute("height", ""+p.getFrame().getHeight());
        panel.addAttribute("width", ""+p.getFrame().getWidth());

        // include contents

        if (log.isDebugEnabled()) log.debug("N elements: "+p.contents.size());
        for (int i=0; i<p.contents.size(); i++) {
            Object sub = p.contents.get(i);
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e!=null) panel.addContent(e);
            } catch (Exception e) {
                log.error("Error storing panel element: "+e);
                e.printStackTrace();
            }
        }

        return panel;
    }


    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a PanelEditor object, then
     * register and fill it, then pop it in a JFrame
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            height = element.getAttribute("height").getIntValue();
            width = element.getAttribute("width").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
        }
        // find the name
        String name = "Panel";
        if (element.getAttribute("name")!=null)
            name = element.getAttribute("name").getValue();
        // create the objects
        JFrame targetFrame = new JFrame(name);
        JLayeredPane targetPanel = new JLayeredPane();
        targetFrame.setSize(width, height);
        targetFrame.setLocation(x,y);

        targetFrame.getContentPane().add(targetPanel);
        targetPanel.setLayout(null);
        PanelEditor panel = new PanelEditor(name+" Editor");
        panel.setFrame(targetFrame);
        panel.setTarget(targetPanel);

        // load the contents
        List items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = (Element)items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via "+adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                // and do it
                adapter.load(item, panel);
            } catch (Exception e) {
                log.error("Exception while loading "+item.getName()+":"+e);
            }
        }

        // display the results
        targetFrame.show();
        panel.pack();
        panel.show();

        // register the result for later configuration
        InstanceManager.configureManagerInstance().register(panel);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}
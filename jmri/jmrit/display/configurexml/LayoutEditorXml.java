package jmri.jmrit.display.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LayoutBlock;

import java.awt.Dimension;
import java.awt.Point;

import java.util.List;
import org.jdom.*;

/**
 * Handle configuration for LayoutEditor panes.
 * 
 * Based in part on PanelEditorXml.java
 *
 * @author Dave Duchamp    Copyright (c) 2007
 * @version $Revision: 1.2 $
 */
public class LayoutEditorXml implements XmlAdapter {

    public LayoutEditorXml() {}

    /**
     * Default implementation for storing the contents of a
     * LayoutEditor
     * @param o Object to store, of type LayoutEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        LayoutEditor p = (LayoutEditor)o;
        Element panel = new Element("LayoutEditor");

        panel.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutEditorXml");
        panel.setAttribute("name", p.getLayoutName());
        panel.setAttribute("x", ""+p.getUpperLeftX());
        panel.setAttribute("y", ""+p.getUpperLeftY());
        panel.setAttribute("height", ""+p.getLayoutHeight());
        panel.setAttribute("width", ""+p.getLayoutWidth());
        panel.setAttribute("editable", ""+(p.isEditable()?"yes":"no"));
        panel.setAttribute("positionable", ""+(p.isPositionable()?"yes":"no"));
        panel.setAttribute("controlling", ""+(p.isControlling()?"yes":"no"));
		panel.setAttribute("showhelpbar", ""+(p.getShowHelpBar()?"yes":"no"));
        panel.setAttribute("mainlinetrackwidth", ""+p.getMainlineTrackWidth());
		panel.setAttribute("xscale", Float.toString((float)p.getXScale()));
		panel.setAttribute("yscale", Float.toString((float)p.getYScale()));
        panel.setAttribute("sidetrackwidth", ""+p.getSideTrackWidth());
		panel.setAttribute("defaulttrackcolor",p.getDefaultTrackColor());
		p.resetDirty();

        // include contents (Icons and Labels)
		int num = p.contents.size();
		if (num>0) {
			for (int i=0; i<num; i++) {
				Object sub = p.contents.get(i);
				if (sub!=null) {
					try {
						Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
						if (e!=null) panel.addContent(e);
					} catch (Exception e) {
						log.error("Error storing panel contents element: "+e); 
					}
				}
				else {
					log.warn("Null entry found when storing panel contents.");
				}
            }
        }
		
		// include LayoutTurnouts
		num = p.turnoutList.size();
        if (log.isDebugEnabled()) log.debug("N layoutturnout elements: "+num);
		if (num>0) {
			for (int i=0; i<num; i++) {
				Object sub = p.turnoutList.get(i);
				try {
					Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
					if (e!=null) panel.addContent(e);
				} catch (Exception e) {
					log.error("Error storing panel layoutturnout element: "+e); 
				}
			}
        }		
		
		// include TrackSegments
		num = p.trackList.size();
        if (log.isDebugEnabled()) log.debug("N tracksegment elements: "+num);
		if (num>0) {
			for (int i=0; i<num; i++) {
				Object sub = p.trackList.get(i);
				try {
					Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
					if (e!=null) panel.addContent(e);
				} catch (Exception e) {
					log.error("Error storing panel tracksegment element: "+e); 
				}
			}
        }		
		// include PositionablePoints
		num = p.pointList.size();
        if (log.isDebugEnabled()) log.debug("N positionablepoint elements: "+num);
		if (num>0) {
			for (int i=0; i<num; i++) {
				Object sub = p.pointList.get(i);
				try {
					Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
					if (e!=null) panel.addContent(e);
				} catch (Exception e) {
					log.error("Error storing panel positionalpoint element: "+e); 
				}
			}
        }				
		// include LevelXings
		num = p.xingList.size();
        if (log.isDebugEnabled()) log.debug("N levelxing elements: "+num);
		if (num>0) {
			for (int i=0; i<num; i++) {
				Object sub = p.xingList.get(i);
				try {
					Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
					if (e!=null) panel.addContent(e);
				} catch (Exception e) {
					log.error("Error storing panel levelxing element: "+e); 
				}
			}
        }		

        return panel;
    }


    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a LayoutEditor object, then
     * register and fill it, then pop it in a JFrame
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
		int sidetrackwidth = 3;
		int mainlinetrackwidth = 3;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            height = element.getAttribute("height").getIntValue();
            width = element.getAttribute("width").getIntValue();
			mainlinetrackwidth = element.getAttribute("mainlinetrackwidth").getIntValue();
			sidetrackwidth = element.getAttribute("sidetrackwidth").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert LayoutEditor's attribute");
        }
		double xScale = 1.0;
		double yScale = 1.0;
		Attribute a = element.getAttribute("xscale");
		if (a!=null) {
			try {
				xScale = (double)(Float.parseFloat(a.getValue()));
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
		a = element.getAttribute("yscale");
		if (a!=null) {
			try {
				yScale = (double)(Float.parseFloat(a.getValue()));
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
        // find the name and default track color
        String name = "";
        if (element.getAttribute("name")!=null)
            name = element.getAttribute("name").getValue();
        String defaultColor = "";
        if (element.getAttribute("defaulttrackcolor")!=null)
            defaultColor = element.getAttribute("defaulttrackcolor").getValue();
			
        // create the objects
        LayoutEditor panel = new LayoutEditor(name);
		panel.setLayoutName(name);
		panel.setMainlineTrackWidth(mainlinetrackwidth);
		panel.setSideTrackWidth(sidetrackwidth);
		panel.setDefaultTrackColor(defaultColor);
		panel.setXScale(xScale);
		panel.setYScale(yScale);

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
                e.printStackTrace();
            }
        }

        // set contents state
        boolean edValue = true;
        if ((a = element.getAttribute("editable"))!=null && a.getValue().equals("no"))
            edValue = false;

        boolean value = true;
        if ((a = element.getAttribute("positionable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllPositionable(value);

        value = true;
        if ((a = element.getAttribute("controlling"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllControlling(value);

       boolean hbValue = true;
        if ((a = element.getAttribute("showhelpbar"))!=null && a.getValue().equals("no"))
            hbValue = false;

		// set default track color
		if ((a = element.getAttribute("defaultTrackColor"))!=null) {
			panel.setDefaultTrackColor(a.getValue());
		}
		
		// final initialization of objects
		panel.setConnections();
			
        // display the results
        panel.setShowHelpBar(hbValue);
		panel.setLayoutDimensions(width,height,x,y);
        panel.pack();
        panel.show();
        panel.setVisible(true);    // always show the panel
        panel.setEditable(edValue);
		panel.resetDirty();

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(panel);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutEditorXml.class.getName());

}
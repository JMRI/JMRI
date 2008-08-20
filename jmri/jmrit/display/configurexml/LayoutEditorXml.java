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
 * @version $Revision: 1.7 $
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
		// From this version onwards separate sizes for window and panel are stored
        panel.setAttribute("windowheight", ""+p.getWindowHeight());
        panel.setAttribute("windowwidth", ""+p.getWindowWidth());
        panel.setAttribute("panelheight", ""+p.getLayoutHeight());
        panel.setAttribute("panelwidth", ""+p.getLayoutWidth());
        panel.setAttribute("editable", ""+(p.isEditable()?"yes":"no"));
        panel.setAttribute("positionable", ""+(p.isPositionable()?"yes":"no"));
        panel.setAttribute("controlling", ""+(p.isControlling()?"yes":"no"));
        panel.setAttribute("animating", ""+(p.isAnimating()?"yes":"no"));
		panel.setAttribute("showhelpbar", ""+(p.getShowHelpBar()?"yes":"no"));
		panel.setAttribute("drawgrid", ""+(p.getDrawGrid()?"yes":"no"));
		panel.setAttribute("snaponadd", ""+(p.getSnapOnAdd()?"yes":"no"));
		panel.setAttribute("snaponmove", ""+(p.getSnapOnMove()?"yes":"no"));
		panel.setAttribute("antialiasing", ""+(p.getAntialiasingOn()?"yes":"no"));
		panel.setAttribute("mainlinetrackwidth", ""+p.getMainlineTrackWidth());
		panel.setAttribute("xscale", Float.toString((float)p.getXScale()));
		panel.setAttribute("yscale", Float.toString((float)p.getYScale()));
        panel.setAttribute("sidetrackwidth", ""+p.getSideTrackWidth());
		panel.setAttribute("defaulttrackcolor",p.getDefaultTrackColor());
		panel.setAttribute("turnoutbx", Float.toString((float)p.getTurnoutBX()));
		panel.setAttribute("turnoutcx", Float.toString((float)p.getTurnoutCX()));
		panel.setAttribute("turnoutwid", Float.toString((float)p.getTurnoutWid()));
		panel.setAttribute("xoverlong", Float.toString((float)p.getXOverLong()));
		panel.setAttribute("xoverhwid", Float.toString((float)p.getXOverHWid()));
		panel.setAttribute("xovershort", Float.toString((float)p.getXOverShort()));
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
		// include LayoutTurntables
		num = p.turntableList.size();
        if (log.isDebugEnabled()) log.debug("N turntable elements: "+num);
		if (num>0) {
			for (int i=0; i<num; i++) {
				Object sub = p.turntableList.get(i);
				try {
					Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
					if (e!=null) panel.addContent(e);
				} catch (Exception e) {
					log.error("Error storing panel turntable element: "+e); 
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
		Attribute a;
        // find coordinates
        int x = 0;
        int y = 0;
		// From this version onwards separate sizes for window and panel are used
        int windowHeight = 400;
        int windowWidth = 300;
        int panelHeight = 340;
        int panelWidth = 280;
		int sidetrackwidth = 3;
		int mainlinetrackwidth = 3;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
			// For compatibility with previous versions, try and see if height and width tags are contained in the file
			if((a = element.getAttribute("height")) != null) {
				windowHeight = a.getIntValue();
				panelHeight = windowHeight - 60;
			}
			if((a = element.getAttribute("width")) != null) {
				windowWidth = a.getIntValue();
				panelWidth = windowWidth - 18;
			}
			// For files created by the new version, retrieve window and panel sizes
			if((a = element.getAttribute("windowheight")) != null) {
				windowHeight = a.getIntValue();
			}
			if((a = element.getAttribute("windowwidth")) != null) {
				windowWidth = a.getIntValue();
			}
			if((a = element.getAttribute("panelheight")) != null) {
				panelHeight = a.getIntValue();
			}
			if((a = element.getAttribute("panelwidth")) != null) {
				panelWidth = a.getIntValue();
			}
			mainlinetrackwidth = element.getAttribute("mainlinetrackwidth").getIntValue();
			sidetrackwidth = element.getAttribute("sidetrackwidth").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert LayoutEditor's attribute");
        }
		double xScale = 1.0;
		double yScale = 1.0;
		a = element.getAttribute("xscale");
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
		// turnout size parameters
		double sz = 20.0;
		a = element.getAttribute("turnoutbx");
		if (a!=null) {
			try {
				sz = (double)(Float.parseFloat(a.getValue()));
				panel.setTurnoutBX(sz);
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
		a = element.getAttribute("turnoutcx");
		if (a!=null) {
			try {
				sz = (double)(Float.parseFloat(a.getValue()));
				panel.setTurnoutCX(sz);
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
		a = element.getAttribute("turnoutwid");
		if (a!=null) {
			try {
				sz = (double)(Float.parseFloat(a.getValue()));
				panel.setTurnoutWid(sz);
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
		a = element.getAttribute("xoverlong");
		if (a!=null) {
			try {
				sz = (double)(Float.parseFloat(a.getValue()));
				panel.setXOverLong(sz);
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
		a = element.getAttribute("xoverhwid");
		if (a!=null) {
			try {
				sz = (double)(Float.parseFloat(a.getValue()));
				panel.setXOverHWid(sz);
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
		a = element.getAttribute("xovershort");
		if (a!=null) {
			try {
				sz = (double)(Float.parseFloat(a.getValue()));
				panel.setXOverShort(sz);
			} catch (Exception e) {
				log.error("failed to convert to float - "+a.getValue());
			}
		}
 
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

        value = true;
        if ((a = element.getAttribute("animating"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setTurnoutAnimation(value);

       boolean hbValue = true;
        if ((a = element.getAttribute("showhelpbar"))!=null && a.getValue().equals("no"))
            hbValue = false;

       boolean dgValue = false;
        if ((a = element.getAttribute("drawgrid"))!=null && a.getValue().equals("yes"))
            dgValue = true;

       boolean sgaValue = false;
        if ((a = element.getAttribute("snaponadd"))!=null && a.getValue().equals("yes"))
            sgaValue = true;

       boolean sgmValue = false;
        if ((a = element.getAttribute("snaponmove"))!=null && a.getValue().equals("yes"))
            sgmValue = true;

       boolean aaValue = false;
        if ((a = element.getAttribute("antialiasing"))!=null && a.getValue().equals("yes"))
            aaValue = true;

		// set default track color
		if ((a = element.getAttribute("defaultTrackColor"))!=null) {
			panel.setDefaultTrackColor(a.getValue());
		}
		
		// final initialization of objects
		panel.setConnections();
			
        // display the results
        panel.setShowHelpBar(hbValue);
		panel.setDrawGrid(dgValue);
		panel.setSnapOnAdd(sgaValue);
		panel.setSnapOnMove(sgmValue);
		panel.setAntialiasingOn(aaValue);
        panel.pack();
		panel.setLayoutDimensions(windowWidth, windowHeight, x, y, panelWidth, panelHeight);
        panel.show();
        panel.setVisible(true);    // always show the panel
        panel.setEditable(edValue);
		panel.resetDirty();

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(panel);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutEditorXml.class.getName());

}
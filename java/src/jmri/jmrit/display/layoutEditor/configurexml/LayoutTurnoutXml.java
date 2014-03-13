// jmri.jmrit.display.layoutEditor.configurexml.LayoutTurnoutXml.java

package jmri.jmrit.display.layoutEditor.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import jmri.Turnout;
import org.jdom.Attribute;
import org.jdom.Element;
import java.awt.geom.*;

/**
 * This module handles configuration for display.LayoutTurnout objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision$
 */
public class LayoutTurnoutXml extends AbstractXmlAdapter {

    public LayoutTurnoutXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutTurnout
     * @param o Object to store, of type LayoutTurnout
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LayoutTurnout p = (LayoutTurnout)o;

        Element element = new Element("layoutturnout");

        // include attributes
        element.setAttribute("ident", p.getName());
		if (p.getTurnoutName().length()>0) {
			element.setAttribute("turnoutname", p.getTurnoutName());
		}
        if (p.getSecondTurnoutName().length()>0) {
			element.setAttribute("secondturnoutname", p.getSecondTurnoutName());
		}
		if (p.getBlockName().length()>0) {
			element.setAttribute("blockname", p.getBlockName());
		}
		if (p.getBlockBName().length()>0) {
			element.setAttribute("blockbname", p.getBlockBName());
		}
		if (p.getBlockCName().length()>0) {
			element.setAttribute("blockcname", p.getBlockCName());
		}
		if (p.getBlockDName().length()>0) {
			element.setAttribute("blockdname", p.getBlockDName());
		}
		element.setAttribute("type", ""+p.getTurnoutType());
        if(p.getHidden())
            element.setAttribute("hidden", ""+(p.getHidden()?"yes":"no"));
		if (p.getConnectA()!=null) {
			element.setAttribute("connectaname", ((TrackSegment)p.getConnectA()).getID());
		}
		if (p.getConnectB()!=null) {
			element.setAttribute("connectbname", ((TrackSegment)p.getConnectB()).getID());
		}
		if (p.getConnectC()!=null) {
			element.setAttribute("connectcname", ((TrackSegment)p.getConnectC()).getID());
		}
		if (p.getConnectD()!=null) {
			element.setAttribute("connectdname", ((TrackSegment)p.getConnectD()).getID());
		}
		if (p.getSignalA1Name().length()>0) {
			element.setAttribute("signala1name", p.getSignalA1Name());
		}
		if (p.getSignalA2Name().length()>0) {
			element.setAttribute("signala2name", p.getSignalA2Name());
		}
		if (p.getSignalA3Name().length()>0) {
			element.setAttribute("signala3name", p.getSignalA3Name());
		}
		if (p.getSignalB1Name().length()>0) {
			element.setAttribute("signalb1name", p.getSignalB1Name());
		}
		if (p.getSignalB2Name().length()>0) {
			element.setAttribute("signalb2name", p.getSignalB2Name());
		}
		if (p.getSignalC1Name().length()>0) {
			element.setAttribute("signalc1name", p.getSignalC1Name());
		}
		if (p.getSignalC2Name().length()>0) {
			element.setAttribute("signalc2name", p.getSignalC2Name());
		}
		if (p.getSignalD1Name().length()>0) {
			element.setAttribute("signald1name", p.getSignalD1Name());
		}
		if (p.getSignalD2Name().length()>0) {
			element.setAttribute("signald2name", p.getSignalD2Name());
		}
		if (p.getLinkedTurnoutName().length()>0) {
			element.setAttribute("linkedturnoutname", p.getLinkedTurnoutName());
			element.setAttribute("linktype", ""+p.getLinkType());
		}
        
        if(p.getSignalAMastName().length()>0){
            element.addContent(new Element("signalAMast").addContent(p.getSignalAMastName()));
        }
        
        if(p.getSignalBMastName().length()>0){
            element.addContent(new Element("signalBMast").addContent(p.getSignalBMastName()));
        }
        if(p.getSignalCMastName().length()>0){
            element.addContent(new Element("signalCMast").addContent(p.getSignalCMastName()));
        }
        if(p.getSignalDMastName().length()>0){
            element.addContent(new Element("signalDMast").addContent(p.getSignalDMastName()));

        }

        if(p.getSensorAName().length()>0){
            element.addContent(new Element("sensorA").addContent(p.getSensorAName()));
        }
        
        if(p.getSensorBName().length()>0){
            element.addContent(new Element("sensorB").addContent(p.getSensorBName()));
        }
        if(p.getSensorCName().length()>0){
            element.addContent(new Element("sensorC").addContent(p.getSensorCName()));
        }
        if(p.getSensorDName().length()>0){
            element.addContent(new Element("sensorD").addContent(p.getSensorDName()));
        }

		element.setAttribute("continuing", ""+p.getContinuingSense());		
        element.setAttribute("disabled", ""+(p.isDisabled()?"yes":"no"));
        element.setAttribute("disableWhenOccupied", ""+(p.isDisabledWhenOccupied()?"yes":"no"));
		Point2D coords = p.getCoordsCenter();
		element.setAttribute("xcen", ""+coords.getX());
		element.setAttribute("ycen", ""+coords.getY());
        coords = p.getCoordsA();
		element.setAttribute("xa", ""+coords.getX());
		element.setAttribute("ya", ""+coords.getY());		
		coords = p.getCoordsB();
		element.setAttribute("xb", ""+coords.getX());
		element.setAttribute("yb", ""+coords.getY());		
		coords = p.getCoordsC();
		element.setAttribute("xc", ""+coords.getX());
		element.setAttribute("yc", ""+coords.getY());
		coords = p.getCoordsD();
		element.setAttribute("xd", ""+coords.getX());
		element.setAttribute("yd", ""+coords.getY());
        element.setAttribute("ver", ""+p.getVersion());
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutTurnoutXml");
        return element;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the levelxing element, then
     * all the other data
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
		
		// get center point
        String name = element.getAttribute("ident").getValue();
		double x = 0.0;
		double y = 0.0;
		int tType = LayoutTurnout.RH_TURNOUT;
		try {
			x = element.getAttribute("xcen").getFloatValue();
			y = element.getAttribute("ycen").getFloatValue();
			tType = element.getAttribute("type").getIntValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout attribute");
        }
        
        int version = 1;
        try {
			version = element.getAttribute("ver").getIntValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout b coords attribute");
        } catch (java.lang.NullPointerException e){
            //can be ignored as panel file may not support method
        }
		
		// create the new LayoutTurnout
        LayoutTurnout l = new LayoutTurnout(name,tType,
							new Point2D.Double(x,y),0.0,1.0,1.0,p, version);
		
		// get remaining attributes
		Attribute a = element.getAttribute("blockname");
		if (a != null) {
			l.tBlockName = a.getValue();
		}
		a = element.getAttribute("blockbname");
		if (a != null) {
			l.tBlockBName = a.getValue();
		}		
		a = element.getAttribute("blockcname");
		if (a != null) {
			l.tBlockCName = a.getValue();
		}		
		a = element.getAttribute("blockdname");
		if (a != null) {
			l.tBlockDName = a.getValue();
		}		
		a = element.getAttribute("turnoutname");
		if (a != null) {
			l.tTurnoutName = a.getValue();
		}
        a = element.getAttribute("secondturnoutname");
		if (a != null) {
			l.tSecondTurnoutName = a.getValue();
		}
		a = element.getAttribute("connectaname");
		if (a != null) {
			l.connectAName = a.getValue();
		}
		a = element.getAttribute("connectbname");
		if (a != null) {
			l.connectBName = a.getValue();
		}
		a = element.getAttribute("connectcname");
		if (a != null) {
			l.connectCName = a.getValue();
		}
		a = element.getAttribute("connectdname");
		if (a != null) {
			l.connectDName = a.getValue();
		}
		a = element.getAttribute("signala1name");
		if (a != null) {
			l.signalA1Name = a.getValue();
		}		
		a = element.getAttribute("signala2name");
		if (a != null) {
			l.signalA2Name = a.getValue();
		}		
		a = element.getAttribute("signala3name");
		if (a != null) {
			l.signalA3Name = a.getValue();
		}		
		a = element.getAttribute("signalb1name");
		if (a != null) {
			l.signalB1Name = a.getValue();
		}		
		a = element.getAttribute("signalb2name");
		if (a != null) {
			l.signalB2Name = a.getValue();
		}		
		a = element.getAttribute("signalc1name");
		if (a != null) {
			l.signalC1Name = a.getValue();
		}		
		a = element.getAttribute("signalc2name");
		if (a != null) {
			l.signalC2Name = a.getValue();
		}		
		a = element.getAttribute("signald1name");
		if (a != null) {
			l.signalD1Name = a.getValue();
		}		
		a = element.getAttribute("signald2name");
		if (a != null) {
			l.signalD2Name = a.getValue();
		}		
		a = element.getAttribute("linkedturnoutname");
		if (a != null) {
			l.linkedTurnoutName = a.getValue();
			try {
				l.linkType = element.getAttribute("linktype").getIntValue();
			} catch (org.jdom.DataConversionException e) {
				log.error("failed to convert linked layout turnout type");
			}			
		}		
		a = element.getAttribute("continuing");
		if (a != null) {
			int continuing = Turnout.CLOSED;
			try {
				continuing = element.getAttribute("continuing").getIntValue();
			} catch (org.jdom.DataConversionException e) {
				log.error("failed to convert continuingsense attribute");
			}
			l.setContinuingSense(continuing);
		}
		boolean value = false;
        if ((a = element.getAttribute("disabled"))!=null && a.getValue().equals("yes"))
            value = true;
        l.setDisabled(value);
        value = false;
        if ((a = element.getAttribute("disableWhenOccupied"))!=null && a.getValue().equals("yes"))
            value = true;
        l.setDisableWhenOccupied(value);
        boolean hide = false;
        if(element.getAttribute("hidden")!=null){
            if (element.getAttribute("hidden").getValue().equals("yes"))
                hide = true;
        }
        l.setHidden(hide);
        try {
			x = element.getAttribute("xa").getFloatValue();
			y = element.getAttribute("ya").getFloatValue();
            l.setCoordsA(new Point2D.Double(x,y));
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout b coords attribute");
        } catch (java.lang.NullPointerException e){
            //can be ignored as panel file may not support method
        }
		
		try {
			x = element.getAttribute("xb").getFloatValue();
			y = element.getAttribute("yb").getFloatValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout b coords attribute");
        }
		l.setCoordsB(new Point2D.Double(x,y));
		try {
			x = element.getAttribute("xc").getFloatValue();
			y = element.getAttribute("yc").getFloatValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout c coords attribute");
        }
		l.setCoordsC(new Point2D.Double(x,y));
        try {
			x = element.getAttribute("xd").getFloatValue();
			y = element.getAttribute("yd").getFloatValue();
            l.setCoordsD(new Point2D.Double(x,y));
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout c coords attribute");
        } catch (java.lang.NullPointerException e){
            //can be ignored as panel file may not support method
        }
		
        
        if (element.getChild("signalAMast")!=null){
            String mast = element.getChild("signalAMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalAMast(mast);
            }
        }
        
        if (element.getChild("signalBMast")!=null){
            String mast = element.getChild("signalBMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalBMast(mast);
            }
        }
        
        if (element.getChild("signalCMast")!=null){
            String mast = element.getChild("signalCMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalCMast(mast);
            }
        }

        if (element.getChild("signalDMast")!=null){
            String mast = element.getChild("signalDMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalDMast(mast);
            }
        }
        
        if (element.getChild("sensorA")!=null){
            String sensor = element.getChild("sensorA").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorA(sensor);
            }
        }
        
        if (element.getChild("sensorB")!=null){
            String sensor = element.getChild("sensorB").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorB(sensor);
            }
        }
        
        if (element.getChild("sensorC")!=null){
            String sensor = element.getChild("sensorC").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorC(sensor);
            }
        }

        if (element.getChild("sensorD")!=null){
            String sensor = element.getChild("sensorD").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorD(sensor);
            }
        }
        
		p.turnoutList.add(l);
    }

    static Logger log = LoggerFactory.getLogger(LayoutTurnoutXml.class.getName());
}

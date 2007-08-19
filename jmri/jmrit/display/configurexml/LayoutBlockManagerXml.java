// jmri.jmrit.display.configurexml.LayoutBlockManagerXML.java

package jmri.jmrit.display.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.LayoutBlock;
import jmri.jmrit.display.LayoutBlockManager;
import jmri.Sensor;
import java.util.List;
import org.jdom.Element;
import org.jdom.DataConversionException;
import org.jdom.Attribute;
import java.awt.Color;

/**
 * Provides the functionality for
 * configuring a LayoutBlockManager
 * <P>
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class LayoutBlockManagerXml implements XmlAdapter {

    public LayoutBlockManagerXml() {
    }

    /**
     * Implementation for storing the contents of a
     *	LayoutBlockManager
     * @param o Object to store, of type LayoutBlockManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element layoutblocks = new Element("layoutblocks");
        setStoreElementClass(layoutblocks);
        LayoutBlockManager tm = (LayoutBlockManager) o;
        if (tm!=null) {
            java.util.Iterator iter = tm.getSystemNameList().iterator();
            while (iter.hasNext()) {
                String sname = (String)iter.next();
                if (sname==null) log.error("System name null during LayoutBlock store");
                log.debug("layoutblock system name is "+sname);
                LayoutBlock b = tm.getBySystemName(sname);
				if (b.getUseCount()>0) {
					// save only those LayoutBlocks that are in use--skip abandoned ones
					String uname = b.getUserName();
					Element elem = new Element("layoutblock")
								.setAttribute("systemName", sname);
					if (uname!=null) elem.setAttribute("userName", uname);
					if (b.getOccupancySensorName() != "") {
						elem.setAttribute("occupancysensor", b.getOccupancySensorName());
					}
					elem.setAttribute("occupiedsense", ""+b.getOccupiedSense());
					elem.setAttribute("trackcolor", b.colorToString(b.getBlockTrackColor()));
					elem.setAttribute("occupiedcolor", b.colorToString(b.getBlockOccupiedColor()));
					layoutblocks.addContent(elem);
				}
			}
		}
		return (layoutblocks);	
	}

    /**
     * Subclass provides implementation to create the correct top
     * element, including the type information.
     * Default implementation is to use the local class here.
     * @param layoutblocks The top-level element being created
     */
    public void setStoreElementClass(Element layoutblocks) {
        layoutblocks.setAttribute("class",
							"jmri.jmrit.display.configurexml.LayoutBlockManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a LayoutBlockManager object of the correct class, then
     * register and fill it.
     * @param layoutblocks Top level Element to unpack.
     */
    public void load(Element layoutblocks) {
        // create the master object
        replaceLayoutBlockManager();
        // load individual layoutblocks
        loadLayoutBlocks(layoutblocks);
    }

    /**
     * Utility method to load the individual LayoutBlock objects.
     * If there's no additional info needed for a specific layoutblock type,
     * invoke this with the parent of the set of layoutblock elements.
     * @param layoutblocks Element containing the layoutblock elements to load.
     */
    public void loadLayoutBlocks(Element layoutblocks) {
		List layoutblockList = layoutblocks.getChildren("layoutblock");
        if (log.isDebugEnabled()) log.debug("Found "+layoutblockList.size()+" layoutblocks");
        LayoutBlockManager tm = InstanceManager.layoutBlockManagerInstance();

        for (int i=0; i<layoutblockList.size(); i++) {
            if ( ((Element)(layoutblockList.get(i))).getAttribute("systemName") == null) {
                log.warn("unexpected null in systemName "+
							((Element)(layoutblockList.get(i)))+" "+
									((Element)(layoutblockList.get(i))).getAttributes());
                break;
            }
            String sysName = ((Element)(layoutblockList.get(i))).
												getAttribute("systemName").getValue();
            String userName = null;
            if ( ((Element)(layoutblockList.get(i))).getAttribute("userName") != null) {
                userName = ((Element)(layoutblockList.get(i))).
												getAttribute("userName").getValue();
			}
            LayoutBlock b = tm.createNewLayoutBlock(sysName, userName);
            if (b!=null) {
				// set attributes
				Color color = b.stringToColor(((Element)(layoutblockList.get(i))).
												getAttribute("trackcolor").getValue());
				b.setBlockTrackColor(color);
				color = b.stringToColor(((Element)(layoutblockList.get(i)))
											.getAttribute("occupiedcolor").getValue());
				b.setBlockOccupiedColor(color);
				Attribute a = ((Element)(layoutblockList.get(i)))
											.getAttribute("occupancysensor");
				if (a!=null) {
					b.setOccupancySensorName(a.getValue());
				}
				a = ((Element)(layoutblockList.get(i))).
											getAttribute("occupancysensorsense");
				int sense = Sensor.ACTIVE;
				try {
					sense = ((Element)(layoutblockList.get(i))).
											getAttribute("occupiedsense").getIntValue();
				}		
				catch (org.jdom.DataConversionException e) {
					log.error("failed to convert occupiedsense attribute");
				}
				b.setOccupiedSense(sense);
			}	
	    }
	}

    /**
     * Replace the current LayoutBlockManager, if there is one, with
     * one newly created during a load operation. This is skipped
     * if they are of the same absolute type.
     */
    protected void replaceLayoutBlockManager() {
        if (InstanceManager.layoutBlockManagerInstance().getClass().getName()
                .equals(LayoutBlockManager.class.getName()))
            return;
        // if old manager exists, remove it from configuration process
        if (InstanceManager.layoutBlockManagerInstance() != null)
            InstanceManager.configureManagerInstance().deregister(
                InstanceManager.layoutBlockManagerInstance() );

        // register new one with InstanceManager
        LayoutBlockManager pManager = LayoutBlockManager.instance();
        InstanceManager.setLayoutBlockManager(pManager);
        // register new one for configuration
        InstanceManager.configureManagerInstance().registerConfig(pManager);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutBlockManagerXml.class.getName());
}

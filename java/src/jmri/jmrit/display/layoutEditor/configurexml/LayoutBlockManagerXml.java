package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.util.List;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.util.ColorUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the functionality for configuring a LayoutBlockManager
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutBlockManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LayoutBlockManagerXml() {
    }

    /**
     * Implementation for storing the contents of a LayoutBlockManager
     *
     * @param o Object to store, of type LayoutBlockManager
     * @return Element containing the complete info
     */
    @Override
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations
    public Element store(Object o) {
        Element layoutblocks = new Element("layoutblocks");
        setStoreElementClass(layoutblocks);
        LayoutBlockManager tm = (LayoutBlockManager) o;
        if (tm.isAdvancedRoutingEnabled()) {
            layoutblocks.setAttribute("blockrouting", "yes");
        }
        if (tm.getNamedStabilisedSensor() != null) {
            layoutblocks.setAttribute("routingStablisedSensor", tm.getNamedStabilisedSensor().getName());
        }

        java.util.Iterator<String> iter = tm.getSystemNameList().iterator();

        // don't return an element if there is nothing to include
        if (!iter.hasNext()) {
            return null;
        }

        while (iter.hasNext()) {
            String sname = iter.next();
            if (sname == null) {
                log.error("System name null during LayoutBlock store");
            } else {
                log.debug("layoutblock system name is " + sname);
                LayoutBlock b = tm.getBySystemName(sname);
                // save only those LayoutBlocks that are in use--skip abandoned ones
                if (b.getUseCount() > 0) {
                    Element elem = new Element("layoutblock").setAttribute("systemName", sname);
                    elem.addContent(new Element("systemName").addContent(sname));
                    storeCommon(b, elem);
                    if (!b.getOccupancySensorName().isEmpty()) {
                        elem.setAttribute("occupancysensor", b.getOccupancySensorName());
                    }
                    elem.setAttribute("occupiedsense", "" + b.getOccupiedSense());
                    elem.setAttribute("trackcolor", ColorUtil.colorToColorName(b.getBlockTrackColor()));
                    elem.setAttribute("occupiedcolor", ColorUtil.colorToColorName(b.getBlockOccupiedColor()));
                    elem.setAttribute("extracolor", ColorUtil.colorToColorName(b.getBlockExtraColor()));
                    if (!b.getMemoryName().isEmpty()) {
                        elem.setAttribute("memory", b.getMemoryName());
                    }
                    if (!b.useDefaultMetric()) {
                        elem.addContent(new Element("metric").addContent("" + b.getBlockMetric()));
                    }
                    layoutblocks.addContent(elem);
                }
            }
        }
        return (layoutblocks);
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param layoutblocks The top-level element being created
     */
    public void setStoreElementClass(Element layoutblocks) {
        layoutblocks.setAttribute("class", getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        replaceLayoutBlockManager();
        // load individual layoutblocks
        loadLayoutBlocks(shared);
        return true;
    }

    /**
     * Utility method to load the individual LayoutBlock objects. If there's no
     * additional info needed for a specific layoutblock type, invoke this with
     * the parent of the set of layoutblock elements.
     *
     * @param layoutblocks Element containing the layoutblock elements to load.
     */
    public void loadLayoutBlocks(Element layoutblocks) {
        LayoutBlockManager tm = InstanceManager.getDefault(LayoutBlockManager.class);
        try {
            tm.enableAdvancedRouting(layoutblocks.getAttribute("blockrouting").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert layout block manager blockrouting attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }
        if (layoutblocks.getAttribute("routingStablisedSensor") != null) {
            try {
                tm.setStabilisedSensor(layoutblocks.getAttribute("routingStablisedSensor").getValue());
            } catch (jmri.JmriException e) {
            }
        }

        List<Element> layoutblockList = layoutblocks.getChildren("layoutblock");
        if (log.isDebugEnabled()) {
            log.debug("Found " + layoutblockList.size() + " layoutblocks");
        }

        for (Element e : layoutblockList) {
            String sysName = getSystemName(e);
            if (sysName == null) {
                log.warn("unexpected null in systemName "
                        + e + " "
                        + e.getAttributes());
                break;
            }

            String userName = getUserName(e);
            LayoutBlock b = tm.createNewLayoutBlock(sysName, userName);

            // load common parts
            loadCommon(b, e);

            if (b != null) {
                // set attributes
                Color color;
                try {
                    color = ColorUtil.stringToColor(e.getAttribute("trackcolor").getValue());
                    b.setBlockTrackColor(color);
                } catch (IllegalArgumentException ex) {
                    b.setBlockTrackColor(Color.BLACK);
                    log.error("Invalid trackcolor {}; using black", e.getAttribute("trackcolor").getValue());
                }
                try {
                    color = ColorUtil.stringToColor(e.getAttribute("occupiedcolor").getValue());
                    b.setBlockOccupiedColor(color);
                } catch (IllegalArgumentException ex) {
                    b.setBlockOccupiedColor(Color.BLACK);
                    log.error("Invalid occupiedcolor {}; using black", e.getAttribute("occupiedcolor").getValue());
                }
                Attribute a = e.getAttribute("extracolor");
                if (a != null) {
                    try {
                        b.setBlockExtraColor(ColorUtil.stringToColor(a.getValue()));
                    } catch (IllegalArgumentException ex) {
                        b.setBlockExtraColor(Color.BLACK);
                        log.error("Invalid extracolor {}; using black", a.getValue());
                    }
                }
                a = e.getAttribute("occupancysensor");
                if (a != null) {
                    b.setOccupancySensorName(a.getValue());
                }
                a = e.getAttribute("memory");
                if (a != null) {
                    b.setMemoryName(a.getValue());
                }
                a = e.getAttribute("occupancysensorsense");
                int sense = Sensor.ACTIVE;
                try {
                    sense = e.getAttribute("occupiedsense").getIntValue();
                } catch (org.jdom2.DataConversionException ex) {
                    log.error("failed to convert occupiedsense attribute");
                }
                b.setOccupiedSense(sense);
                if (e.getChild("metric") != null) {
                    String stMetric = e.getChild("metric").getText();
                    try {
                        b.setBlockMetric(Integer.parseInt(stMetric));
                    } catch (java.lang.NumberFormatException ex) {
                        log.error("failed to convert metric attribute for block " + b.getDisplayName());
                    }
                }
            }
        }
    }

    /**
     * Replace the current LayoutBlockManager, if there is one, with one newly
     * created during a load operation. This is skipped if they are of the same
     * absolute type.
     */
    protected void replaceLayoutBlockManager() {
        if (InstanceManager.getDefault(LayoutBlockManager.class).getClass().getName()
                .equals(LayoutBlockManager.class.getName())) {
            return;
        }
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(LayoutBlockManager.class) != null) {
            InstanceManager.getDefault(jmri.ConfigureManager.class).deregister(
                    InstanceManager.getDefault(LayoutBlockManager.class));
        }

        // register new one with InstanceManager
        LayoutBlockManager pManager = InstanceManager.getDefault(LayoutBlockManager.class);
        // register new one for configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerConfig(pManager, jmri.Manager.LAYOUTBLOCKS);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutBlockManagerXml.class);
}

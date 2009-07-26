
package jmri.jmrit.display.configurexml;

import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.MemoryInputIcon;
import org.jdom.Element;

/**
 * Handle configuration for display.MemorySpinnerIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 * @version $Revision: 1.1 $
 */
public class MemoryInputIconXml extends PositionableLabelXml {

    public MemoryInputIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MemorySpinnerIcon
     * @param o Object to store, of type MemorySpinnerIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MemoryInputIcon p = (MemoryInputIcon)o;

        Element element = new Element("memoryicon");

        // include attributes
        element.setAttribute("colWidth", ""+p.getNumColumns());
        element.setAttribute("memory", p.getMemory().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryInputIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the memoryicon element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    @SuppressWarnings("unchecked")
	public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;

        int nCol = 2;
        try {
            nCol = element.getAttribute("colWidth").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert colWidth attribute");
        }

        MemoryInputIcon l = new MemoryInputIcon(nCol);

        l.setMemory(jmri.InstanceManager.memoryManagerInstance().getMemory(
            element.getAttribute("memory").getValue()));
        
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
 
         // find display level
        int level = PanelEditor.MEMORIES.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);

        p.putJPanel(l);
            
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MemoryInputIconXml.class.getName());
}

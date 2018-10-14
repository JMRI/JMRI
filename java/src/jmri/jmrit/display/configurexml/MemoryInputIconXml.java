package jmri.jmrit.display.configurexml;

import jmri.Memory;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemoryInputIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.MemorySpinnerIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 */
public class MemoryInputIconXml extends PositionableLabelXml {

    public MemoryInputIconXml() {
    }

    /**
     * Default implementation for storing the contents of a MemorySpinnerIcon
     *
     * @param o Object to store, of type MemorySpinnerIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        MemoryInputIcon p = (MemoryInputIcon) o;

        Element element = new Element("memoryInputIcon");

        // include attributes
        element.setAttribute("colWidth", "" + p.getNumColumns());
        element.setAttribute("memory", p.getNamedMemory().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryInputIconXml");
        return element;
    }

    /**
     * Load, starting with the memoryInputIcon element, then all the value-icon
     * pairs
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor) o;

        int nCol = 2;
        try {
            nCol = element.getAttribute("colWidth").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert colWidth attribute");
        }

        MemoryInputIcon l = new MemoryInputIcon(nCol, p);

        loadTextInfo(l, element);
        String name;
        Attribute attr = element.getAttribute("memory");
        if (attr == null) {
            log.error("incorrect information for a memory location; must use memory name");
            p.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }

        Memory m = jmri.InstanceManager.memoryManagerInstance().getMemory(name);

        if (m != null) {
            l.setMemory(name);
        } else {
            log.error("Memory named '" + attr.getValue() + "' not found.");
            p.loadFailed();
            return;
        }

        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);
        
        javax.swing.JComponent textField = l.getTextComponent();
        jmri.jmrit.display.PositionablePopupUtil util = l.getPopupUtility();
        if (util.hasBackground()) {
            textField.setBackground(util.getBackground());            
        } else {
            textField.setBackground(null);
            textField.setOpaque(false);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryInputIconXml.class);
}

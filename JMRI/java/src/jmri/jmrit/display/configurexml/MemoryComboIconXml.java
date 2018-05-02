package jmri.jmrit.display.configurexml;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import jmri.Memory;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemoryComboIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.MemorySpinnerIcon objects.
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class MemoryComboIconXml extends PositionableLabelXml {

    public MemoryComboIconXml() {
    }

    /**
     * Default implementation for storing the contents of a MemorySpinnerIcon
     *
     * @param obj Object to store, of type MemorySpinnerIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object obj) {

        MemoryComboIcon memoryIcon = (MemoryComboIcon) obj;

        Element element = new Element("memoryComboIcon");

        Element elem = new Element("itemList");
        DefaultComboBoxModel<String> model = memoryIcon.getComboModel();
        for (int i = 0; i < model.getSize(); i++) {
            Element e = new Element("item");
            e.setAttribute("index", "" + i);
            e.addContent(model.getElementAt(i));
            elem.addContent(e);
        }
        element.addContent(elem);

        // include attributes
        element.setAttribute("memory", memoryIcon.getNamedMemory().getName());
        storeCommonAttributes(memoryIcon, element);
        storeTextInfo(memoryIcon, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryComboIconXml");
        return element;
    }

    /**
     * Load, starting with the memoryComboIcon element, then all the value-icon
     * pairs
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor) o;

        Element elem = element.getChild("itemList");
        List<Element> list = elem.getChildren("item");
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Element e = list.get(i);
            String item = e.getText();
            try {
                int idx = e.getAttribute("index").getIntValue();
                items[idx] = item;
            } catch ( org.jdom2.DataConversionException ex) {
                log.error("failed to convert ComboBoxIcon index attribute");
                if (items[i]==null) {
                    items[i] = item;                    
                }
            }
        }

        MemoryComboIcon l = new MemoryComboIcon(p, items);

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
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryComboIconXml.class);
}

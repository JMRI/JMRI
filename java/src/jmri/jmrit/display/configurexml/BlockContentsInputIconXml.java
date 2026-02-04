package jmri.jmrit.display.configurexml;

import jmri.Block;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.BlockContentsInputIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 *
 * Cloned from MemoryInputIconXml
 */
public class BlockContentsInputIconXml extends PositionableLabelXml {

    public BlockContentsInputIconXml() {
    }

    /**
     * Default implementation for storing the contents of a MemorySpinnerIcon
     *
     * @param o Object to store, of type MemorySpinnerIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        BlockContentsInputIcon p = (BlockContentsInputIcon) o;

        Element element = new Element("blockContentsInputIcon");

        // include attributes
        element.setAttribute("colWidth", "" + p.getNumColumns());
        element.setAttribute("block", p.getNamedBlock().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        storeLogixNG_Data(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.BlockContentsInputIconXml");
        return element;
    }

    /**
     * Load, starting with the blockContentsInputIcon element, then all the value-icon
     * pairs
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        Editor p = (Editor) o;

        int nCol = 2;
        try {
            nCol = element.getAttribute("colWidth").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert colWidth attribute");
        }

        BlockContentsInputIcon l = new BlockContentsInputIcon(nCol, p);

        loadTextInfo(l, element);
        String name;
        Attribute attr = element.getAttribute("block");
        if (attr == null) {
            log.error("incorrect information for a block location; must use block name");
            p.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }

        Block m = jmri.InstanceManager.getDefault(jmri.BlockManager.class).getBlock(name);

        if (m != null) {
            l.setBlock(name);
        } else {
            log.error("Block named '{}' not found.", attr.getValue());
            p.loadFailed();
            return;
        }

        try {
            p.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }

        loadLogixNG_Data(l, element);

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

    private final static Logger log = LoggerFactory.getLogger(BlockContentsInputIconXml.class);
}

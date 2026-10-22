package jmri.jmrix.dccpp.swing.messages.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;
import jmri.jmrit.display.configurexml.PositionableLabelXml;
import jmri.jmrix.dccpp.swing.messages.MessagesPositionable;

import org.jdom2.Element;

/** Handle configuration for MessagesPositionable objects. */
public class MessagesPositionableXml extends PositionableLabelXml {

    public MessagesPositionableXml() {
    }

    /**
     * Store a MessagesPositionable.
     *
     * @param o Object to store, of type MessagesPositionable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        MessagesPositionable p = (MessagesPositionable) o;
        if (!p.isActive()) {
            return null; // if flagged as inactive, don't store
        }

        Element element = new Element("dccex_messages");

        element.addContent(MessagesConfigurationXml.store(p.getMessagesPanel()));

        if (p.getId() != null) element.setAttribute("id", p.getId());
        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());

        element.setAttribute("class", this.getClass().getName());

        storeLogixNG_Data(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a MessagesPositionable, then add it to a panel.
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * @throws JmriConfigureXmlException when the connection can't be found or the id isn't unique
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        Editor ed = (Editor) o;

        MessagesPositionable l = new MessagesPositionable(ed);

        MessagesConfigurationXml.load(l.getMessagesPanel(), element, false);

        l.initComponents();

        int x = 0;
        int y = 0;
        try {
            if (element.getAttribute("id") != null) {
                try {
                    l.setId(element.getAttribute("id").getValue());
                } catch (Positionable.DuplicateIdException e) {
                    throw new JmriConfigureXmlException("Positionable id is not unique", e);
                }
            }
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setOpaque(false);
        l.setLocation(x, y);

        l.setDisplayLevel(Editor.ICONS);
        try {
            ed.putItem(l, true);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }

        loadLogixNG_Data(l, element);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagesPositionableXml.class);
}

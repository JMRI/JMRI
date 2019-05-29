package jmri.jmrit.operations.trains.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.configurexml.LocoIconXml;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.roster.RosterEntry;

/**
 * Handle configuration for display.TrainIcon objects.
 *
 * @author mstevetodd Copyright: Copyright (c) 2012
 * 
 */
public class TrainIconXml extends LocoIconXml {

    public TrainIconXml() {
    }

    /**
     * Default implementation for storing the contents of a TrainIcon
     *
     * @param o Object to store, of type TrainIcon
     * @return Element containing the complete info
     * @deprecated Never ever used in operations or anywhere else, since 4.15.7
     */
    @Deprecated
    @Override
    public Element store(Object o) {

        TrainIcon p = (TrainIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element(Xml.TRAINICON);
        element.setAttribute(Xml.TRAIN, p.getTrain().getId());
        element.setAttribute(Xml.TRAIN_NAME, p.getTrain().getName());
        storeCommonAttributes(p, element);

        // include contents
        if (p.getUnRotatedText() != null) {
            element.setAttribute(Xml.TEXT, p.getUnRotatedText());
        }
        storeTextInfo(p, element);
        element.setAttribute(Xml.ICON, Xml.YES);
        element.setAttribute(Xml.DOCK_X, "" + p.getDockX());
        element.setAttribute(Xml.DOCK_Y, "" + p.getDockY());
//  element.setAttribute("iconId", p.getIconId());
        element.addContent(storeIcon(Xml.ICON, (NamedIcon) p.getIcon()));
        RosterEntry entry = p.getRosterEntry();
        if (entry != null) {
            element.setAttribute(Xml.ROSTERENTRY, entry.getId());
        }

        element.setAttribute(Xml.CLASS, this.getClass().getName());

        return element;
    }

    /**
     * TrainIcons should be loaded by the operations function, not here
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * @deprecated Never ever used in operations or anywhere else, since 4.15.7
     */
    @Override
    @Deprecated
    public void load(Element element, Object o) {
        //NOTE: this method should not be populated.  
        //  The operations program restores the Icons when the Trains window is opened.   
        //  The train icons have to be placed based on the trains database state,
        //  and not where the icons were on the panel when the panel was saved.
        log.debug("Warning: loading of TrainIcon not implemented, TrainIcons will be placed by Operations");
    }

    private final static Logger log = LoggerFactory.getLogger(TrainIconXml.class);
}

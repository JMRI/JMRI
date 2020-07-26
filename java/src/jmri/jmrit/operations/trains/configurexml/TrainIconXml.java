package jmri.jmrit.operations.trains.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.display.configurexml.LocoIconXml;

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
     * Default implementation for storing the contents of a TrainIcon.
     * TrainIcons are restored by the operations code.
     *
     * @param o Object to store, of type TrainIcon
     * @return Element containing the complete info
     * 
     */
    @Override
    public Element store(Object o) {
        return null;
    }

    /**
     * TrainIcons should be loaded by the operations function, not here
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * 
     */
    @Override
    public void load(Element element, Object o) {
        //NOTE: this method should not be populated.  
        //  The operations program restores the Icons when the Trains window is opened.   
        //  The train icons have to be placed based on the trains database state,
        //  and not where the icons were on the panel when the panel was saved.
        log.debug("Warning: loading of TrainIcon not implemented, TrainIcons will be placed by Operations");
    }

    private final static Logger log = LoggerFactory.getLogger(TrainIconXml.class);
}

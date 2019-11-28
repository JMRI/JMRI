package jmri.jmrit.audio.configurexml;

import jmri.InstanceManager;
import jmri.managers.configurexml.AbstractAudioManagerConfigXML;
import org.jdom2.Element;

/**
 * Persistency implementation for the default AudioManager persistence.
 * <p>
 * The state of audio objects is not persisted, just their existence.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2009
 */
public class DefaultAudioManagerXml extends AbstractAudioManagerConfigXML {

    /**
     * Default constructor
     */
    public DefaultAudioManagerXml() {
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param audio The top-level element being created
     */
    @Override
    public void setStoreElementClass(Element audio) {
        audio.setAttribute("class", "jmri.jmrit.audio.configurexml.DefaultAudioManagerXml");
    }

    /**
     * Create a AudioManager object of the correct class, then register and fill
     * it.
     *
     * @param shared Top level Element to unpack.
     * @param perNode Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        InstanceManager.getDefault(jmri.AudioManager.class);
        // load individual shared objects
        loadAudio(shared);
        return true;
    }
}

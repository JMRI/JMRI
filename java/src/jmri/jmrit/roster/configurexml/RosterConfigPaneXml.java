package jmri.jmrit.roster.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.roster.RosterConfigPane;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of Roster default values.
 * <p>
 * This class is named as being the persistant form of the RosterConfigPane
 * class, but there's no object of that form created when this is read back.
 * Instead, this persists static members of the roster.Roster class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class RosterConfigPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public RosterConfigPaneXml() {
    }

    /**
     * Default implementation for storing the static contents
     *
     * @param o Object to store
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        RosterConfigPane p = (RosterConfigPane) o;
        // any reason to write?
        if ((p.getSelectedItem() == null || p.getSelectedItem().isEmpty())
                && p.getDefaultOwner().isEmpty()) {
            return null;
        }

        // create and write element
        Element roster = new Element("roster");
        if (p.getSelectedItem() != null && !p.getSelectedItem().isEmpty()) {
            roster.setAttribute("directory", p.getSelectedItem());
        }
        roster.setAttribute("class", this.getClass().getName());
        roster.setAttribute("ownerDefault", p.getDefaultOwner());
        return roster;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        Profile project = ProfileManager.getDefault().getActiveProfile();
        if (shared.getAttribute("directory") != null) {
            InstanceManager.getDefault(RosterConfigManager.class).setDirectory(project, shared.getAttribute("directory").getValue());
            if (log.isDebugEnabled()) {
                log.debug("set roster location (1): " + shared.getAttribute("directory").getValue());
            }
        }
        if (shared.getAttribute("ownerDefault") != null) {
            InstanceManager.getDefault(RosterConfigManager.class).setDefaultOwner(project, shared.getAttribute("ownerDefault").getValue());
        }
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerPref(new RosterConfigPane());
        }
        return result;
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        if (log.isDebugEnabled()) {
            log.debug("set roster location (2): " + element.getAttribute("directory").getValue());
        }
        if (element.getAttribute("directory") != null) {
            Roster.getDefault().setRosterLocation(element.getAttribute("directory").getValue());
        }
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerPref(new RosterConfigPane());
        }
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(RosterConfigPaneXml.class);

}

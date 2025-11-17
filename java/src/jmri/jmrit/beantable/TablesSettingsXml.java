package jmri.jmrit.beantable;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles XML persistence of the TablesSettings.
 *
 * @author Bill Hood Copyright (C) 2024
 */
public class TablesSettingsXml extends jmri.configurexml.AbstractXmlAdapter {

    public TablesSettingsXml() {
        super();
    }

    @Override
    public Element store(Object o) {
        Element e = new Element("tablesSettings");
        e.setAttribute("mainMenuEnabled", "" + TablesSettings.isMainMenuEnabled());
        return e;
    }

    @Override
    public boolean load(Element e) {
        if (e.getAttribute("mainMenuEnabled") != null) {
            TablesSettings.setMainMenuEnabled(e.getAttribute("mainMenuEnabled").getValue().equals("true"));
        }
        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(TablesSettingsXml.class);
}

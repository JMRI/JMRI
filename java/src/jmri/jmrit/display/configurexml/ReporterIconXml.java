package jmri.jmrit.display.configurexml;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ReporterIcon;
import org.jdom2.Element;

/**
 * Handle configuration for display.ReporterIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 */
public class ReporterIconXml extends PositionableLabelXml {

    public ReporterIconXml() {
    }

    /**
     * Default implementation for storing the contents of a ReporterIcon
     *
     * @param o Object to store, of type ReporterIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        ReporterIcon p = (ReporterIcon) o;

        Element element = new Element("reportericon");

        // include contents
        element.setAttribute("reporter", p.getReporter().getSystemName());
        storeCommonAttributes(p, element);

        storeTextInfo(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.ReporterIconXml");

        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        Editor ed = (Editor) o;
        ReporterIcon l = new ReporterIcon(ed);

        loadTextInfo(l, element);

        l.setReporter(jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(
                element.getAttribute("reporter").getValue()));

        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.REPORTERS, element);
    }
}

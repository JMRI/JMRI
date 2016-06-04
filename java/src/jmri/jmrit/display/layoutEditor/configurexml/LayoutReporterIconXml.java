package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.jmrit.display.configurexml.PositionableLabelXml;
import jmri.jmrit.display.configurexml.ReporterIconXml;
import org.jdom2.Element;

/**
 * Dummy class, just present so files that refer to this class (e.g. pre JMRI
 * 2.7.8 files) can still be read by deferring to the present class.
 *
 * Handle configuration for display.LayoutReporterIcon objects.
 *
 * @author Dave Duchamp Copyright: Copyright (c) 2008
 * @author Kevin Dickerson, Deprecated
 * @deprecated 2.7.8  - left so old files can be read
 */
@Deprecated
public class LayoutReporterIconXml extends PositionableLabelXml {

    public LayoutReporterIconXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutReporterIcon
     *
     * @param o Object to store, of type LayoutReporterIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        ReporterIconXml tmp = new ReporterIconXml();
        return tmp.store(o);
    }

    /**
     * Create a LayoutPositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    public void load(Element element, Object o) {

        ReporterIconXml tmp = new ReporterIconXml();
        tmp.load(element, o);
        // create the object
    }
}

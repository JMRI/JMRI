
package jmri.jmrit.display.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;

import org.jdom.Element;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.8 files) can still be read by
 * deferring to the present class.
 *
 * @author Pete Cressman, Deprecated
 * @version $Revision: 1.18 $
 * @deprecated 2.9
 */
 
@Deprecated
public class LayoutEditorXml extends AbstractXmlAdapter {

    public LayoutEditorXml() {
    }

    /**
     * Default implementation for storing the contents of PanelEditor
     * @param o Object to store, of type LayoutSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml tmp = 
            new jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml();
        return tmp.store(o);
    }

    public boolean load(Element element) {
        jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml tmp = 
            new jmri.jmrit.display.layoutEditor.configurexml.LayoutEditorXml();
        return tmp.load(element);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }  

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutEditorXml.class.getName());
}


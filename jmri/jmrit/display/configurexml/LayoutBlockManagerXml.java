// jmri.jmrit.display.configurexml.LayoutBlockManagerXML.java

package jmri.jmrit.display.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.InstanceManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.Sensor;
import java.util.List;
import org.jdom.Element;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.8 files) can still be read by
 * deferring to the present class.
 *
 * @author Pete Cressman, Deprecated
 * @version $Revision: 1.9 $
 * @deprecated 2.9
 */
 
@Deprecated
public class LayoutBlockManagerXml extends AbstractXmlAdapter {

    public LayoutBlockManagerXml() {
    }

    /**
     * Implementation for storing the contents of a
     *	LayoutBlockManager
     * @param o Object to store, of type LayoutBlockManager
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        jmri.jmrit.display.layoutEditor.configurexml.LayoutBlockManagerXml tmp = 
            new jmri.jmrit.display.layoutEditor.configurexml.LayoutBlockManagerXml();
        return tmp.store(o);
	}

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a LayoutBlockManager object of the correct class, then
     * register and fill it.
     * @param layoutblocks Top level Element to unpack.
     * @return true if successful
     */
    public boolean load(Element layoutblocks) {
        jmri.jmrit.display.layoutEditor.configurexml.LayoutBlockManagerXml tmp = 
            new jmri.jmrit.display.layoutEditor.configurexml.LayoutBlockManagerXml();
        return tmp.load(layoutblocks);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutBlockManagerXml.class.getName());
}

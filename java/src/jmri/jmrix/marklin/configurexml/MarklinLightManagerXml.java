package jmri.jmrix.marklin.configurexml;

import org.jdom2.Element;

/**
 * Load and store Marklin lights using the common light XML format.
 */
public class MarklinLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    /** {@inheritDoc} */
    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.marklin.configurexml.MarklinLightManagerXml");
    }

    /** {@inheritDoc} */
    @Override
    public boolean load(Element shared, Element perNode) {
        return loadLights(shared);
    }
}

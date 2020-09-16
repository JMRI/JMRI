package jmri.managers.configurexml;

import org.jdom2.Element;

/**
 * Persistency implementation for the default MeterGroupManager persistence.
 * <p>
 * The state of memory objects is not persisted, just their existence.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class DefaultMeterGroupManagerXml extends AbstractNamedBeanManagerConfigXML {

    public DefaultMeterGroupManagerXml() {
    }

    /** {@inheritDoc} */
    @Override
    public Element store(Object o) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean load(Element sharedMemories, Element perNodeMemories) {
        return true;
    }

}

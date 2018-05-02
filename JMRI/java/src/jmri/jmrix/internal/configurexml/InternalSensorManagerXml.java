package jmri.jmrix.internal.configurexml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring InternalSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "name assigned historically")
public class InternalSensorManagerXml extends jmri.managers.configurexml.InternalSensorManagerXml {

    public InternalSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }
}

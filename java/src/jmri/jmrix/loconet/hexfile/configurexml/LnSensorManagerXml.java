package jmri.jmrix.loconet.hexfile.configurexml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring LnSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2003
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "This is ineffect the same as its super class")
public class LnSensorManagerXml extends jmri.jmrix.loconet.configurexml.LnSensorManagerXml {

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", super.getClass().getSuperclass().getName()); // NOI18N
    }

}

package jmri.jmrix.internal.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring
 * InternalTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "name assigned historically")
public class InternalTurnoutManagerXml extends jmri.managers.configurexml.InternalTurnoutManagerXml {

    public InternalTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", this.getClass().getName());
    }

}

package jmri.jmrix.loconet.configurexml;

//import jmri.SignalHead;
import jmri.jmrix.loconet.LNCPSignalMast;
import org.jdom2.Element;

//import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;

/*import java.util.List;
 import org.jdom2.DataConversionException;
 import org.jdom2.Element;*/
/**
 * Handle XML configuration for loconet.LNCPSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 */
public class LNCPSignalMastXml extends jmri.implementation.configurexml.DccSignalMastXml {

    public LNCPSignalMastXml() {
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        LNCPSignalMast m;
        String sys = getSystemName(shared);
        m = new jmri.jmrix.loconet.LNCPSignalMast(sys);

        if (getUserName(shared) != null) {
            m.setUserName(getUserName(shared));
        }
        return loadCommonDCCMast(m, shared);

    }
}

package jmri.jmrix.loconet.configurexml;

//import jmri.SignalHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.loconet.LNCPSignalMast;
import org.jdom.Element;

//import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;

/*import java.util.List;
import org.jdom.DataConversionException;
import org.jdom.Element;*/

/**
 * Handle XML configuration for loconet.LNCPSignalMast objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @version $Revision: 17977 $
 */
public class LNCPSignalMastXml extends jmri.implementation.configurexml.DccSignalMastXml {

    public LNCPSignalMastXml() {
    }
    
    public boolean load(Element element) {
        LNCPSignalMast m;
        String sys = getSystemName(element);
            m = new jmri.jmrix.loconet.LNCPSignalMast(sys);
        
        if (getUserName(element) != null)
            m.setUserName(getUserName(element));
        return loadCommonDCCMast(m, element);
        
    }

    static Logger log = LoggerFactory.getLogger(LNCPSignalMastXml.class.getName());
}

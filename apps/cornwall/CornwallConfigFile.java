// CornwallConfigFile.java

package apps.cornwall;

import com.sun.java.util.collections.List;
import java.io.*;
import jmri.jmrit.XmlFile;
import apps.*;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates the preferences information for the
 * JmriDemo application. Works with the CornwallConfigFrame
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.1 $
 * @see apps.AbstractConfigFrame
 */
public class CornwallConfigFile extends apps.AbstractConfigFile {

    protected String configFileName() { return "CornwallConfig.xml";}

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CornwallConfigFile.class.getName());
}

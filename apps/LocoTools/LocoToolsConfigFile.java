// LocoToolsConfigFile.java

package apps.LocoTools;

import com.sun.java.util.collections.List;
import java.io.*;
import jmri.jmrit.XmlFile;
import apps.*;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates the preferences information for the
 * LocoTools application. Works with the LocoToolsConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version		 	$Revision: 1.5 $
 * @see apps.AbstractConfigFrame
 */
public class LocoToolsConfigFile extends apps.AbstractConfigFile {

    protected String configFileName() { return "LocoToolsConfig.xml";}

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoToolsConfigFile.class.getName());
}

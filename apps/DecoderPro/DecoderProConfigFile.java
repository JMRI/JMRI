// DecoderProConfigFile.java

package apps.DecoderPro;

import com.sun.java.util.collections.List;
import java.io.*;
import apps.*;
import jmri.jmrit.XmlFile;
import org.jdom.*;
import org.jdom.output.*;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates the preferences information for the
 * DecoderPro application. Works with the DecoderProConfigFrame
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version		 	$Revision: 1.11 $
 * @see apps.DecoderPro.DecoderProConfigFrame
 */
public class DecoderProConfigFile extends apps.AbstractConfigFile {

    protected String configFileName() { return "DecoderProConfig.xml";}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFile.class.getName());

}

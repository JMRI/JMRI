// PanelProConfigFile.java

package apps.PanelPro;

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
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version	$Revision: 1.1 $
 * @see apps.AbstractConfigFrame
 */
public class PanelProConfigFile extends apps.AbstractConfigFile {

    protected String configFileName() { return "PanelProConfig.xml";}

    protected void writeConnection(Element root, AbstractConfigFrame f) {
        // add connection elements
        root.addContent(f.getCommPane().getConnection());
        root.addContent(((PanelProConfigFrame)f).getCommPane2().getConnection());
    }

    public Element getConnectionElement2() {
        return _connection2;
    }

    protected void readConnection(Element root) {
        List l = root.getChildren("connection");
        if (l.size()!=2) log.error("wrong number of connection elements: "+l.size());
        if (l.size()>=1) _connection = (Element)l.get(0);
        if (l.size()>=2) _connection2 = (Element)l.get(1);
    }
    Element _connection2;

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelProConfigFile.class.getName());
}

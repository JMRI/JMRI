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
 * @version		 	$Revision: 1.10 $
 * @see apps.DecoderPro.DecoderProConfigFrame
 */
public class DecoderProConfigFile extends apps.AbstractConfigFile {

    public void writeFile(String name, AbstractConfigFrame f) {
        try {
            // This is taken in large part from "Java and XML" page 368

            // create file Object
            XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
            File file = new File(prefsDir()+name);

            // create root element
            Element root = new Element("preferences-config");
            Document doc = new Document(root);
            doc.setDocType(new DocType("preferences-config","preferences-config.dtd"));

            // add connection element
            Element values;
            root.addContent(f.getConnection());

            // add gui element
            root.addContent(f.getGUI())
                ;

            // add programmer element
            root.addContent(f.getProgrammer());

            // write the result to selected file
            java.io.FileOutputStream o = new java.io.FileOutputStream(file);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setNewlines(true);   // pretty printing
            fmt.setIndent(true);
            fmt.output(doc, o);
            o.close();

        }
        catch (Exception e) {
            log.error(e);
        }
    }

    protected String configFileName() { return "DecoderProConfig.xml";}

    public String defaultConfigFilename() { return configFileName();}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigFile.class.getName());

}

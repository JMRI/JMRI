package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmri.jmrit.throttle.ThrottleFrame;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrit.XmlFile;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.DocType;
import org.jdom.output.XMLOutputter;

/**
 * Save throttles to XML
 *
 * @author			Glen Oberhauser
 * @version
 */
public class StoreXmlThrottleAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public StoreXmlThrottleAction(String s) {
        super(s);
    }

    /**
     * The action is performed. Let the user choose the file to save to.
     * Write XML for each ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser fileChooser = new JFileChooser(XmlFile.prefsDir());
        int retVal = fileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION)
        {
            return;  // give up if no file selected
        }

        try
        {
            ThrottleFrameManager manager = InstanceManager.throttleFrameManagerInstance();
            Element root = new Element("throttle-config");
            Document doc = new Document(root);
            doc.setDocType(new DocType("throttle-config", "throttle-config.dtd"));

            com.sun.java.util.collections.ArrayList children =
                    new com.sun.java.util.collections.ArrayList(5);
            for (Iterator i = manager.getThrottleFrames(); i.hasNext(); )
            {
                ThrottleFrame f = (ThrottleFrame)i.next();
                Element throttleElement = f.getXml();
                children.add(throttleElement);
            }
            root.setChildren(children);

            FileOutputStream o = new java.io.FileOutputStream(fileChooser.getSelectedFile());
            XMLOutputter fmt = new XMLOutputter();
            fmt.setNewlines(true);   // pretty printing
            fmt.setIndent(true);
            fmt.output(doc, o);

        }
        catch (FileNotFoundException ex)
        {
			log.warn("Exception in storing throttle xml: "+ex);

        }
        catch (IOException ex)
        {
            log.warn("Exception in storing throttle xml: "+ex);

        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(StoreXmlThrottleAction.class.getName());

}
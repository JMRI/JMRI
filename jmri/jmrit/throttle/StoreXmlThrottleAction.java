package jmri.jmrit.throttle;

import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import com.sun.java.util.collections.*;
import org.jdom.*;
import org.jdom.output.*;

/**
 * Save throttles to XML
 *
 * @author			Glen Oberhauser
 * @version     $Revision: 1.9 $
 */
public class StoreXmlThrottleAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public StoreXmlThrottleAction(String s) {
        super(s);
	// disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }
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
            Element root = new Element("throttle-config");
            Document doc = XmlFile.newDocument(root, "throttle-config.dtd");

            com.sun.java.util.collections.ArrayList children =
                    new com.sun.java.util.collections.ArrayList(5);
            for (Iterator i = ThrottleFrameManager.instance().getThrottleFrames(); i.hasNext(); )
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
            o.close();

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

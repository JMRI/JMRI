package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.Iterator;

import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import jmri.jmrit.throttle.ThrottleFrame;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrit.XmlFile;

import org.jdom.Element;

/**
 * Load throttles from XML
 *
 * @author			Glen Oberhauser
 * @version
 */
public class LoadXmlThrottleAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public LoadXmlThrottleAction(String s) {
        super(s);
    }

    /**
     * The action is performed. Create a new ThrottleFrame and
     * position it adequately on the screen.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        JFileChooser fileChooser = new JFileChooser(XmlFile.prefsDir());
        int retVal = fileChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION)
        {
            return;  // give up if no file selected
        }

        try
        {
            ThrottlePrefs prefs = new ThrottlePrefs();
            Element root = prefs.rootFromFile(fileChooser.getSelectedFile());
            List throttles = root.getChildren("ThrottleFrame");
            for (Iterator i = throttles.iterator(); i.hasNext();)
            {
                ThrottleFrame tf = new ThrottleFrame();
                tf.setXml((Element)i.next());
                tf.setVisible(true);
            }

        }
        catch (org.jdom.JDOMException ex)
        {
            System.out.println(ex);
        }
        catch (FileNotFoundException ex)
        {
            System.out.println(ex);
        }

    }

    class ThrottlePrefs extends XmlFile
    {

    }
    // initialize logging
    //static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleCreationAction.class.getName());

}
package jmri.jmrit.display.configurexml;

import org.jdom.Element;
import com.sun.java.util.collections.List;

import jmri.InstanceManager;
import jmri.jmrit.display.PanelEditor;
import jmri.configurexml.XmlAdapter;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Handle configuration for display.PanelEditor panes.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class PanelEditorXml implements XmlAdapter {

    public PanelEditorXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PanelEditor
     * @param o Object to store, of type PanelEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element panel = new Element("paneleditor");
        panel.addAttribute("class", "jmri.jmrit.display.configurexml.PanelEditorXml");
        return panel;
    }


    /**
     * Create a PanelEditor object, then
     * register and fill it, then pop it in a JFrame
     * @param turnouts Top level Element to unpack.
     */
    public void load(Element turnouts) {
        // create the objects
        JFrame targetFrame = new JFrame("Panel");
        JPanel targetPanel = new JPanel();
        targetFrame.getContentPane().add(targetPanel);
        targetPanel.setLayout(null);
        PanelEditor panel = new PanelEditor();
        panel.setTarget(targetPanel);
        JFrame editFrame = new JFrame("PanelEditor");
        editFrame.getContentPane().add(panel);

        // load the contents

        // display the results
        targetFrame.show();
        editFrame.pack();
        editFrame.show();

        // register the result for later configuration
        InstanceManager.configureManagerInstance().register(panel);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}
package jmri.jmrit.display.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdom.Element;

/**
 * Dummy class, just present so files that refer to this 
 * class (e.g. pre JMRI 2.8 files) can still be read by
 * deferring to the present class.
 *
 * @author Pete Cressman, Deprecated
 * @version $Revision: 1.39 $
 * @deprecated 2.9
 */
 
@Deprecated
public class PanelEditorXml extends AbstractXmlAdapter {

    static int STRUT_SIZE = 10;
    public PanelEditorXml() {
    }

    /**
     * Default implementation for storing the contents of PanelEditor
     * @param o Object to store, of type LayoutSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml tmp = 
            new jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml();
        return tmp.store(o);
    }

    boolean loadPanelEditor;
    public boolean load(Element element) {
        final JDialog pickEditorDialog = new JDialog((java.awt.Frame)null, "Choose Editor", true);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(new JLabel("THIS DIALOG IS TEMPORARY - Choose an editor to view panel configuration."));
        mainPanel.add(panel, BorderLayout.NORTH); 
        //panel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

        JButton peButton = new JButton("   PanelEditor   ");
        peButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadPanelEditor = true;
                    pickEditorDialog.dispose();
                }
            });

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.add(peButton);
        panel.add(new JLabel("\"Classic\" Panel Editor with version 2.9.3 functionality"));
        middlePanel.add(panel);

        JButton cpButton = new JButton("ControlPanelEditor");
        cpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loadPanelEditor = false;
                    pickEditorDialog.dispose();
                }
            });

        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.add(cpButton);
        panel.add(new JLabel("A Panel Editor (in progress) with all the bells and whistles"));
        middlePanel.add(panel);

        mainPanel.add(middlePanel, BorderLayout.SOUTH);
        pickEditorDialog.getContentPane().setLayout(new BorderLayout(5,5));
        pickEditorDialog.getContentPane().add(mainPanel);
        pickEditorDialog.setLocation(400, 300);
        pickEditorDialog.pack();
        pickEditorDialog.setVisible(true);

        pickEditorDialog.dispose();
        if (log.isDebugEnabled()) log.debug("Load "+(loadPanelEditor?"PanelEditor":"ControlPanelEditor"));
        if (loadPanelEditor) {
            jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml tmp = 
                new jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml();
            return tmp.load(element);
        } else {
            jmri.jmrit.display.controlPanelEditor.configurexml.ControlPanelEditorXml tmp = 
                new jmri.jmrit.display.controlPanelEditor.configurexml.ControlPanelEditorXml();
            return tmp.load(element);
        }
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }  

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PanelEditorXml.class.getName());
}


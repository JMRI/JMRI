package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.throttle.implementation.ThrottleUICore;
import jmri.util.swing.JmriJOptionPane;

import org.jdom2.Element;

/**
 * Load throttles from XML
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Glen Oberhauser 2004
 */
public class LoadXmlThrottlesLayoutAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public LoadXmlThrottlesLayoutAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public LoadXmlThrottlesLayoutAction() {
        this("Open Throttle");
    }

    JFileChooser fileChooser;

    /**
     * The action is performed. Let the user choose the file to load from. Read
     * XML for each ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (fileChooser == null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setCurrentDirectory(new File(ThrottleUICore.getDefaultThrottleFolder()));
        }
        int retVal = fileChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
            // give up if no file selected
        }

        // if exising frames are open ask to destroy those or merge.
        if (InstanceManager.getDefault(ThrottleFrameManager.class).iterator().hasNext()) {
            Object[] possibleValues = {Bundle.getMessage("LabelMerge"),
                Bundle.getMessage("LabelReplace"),
                Bundle.getMessage("ButtonCancel")};
            int selectedValue = JmriJOptionPane.showOptionDialog(null,
                    Bundle.getMessage("DialogMergeOrReplace"),
                    Bundle.getMessage("OptionLoadingThrottles"),
                    JmriJOptionPane.DEFAULT_OPTION,
                    JmriJOptionPane.INFORMATION_MESSAGE, null, possibleValues,
                    possibleValues[0]);
            if (selectedValue == 2 || selectedValue == JmriJOptionPane.CLOSED_OPTION ) {
                return; // array position 2 ButtonCancel or Dialog closed
            }
            if (selectedValue == 1 ) { // array position 1, LabelReplace
                // replace chosen - close all then load
                InstanceManager.getDefault(ThrottleFrameManager.class).requestAllThrottleWindowsDestroyed();
            }
        }
        try {
            loadThrottlesLayout(fileChooser.getSelectedFile());
        } catch (java.io.IOException e1) {
            log.warn("Exception while reading file", e1);
        }
    }

    /**
     * Parse the XML file and create ThrottleFrames.
     * @return  true if throttle loaded successfully, else false.
     * @param f The XML file containing throttles.
     * @throws java.io.IOException on error.
     */
    public boolean loadThrottlesLayout(java.io.File f) throws java.io.IOException {
        try {
            ThrottlePrefs prefs = new ThrottlePrefs();
            prefs.setValidate(XmlFile.Validate.CheckDtdThenSchema);
            Element root = prefs.rootFromFile(f);
            List<Element> throttles = root.getChildren("ThrottleFrame");
            ThrottleFrameManager tfManager = InstanceManager.getDefault(ThrottleFrameManager.class);
            if ((throttles != null) && (throttles.size() > 0)) { // OLD FORMAT
                for (Element e : throttles) {
                    SwingUtilities.invokeLater(() -> {
                        tfManager.createThrottleWindow(e).setVisible(true);                        
                    });
                }
            } else {
                throttles = root.getChildren("ThrottleWindow");
                for (Element e : throttles) {
                    SwingUtilities.invokeLater(() -> {
                        tfManager.createThrottleWindow(e).setVisible(true);
                    });
                }
                Element tlp = root.getChild("ThrottlesListPanel");
                if (tlp != null) {
                    InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesListPanel().setXml(tlp);
                }
            }
        } catch (org.jdom2.JDOMException ex) {
            log.warn("Loading Throttles exception", ex);
            jmri.configurexml.ConfigXmlManager.creationErrorEncountered(
                    null, "parsing file " + f.getName(),
                    "Parse error", null, null, ex);
            return false;
        }
        return true;
    }

    /**
     * An extension of the abstract XmlFile. No changes made to that class.
     *
     * @author glen
     */
    public static class ThrottlePrefs extends XmlFile {
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoadXmlThrottlesLayoutAction.class);

}

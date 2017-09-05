package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.jmrit.XmlFile;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load VSDecoder Profiles from XML
 *
 * Adapted from LoadXmlThrottleProfileAction by Glen Oberhauser (2004)
 *
 * @author Mark Underwood 2011
 */
public class LoadXmlVSDecoderAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public LoadXmlVSDecoderAction(String s) {
        super(s);
        // Pretty sure I don't need this
        // disable the ourselves if there is no throttle Manager
 /*
         if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
         setEnabled(false);
         }
         */
    }

    public LoadXmlVSDecoderAction() {
        this("Load VSDecoder Profile"); // Shouldn't this be in the resource bundle?
    }

    JFileChooser fileChooser;

    /**
     * The action is performed. Let the user choose the file to load from. Read
     * XML for each VSDecoder Profile.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (fileChooser == null) {
            fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setCurrentDirectory(new File(VSDecoderPane.getDefaultVSDecoderFolder()));
        }
        int retVal = fileChooser.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;
            // give up if no file selected
        }

        try {
            loadVSDecoderProfile(fileChooser.getSelectedFile());
        } catch (java.io.IOException e1) {
            log.warn("Exception while reading file", e1);
        }
    }

    /**
     * Parse the XML file and create ThrottleFrames. Returns true if throttle
     * loaded successfully.
     *
     * @param f The XML file containing throttles.
     */
    public boolean loadVSDecoderProfile(java.io.File f) throws java.io.IOException {
        try {
            VSDecoderPrefs prefs = new VSDecoderPrefs();
            Element root = prefs.rootFromFile(f);

            // WARNING: This may be out of sync with the Store... the root element is <VSDecoderConfig>
            // not sure, must investigate.  See what XmlFile.rootFromFile(f) does...
            List<Element> profiles = root.getChildren("VSDecoder");
            if ((profiles != null) && (profiles.size() > 0)) {
                // Create a new VSDecoder object for each Profile in the XML file.
                for (java.util.Iterator<Element> i = profiles.iterator(); i.hasNext();) {
                    Element e = i.next();
                    log.debug(e.toString());
                    //VSDecoder vsd = VSDecoderManager.instance().getVSDecoder(e.getAttribute("name").getValue(), f.getPath());
                }
            }

        } catch (org.jdom2.JDOMException ex) {
            log.warn("Loading VSDecoder Profile exception", ex);
            return false;
        }
        return true;
    }

    /**
     * An extension of the abstract XmlFile. No changes made to that class.
     *
     * @author glen
         */
    static class VSDecoderPrefs extends XmlFile {
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LoadXmlVSDecoderAction.class);

}

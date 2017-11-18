package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import jmri.InstanceManager;
import jmri.configurexml.StoreXmlConfigAction;
import jmri.jmrit.XmlFile;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save throttles to XML
 *
 * @author Glen Oberhauser
 * @author Daniel Boudreau (C) Copyright 2008
 */
public class StoreXmlThrottlesLayoutAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public StoreXmlThrottlesLayoutAction(String s) {
        super(s);
        // disable this ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public StoreXmlThrottlesLayoutAction() {
        this("Save default throttle layout...");
    }

    /**
     * The action is performed. Let the user choose the file to save to. Write
     * XML for each ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = jmri.jmrit.XmlFile.userFileChooser(Bundle.getMessage("PromptXmlFileTypes"), "xml");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(new File(ThrottleFrame.getDefaultThrottleFolder()));
        java.io.File file = StoreXmlConfigAction.getFileName(fileChooser);
        if (file == null) {
            return;
        }
        saveThrottlesLayout(file);
    }

    public void saveThrottlesLayout(java.io.File f) {

        try {
            Element root = new Element("throttle-layout-config");
            Document doc = XmlFile.newDocument(root, XmlFile.getDefaultDtdLocation() + "throttle-layout-config.dtd");

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/throttle-layout-config.xsl"?>
/*TODO   java.util.Map<String,String> m = new java.util.HashMap<String,String>();
             m.put("type", "text/xsl");
             m.put("href", jmri.jmrit.XmlFile.xsltLocation + "throttle-layout-config.xsl");
             ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
             doc.addContent(0, p); */
            java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(5);

            // throttle list window
            children.add(InstanceManager.getDefault(ThrottleFrameManager.class).getThrottlesListPanel().getXml());

            // throttle windows
            for (Iterator<ThrottleWindow> i = InstanceManager.getDefault(ThrottleFrameManager.class).getThrottleWindows(); i.hasNext();) {
                ThrottleWindow tw = i.next();
                Element throttleElement = tw.getXml();
                children.add(throttleElement);
            }
            root.setContent(children);

            FileOutputStream o = new java.io.FileOutputStream(f);
            try {
                XMLOutputter fmt = new XMLOutputter();
                fmt.setFormat(Format.getPrettyFormat()
                        .setLineSeparator(System.getProperty("line.separator"))
                        .setTextMode(Format.TextMode.PRESERVE));
                fmt.output(doc, o);
            } catch (IOException ex) {
                log.warn("Exception in storing throttle xml: " + ex);
            } finally {
                o.close();
            }
        } catch (FileNotFoundException ex) {
            log.warn("Exception in storing throttle xml: " + ex);
        } catch (IOException ex) {
            log.warn("Exception in storing throttle xml: " + ex);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(StoreXmlThrottlesLayoutAction.class);

}

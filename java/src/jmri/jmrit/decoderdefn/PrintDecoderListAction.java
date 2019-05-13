package jmri.jmrit.decoderdefn;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.Version;
import jmri.util.FileUtil;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of available decoder definitions
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 */
public class PrintDecoderListAction extends AbstractAction {

    public PrintDecoderListAction(String actionName, Frame frame, boolean preview) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, "DecoderPro V" + Version.name() + " Decoder Definitions", 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(FileUtil.findURL("resources/decoderpro.gif", FileUtil.Location.INSTALLED));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));

        // Loop through the decoder index, printing as needed
        String lastMfg = "";
        String lastFamily = "";

        DecoderIndexFile f = InstanceManager.getDefault(DecoderIndexFile.class);
        List<DecoderFile> l = f.matchingDecoderList(null, null, null, null, null, null); // take all
        int i = -1;
        log.debug("Roster list size: " + l.size());
        for (i = 0; i < l.size(); i++) {
            DecoderFile d = l.get(i);
            if (!d.getMfg().equals(lastMfg)) {
                printMfg(d, writer);
                lastMfg = d.getMfg();
                lastFamily = "";
            }
            if (!d.getFamily().equals(lastFamily)) {
                printFamily(d, writer);
                lastFamily = d.getFamily();
            }
            if (!d.getFamily().equals(d.getModel())) {
                printEntry(d, writer);
            }
        }

        // and force completion of the printing
        writer.close();
    }

    void printEntry(DecoderFile d, HardcopyWriter w) {
        try {
            String s = "\n                       " + d.getModel();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: " + e);
        }
    }

    void printMfg(DecoderFile d, HardcopyWriter w) {
        try {
            String s = "\n\n" + d.getMfg();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: " + e);
        }
    }

    void printFamily(DecoderFile d, HardcopyWriter w) {
        try {
            String s = "\n           " + d.getFamily();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: " + e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PrintDecoderListAction.class);
}

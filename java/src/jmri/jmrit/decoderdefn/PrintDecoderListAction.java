// PrintDecoderListAction.java

package jmri.jmrit.decoderdefn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.List;


/**
 * Action to print a summary of available decoder definitions
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @version     $Revision$
 */
public class PrintDecoderListAction  extends AbstractAction {


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
    

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, "DecoderPro V"+Version.name()+" Decoder Definitions", 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));

        // Loop through the decoder index, printing as needed
        String lastMfg = "";
        String lastFamily = "";

        DecoderIndexFile f = DecoderIndexFile.instance();
        List<DecoderFile> l = f.matchingDecoderList(null, null, null, null, null, null); // take all
        int i=-1;
        log.debug("Roster list size: "+l.size());
        for (i = 0; i<l.size(); i++) {
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
            if (!d.getFamily().equals(d.getModel())) printEntry(d,writer);
        }

        // and force completion of the printing
        writer.close();
    }

    void printEntry(DecoderFile d, HardcopyWriter w) {
        try {
            String s ="\n                       "+d.getModel();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: "+e);
        }
    }

    void printMfg(DecoderFile d, HardcopyWriter w) {
        try {
            String s ="\n\n"+d.getMfg();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: "+e);
        }
    }

    void printFamily(DecoderFile d, HardcopyWriter w) {
        try {
            String s ="\n           "+d.getFamily();
            w.write(s, 0, s.length());
        } catch (java.io.IOException e) {
            log.error("Error printing: "+e);
        }
    }

    static Logger log = LoggerFactory.getLogger(PrintDecoderListAction.class.getName());
}

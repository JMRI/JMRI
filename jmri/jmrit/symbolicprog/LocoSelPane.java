// LocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.*;
import java.io.*;

/**
 * Base class for GUI to select a locomotive (or create a new one in some cases)
 * <P>
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.2 $
 */
public class LocoSelPane extends javax.swing.JPanel {


    static public String[] findListOfProgFiles() {
        // create an array of file names from prefs/programmers, count entries
        int i;
        int np = 0;
        String[] sp = null;
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+"programmers");
        File fp = new File(XmlFile.prefsDir()+"programmers");
        if (fp.exists()) {
            sp = fp.list();
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml")) np++;
            }
        } else {
            log.warn(XmlFile.prefsDir()+"programmers was missing, though tried to create it");
        }
        // create an array of file names from xml/programmers, count entries
        String[] sx = (new File(XmlFile.xmlDir()+"programmers")).list();
        int nx = 0;
        for (i=0; i<sx.length; i++) {
            if (sx[i].endsWith(".xml")) nx++;
        }
        // copy the programmer entries to the final array
        // note: this results in duplicate entries if the same name is also local.
        // But for now I can live with that.
        String sbox[] = new String[np+nx];
        int n=0;
        if (sp != null && np> 0)
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml")) sbox[n++] = sp[i].substring(0, sp[i].length()-1-3);
            }
        for (i=0; i<sx.length; i++) {
            if (sx[i].endsWith(".xml")) sbox[n++] = sx[i].substring(0, sx[i].length()-1-3);
        }
        return sbox;
    }
    static protected String defaultProgFile = null;
    static public void setDefaultProgFile(String s) { defaultProgFile = s; }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoSelPane.class.getName());

}

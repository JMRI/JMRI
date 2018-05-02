package jmri.jmrit.decoderdefn;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install decoder definition from URL
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @see jmri.jmrit.XmlFile
 */
public class InstallDecoderURLAction extends JmriAbstractAction {

    public InstallDecoderURLAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public InstallDecoderURLAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public InstallDecoderURLAction(String s) {
        super(s);
    }

    public InstallDecoderURLAction(String s, JPanel who) {
        super(s);
    }

    JPanel _who;

    URL pickURL(JPanel who) {
        // show input dialog
        String urlname = JOptionPane.showInputDialog(who, Bundle.getMessage("InputURL"));
        try {
            URL url = new URL(urlname);
            return url;
        } catch (java.net.MalformedURLException e) {
            JOptionPane.showMessageDialog(who, Bundle.getMessage("MalformedURL"));
        }

        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // get the input URL
        URL url = pickURL(_who);
        if (url == null) {
            return;
        }

        if (checkFile(url, _who)) {
            // OK, do the actual copy
            copyAndInstall(url, _who);
        }
    }

    void copyAndInstall(URL from, JPanel who) {
        log.debug("[" + from.getFile() + "]");

        // get output name
        File temp = new File(from.getFile());

        log.debug("[" + temp.toString() + "]");

        // ensure directories exist
        FileUtil.createDirectory(FileUtil.getUserFilesPath() + "decoders");

        // output file
        File toFile = new File(FileUtil.getUserFilesPath() + "decoders" + File.separator + temp.getName());
        log.debug("[" + toFile.toString() + "]");

        // first do the copy, but not if source and output files are the same
        if (!temp.toString().equals(toFile.toString())) {
            if (!copyfile(from, toFile, _who)) {
                return;
            }
        } else {
            // write a log entry
            log.info("Source and destination files identical - file not copied");
            log.info("  source file: " + temp.toString());
            log.info("  destination: " + toFile.toString());
        }

        // and rebuild index
        DecoderIndexFile.forceCreationOfNewIndex();

        // Done OK
        JOptionPane.showMessageDialog(who, Bundle.getMessage("CompleteOK"));
    }

    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION", justification = "Looks like false positive")
    boolean copyfile(URL from, File toFile, JPanel who) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = from.openConnection().getInputStream();

            // open for overwrite
            out = new FileOutputStream(toFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // done - finally cleans up
        } catch (FileNotFoundException ex) {
            log.debug("" + ex);
            JOptionPane.showMessageDialog(who, Bundle.getMessage("CopyError1"));
            return false;
        } catch (IOException e) {
            log.debug("" + e);
            JOptionPane.showMessageDialog(who, Bundle.getMessage("CopyError2"));
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e1) {
                log.error("exception closing in stream", e1);
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e2) {
                log.error("exception closing out stream", e2);
            }
        }

        return true;
    }

    boolean checkFile(URL url, JPanel who) {
        // read the definition to check it (later should be outside this thread?)
        try {
            Element root = readFile(url);
            if (log.isDebugEnabled()) {
                log.debug("parsing complete");
            }

            // check to see if there's a decoder element
            if (root.getChild("decoder") == null) {
                JOptionPane.showMessageDialog(who, Bundle.getMessage("WrongContent"));
                return false;
            }
            return true;

        } catch (java.io.IOException | org.jdom2.JDOMException ex) {
            log.debug("" + ex);
            JOptionPane.showMessageDialog(who, Bundle.getMessage("ParseError"));
            return false;
        }
    }

    /**
     * Read and verify an XML file.
     *
     * @param url the URL of the file
     * @return the root element in the file
     * @throws org.jdom2.JDOMException if the file cannot be parsed
     * @throws java.io.IOException     if the file cannot be read
     */
    Element readFile(URL url) throws org.jdom2.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile() {
        };   // odd syntax is due to XmlFile being abstract

        return xf.rootFromURL(url);

    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(InstallDecoderURLAction.class);
}

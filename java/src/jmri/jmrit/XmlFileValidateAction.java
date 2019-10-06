package jmri.jmrit;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make sure an XML file is readable, and validates OK against its schema and DTD.
 * <p>
 * Can also be run from the command line with e.g. ./runtest.csh
 * jmri/jmrit/XmlFileValidateAction foo.xml in which case if there's a filename
 * argument, it checks that directly, otherwise it pops a file selection dialog.
 * (The dialog form has to be manually canceled when done)
 *
 * @author Bob Jacobsen Copyright (C) 2005, 2007
 * @see jmri.jmrit.XmlFile
 * @see jmri.jmrit.XmlFileCheckAction
 */
public class XmlFileValidateAction extends jmri.util.swing.JmriAbstractAction {

    public XmlFileValidateAction(String s, Component who) {
        super(s);
        _who = who;
    }

    public XmlFileValidateAction(String s, WindowInterface wi) {
        this(s, wi != null ? wi.getFrame() : null);
    }

    public XmlFileValidateAction() {
        super(Bundle.getMessage("XmlFileValidateAction")); // NOI18N
    }

    private JFileChooser fci;

    private Component _who;

    private XmlFile xmlfile = new XmlFile() {};   // odd syntax is due to XmlFile being abstract
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (fci == null) {
            fci = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");
        }
        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(_who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            processFile(fci.getSelectedFile());
        } else {
            log.debug("XmlFileValidateAction cancelled in open dialog");
        }
    }

    protected void processFile(File file) {
        log.debug("located file \"{}\" for XML processing", file);
        // handle the file (TODO should be outside this thread?)
        try {
            xmlfile.setValidate(XmlFile.Validate.CheckDtdThenSchema);
            readFile(file);
        } catch (Exception ex) {
            showFailResults(_who, file.getName(), ex.getMessage());
            return;
        }
        showOkResults(_who, Bundle.getMessage("ValidatedOk", file.getName()));
        log.debug("parsing xml complete");
    }

    protected void showOkResults(Component who, String text) {
        JOptionPane.showMessageDialog(who, text);
    }

    protected void showFailResults(Component who, String fileName, String text) {
        final String html = "<html><body style='width: %1spx'>%1s<br><br>%1s</body></html>"; // reflow in dialog
        final int dialogWidth = 300;
        JOptionPane.showMessageDialog(who, String.format(html, dialogWidth,
                Bundle.getMessage("ValidationErrorInFile", fileName), text)); // html markup
    }

    /**
     * Read and verify a file is schema valid XML.
     *
     * @param file the file to read
     * @throws org.jdom2.JDOMException if file is not schema valid XML
     * @throws java.io.IOException     if unable to read file
     */
    void readFile(File file) throws org.jdom2.JDOMException, java.io.IOException {
        xmlfile.rootFromFile(file);

    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // Main entry point fires the action
    static public void main(String[] args) {
        // if a 1st argument provided, act
        if (args.length == 0) {
            new XmlFileValidateAction("", (Component) null).actionPerformed(null);
        } else {
            jmri.util.Log4JUtil.initLogging("default.lcf");
            new XmlFileValidateAction("", (Component) null) {
                @Override
                protected void showFailResults(Component who, String fileName, String text) {
                    log.error("{}: {}", Bundle.getMessage("ValidationErrorInFile", fileName), text);
                }

                @Override
                protected void showOkResults(Component who, String text) {
                    // silent if OK
                }
            }.processFile(new File(args[0]));
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XmlFileValidateAction.class);

}

package apps.jmrit;

import jmri.jmrit.*;

import java.awt.Component;
import java.io.File;

import jmri.util.swing.WindowInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make sure an XML file is readable, and validates OK against its schema and
 * DTD.
 * <p>
 * Intended to be run from the command line with e.g. ./runtest.csh
 * jmri/jmrit/XmlFileValidateRunner foo.xml in which case if there's a filename
 * argument, it checks that directly, otherwise it pops a file selection dialog.
 * (The dialog form has to be manually canceled when done)
 *
 * @author Bob Jacobsen Copyright (C) 2005, 2007
 * @see jmri.jmrit.XmlFile
 * @see jmri.jmrit.XmlFileCheckAction
 */
public class XmlFileValidateRunner extends jmri.jmrit.XmlFileValidateAction {

    private XmlFileValidateRunner(String s, Component who) {
        super(s, who);
    }

    // package protected for testing
    XmlFileValidateRunner() {
        super();
    }

    // Main entry point fires the action
    static public void main(String[] args) {
        // if a 1st argument provided, act
        if (args.length == 0) {
            new XmlFileValidateRunner("", (Component) null).actionPerformed(null);
        } else {
            apps.util.Log4JUtil.initLogging("default.lcf");
            new XmlFileValidateRunner("", (Component) null) {
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
    private final static Logger log = LoggerFactory.getLogger(XmlFileValidateRunner.class);

}

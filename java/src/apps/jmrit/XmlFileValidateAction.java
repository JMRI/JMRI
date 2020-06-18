package apps.jmrit;

import jmri.jmrit.*;

import java.awt.Component;
import java.io.File;

import jmri.util.swing.WindowInterface;

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
public class XmlFileValidateAction extends jmri.jmrit.XmlFileValidateAction {

    private XmlFileValidateAction(String s, Component who) {
        super(s, who);
    }

    private XmlFileValidateAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    // package protected for testing
    XmlFileValidateAction() {
        super();
    }

    // Main entry point fires the action
    static public void main(String[] args) {
        // if a 1st argument provided, act
        if (args.length == 0) {
            new XmlFileValidateAction("", (Component) null).actionPerformed(null);
        } else {
            apps.util.Log4JUtil.initLogging("default.lcf");
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

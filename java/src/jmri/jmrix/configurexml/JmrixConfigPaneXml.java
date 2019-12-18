package jmri.jmrix.configurexml;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.JmrixConfigPane;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of layout connections.
 * <p>
 * This class is named as being the persistant form of the JmrixConfigPane
 * class, but there's no object of that form created or used. Instead, this
 * interacts forwards to a similar class in one of the protocol-specific
 * packages, e.g. jmrix.easydcc.serialdriver.configurexml
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class JmrixConfigPaneXml extends AbstractXmlAdapter {

    public JmrixConfigPaneXml() {
    }

    /**
     * Forward to the configurexml class for the specific object type.
     */
    @Override
    public Element store(Object o) {
        ConnectionConfig oprime = ((JmrixConfigPane) o).getCurrentObject();
        if (oprime == null) {
            return null;
        }
        String adapter = ConfigXmlManager.adapterName(oprime);
        log.debug("forward to " + adapter);
        try {
            XmlAdapter x = (XmlAdapter) Class.forName(adapter).getDeclaredConstructor().newInstance();
            return x.store(oprime);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            log.error("Exception: ", e);
            return null;
        }
    }

    @Override
    public Element store(Object o, boolean shared) {
        return this.store(o);
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        java.util.Hashtable<String, String> installedLAFs = new java.util.Hashtable<String, String>(plafs.length);
        for (UIManager.LookAndFeelInfo plaf : plafs) {
            installedLAFs.put(plaf.getName(), plaf.getClassName());
        }
        String name = shared.getAttribute("LAFclass").getValue();
        String className = installedLAFs.get(name);
        log.debug("GUI selection: " + name + " class name: " + className);
        // set the GUI
        if (className != null) {
            try {
                if (!className.equals(UIManager.getLookAndFeel().getClass().getName())) {
                    log.debug("set GUI to " + name + "," + className);
                    updateLookAndFeel(name, className);
                } else {
                    log.debug("skip updateLAF as already has className==" + className);
                }
            } catch (Exception ex) {
                log.error("Exception while setting GUI look & feel: " + ex);
                result = false;
            }
        }
        return result;
    }

    /**
     * Change the look-and-feel to the specified class. Alert the user if there
     * were problems loading the PLAF.
     *
     * @param name      (String) the presentable name for the class
     * @param className (String) the className to be fed to the UIManager
     */
    public void updateLookAndFeel(String name, String className) {
        try {
            // Set the new look and feel, and update the sample message to reflect it.
            UIManager.setLookAndFeel(className);
        } catch (Exception e) {
            String errMsg = "The " + name + " look-and-feel ";
            if (e instanceof UnsupportedLookAndFeelException) {
                errMsg += "is not supported on this platform.";
            } else if (e instanceof ClassNotFoundException) {
                errMsg += "could not be found.";
            } else {
                errMsg += "could not be loaded.";
            }

            log.error(errMsg);

        }
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        jmri.jmrit.symbolicprog.ProgDefault.setDefaultProgFile(element.getAttribute("defaultFile").getValue());
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JmrixConfigPaneXml.class);

}

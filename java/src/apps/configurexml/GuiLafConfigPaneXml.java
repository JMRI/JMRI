package apps.configurexml;

import apps.GuiLafConfigPane;
import apps.gui.GuiLafPreferencesManager;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of SwingGuiLaf default values.
 * <p>
 * This class is named as being the persistant form of the GuiLafConfigPane
 * class, but there's no object of that form created when this is read back.
 * Instead, this interacts directly with Swing and the default Locale.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2010
 * @see apps.GuiLafConfigPane
 * @since 2.9.5
 */
public class GuiLafConfigPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public GuiLafConfigPaneXml() {
    }

    /**
     * Default implementation for storing the static contents of the Swing LAF
     *
     * @param o Object to store, of type GuiLafConfigPane
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element e = new Element("gui");
        GuiLafConfigPane g = (GuiLafConfigPane) o;
        String lafClassName = g.getClassName();

        e.setAttribute("LAFclass", lafClassName);
        e.setAttribute("class", this.getClass().getName());

        Locale l = g.getLocale();
        e.setAttribute("LocaleLanguage", l.getLanguage());
        e.setAttribute("LocaleCountry", l.getCountry());
        e.setAttribute("LocaleVariant", l.getVariant());

        GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
        if (manager.getFontSize() != manager.getDefaultFontSize()) {
            e.setAttribute("fontsize", Integer.toString(manager.getFontSize()));
        }
        e.setAttribute("nonStandardMouseEvent",
                (g.mouseEvent.isSelected() ? "yes" : "no"));
        e.setAttribute("graphicTableState",
                (g.graphicStateDisplay.isSelected() ? "yes" : "no"));
        return e;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        java.util.Hashtable<String, String> installedLAFs = new java.util.Hashtable<String, String>(plafs.length);
        for (int i = 0; i < plafs.length; i++) {
            installedLAFs.put(plafs[i].getName(), plafs[i].getClassName());
        }
        String name = shared.getAttribute("LAFclass").getValue();
        String className = installedLAFs.get(name);
        log.debug("GUI selection: " + name + " class name: " + className);
        // set the GUI
        if (className != null) {
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setLookAndFeel(name);
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
        Attribute langAttr = shared.getAttribute("LocaleLanguage");
        Attribute countryAttr = shared.getAttribute("LocaleCountry");
        Attribute varAttr = shared.getAttribute("LocaleVariant");
        if (countryAttr != null && langAttr != null && varAttr != null) {
            Locale locale = new Locale(langAttr.getValue(), countryAttr.getValue(), varAttr.getValue());
            
            log.debug("About to setDefault Locale", new Exception(""));
            Locale.setDefault(locale);
            javax.swing.JComponent.setDefaultLocale(locale);
            javax.swing.JOptionPane.setDefaultLocale(locale);
            
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setLocale(locale);
        }
        Attribute clickAttr = shared.getAttribute("nonStandardMouseEvent");
        if (clickAttr != null) {
            boolean nonStandardMouseEvent = clickAttr.getValue().equals("yes");
            jmri.util.swing.SwingSettings.setNonStandardMouseEvent(nonStandardMouseEvent);
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setNonStandardMouseEvent(nonStandardMouseEvent);
        }
        Attribute graphicAttr = shared.getAttribute("graphicTableState");
        if (graphicAttr != null) {
            boolean graphicTableState = graphicAttr.getValue().equals("yes");
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setGraphicTableState(graphicTableState);
        }
        GuiLafConfigPane g = new GuiLafConfigPane();
        ConfigureManager cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerPref(g);
        }

        Attribute fontsize = shared.getAttribute("fontsize");
        if (fontsize != null) {
            int size = Integer.parseInt(fontsize.getValue());
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setFontSize(size);
            this.setUIFontSize(size);
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
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(GuiLafConfigPaneXml.class);

    public void setUIFontSize(float size) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        Font f;
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                f = UIManager.getFont(key).deriveFont(((Font) value).getStyle(), size);
                UIManager.put(key, f);
            }
        }
    }

    public void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

}

package apps.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import apps.GuiLafConfigPane;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML persistance of SwingGuiLaf default values.
 * <P>
 * This class is named as being the persistant form of the
 * GuiLafConfigPane class, but there's no object of that
 * form created when this is read back.  Instead, this interacts directly with Swing
 * and the default Locale.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2010
 * @version $Revision$
 * @see jmri.GuiLafConfigPane
 * @since 2.9.5
 */
public class GuiLafConfigPaneXml extends jmri.configurexml.AbstractXmlAdapter {

    public GuiLafConfigPaneXml() {
    }

    /**
     * Default implementation for storing the static contents of the Swing LAF
     * @param o Object to store, of type GuiLafConfigPane
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("gui");
        GuiLafConfigPane g = (GuiLafConfigPane) o;
        String lafClassName = g.getClassName();

        e.setAttribute("LAFclass", lafClassName);
        e.setAttribute("class", this.getClass().getName());

        Locale l = g.getLocale();
        e.setAttribute("LocaleLanguage",l.getLanguage());
        e.setAttribute("LocaleCountry",l.getCountry());
        e.setAttribute("LocaleVariant",l.getVariant());
        
        if (GuiLafConfigPane.getFontSize() != 0)
        	e.setAttribute("fontsize", Integer.toString(GuiLafConfigPane.getFontSize()));
        
        e.setAttribute("nonStandardMouseEvent", 
                (g.mouseEvent.isSelected() ?"yes":"no"));
        return e;
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        java.util.Hashtable<String,String> installedLAFs = new java.util.Hashtable<String,String>(plafs.length);
        for (int i = 0; i < plafs.length; i++){
            installedLAFs.put(plafs[i].getName(), plafs[i].getClassName());
        }
        String name = e.getAttribute("LAFclass").getValue();
        String className = installedLAFs.get(name);
        log.debug("GUI selection: "+name+" class name: "+className);
        // set the GUI
        if (className != null) {
            try {
                if (!className.equals(UIManager.getLookAndFeel().getClass().getName())) {
                    log.debug("set GUI to "+name+","+className);
                    updateLookAndFeel(name, className);
                } else
                    log.debug("skip updateLAF as already has className=="+className);
            } catch (Exception ex) {
                log.error("Exception while setting GUI look & feel: "+ex);
                result = false;
            }
        }
        Attribute langAttr = e.getAttribute("LocaleLanguage");
        Attribute countryAttr = e.getAttribute("LocaleCountry");
        Attribute varAttr = e.getAttribute("LocaleVariant");
        if (countryAttr!=null && langAttr!=null && varAttr!=null)
            Locale.setDefault(new Locale(langAttr.getValue(),countryAttr.getValue(),
                                varAttr.getValue()));

        Attribute clickAttr = e.getAttribute("nonStandardMouseEvent");
        if (clickAttr != null)
            jmri.util.swing.SwingSettings.setNonStandardMouseEvent(clickAttr.getValue().equals("yes"));
        GuiLafConfigPane g = new GuiLafConfigPane();
        jmri.InstanceManager.configureManagerInstance().registerPref(g);
        
        Attribute fontsize = e.getAttribute("fontsize");
        if (fontsize != null){
        	int size = Integer.parseInt(fontsize.getValue());
        	GuiLafConfigPane.setFontSize(size);
           	jmri.InstanceManager.tabbedPreferencesInstance().setUIFontSize(size);
        }
        return result;
    }

    /**
     *  Change the look-and-feel to the specified class.
     *  Alert the user if there were problems loading the PLAF.
     *  @param name (String) the presentable name for the class
     *  @param className (String) the className to be fed to the UIManager
     */
    public void updateLookAndFeel(String name, String className) {
	try {
            // Set the new look and feel, and update the sample message to reflect it.
            UIManager.setLookAndFeel(className);
        } catch (Exception e) {
            String errMsg = "The " + name + " look-and-feel ";
            if (e instanceof UnsupportedLookAndFeelException){
                errMsg += "is not supported on this platform.";
            } else if (e instanceof ClassNotFoundException){
                errMsg += "could not be found.";
            } else {
                errMsg += "could not be loaded.";
            }

            log.error(errMsg);

        }
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(GuiLafConfigPaneXml.class.getName());

}

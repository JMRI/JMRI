package jmri.jmrix.configurexml;

import jmri.configurexml.*;
import jmri.jmrix.PortAdapter;

import org.jdom.Element;
import java.util.List;

/**
 * Abstract base (and partial implementation) for
 * classes persisting the status of serial port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
abstract public class AbstractConnectionConfigXml extends AbstractXmlAdapter {

    public AbstractConnectionConfigXml() {
    }

    static java.util.ResourceBundle rb = 
        java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");
    
    abstract protected void getInstance();
    abstract protected void register();

    /**
     * Default implementation for storing the static contents of the serial port implementation
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    abstract public Element store(Object o);
    
    protected void storeCommon(Element e,  PortAdapter adapter){
        if (adapter.getSystemConnectionMemo()!=null){
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getManufacturer()!=null)
            e.setAttribute("manufacturer", adapter.getManufacturer());
            
        if (adapter.getDisabled())
            e.setAttribute("disabled", "yes");
        else e.setAttribute("disabled", "no");
        saveOptions(e, adapter);
    }


    /**
     * Customizable method if you need to add anything more
     * @param e Element being created, update as needed
     */
    protected void extendElement(Element e) {}

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    abstract public boolean load(Element e) throws Exception;
    
    protected void loadCommon(Element e, PortAdapter adapter){
        if (e.getAttribute("option1")!=null) {
            String option1Setting = e.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        if (e.getAttribute("option2")!=null) {
            String option2Setting = e.getAttribute("option2").getValue();
            adapter.configureOption2(option2Setting);
        }
        if (e.getAttribute("option3")!=null) {
            String option3Setting = e.getAttribute("option3").getValue();
            adapter.configureOption3(option3Setting);
        }
        if (e.getAttribute("option4")!=null) {
            String option4Setting = e.getAttribute("option4").getValue();
            adapter.configureOption4(option4Setting);
        }
        
        loadOptions(e.getChild("options"), adapter);
        
        try { 
            adapter.setManufacturer(e.getAttribute("manufacturer").getValue());
        } catch ( NullPointerException ex) { //Considered normal if not present
            
        }

        if (adapter.getSystemConnectionMemo()!=null){
            if (e.getAttribute("userName")!=null){
                adapter.getSystemConnectionMemo().setUserName(e.getAttribute("userName").getValue());
            }

            if (e.getAttribute("systemPrefix")!=null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(e.getAttribute("systemPrefix").getValue());
            }
        }
        
        if (e.getAttribute("disabled")!=null) {
            String yesno = e.getAttribute("disabled").getValue();
            if ( (yesno!=null) && (!yesno.equals("")) ) {
                if (yesno.equals("no")) adapter.setDisabled(false);
                else if (yesno.equals("yes")) adapter.setDisabled(true);
            }
        }
    
    }

    protected void saveOptions(Element e, PortAdapter adapter){
        Element element = new Element("options");
        String[] options = adapter.getOptions();

        for(String i:options){
            Element elem = new Element("option");
            elem.addContent(new Element("name").addContent(i));
            elem.addContent(new Element("value").addContent(adapter.getOptionState(i)));
            element.addContent(elem);
        }
        e.addContent(element);
    }
    
    protected void loadOptions(Element e, PortAdapter adapter){
        if(e==null)
            return;
        @SuppressWarnings("unchecked")
        List<Element> optionList = e.getChildren("option");
        for (Element so : optionList) {
            adapter.setOptionState(so.getChild("name").getText(), so.getChild("value").getText());
        }
    }
    /**
     * Customizable method if you need to add anything more
     * @param e Element being created, update as needed
     */
    protected void unpackElement(Element e) {}

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }


    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractConnectionConfigXml.class.getName());

}
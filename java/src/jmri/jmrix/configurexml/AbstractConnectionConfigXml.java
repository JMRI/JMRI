package jmri.jmrix.configurexml;

import java.util.List;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.PortAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of serial port adapters.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
abstract public class AbstractConnectionConfigXml extends AbstractXmlAdapter {

    public AbstractConnectionConfigXml() {
    }

    /**
     * get instance
     */
    abstract protected void getInstance();

    abstract protected void register();

    protected void register(ConnectionConfig c) {
        c.register();
    }

    @Override
    public Element store(Object o, boolean shared) {
        return this.store(o);
    }

    protected void storeCommon(Element e, PortAdapter adapter) {
        if (adapter.getSystemConnectionMemo() != null) {
            e.setAttribute("userName", adapter.getSystemConnectionMemo().getUserName());
            e.setAttribute("systemPrefix", adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (adapter.getManufacturer() != null) {
            e.setAttribute("manufacturer", adapter.getManufacturer());
        }

        if (adapter.getDisabled()) {
            e.setAttribute("disabled", "yes");
        } else {
            e.setAttribute("disabled", "no");
        }
        saveOptions(e, adapter);
    }

    /**
     * Customizable method if you need to add anything more
     *
     * @param e Element being created, update as needed
     */
    protected void extendElement(Element e) {
    }

    /**
     * load common attributes and elements
     *
     * @param shared  the shared element
     * @param perNode the per node element
     * @param adapter the port adapter
     */
    protected void loadCommon(Element shared, Element perNode, PortAdapter adapter) {
        if (perNode.getAttribute("option1") != null) {
            String option1Setting = perNode.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        if (perNode.getAttribute("option2") != null) {
            String option2Setting = perNode.getAttribute("option2").getValue();
            adapter.configureOption2(option2Setting);
        }
        if (perNode.getAttribute("option3") != null) {
            String option3Setting = perNode.getAttribute("option3").getValue();
            adapter.configureOption3(option3Setting);
        }
        if (perNode.getAttribute("option4") != null) {
            String option4Setting = perNode.getAttribute("option4").getValue();
            adapter.configureOption4(option4Setting);
        }

        loadOptions(perNode.getChild("options"), perNode.getChild("options"), adapter);

        try {
            adapter.setManufacturer(perNode.getAttribute("manufacturer").getValue());
        } catch (NullPointerException ex) { //Considered normal if not present

        }

        if (adapter.getSystemConnectionMemo() != null) {
            if (shared.getAttribute("userName") != null) {
                adapter.getSystemConnectionMemo().setUserName(shared.getAttribute("userName").getValue());
            }

            if (shared.getAttribute("systemPrefix") != null) {
                adapter.getSystemConnectionMemo().setSystemPrefix(shared.getAttribute("systemPrefix").getValue());
                checkAndWarnPrefix(shared.getAttribute("systemPrefix").getValue()); // for removal after #4670 resolved
            }
        }

        if (shared.getAttribute("disabled") != null) {
            String yesno = shared.getAttribute("disabled").getValue();
            if ((yesno != null) && (!yesno.isEmpty())) {
                if (yesno.equals("no")) {
                    adapter.setDisabled(false);
                } else if (yesno.equals("yes")) {
                    adapter.setDisabled(true);
                }
            }
        }
    }
    
    
    /** 
     * Check for a deprecated system prefix and warn if found
     * @deprecated 4.15.3  part of #4670 migration to parsable prefixes
     */
    @Deprecated // part of #4670 migration to parsable prefixes
    protected void checkAndWarnPrefix(String prefix) {
        if (prefix.length() == 1 && ! org.apache.commons.lang3.StringUtils.isNumeric(prefix) ) return;
        if (prefix.length() > 1 
                && ! org.apache.commons.lang3.StringUtils.isNumeric(prefix.substring(0,1)) 
                && org.apache.commons.lang3.StringUtils.isNumeric(prefix.substring(1)) ) return;
        
        // No longer checking jmri.Manager.isLegacySystemPrefix(prefix)) as this is more rigorous
            
            
        // unparsable, so warn
        log.warn("Connection is using a prefix that needs to be migrated: {}", prefix);
        log.warn("See http://jmri.org/help/en/html/setup/MigrateSystemPrefixes.shtml for more information");
        
        // and show clickable dialog
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            javax.swing.JLabel message = new javax.swing.JLabel("<html><body>You have a connection with prefix \""
                                                        +prefix+"\" that needs to migrated.<br>"
                                                        +"See <a href=\"http://jmri.org/help/en/html/setup/MigrateSystemPrefixes.shtml\">"
                                                            +"http://jmri.org/help/en/html/setup/MigrateSystemPrefixes.shtml</a>"
                                                        +"<br>for more information.</body></html>"                 
                );
            message.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            message.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI("http://jmri.org/help/en/html/setup/MigrateSystemPrefixes.shtml"));
                    } catch (java.net.URISyntaxException | java.io.IOException ex) {
                        log.error("couldn't open JMRI web site", ex);
                    }
                }
             });
            javax.swing.JOptionPane.showMessageDialog(null, message, "Migration Required", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * save options
     *
     * @param e       the element
     * @param adapter the port adapter
     */
    protected void saveOptions(Element e, PortAdapter adapter) {
        Element element = new Element("options");
        String[] options = adapter.getOptions();

        for (String i : options) {
            Element elem = new Element("option");
            elem.addContent(new Element("name").addContent(i));
            elem.addContent(new Element("value").addContent(adapter.getOptionState(i)));
            element.addContent(elem);
        }
        e.addContent(element);
    }

    /**
     * load options
     *
     * @param shared  the shared element
     * @param perNode the per node element
     * @param adapter the port adapter
     */
    protected void loadOptions(Element shared, Element perNode, PortAdapter adapter) {
        if (perNode == null) {
            return;
        }
        List<Element> optionList = perNode.getChildren("option");
        for (Element so : optionList) {
            adapter.setOptionState(so.getChild("name").getText(), so.getChild("value").getText());
        }
    }

    /**
     * Method to unpack additional XML structures after connection creation, but
     * before connection is usable.
     *
     * @param shared  connection information common to all nodes
     * @param perNode connection information unique to this node
     */
    protected void unpackElement(Element shared, Element perNode) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }

    /**
     * Service routine to look through "parameter" child elements to find a
     * particular parameter value
     *
     * @param e    Element containing parameters
     * @param name name of desired parameter
     * @return String value
     */
    protected String findParmValue(Element e, String name) {
        List<Element> l = e.getChildren("parameter");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            if (n.getAttributeValue("name").equals(name)) {
                return n.getTextTrim();
            }
        }
        return null;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractConnectionConfigXml.class);

}

package jmri.jmrix.configurexml;

import java.util.List;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.PortAdapter;
import org.jdom2.Element;

/**
 * Abstract base (and partial implementation) for classes persisting the status
 * of connections.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
abstract public class AbstractConnectionConfigXml extends AbstractXmlAdapter {

    public AbstractConnectionConfigXml() {
    }

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

        e.setAttribute("reconnectMaxInterval",String.valueOf(adapter.getReconnectMaxInterval()));
        e.setAttribute("reconnectMaxAttempts",String.valueOf(adapter.getReconnectMaxAttempts()));

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
     * Load common attributes and elements.
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

        if (shared.getAttribute("reconnectMaxInterval") != null) {
            String reconnectI = shared.getAttribute("reconnectMaxInterval").getValue();
            if ((reconnectI!=null) && (!reconnectI.isEmpty() )) {
                adapter.setReconnectMaxInterval(Integer.parseInt(reconnectI));
            }
        }

        if (shared.getAttribute("reconnectMaxAttempts") != null) {
            String reconnectA = shared.getAttribute("reconnectMaxAttempts").getValue();
            if ((reconnectA!=null) && (!reconnectA.isEmpty() )) {
                adapter.setReconnectMaxAttempts(Integer.parseInt(reconnectA));
            }
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
     * Service routine to look through "parameter" child elements to find a
     * particular parameter value
     *
     * @param e    Element containing parameters
     * @param name name of desired parameter
     * @return String value
     */
    protected String findParmValue(Element e, String name) {
        List<Element> l = e.getChildren("parameter");
        for (Element n : l) {
            if (n.getAttributeValue("name").equals(name)) {
                return n.getTextTrim();
            }
        }
        return null;
    }

    /**
     * Store the outputInterval in a connection element for persistence.
     *
     * @param adapter the adapter for which properties are stored
     * @param e the "connection" element being filled
     */
    protected void setOutputInterval(PortAdapter adapter, Element e) {
        if (adapter.getSystemConnectionMemo().getOutputInterval() > 0) {
            e.setAttribute("turnoutInterval", String.valueOf(adapter.getSystemConnectionMemo().getOutputInterval()));
        } else {
            e.setAttribute("turnoutInterval", "0");
        }
    }

    // initialize logging
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractConnectionConfigXml.class);

}

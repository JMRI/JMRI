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
 * @version $Revision$
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
     *
     * @param e
     * @param adapter
     * @deprecated use {@link #loadCommon(org.jdom2.Element, org.jdom2.Element, jmri.jmrix.PortAdapter)
     * }
     */
    @Deprecated
    protected void loadCommon(Element e, PortAdapter adapter) {
        this.loadCommon(e, null, adapter);
    }

    protected void loadCommon(Element shared, Element perNode, PortAdapter adapter) {
        if (shared.getAttribute("option1") != null) {
            String option1Setting = shared.getAttribute("option1").getValue();
            adapter.configureOption1(option1Setting);
        }
        if (shared.getAttribute("option2") != null) {
            String option2Setting = shared.getAttribute("option2").getValue();
            adapter.configureOption2(option2Setting);
        }
        if (shared.getAttribute("option3") != null) {
            String option3Setting = shared.getAttribute("option3").getValue();
            adapter.configureOption3(option3Setting);
        }
        if (shared.getAttribute("option4") != null) {
            String option4Setting = shared.getAttribute("option4").getValue();
            adapter.configureOption4(option4Setting);
        }

        loadOptions(shared.getChild("options"), adapter);

        try {
            adapter.setManufacturer(shared.getAttribute("manufacturer").getValue());
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

    }

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
     *
     * @param e
     * @param adapter
     * @deprecated Use {@link #loadOptions(org.jdom2.Element, org.jdom2.Element, jmri.jmrix.PortAdapter)
     * }
     */
    @Deprecated
    protected void loadOptions(Element e, PortAdapter adapter) {
        this.loadOptions(e, null, adapter);
    }

    protected void loadOptions(Element shared, Element perNode, PortAdapter adapter) {
        if (shared == null) {
            return;
        }
        List<Element> optionList = shared.getChildren("option");
        for (Element so : optionList) {
            adapter.setOptionState(so.getChild("name").getText(), so.getChild("value").getText());
        }
    }

    /**
     * Customizable method if you need to add anything more
     *
     * @param e Element being created, update as needed
     */
    protected void unpackElement(Element e) {
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       An object to update with data from element.
     */
    @Override
    public void load(Element element, Object o) {
        log.error("method with two args invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(AbstractConnectionConfigXml.class);

}

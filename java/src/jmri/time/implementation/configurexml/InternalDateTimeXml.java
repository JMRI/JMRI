package jmri.time.implementation.configurexml;

import java.util.Objects;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.time.TimeProvider;
import jmri.time.TimeProviderManager;
import jmri.time.configurexml.LocalDateTimeXml;
import jmri.time.implementation.InternalDateTime;
import jmri.time.rate.configurexml.ChangeableDoubleRateXml;

import org.jdom2.Element;

/**
 * Store and load an InternalDateTime.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class InternalDateTimeXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    @Override
    public Element store(Object o) {
        InternalDateTime p = (InternalDateTime) o;

        Element element = new Element("InternalDateTime");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(new Element("isRunning").addContent(p.isRunning() ? "yes" : "no"));
        ChangeableDoubleRateXml.store(p.getRate(), element);
        LocalDateTimeXml.store(p.getTime(), element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);

        TimeProvider tp = InstanceManager.getDefault(TimeProviderManager.class).getBySystemName(sys);

        if (tp == null) {
            tp = new InternalDateTime(sys, uname);
            InstanceManager.getDefault(TimeProviderManager.class).register(tp);
        }

        if (!Objects.equals(uname, tp.getUserName())) {
            tp.setUserName(uname);
        }

        loadCommon(tp, shared);

        if (tp instanceof InternalDateTime) {
            InternalDateTime idt = (InternalDateTime)tp;

            Element elem = shared.getChild("isRunning");
            if (elem != null) {
                if (elem.getTextTrim().equals("yes")) {
                    idt.start();
                } else {
                    idt.stop();
                }
            }

            elem = shared.getChild("rate");
            if (elem != null) {
                try  {
                    double rate = Double.parseDouble(elem.getTextTrim());
                    idt.setRate(rate);
                } catch (NumberFormatException e) {
                    log.error("Rate is not a double: {}", elem.getTextTrim());
                    return false;
                }
            }

            if (! LocalDateTimeXml.load(shared, idt)) {
                return false;
            }

            return true;
        } else {
            log.error("The loaded time provider is not an InternalDateTime: {}",
                    tp.getClass().getName());
            return false;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalDateTimeXml.class);
}

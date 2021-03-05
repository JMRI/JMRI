package jmri.managers.configurexml;

import java.util.List;
import java.util.SortedSet;
import jmri.InstanceManager;
import jmri.Meter;
import jmri.MeterManager;
import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the basic load and store functionality for configuring
 * MeterManagers, working with AbstractMeterManagers.
 * <p>
 * This class cannot create Meters, so the meters must either be already
 * created, for example by the connections, in which case this class only
 * updates the data of the meter, for example its user name.
 * Or this class is overridden by a class that knows how to create the meters.
 *
 * @author Bob Jacobsen      Copyright (C) 2002, 2008
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class AbstractMeterManagerXml extends AbstractNamedBeanManagerConfigXML {

    public AbstractMeterManagerXml() {
    }

    /**
     * Default implementation for storing the contents of a MeterManager.
     *
     * @param o Object to store, of type MeterManager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element meters = new Element("meters");
        setStoreElementClass(meters);
        MeterManager mm = (MeterManager) o;
        if (mm != null) {
            SortedSet<Meter> memList = mm.getNamedBeanSet();
            // don't return an element if there are no meters to include
            if (memList.isEmpty()) {
                return null;
            }
            // store the meters
            for (Meter m : memList) {
                String mName = m.getSystemName();
                log.debug("system name is {}", mName);

                Element elem = new Element("meter");
                elem.addContent(new Element("systemName").addContent(mName));

                // store common part
                storeCommon(m, elem);

                log.debug("store Meter {}", mName);
                meters.addContent(elem);
            }
        }
        return meters;
    }

    /**
     * Subclass provides implementation to create the correct top element,
     * including the type information. Default implementation is to use the
     * local class here.
     *
     * @param meters The top-level element being created
     */
    public void setStoreElementClass(Element meters) {
        meters.setAttribute("class", this.getClass().getName());  // NOI18N
    }

    /**
     * Create a MeterManager object of the correct class, then register and
     * fill it.
     *
     * @param sharedMeters  Shared top level Element to unpack.
     * @param perNodeMemories Per-node top level Element to unpack.
     * @return true if successful
     * @throws jmri.configurexml.JmriConfigureXmlException if error during load.
     */
    @Override
    public boolean load(Element sharedMeters, Element perNodeMemories) throws JmriConfigureXmlException {
        loadMeters(sharedMeters);
        return true;
    }

    /**
     * Utility method to load the individual Meter objects. If there's no
     * additional info needed for a specific Meter type, invoke this with the
     * parent of the set of Meter elements.
     *
     * @param meters Element containing the Meter elements to load.
     */
    public void loadMeters(Element meters) {
        List<Element> meterList = meters.getChildren("meter");
        log.debug("Found {} Meter objects", meterList.size());
        MeterManager mm = InstanceManager.getDefault(MeterManager.class);

        for (Element el : meterList) {
            String sysName = getSystemName(el);
            if (sysName == null) {
                log.warn("unexpected null in systemName {}", (el));
                break;
            }

            String userName = getUserName(el);

            checkNameNormalization(sysName, userName, mm);

            log.debug("get Meter: ({})({})", sysName, (userName == null ? "<null>" : userName));
            Meter m = mm.getBySystemName(sysName);
            if (m != null) {
                m.setUserName(userName);
                // load common parts
                loadCommon(m, el);
            } else {
                log.debug("Meter ({}) does not exists and cannot be created", sysName);
            }
        }
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(MeterManager.class).getXMLOrder();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractMeterManagerXml.class);

}

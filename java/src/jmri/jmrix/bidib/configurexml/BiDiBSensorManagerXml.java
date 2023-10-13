package jmri.jmrix.bidib.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.bidib.BiDiBSensorManager;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;


/**
 * Provides load and store functionality for configuring BiDiBSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Eckart Meyer Copyright (C) 2019
 * @since 2.3.1
 */
public class BiDiBSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public BiDiBSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        log.debug("load {} {}", shared, perNode);
        // We tell the sensor managers that we will be loading sensors from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple connections registered.
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            if (!memo.getDisabled()) {
                ((BiDiBSensorManager)memo.getSensorManager()).startLoad();
            }
        }
        // load individual sensors
        boolean result = loadSensors(shared);

        // Notifies sensor managers that the loading of XML is complete.
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            if (!memo.getDisabled()) {
                ((BiDiBSensorManager)memo.getSensorManager()).finishLoad();
            }
        }

//        if (result) {
//            ProxySensorManager pm = (ProxySensorManager)InstanceManager.getDefault(jmri.SensorManager.class);
//            //log.debug("Sensor Manager List: {}", pm.getManagerList());
//            // the following is probably wrong since the we cannot guarantee that the first
//            // entry in the list is the correct one.
//            BiDiBSensorManager mgr = (BiDiBSensorManager)pm.getManagerList().get(0);
//            if (mgr != null  && mgr instanceof BiDiBSensorManager) {
//                mgr.updateAll();
//            }
//        }
        
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBSensorManagerXml.class);
}

package jmri.managers;

import jmri.DigitalIO;
import jmri.DigitalIOManager;

/**
 * Default implementation of a DigitalIOManager
 * 
 * @author Daniel Bergqvist 2019
 */
public class DefaultDigitalIOManager extends GenericManager<DigitalIO> implements DigitalIOManager {

    public DefaultDigitalIOManager(String beanTypeHandled, int xmlOrder) {
        super(beanTypeHandled, xmlOrder);
    }

}

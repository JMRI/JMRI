package jmri.managers;

import jmri.AnalogIO;
import jmri.AnalogIOManager;

/**
 * Default implementation of a AnalogIOManager
 * 
 * @author Daniel Bergqvist 2019
 */
public class DefaultAnalogIOManager extends GenericManager<AnalogIO> implements AnalogIOManager {

    public DefaultAnalogIOManager(String beanTypeHandled, int xmlOrder) {
        super(beanTypeHandled, xmlOrder);
    }
    
}

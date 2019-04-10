package jmri.managers;

import jmri.StringIO;
import jmri.StringIOManager;

/**
 * Default implementation of a StringIOManager
 * 
 * @author Daniel Bergqvist 2019
 */
public class DefaultStringIOManager extends GenericIOManager<StringIO> implements StringIOManager {

    public DefaultStringIOManager(String beanTypeHandled, int xmlOrder) {
        super(beanTypeHandled, xmlOrder);
    }

}

package jmri.managers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.List;
import java.util.SortedSet;
import jmri.Manager;
import jmri.NamedBean;
import jmri.util.com.dictiography.collections.IndexedTreeSet;

/**
 * Manager for the generic types DigitalIO, AnalogIO and StringIO.
 * The manager holds a manager for the type itself and a ProxyManager for the
 * types that inherits this type.
 * 
 * @author Daniel Bergqvist 2019
 */
public class GenericManager<E extends NamedBean> extends AbstractProxyManager<E> {

    private final String _beanTypeHandled;
    private final int _xmlOrder;
    
    public GenericManager(String beanTypeHandled, int xmlOrder) {
        super(new IndexedTreeSet<>(new java.util.Comparator<Manager<E>>(){
            @Override
            public int compare(Manager<E> e1, Manager<E> e2) {
                int result = e1.getSystemPrefix().compareTo(e2.getSystemPrefix());
                if (result == 0) {
                    if (e1.typeLetter() < e2.typeLetter()) {
                        result = -1;
                    } else {
                        result = e1.typeLetter() == e2.typeLetter() ? 0 : 1;
                    }
                }
                return result;
            }
        }));
        
        this._beanTypeHandled = beanTypeHandled;
        this._xmlOrder = xmlOrder;
    }
    
    @Override
    protected Manager<E> makeInternalManager() {
        return null;
    }

    @Override
    protected E makeBean(int index, String systemName, String userName) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public E provide(String name) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        int i = matchTentative(systemName);
        if (i >= 0) {
            return getMgr(i).validSystemNameFormat(systemName);
        }
        return NameValidity.INVALID;
    }

    @Override
    public int getXMLOrder() {
        return _xmlOrder;
    }

    @Override
    public String getBeanTypeHandled() {
        return _beanTypeHandled;
    }

}

// AbstractSignalHeadManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.managers.AbstractManager;


/**
 * Abstract partial implementation of a SignalHeadManager.
 * <P>
 * Not truly an abstract class, this might have been better named
 * DefaultSignalHeadManager.  But we've got it here for the eventual
 * need to provide system-specific implementations.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision$
 */
public class AbstractSignalHeadManager extends AbstractManager
    implements SignalHeadManager, java.beans.PropertyChangeListener {

    public AbstractSignalHeadManager() {
        super();
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }
    
    public int getXMLOrder(){
        return Manager.SIGNALHEADS;
    }

    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'H'; }

    public SignalHead getSignalHead(String name) {
        if (name==null || name.length()==0) { return null; }
        SignalHead t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalHead getBySystemName(String name) {
        return (SignalHead)_tsys.get(name);
    }

    public SignalHead getByUserName(String key) {
        return (SignalHead)_tuser.get(key);
    }
    
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if("CanDelete".equals(evt.getPropertyName())){ //IN18N
            StringBuilder message = new StringBuilder();
            boolean found = false;
            message.append(Bundle.getMessage("VetoFoundInSignalHeads"));
            message.append("<ul>");
            for(NamedBean nb:_tsys.values()){
                try {
                    nb.vetoableChange(evt);
                } catch (java.beans.PropertyVetoException e) {
                    if(e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")){ //IN18N
                        throw e;
                    }
                    found = true;
                    message.append("<li>" + e.getMessage() + "</li>");
                }
            }
            message.append("</ul>");
            message.append(Bundle.getMessage("VetoWillBeRemovedFromSignalHeads"));
            if(found)
                throw new java.beans.PropertyVetoException(message.toString(), evt);
        } else {
            for(NamedBean nb:_tsys.values()){
                try {
                    nb.vetoableChange(evt);
                } catch (java.beans.PropertyVetoException e) {
                    throw e;
                }
            }
        }
    }


    static Logger log = LoggerFactory.getLogger(AbstractSignalHeadManager.class.getName());
}

/* @(#)AbstractSignalHeadManager.java */

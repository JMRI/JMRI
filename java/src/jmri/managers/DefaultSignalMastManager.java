// DefaultSignalMastManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.managers.AbstractManager;
import jmri.implementation.SignalMastRepeater;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of a SignalMastManager.
 * <P>
 * Note that this does not enforce any particular system naming convention
 * at the present time.  They're just names...
 *
 * @author  Bob Jacobsen Copyright (C) 2009
 * @version	$Revision$
 */
public class DefaultSignalMastManager extends AbstractManager
    implements SignalMastManager, java.beans.PropertyChangeListener, java.beans.VetoableChangeListener {

    public DefaultSignalMastManager() {
        super();
        jmri.InstanceManager.signalHeadManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    public int getXMLOrder(){
        return Manager.SIGNALMASTS;
    }
    
    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'F'; }

    public SignalMast getSignalMast(String name) {
        if (name==null || name.length()==0) { return null; }
        SignalMast t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalMast provideSignalMast(String prefix, // nominally IF$shsm
                                        String signalSystem,
                                        String mastName,
                                        String[] heads) {
        StringBuilder name = new StringBuilder(prefix);
        name.append(":");
        name.append(signalSystem);
        name.append(":");
        for (String s : heads) {
            name.append("(");
            name.append(jmri.util.StringUtil.parenQuote(s));
            name.append(")");
        }
        return provideSignalMast(new String(name));
    }

    public SignalMast provideSignalMast(String name) {
        SignalMast m = getSignalMast(name);
        if (m == null) {
            m = new jmri.implementation.SignalHeadSignalMast(name);

            register(m);
        }
        return m;
    }

    public SignalMast getBySystemName(String key) {
        return (SignalMast)_tsys.get(key);
    }

    public SignalMast getByUserName(String key) {
        return (SignalMast)_tuser.get(key);
    }
    
    ArrayList<SignalMastRepeater> repeaterList = new ArrayList<SignalMastRepeater>();
    
    public void addRepeater(SignalMastRepeater rp) throws jmri.JmriException{
        for(SignalMastRepeater rpeat:repeaterList){
            if(rpeat.getMasterMast()==rp.getMasterMast() &&
                rpeat.getSlaveMast() == rp.getSlaveMast()){
                    log.error("Signal repeater already Exists");
                    throw new jmri.JmriException("Signal mast Repeater already exists");
            }
            else if(rpeat.getMasterMast()==rp.getSlaveMast() &&
                rpeat.getSlaveMast() == rp.getMasterMast()){
                    log.error("Signal repeater already Exists");
                    throw new jmri.JmriException("Signal mast Repeater already exists");
                }
        }
        repeaterList.add(rp);
        firePropertyChange("repeaterlength", null, null);
    }
    
    public void removeRepeater(SignalMastRepeater rp){
        rp.dispose();
        repeaterList.remove(rp);
        firePropertyChange("repeaterlength", null, null);
    }
    
    public List<SignalMastRepeater> getRepeaterList(){
        return repeaterList;
    }
    
    public void initialiseRepeaters(){
        for(SignalMastRepeater smr:repeaterList){
            smr.initialise();
        }
    }
    
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if("CanDelete".equals(evt.getPropertyName())){ //IN18N
            StringBuilder message = new StringBuilder();
            boolean found = false;
            message.append(Bundle.getMessage("VetoFoundInSignalMast"));
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
            message.append(Bundle.getMessage("VetoWillBeRemovedFromSignalMast")); //IN18N
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

    static Logger log = LoggerFactory.getLogger(DefaultSignalMastManager.class.getName());
}

/* @(#)DefaultSignalMastManager.java */

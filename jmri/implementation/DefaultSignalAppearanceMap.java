// DefaultSignalAppearanceMap.java

package jmri.implementation;

import java.util.ResourceBundle;

import jmri.SignalAppearanceMap;
import jmri.SignalHead;
import jmri.SignalAspectTable;

 /**
 * Default implementation of a basic signal head table.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file.
 * This makes creation a little more heavy-weight, but speeds operation.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.2 $
 */
public class DefaultSignalAppearanceMap extends AbstractNamedBean implements SignalAppearanceMap  {

    public DefaultSignalAppearanceMap(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAppearanceMap(String systemName) {
        super(systemName);
    }

    public void loadDefaults() {
        
        if (rbr == null || rbr.get() == null) rbr = new java.lang.ref.SoftReference<ResourceBundle>(
                                                    java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle"));
        ResourceBundle rb = rbr.get();
        
        log.debug("start loadDefaults");
        
        String ra;
        ra = rb.getString("SignalAspectDefaultRed");
        addAspect(ra, new int[]{SignalHead.RED});

        ra = rb.getString("SignalAspectDefaultYellow");
        addAspect(ra, new int[]{SignalHead.YELLOW});

        ra = rb.getString("SignalAspectDefaultGreen");
        addAspect(ra, new int[]{SignalHead.GREEN});
    }
    
    public void setAppearances(String aspect, SignalHead[] heads) {
        if (aspectTable != null && aspectTable.checkAspect(aspect))
            log.warn("Attempt to set "+getSystemName()+" to undefined aspect: "+aspect);
        for (int i = 0; i < heads.length; i++)
            heads[i].setAppearance(table.get(aspect)[i]);
        if (log.isDebugEnabled()) log.debug("Set 1st head to "+table.get(aspect)[0]);
        return;
    }
    
    public void addAspect(String aspect, int[] appearances) {
        if (log.isDebugEnabled()) log.debug("add aspect \""+aspect+"\" for "+appearances.length+" heads "
                                        +appearances[0]);
        table.put(aspect, appearances);
    }
    
    public java.util.Enumeration<String> getAspects() {
        return table.keys();
    }
    
    
    public SignalAspectTable getAspectTable() { return aspectTable; }
    public void setAspectTable(SignalAspectTable t) { aspectTable = t; }
    protected SignalAspectTable aspectTable;
    
    public int getState() {
        throw new NoSuchMethodError();
    }
    
    public void setState(int s) {
        throw new NoSuchMethodError();
    }

    static private java.lang.ref.SoftReference<ResourceBundle> rbr;
    protected java.util.Hashtable<String, int[]> table = new jmri.util.OrderedHashtable<String, int[]>();

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalAppearanceMap.class.getName());
}

/* @(#)DefaultSignalAppearanceMap.java */

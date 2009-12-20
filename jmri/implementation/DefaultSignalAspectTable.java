// DefaultSignalAspectTable.java

package jmri.implementation;

import java.util.ResourceBundle;

import jmri.SignalAspectTable;
import jmri.SignalHead;

 /**
 * Default implementation of a basic signal head table.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file.
 * This makes creation a little more heavy-weight, but speeds operation.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.1 $
 */
public class DefaultSignalAspectTable extends AbstractNamedBean implements SignalAspectTable  {

    public DefaultSignalAspectTable(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAspectTable(String systemName) {
        super(systemName);
    }

    public void loadDefaults() {
        
        if (rb == null) rb = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

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
    
    public int getState() {
        throw new NoSuchMethodError();
    }
    
    public void setState(int s) {
        throw new NoSuchMethodError();
    }

    static private ResourceBundle rb;
    protected java.util.Hashtable<String, int[]> table = new jmri.util.OrderedHashtable<String, int[]>();

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalAspectTable.class.getName());
}

/* @(#)DefaultSignalAspectTable.java */

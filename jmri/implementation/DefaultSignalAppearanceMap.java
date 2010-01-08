// DefaultSignalAppearanceMap.java

package jmri.implementation;

import java.util.ResourceBundle;
import java.util.List;

import java.io.File;

import org.jdom.Element;
import org.jdom.JDOMException;

import jmri.util.NamedBeanHandle;

import jmri.SignalHead;
import jmri.SignalSystem;

 /**
 * Default implementation of a basic signal head table.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file.
 * This makes creation a little more heavy-weight, but speeds operation.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.8 $
 */
public class DefaultSignalAppearanceMap extends AbstractNamedBean  {

    public DefaultSignalAppearanceMap(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAppearanceMap(String systemName) {
        super(systemName);
    }

    static public DefaultSignalAppearanceMap getMap(String signalSystemName, String aspectMapName) {
        DefaultSignalAppearanceMap map = maps.get("map:"+signalSystemName+":"+aspectMapName);
        if (map == null) {
            map = loadMap(signalSystemName, aspectMapName);
        }
        return map;
    }
    
    static DefaultSignalAppearanceMap loadMap(String signalSystemName, String aspectMapName) {
        DefaultSignalAppearanceMap map = 
            new DefaultSignalAppearanceMap("map:"+signalSystemName+":"+aspectMapName);
        maps.put("map:"+signalSystemName+":"+aspectMapName, map);

        File file = new File("xml"+File.separator
                                +"signals"+File.separator
                                +signalSystemName+File.separator
                                +"appearance-"+aspectMapName+".xml");
        if (!file.exists()) {
            log.error("appearance file doesn't exist: "+file.getPath());
            throw new IllegalArgumentException("appearance file doesn't exist: "+file.getPath());
        }
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile(){};
        Element root;
        try {
            root = xf.rootFromFile(file);
            // get appearances
            @SuppressWarnings("unchecked")
            List<Element> l = root.getChild("appearances").getChildren("appearance");
            
            // find all appearances, include them by aspect name, 
            // add 'show' sub-elements as ints
            for (int i = 0; i < l.size(); i++) {
                String name = l.get(i).getChild("aspectname").getText();
                if (log.isDebugEnabled()) log.debug("aspect name "+name);
                
                @SuppressWarnings("unchecked")
                List<Element> c = l.get(i).getChildren("show");
                
                int[] appearances = new int[c.size()];
                for (int j = 0; j < c.size(); j++) {
                    // note: includes setting name; redundant, but needed
                    int ival;
                    String sval = c.get(j).getText().toUpperCase();
                    if (sval.equals("LUNAR")) ival = SignalHead.LUNAR;
                    else if (sval.equals("GREEN")) ival = SignalHead.GREEN;
                    else if (sval.equals("YELLOW")) ival = SignalHead.YELLOW;
                    else if (sval.equals("RED")) ival = SignalHead.RED;
                    else if (sval.equals("FLASHLUNAR")) ival = SignalHead.FLASHLUNAR;
                    else if (sval.equals("FLASHGREEN")) ival = SignalHead.FLASHGREEN;
                    else if (sval.equals("FLASHYELLOW")) ival = SignalHead.FLASHYELLOW;
                    else if (sval.equals("FLASHRED")) ival = SignalHead.FLASHRED;
                    else if (sval.equals("DARK")) ival = SignalHead.DARK;
                    else throw new JDOMException("invalid content: "+sval);
                    
                    appearances[j] = ival;
                }
                map.addAspect(name, appearances);
            }
        } catch (Exception e) {
            log.error("error reading file \""+file.getName()+"\" due to: "+e);
            return null;
        }
        return map;
    }

    static protected java.util.Hashtable<String, DefaultSignalAppearanceMap> maps
            = new jmri.util.OrderedHashtable<String, DefaultSignalAppearanceMap>();

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
    
    public void setAppearances(String aspect, List<NamedBeanHandle<SignalHead>> heads) {
        if (systemDefn != null && systemDefn.checkAspect(aspect))
            log.warn("Attempt to set "+getSystemName()+" to undefined aspect: "+aspect);
        for (int i = 0; i < heads.size(); i++) {
            heads.get(i).getBean().setAppearance(table.get(aspect)[i]);
            if (log.isDebugEnabled()) log.debug("Setting "+heads.get(i).getBean().getSystemName()+" to "+table.get(aspect)[i]);
        }
        return;
    }
    
    public boolean checkAspect(String aspect) {
        return table.get(aspect) != null;
    }

    public void addAspect(String aspect, int[] appearances) {
        if (log.isDebugEnabled()) log.debug("add aspect \""+aspect+"\" for "+appearances.length+" heads "
                                        +appearances[0]);
        table.put(aspect, appearances);
    }
    
    public java.util.Enumeration<String> getAspects() {
        return table.keys();
    }
    
    
    public SignalSystem getSignalSystem() { return systemDefn; }
    public void setSignalSystem(SignalSystem t) { systemDefn = t; }
    protected SignalSystem systemDefn;
    
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

// DefaultSignalAppearanceMap.java

package jmri.implementation;

import java.util.ResourceBundle;
import java.util.List;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
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
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.22 $
 */
public class DefaultSignalAppearanceMap extends AbstractNamedBean implements jmri.SignalAppearanceMap {

    public DefaultSignalAppearanceMap(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAppearanceMap(String systemName) {
        super(systemName);
    }

    static public DefaultSignalAppearanceMap getMap(String signalSystemName, String aspectMapName) {
        if (log.isDebugEnabled()) log.debug("getMap signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
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
            for (int i = 0; i < l.size(); i++) {
                String name = l.get(i).getChild("aspectname").getText();
                if (log.isDebugEnabled()) log.debug("aspect name "+name);
                
                // add 'show' sub-elements as ints
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

                java.util.Hashtable<String, String> images = new java.util.Hashtable<String, String>();
                
                @SuppressWarnings("unchecked")
                List<Element> img = l.get(i).getChildren("imagelink");
                for (int j = 0; j < img.size(); j++) {
                    String key = "default";
                    if((img.get(j).getAttribute("type"))!=null)
                        key = img.get(j).getAttribute("type").getValue();
                    String value = img.get(j).getText();
                    images.put(key, value);
                }
                
                map.aspectImageMap.put(name, images);
                
                // now add the rest of the attributes
                java.util.Hashtable<String, String> hm = new java.util.Hashtable<String, String>();
                
                @SuppressWarnings("unchecked")
                List<Element> a = l.get(i).getChildren();

                for (int j = 0; j < a.size(); j++) {
                    String key = a.get(j).getName();
                    String value = a.get(j).getText();
                    hm.put(key, value);
                }
                
                map.aspectAttributeMap.put(name, hm);
            }
        } catch (java.io.IOException e) {
            log.error("error reading file \""+file.getName(), e);
            return null;
        } catch (org.jdom.JDOMException e) {
            log.error("error parsing file \""+file.getName(), e);
            return null;
        }
        return map;
    }

    public final static int HELD = 0;
    public final static int PERMISSIVE = 1;
    public final static int DANGER = 2;
    public final static int DARK = 3;
    public final static int NUMSPECIFIC = 4;
    
    static public Hashtable<Integer, String> getSpecificMap(String signalSystemName, String aspectMapName){
        if (log.isDebugEnabled()) log.debug("getSpecificMap signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
        Hashtable<Integer,String> map = specificMaps.get("map:"+signalSystemName+":"+aspectMapName);
        if (map==null){
            map = loadSpecificMap(signalSystemName, aspectMapName);
        }
        return map;
    }

    static Hashtable<Integer, String> loadSpecificMap(String signalSystemName, String aspectMapName){
        if (log.isDebugEnabled()) log.debug("load specific signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
        Hashtable<Integer, String> map =  new Hashtable<Integer, String>();
        specificMaps.put("map:"+signalSystemName+":"+aspectMapName, map);
        for (int i = 0; i<NUMSPECIFIC; i++){
            String aspect = loadSpecificAspect(signalSystemName, aspectMapName, i);
            if (aspect!=null)
                map.put(i, aspect);
        }
        return map;
    }

    static String loadSpecificAspect(String signalSystemName, String aspectMapName, int aspectType) {
        String appearance;
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
            String child;
            switch (aspectType){
                case HELD:  child = "held";
                            break;
                case DANGER: child = "danger";
                            break;
                case PERMISSIVE: child = "permissive";
                        break;
                case DARK: child = "dark";
                        break;
                default: child = "danger";
            }
            appearance = root.getChild("specificappearances").getChild(child).getText();
        } catch (java.lang.NullPointerException e){
            log.debug("appearance not configured");
            return null;
        } catch (java.io.IOException e) {
            log.error("error reading file \""+file.getName(), e);
            return null;
        } catch (org.jdom.JDOMException e) {
            log.error("error parsing file \""+file.getName(), e);
            return null;
        }
        return appearance;
    }

    static public Hashtable<String, String[]> getAspectRelationMap(String signalSystemName, String aspectMapName){
        if (log.isDebugEnabled()) log.debug("getAspectRelationMap signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
        Hashtable<String,String[]> map = aspectRelationshipMap.get("map:"+signalSystemName+":"+aspectMapName);
        if (map==null){
            map = loadAspectRelationMap(signalSystemName, aspectMapName);
        }
        return map;

    }

    static Hashtable<String, String[]> loadAspectRelationMap(String signalSystemName, String aspectMapName) {
        if (log.isDebugEnabled()) log.debug("load aspect relation map signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
        Hashtable<String, String[]> map =  new Hashtable<String, String[]>();
        aspectRelationshipMap.put("map:"+signalSystemName+":"+aspectMapName, map);

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
            @SuppressWarnings("unchecked")
            List<Element> l = root.getChild("aspectMappings").getChildren("aspectMapping");
            for (int i = 0; i < l.size(); i++) {
                String advanced = l.get(i).getChild("advancedAspect").getText();
                @SuppressWarnings("unchecked")
                List<Element> o = l.get(i).getChildren("ourAspect");
                String[] appearances = new String[o.size()];
                for (int j = 0; j < o.size(); j++) {
                    appearances[j] = o.get(j).getText();
                }
                map.put(advanced, appearances);
            }

        } catch (java.lang.NullPointerException e){
            log.debug("appearance not configured");
            return null;
        } catch (java.io.IOException e) {
            log.error("error reading file \""+file.getName(), e);
            return null;
        } catch (org.jdom.JDOMException e) {
            log.error("error parsing file \""+file.getName(), e);
            return null;
        }
        return map;
    }

    /**
     * Get a property associated with a specific aspect
     */
    public String getProperty(String aspect, String key) {
        return aspectAttributeMap.get(aspect).get(key);
    }
    
    public String getImageLink(String aspect, String type){
        if(type==null|| type.equals(""))
            type = "default";
        String value = aspectImageMap.get(aspect).get(type);
        //if we don't return a valid image set, then we will use which ever set is loaded in teh getProperty
        if(value==null){
            return getProperty(aspect, "imagelink");
        }
        return value;
    }
    
    public Vector<String> getImageTypes(String aspect) {
        if(!checkAspect(aspect))
            return new Vector<String>();
        java.util.Enumeration<String> e = aspectImageMap.get(aspect).keys();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    protected java.util.Hashtable<String, java.util.Hashtable<String, String>> aspectAttributeMap 
            = new java.util.Hashtable<String, java.util.Hashtable<String, String>>();
            
    protected java.util.Hashtable<String, java.util.Hashtable<String, String>> aspectImageMap 
            = new java.util.Hashtable<String, java.util.Hashtable<String, String>>();

    static java.util.Hashtable<String, DefaultSignalAppearanceMap> maps
            = new jmri.util.OrderedHashtable<String, DefaultSignalAppearanceMap>();

    static java.util.Hashtable<String, java.util.Hashtable<Integer, String>> specificMaps
            = new java.util.Hashtable<String, java.util.Hashtable<Integer, String>>(); 

    static java.util.Hashtable<String, java.util.Hashtable<String, String[]>> aspectRelationshipMap
            = new java.util.Hashtable<String, java.util.Hashtable<String, String[]>>();

    public void loadDefaults() {
        
        if (rbr == null) rbr = new java.lang.ref.SoftReference<ResourceBundle>(
                                                    java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle"));

        ResourceBundle rb = rbr.get();
        if (rb == null) {
            log.error("Failed to load defaults because of missing bundle");
            return;
        }
        
        log.debug("start loadDefaults");
        
        String ra;
        ra = rb.getString("SignalAspectDefaultRed");
        if (ra!=null) addAspect(ra, new int[]{SignalHead.RED});
        else log.error("no default red aspect");

        ra = rb.getString("SignalAspectDefaultYellow");
        if (ra!=null) addAspect(ra, new int[]{SignalHead.YELLOW});
        else log.error("no default yellow aspect");

        ra = rb.getString("SignalAspectDefaultGreen");
        if (ra!=null) addAspect(ra, new int[]{SignalHead.GREEN});
        else log.error("no default green aspect");
    }
    
    public void setAppearances(String aspect, List<NamedBeanHandle<SignalHead>> heads) {
        if (systemDefn != null && systemDefn.checkAspect(aspect))
            log.warn("Attempt to set "+getSystemName()+" to undefined aspect: "+aspect);
        if (heads.size() > table.get(aspect).length)
            log.warn("setAppearance to \""+aspect+"\" finds "+heads.size()+" heads but only "+table.get(aspect).length+" settings");

        for (int i = 0; i < heads.size(); i++) {
            // some extensive checking
            boolean error = false;
            if (heads.get(i) == null){
                log.error("Null head "+i+" in setAppearances");
                error = true;
            }
            if (heads.get(i).getBean() == null){
                log.error("Could not get bean for head "+i+" in setAppearances");
                error = true;
            }
            if (table.get(aspect) == null){
                log.error("Couldn't get table array for aspect \""+aspect+"\" in setAppearances");
                error = true;
            }
            
            if(!error)
                heads.get(i).getBean().setAppearance(table.get(aspect)[i]);
            else 
                log.error("head appearance not set due to an error");
            if (log.isDebugEnabled()) log.debug("Setting "+heads.get(i).getBean().getSystemName()+" to "+
                                                heads.get(i).getBean().getAppearanceName(table.get(aspect)[i]));
        }
        return;
    }
    
    public boolean checkAspect(String aspect) {
        if (aspect==null) return false;
        return table.containsKey(aspect);// != null;
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

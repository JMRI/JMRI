// DefaultSignalAppearanceMap.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import java.io.File;
import java.util.Vector;
import org.jdom.Element;
import org.jdom.JDOMException;

import jmri.SignalHead;
import jmri.SignalSystem;
import jmri.util.FileUtil;

 /**
 * Default implementation of a basic signal head table.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file.
 * This makes creation a little more heavy-weight, but speeds operation.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision$
 */
public class DefaultSignalAppearanceMap extends AbstractNamedBean implements jmri.SignalAppearanceMap {

    public DefaultSignalAppearanceMap(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAppearanceMap(String systemName) {
        super(systemName);
    }
    
    public String getBeanType(){
        return Bundle.getMessage("BeanNameSignalAppMap");
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

        File file = new File(FileUtil.getUserFilesPath()
                                +"resources"+File.separator
                                +"signals"+File.separator
                                +signalSystemName+File.separator
                                +"appearance-"+aspectMapName+".xml");
        if(!file.exists()){
            file = new File("xml"+File.separator
                                    +"signals"+File.separator
                                    +signalSystemName+File.separator
                                    +"appearance-"+aspectMapName+".xml");
            if (!file.exists()) {
                log.error("appearance file doesn't exist: "+file.getPath());
                throw new IllegalArgumentException("appearance file doesn't exist: "+file.getPath());
            }
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

                //java.util.Hashtable<String, String> images = new java.util.Hashtable<String, String>();
                
                @SuppressWarnings("unchecked")
                List<Element> img = l.get(i).getChildren("imagelink");
                loadImageMaps(img, name, map);
                
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
            loadSpecificMap(signalSystemName, aspectMapName, map, root);
            loadAspectRelationMap(signalSystemName, aspectMapName, map, root);
        } catch (java.io.IOException e) {
            log.error("error reading file \""+file.getName(), e);
            return null;
        } catch (org.jdom.JDOMException e) {
            log.error("error parsing file \""+file.getName(), e);
            return null;
        }
        
        return map;
    }
    
    static void loadImageMaps(List<Element> img, String name, DefaultSignalAppearanceMap map){
        java.util.Hashtable<String, String> images = new java.util.Hashtable<String, String>();
        for (int j = 0; j < img.size(); j++) {
            String key = "default";
            if((img.get(j).getAttribute("type"))!=null)
                key = img.get(j).getAttribute("type").getValue();
            String value = img.get(j).getText();
            images.put(key, value);
        }
        map.aspectImageMap.put(name, images);
    }

    public final static int NUMSPECIFIC = 4;
    
    static void loadSpecificMap(String signalSystemName, String aspectMapName, DefaultSignalAppearanceMap SMmap, Element root){
        if (log.isDebugEnabled()) log.debug("load specific signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
        for (int i = 0; i<NUMSPECIFIC; i++){
            loadSpecificAspect(signalSystemName, aspectMapName, i, SMmap, root);
        }
    }

    static void loadSpecificAspect(String signalSystemName, String aspectMapName, int aspectType, DefaultSignalAppearanceMap SMmap, Element root) {
        
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
        
        String appearance = null;
        if(root.getChild("specificappearances")==null || root.getChild("specificappearances").getChild(child)==null){
            log.debug("appearance not configured " + child);
            return;
        }
        try {
            appearance = root.getChild("specificappearances").getChild(child).getChild("aspect").getText();
            SMmap.specificMaps.put(aspectType, appearance);
        } catch (java.lang.NullPointerException e){
            log.debug("aspect for specific appearance not configured " + child);
        }
        
        try {
            @SuppressWarnings("unchecked")
            List<Element> img = root.getChild("specificappearances").getChild(child).getChildren("imagelink");
            String name = "$"+child;
            if (img.size()==0){
                if(appearance!=null){
                   //We do not have any specific images created, therefore we use the
                   //those associated with the aspect.
                   List<String> app = SMmap.getImageTypes(appearance);
                   java.util.Hashtable<String, String> images = new java.util.Hashtable<String, String>();
                   String type = "";
                   for (int i = 0; i<app.size(); i++){
                        type = SMmap.getImageLink(appearance, app.get(i));
                        images.put(app.get(i), type);
                   }
                   //We will register the last aspect as a default.
                   images.put("default", type);
                   SMmap.aspectImageMap.put(name, images);
               }
            } else {
                loadImageMaps(img, name, SMmap);
                java.util.Hashtable<String, String> hm = new java.util.Hashtable<String, String>();
                
                //Register the last aspect as the default
                String key = img.get(img.size()-1).getName();
                String value = img.get(img.size()-1).getText();
                hm.put(key, value);
                
                SMmap.aspectAttributeMap.put(name, hm);
            }
        } catch (java.lang.NullPointerException e){
            //Considered Normal if held aspect uses default signal appearance
        }
    }

    static void loadAspectRelationMap(String signalSystemName, String aspectMapName, DefaultSignalAppearanceMap SMmap, Element root) {
        if (log.isDebugEnabled()) log.debug("load aspect relation map signalSystem= \""+signalSystemName+"\", aspectMap= \""+aspectMapName+"\"");
        
        try {
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
                SMmap.aspectRelationshipMap.put(advanced, appearances);
            }

        } catch (java.lang.NullPointerException e){
            log.debug("appearance not configured");
            return;
        }

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
        String value;
        try {
            value = aspectImageMap.get(aspect).get(type);
            //if we don't return a valid image set, then we will use which ever set is loaded in the getProperty
            if(value==null){
                value = getProperty(aspect, "imagelink");
            }
        } catch (java.lang.NullPointerException e){
            /* Can be considered normal for situations where a specific aspect
            has been asked for but it hasn't yet been loaded or configured */
            value = "";
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

    protected java.util.Hashtable<Integer, String> specificMaps
            = new java.util.Hashtable<Integer, String>();
    
    protected java.util.Hashtable<String, String[]> aspectRelationshipMap
            = new java.util.Hashtable<String, String[]>();

    public void loadDefaults() {
                
        log.debug("start loadDefaults");
        
        String ra;
        ra = Bundle.getMessage("SignalAspectDefaultRed");
        if (ra!=null) addAspect(ra, new int[]{SignalHead.RED});
        else log.error("no default red aspect");

        ra = Bundle.getMessage("SignalAspectDefaultYellow");
        if (ra!=null) addAspect(ra, new int[]{SignalHead.YELLOW});
        else log.error("no default yellow aspect");

        ra = Bundle.getMessage("SignalAspectDefaultGreen");
        if (ra!=null) addAspect(ra, new int[]{SignalHead.GREEN});
        else log.error("no default green aspect");
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
    
    public String getSpecificAppearance(int appearance){
        if (specificMaps.containsKey(appearance)){
            return specificMaps.get(appearance);
        }
        return null;
    }
    
    /**
    * Returns a list of postential aspects that we could set the signalmast to
    * given the state of the advanced signal mast.
    */
    public String[] getValidAspectsForAdvancedAspect(String advancedAspect){
        if (aspectRelationshipMap==null){
            log.error("aspect relationships have not been defined or loaded");
            throw new IllegalArgumentException("aspect relationships have not been defined or loaded");
        }
        if (advancedAspect==null){
            String[] danger = new String[1];
            danger[0] = getSpecificAppearance(DANGER);
            return danger;
        }
        if(aspectRelationshipMap.containsKey(advancedAspect)){
            //String[] validAspects = aspectRelationMap.get(advancedAspect);
            return aspectRelationshipMap.get(advancedAspect);
        }
        return null;
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
    
    public int[] getAspectSettings(String aspect){
        return table.get(aspect);
    }

    protected java.util.Hashtable<String, int[]> table = new jmri.util.OrderedHashtable<String, int[]>();
    static Logger log = LoggerFactory.getLogger(DefaultSignalAppearanceMap.class.getName());
}

/* @(#)DefaultSignalAppearanceMap.java */

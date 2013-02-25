// DefaultSignalSystem.java

package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Hashtable;
import java.util.Enumeration;

import jmri.SignalSystem;

 /**
 * Default implementation of a basic signal system definition.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file.
 * This makes creation a little more heavy-weight, but speeds operation.
 *
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision$
 */
public class DefaultSignalSystem extends AbstractNamedBean implements SignalSystem  {

    public DefaultSignalSystem(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalSystem(String systemName) {
        super(systemName);
    }

    public void setProperty(String aspect, String key, Object value) {
        getTable(aspect).put(key, value);
        if (! keys.contains(key)) keys.add(key);
    }
    
    public Object getProperty(String aspect, String key) {
        if (aspect==null) {
            return null;
        }
        return getTable(aspect).get(key);
    }
    
    public void setImageType(String type) {
        if (! imageTypes.contains(type)) imageTypes.add(type);
    }
    
    public Enumeration<String> getImageTypeList() {
        return imageTypes.elements();
    }

    public String getAspect(Object obj, String key){
        if (obj==null)
            return null;
        Enumeration<String> aspectKeys = aspects.keys();
        while ( aspectKeys.hasMoreElements() )
           {
           String aspect = aspectKeys.nextElement();
           if(getTable(aspect).containsKey(key)){
               if (getTable(aspect).get(key).equals(obj))
                   return aspect;
           }
        }
        return null;
    }

    protected Hashtable<String, Object> getTable(String aspect) {
        Hashtable<String, Object> t = aspects.get(aspect);
        if ( t == null) {
            t = new Hashtable<String, Object>();
            aspects.put(aspect, t);
        }
        return t;
    }
    
    public Enumeration<String> getAspects() {
        return aspects.keys();
    }

    public Enumeration<String> getKeys() {
        return keys.elements();
    }

    public boolean checkAspect(String aspect) {
        return aspects.get(aspect) != null;
    }

    public void loadDefaults() {
        
        log.debug("start loadDefaults");
        
        String aspect;
        String key = Bundle.getMessage("SignalAspectKey");
        String value;
        
        aspect = Bundle.getMessage("SignalAspectDefaultRed");
        value = Bundle.getMessage("SignalAspect_"+key+"_"+aspect);
        setProperty(aspect, key, value);

        aspect = Bundle.getMessage("SignalAspectDefaultYellow");
        value = Bundle.getMessage("SignalAspect_"+key+"_"+aspect);
        setProperty(aspect, key, value);

        aspect = Bundle.getMessage("SignalAspectDefaultGreen");
        value = Bundle.getMessage("SignalAspect_"+key+"_"+aspect);
        setProperty(aspect, key, value);

    }
    

    public int getState() {
        throw new NoSuchMethodError();
    }
    
    public void setState(int s) {
        throw new NoSuchMethodError();
    }
    
    float maximumLineSpeed = 0.0f;
    
    public float getMaximumLineSpeed(){
        if(maximumLineSpeed == 0.0f){
            for(String as:aspects.keySet()){
                String speed = (String)getProperty(as, "speed");
                if(speed!=null){
                    float aspectSpeed = 0.0f;
                    try {
                        aspectSpeed = new Float(speed);
                    }catch (NumberFormatException nx) {
                        try{
                            aspectSpeed = jmri.implementation.SignalSpeedMap.getMap().getSpeed(speed);
                        } catch (Exception ex){
                            //Considered Normal if the speed does not appear in the map
                        }
                    }
                    if(aspectSpeed>maximumLineSpeed){
                        maximumLineSpeed=aspectSpeed;
                    }
                }
                
            }
        }
        if(maximumLineSpeed ==0.0f){
            //no speeds configured so will use the default.
            maximumLineSpeed = jmri.implementation.SignalSpeedMap.getMap().getSpeed("Maximum");
        }
        return maximumLineSpeed;
    }
    
    protected java.util.Hashtable<String, Hashtable<String, Object>> aspects
            = new jmri.util.OrderedHashtable<String, Hashtable<String, Object>>();

    protected java.util.Vector<String> keys = new java.util.Vector<String>();
    
    protected java.util.Vector<String> imageTypes = new java.util.Vector<String>();
    
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public String toString() {
        String retval = "SignalSystem "+getSystemName()+"\n";
        Enumeration<String> e1 = getAspects();
        while (e1.hasMoreElements()) {
            String s1 = e1.nextElement();
            retval += "  "+s1+"\n";
            Enumeration<String> e2 = getKeys();
            while (e2.hasMoreElements()) {
                String s2 = e2.nextElement();
                retval += "    "+s2+": "+getProperty(s1, s2)+"\n";
            }
        }
        return retval;
    }

    static Logger log = LoggerFactory.getLogger(DefaultSignalSystem.class.getName());
}

/* @(#)DefaultSignalSystem.java */

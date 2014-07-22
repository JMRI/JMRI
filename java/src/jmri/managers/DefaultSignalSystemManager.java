// DefaultSignalSystemManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.implementation.DefaultSignalSystem;

import java.io.*;
import java.lang.reflect.*;

import java.util.List;
import java.util.ArrayList;
import jmri.util.FileUtil;

import org.jdom.Element;

/**
 * Default implementation of a SignalSystemManager.
 * <P>
 * This loads automatically the first time used.
 * <p>
 *
 *
 * @author  Bob Jacobsen Copyright (C) 2009
 * @version	$Revision$
 */
public class DefaultSignalSystemManager extends AbstractManager
    implements SignalSystemManager, java.beans.PropertyChangeListener {

    public DefaultSignalSystemManager() {
        super();
        
        // load when created, which will generally
        // be the first time referenced
        load();
    }
    
    public int getXMLOrder(){
        return 65400;
    }

    /**
     * Don't want to store this information
     */
    @Override
    protected void registerSelf() {}
    
    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'F'; }
    
    public SignalSystem getSystem(String name) {
        SignalSystem t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public SignalSystem getBySystemName(String key) {
        return (SignalSystem)_tsys.get(key);
    }

    public SignalSystem getByUserName(String key) {
        return (SignalSystem)_tuser.get(key);
    }

    void load() {
        List<String> list = getListOfNames();
        for (int i = 0; i < list.size(); i++) {
            SignalSystem s = makeBean(list.get(i));
            register(s);
        }
    }

    List<String> getListOfNames() {
        List<String> retval = new ArrayList<String>();
        // first locate the signal system directory
        // and get names of systems
        
        //First get the default pre-configured signalling systems
        File signalDir = new File("xml"+File.separator+"signals");
        File[] files = signalDir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                // check that there's an aspects.xml file
                File aspects = new File(files[i].getPath()+File.separator+"aspects.xml");
                if (aspects.exists()) {
                    log.debug("found system: "+files[i].getName());
                    retval.add(files[i].getName());
                }
            }
        }
        //Now get the user defined systems.
        signalDir = new File(FileUtil.getUserFilesPath()
            +"resources"+File.separator+"signals");
        if(!signalDir.exists()){
            log.info("User signal resource directory has not been created");
            try {
                signalDir.mkdir();
            } catch (Exception ex){
                log.error("Unable to create signal resource directory " + ex);
            }
        }
        files = signalDir.listFiles();
        if(files!=null){
            for (int i=0; i<files.length; i++) {
                if (files[i].isDirectory()) {
                    // check that there's an aspects.xml file
                    File aspects = new File(files[i].getPath()+File.separator+"aspects.xml");
                    if ((aspects.exists()) && (!retval.contains(files[i].getName()))) {
                        log.debug("found system: "+files[i].getName());
                        retval.add(files[i].getName());
                    }
                }
            }
        }
        return retval;
    }

    SignalSystem makeBean(String name) {
        
        //First check to see if the bean is in the default system directory
        String filename = "xml"+File.separator+"signals"
                            +File.separator+name
                            +File.separator+"aspects.xml";
        log.debug("load from "+filename);
        XmlFile xf = new AspectFile();
        File file = new File(filename);
        if(file.exists()){
            try {
                Element root = xf.rootFromName(filename);
                DefaultSignalSystem s = new DefaultSignalSystem(name);
                loadBean(s, root);
                return s;
            } catch (Exception e) {
                log.error("Could not parse aspect file \""+filename+"\" due to: "+e);
            }
        }

        //if the file doesn't exist or fails the load from the default location then try the user directory
        filename = FileUtil.getUserFilesPath()+"resources"
                            +File.separator+"signals"
                            +File.separator+name
                            +File.separator+"aspects.xml";
        log.debug("load from "+filename);
        file = new File(filename);
        if(file.exists()){
            xf = new AspectFile();
            try {
                Element root = xf.rootFromName(filename);
                DefaultSignalSystem s = new DefaultSignalSystem(name);
                loadBean(s, root);
                return s;
            } catch (Exception e) {
                log.error("Could not parse aspect file \""+filename+"\" due to: "+e);
            }
        }
        
        return null;
    }

    void loadBean(DefaultSignalSystem s, Element root) {
        @SuppressWarnings("unchecked")
        List<Element> l = root.getChild("aspects").getChildren("aspect");
        
        // set user name from system name element
        s.setUserName(root.getChild("name").getText());
        
        // find all aspects, include them by name, 
        // add all other sub-elements as key/value pairs
        for (int i = 0; i < l.size(); i++) {
            String name = l.get(i).getChild("name").getText();
            if (log.isDebugEnabled()) log.debug("aspect name "+name);
 
            @SuppressWarnings("unchecked")
            List<Element> c = l.get(i).getChildren();

            for (int j = 0; j < c.size(); j++) {
                // note: includes setting name; redundant, but needed
                s.setProperty(name, c.get(j).getName(), c.get(j).getText());
            }
        }
        
        if(root.getChild("imagetypes")!=null){
            @SuppressWarnings("unchecked")
            List<Element> t = root.getChild("imagetypes").getChildren("imagetype");
            for(int i = 0;i<t.size();i++){
                String type = t.get(i).getAttribute("type").getValue();
                s.setImageType(type);
            }
        }
        //loadProperties(s, root);
        if(root.getChild("properties")!=null){
            for (Object next : root.getChild("properties").getChildren("property")) {
                Element e = (Element) next;
                
                try {
                    Class<?> cl;
                    Constructor<?> ctor;
                    // create key object
                    cl = Class.forName(e.getChild("key").getAttributeValue("class"));
                    ctor = cl.getConstructor(new Class<?>[] {String.class});
                    Object key = ctor.newInstance(new Object[] {e.getChild("key").getText()});

                    // create value object
                    Object value = null;
                    if (e.getChild("value") != null) {
                        cl = Class.forName(e.getChild("value").getAttributeValue("class"));
                        ctor = cl.getConstructor(new Class<?>[] {String.class});
                        value = ctor.newInstance(new Object[] {e.getChild("value").getText()});
                    }
                    
                    // store
                    s.setProperty(key, value);
                } catch (Exception ex) {
                    log.error("Error loading properties", ex);
                }
            }
        }
    }
    
    void loadProperties(NamedBean t, Element elem) {
        Element p = elem.getChild("properties");
        if (p == null) return;
        
    }

    /** 
     * XmlFile is abstract, so this extends for local use
     */
    static class AspectFile extends XmlFile {
    }
    
    public String getBeanTypeHandled(){
        return Bundle.getMessage("BeanNameSignalSystem");
    }

    static Logger log = LoggerFactory.getLogger(DefaultSignalSystemManager.class.getName());
}

/* @(#)DefaultSignalSystemManager.java */

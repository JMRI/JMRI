package jmri.implementation;

import java.net.URL;
import java.util.*;

import jmri.SignalHead;
import jmri.SignalSystem;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a basic signal mast aspect - appearance mapping.
 * <p>
 * The default contents are taken from the NamedBeanBundle properties file. This
 * makes creation a little more heavy-weight, but speeds operation.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalAppearanceMap extends AbstractNamedBean implements jmri.SignalAppearanceMap {

    public DefaultSignalAppearanceMap(String systemName, String userName) {
        super(systemName, userName);
    }

    public DefaultSignalAppearanceMap(String systemName) {
        super(systemName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameSignalAppMap");
    }

    static public DefaultSignalAppearanceMap getMap(String signalSystemName, String aspectMapName) {
        log.debug("getMap signalSystem= \"{}\", aspectMap= \"{}\"", signalSystemName, aspectMapName);
        DefaultSignalAppearanceMap map = maps.get("map:" + signalSystemName + ":" + aspectMapName);
        if (map == null) {
            log.debug("not located, request loadMap signalSystem= \"{}\", aspectMap= \"{}\"", signalSystemName, aspectMapName);
            map = loadMap(signalSystemName, aspectMapName);
        }
        return map;
    }

    // added 3.9.7 so CATS can create own implementations
    protected void registerMap() {
        maps.put(getSystemName(), this);
    }

    // added 3.9.7 so CATS can create own implementations
    static public DefaultSignalAppearanceMap findMap(String systemName) {
        return maps.get(systemName);
    }

    static DefaultSignalAppearanceMap loadMap(String signalSystemName, String aspectMapName) {
        DefaultSignalAppearanceMap map
                = new DefaultSignalAppearanceMap("map:" + signalSystemName + ":" + aspectMapName);
        maps.put("map:" + signalSystemName + ":" + aspectMapName, map);

        String path = "signals/" + signalSystemName + "/appearance-" + aspectMapName + ".xml";
        URL file = FileUtil.findURL(path, "resources", "xml");
        if (file == null) {
            log.error("appearance file (xml/{}) doesn't exist", path);
            throw new IllegalArgumentException("appearance file (xml/" + path + ") doesn't exist");
        }
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile() {
        };
        Element root;
        try {
            root = xf.rootFromURL(file);
            // get appearances

            List<Element> l = root.getChild("appearances").getChildren("appearance");

            // find all appearances, include them by aspect name, 
            log.debug("   reading {} aspectname elements", l.size());
            for (int i = 0; i < l.size(); i++) {
                String name = l.get(i).getChild("aspectname").getText();
                log.debug("aspect name {}", name);

                // add 'show' sub-elements as ints
                List<Element> c = l.get(i).getChildren("show");

                int[] appearances = new int[c.size()];
                for (int j = 0; j < c.size(); j++) {
                    // note: includes setting name; redundant, but needed
                    int ival;
                    String sval = c.get(j).getText().toUpperCase();
                    if (sval.equals("LUNAR")) {
                        ival = SignalHead.LUNAR;
                    } else if (sval.equals("GREEN")) {
                        ival = SignalHead.GREEN;
                    } else if (sval.equals("YELLOW")) {
                        ival = SignalHead.YELLOW;
                    } else if (sval.equals("RED")) {
                        ival = SignalHead.RED;
                    } else if (sval.equals("FLASHLUNAR")) {
                        ival = SignalHead.FLASHLUNAR;
                    } else if (sval.equals("FLASHGREEN")) {
                        ival = SignalHead.FLASHGREEN;
                    } else if (sval.equals("FLASHYELLOW")) {
                        ival = SignalHead.FLASHYELLOW;
                    } else if (sval.equals("FLASHRED")) {
                        ival = SignalHead.FLASHRED;
                    } else if (sval.equals("DARK")) {
                        ival = SignalHead.DARK;
                    } else {
                        log.error("found invalid content: {}", sval);
                        throw new JDOMException("invalid content: " + sval);
                    }

                    appearances[j] = ival;
                }
                map.addAspect(name, appearances);

                List<Element> img = l.get(i).getChildren("imagelink");
                loadImageMaps(img, name, map);

                // now add the rest of the attributes
                Hashtable<String, String> hm = new Hashtable<String, String>();

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
            log.debug("loading complete");
        } catch (java.io.IOException | org.jdom2.JDOMException e) {
            log.error("error reading file " + file.getPath(), e);
            return null;
        }

        return map;
    }

    static void loadImageMaps(List<Element> img, String name, DefaultSignalAppearanceMap map) {
        Hashtable<String, String> images = new Hashtable<String, String>();
        for (int j = 0; j < img.size(); j++) {
            String key = "default";
            if ((img.get(j).getAttribute("type")) != null) {
                key = img.get(j).getAttribute("type").getValue();
            }
            String value = img.get(j).getText();
            images.put(key, value);
        }
        map.aspectImageMap.put(name, images);
    }

    static void loadSpecificMap(String signalSystemName, String aspectMapName, DefaultSignalAppearanceMap SMmap, Element root) {
        log.debug("load specific signalSystem= \"{}\", aspectMap= \"{}\"" + signalSystemName, aspectMapName);
        loadSpecificAspect(signalSystemName, aspectMapName, HELD, SMmap, root);
        loadSpecificAspect(signalSystemName, aspectMapName, DANGER, SMmap, root);
        loadSpecificAspect(signalSystemName, aspectMapName, PERMISSIVE, SMmap, root);
        loadSpecificAspect(signalSystemName, aspectMapName, DARK, SMmap, root);
    }

    static void loadSpecificAspect(String signalSystemName, String aspectMapName, int aspectType, DefaultSignalAppearanceMap SMmap, Element root) {

        String child;
        switch (aspectType) {
            case HELD:
                child = "held";
                break;
            case DANGER:
                child = "danger";
                break;
            case PERMISSIVE:
                child = "permissive";
                break;
            case DARK:
                child = "dark";
                break;
            default:
                child = "danger";
        }

        String appearance = null;
        if (root.getChild("specificappearances") == null || root.getChild("specificappearances").getChild(child) == null) {
            log.debug("appearance not configured " + child);
            return;
        }
        try {
            appearance = root.getChild("specificappearances").getChild(child).getChild("aspect").getText();
            SMmap.specificMaps.put(aspectType, appearance);
        } catch (java.lang.NullPointerException e) {
            log.debug("aspect for specific appearance not configured " + child);
        }

        try {
            List<Element> img = root.getChild("specificappearances").getChild(child).getChildren("imagelink");
            String name = "$" + child;
            if (img.size() == 0) {
                if (appearance != null) {
                    //We do not have any specific images created, therefore we use the
                    //those associated with the aspect.
                    List<String> app = SMmap.getImageTypes(appearance);
                    Hashtable<String, String> images = new Hashtable<String, String>();
                    String type = "";
                    for (int i = 0; i < app.size(); i++) {
                        type = SMmap.getImageLink(appearance, app.get(i));
                        images.put(app.get(i), type);
                    }
                    //We will register the last aspect as a default.
                    images.put("default", type);
                    SMmap.aspectImageMap.put(name, images);
                }
            } else {
                loadImageMaps(img, name, SMmap);
                Hashtable<String, String> hm = new Hashtable<String, String>();

                //Register the last aspect as the default
                String key = img.get(img.size() - 1).getName();
                String value = img.get(img.size() - 1).getText();
                hm.put(key, value);

                SMmap.aspectAttributeMap.put(name, hm);
            }
        } catch (java.lang.NullPointerException e) {
            //Considered Normal if held aspect uses default signal appearance
        }
    }

    static void loadAspectRelationMap(String signalSystemName, String aspectMapName, DefaultSignalAppearanceMap SMmap, Element root) {
        if (log.isDebugEnabled()) {
            log.debug("load aspect relation map signalSystem= \"" + signalSystemName + "\", aspectMap= \"" + aspectMapName + "\"");
        }

        try {
            List<Element> l = root.getChild("aspectMappings").getChildren("aspectMapping");
            for (int i = 0; i < l.size(); i++) {
                String advanced = l.get(i).getChild("advancedAspect").getText();

                List<Element> o = l.get(i).getChildren("ourAspect");
                String[] appearances = new String[o.size()];
                for (int j = 0; j < o.size(); j++) {
                    appearances[j] = o.get(j).getText();
                }
                SMmap.aspectRelationshipMap.put(advanced, appearances);
            }

        } catch (java.lang.NullPointerException e) {
            log.debug("appearance not configured");
            return;
        }
    }

    /**
     * Get a property associated with a specific aspect.
     */
    @Override
    public String getProperty(String aspect, String key) {
        return aspectAttributeMap.get(aspect).get(key);
    }

    @Override
    public String getImageLink(String aspect, String type) {
        if (type == null || type.equals("")) {
            type = "default";
        }
        String value;
        try {
            value = aspectImageMap.get(aspect).get(type);
            //if we don't return a valid image set, then we will use which ever set is loaded in the getProperty
            if (value == null) {
                value = getProperty(aspect, "imagelink");
            }
        } catch (java.lang.NullPointerException e) {
            /* Can be considered normal for situations where a specific aspect
             has been asked for but it hasn't yet been loaded or configured */
            value = "";
        }
        return value;
    }

    @Override
    public Vector<String> getImageTypes(String aspect) {
        if (!checkAspect(aspect)) {
            return new Vector<String>();
        }
        Enumeration<String> e = aspectImageMap.get(aspect).keys();
        Vector<String> v = new Vector<String>();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }

    protected Hashtable<String, Hashtable<String, String>> aspectAttributeMap
            = new Hashtable<String, Hashtable<String, String>>();

    protected Hashtable<String, Hashtable<String, String>> aspectImageMap
            = new Hashtable<String, Hashtable<String, String>>();

    static HashMap<String, DefaultSignalAppearanceMap> maps
            = new LinkedHashMap<String, DefaultSignalAppearanceMap>();

    protected Hashtable<Integer, String> specificMaps
            = new Hashtable<Integer, String>();

    protected Hashtable<String, String[]> aspectRelationshipMap
            = new Hashtable<String, String[]>();

    public void loadDefaults() {

        log.debug("start loadDefaults");

        String ra;
        ra = Bundle.getMessage("SignalAspectDefaultRed");
        if (ra != null) {
            addAspect(ra, new int[]{SignalHead.RED});
        } else {
            log.error("no default red aspect");
        }

        ra = Bundle.getMessage("SignalAspectDefaultYellow");
        if (ra != null) {
            addAspect(ra, new int[]{SignalHead.YELLOW});
        } else {
            log.error("no default yellow aspect");
        }

        ra = Bundle.getMessage("SignalAspectDefaultGreen");
        if (ra != null) {
            addAspect(ra, new int[]{SignalHead.GREEN});
        } else {
            log.error("no default green aspect");
        }
    }

    @Override
    public boolean checkAspect(String aspect) {
        if (aspect == null) {
            return false;
        }
        return table.containsKey(aspect);// != null;
    }

    public void addAspect(String aspect, int[] appearances) {
        if (log.isDebugEnabled()) {
            log.debug("add aspect \"" + aspect + "\" for " + appearances.length + " heads "
                    + appearances[0]);
        }
        table.put(aspect, appearances);
    }

    /**
     * Provide the Aspect elements to GUI and store methods.
     *
     * @return all aspects in this signal mast appearance map, in the order defined in xml definition
     */
    @Override
    public Enumeration<String> getAspects() {
        log.debug("list of aspects provided");
        return new Vector<String>(table.keySet()).elements();  // this will be greatly simplified when we can just return keySet
    }

    @Override
    public String getSpecificAppearance(int appearance) {
        if (specificMaps.containsKey(appearance)) {
            return specificMaps.get(appearance);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "PZLA_PREFER_ZERO_LENGTH_ARRAYS",
        justification = "null returned is documented to mean no valid result")
    @Override
    public String[] getValidAspectsForAdvancedAspect(String advancedAspect) {
        if (aspectRelationshipMap == null) {
            log.error("aspect relationships have not been defined or loaded");
            throw new IllegalArgumentException("aspect relationships have not been defined or loaded");
        }
        if (advancedAspect == null) {
            String[] danger = new String[1];
            danger[0] = getSpecificAppearance(DANGER);
            return danger;
        }
        if (aspectRelationshipMap.containsKey(advancedAspect)) {
            //String[] validAspects = aspectRelationMap.get(advancedAspect);
            return aspectRelationshipMap.get(advancedAspect);
        }
        return null;
    }

    @Override
    public SignalSystem getSignalSystem() {
        return systemDefn;
    }

    public void setSignalSystem(SignalSystem t) {
        systemDefn = t;
    }
    protected SignalSystem systemDefn;

    /**
     * {@inheritDoc}
     *
     * This method returns a constant result on the DefaultSignalAppearanceMap.
     *
     * @return {@link jmri.NamedBean#INCONSISTENT}
     */
    @Override
    public int getState() {
        return INCONSISTENT;
    }

    /**
     * {@inheritDoc}
     *
     * This method has no effect on the DefaultSignalAppearanceMap.
     */
    @Override
    public void setState(int s) {
        // do nothing
    }

    public int[] getAspectSettings(String aspect) {
        return table.get(aspect);
    }

    protected HashMap<String, int[]> table = new LinkedHashMap<String, int[]>();

    @Override
    /**
     * {@inheritDoc}
     */
    public String summary() {
        StringBuilder retval = new StringBuilder();
        retval.append(toString());
        retval.append("\n  BeanType: "+getBeanType());
                
        retval.append("\n  aspects:");
        Enumeration<String> values = getAspects();
        while (values.hasMoreElements()) {
            String aspect = values.nextElement();
            retval.append("\n    aspect: "+aspect);
            retval.append("\n       len aspectSettings: "+getAspectSettings(aspect).length);
            retval.append("\n       attribute map:");
            Enumeration<String> keys = aspectAttributeMap.get(aspect).keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                retval.append("\n       key: "+key+" value: "+aspectAttributeMap.get(aspect).get(key));
            }
        }
        
        retval.append("\n  SignalSystem = "+getSignalSystem());
        
        return new String(retval);
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalAppearanceMap.class);

}

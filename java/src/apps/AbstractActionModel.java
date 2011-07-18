// AbstractActionModel.java

package apps;

import java.util.ResourceBundle;
import java.util.Enumeration;

/**
 * Provide services for invoking actions during configuration
 * and startup.
 * <P>
 * The action classes and corresponding human-readable names are kept in the 
 * apps.ActionListBundle properties file (which can be translated).
 * They are displayed in lexical order by human-readable name.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003, 2007
 * @version     $Revision$
 * @see PerformActionPanel
 */
public abstract class AbstractActionModel {

    public AbstractActionModel() {
        className="";
    }

    String className;

    public String getClassName() {
        return className;
    }

    public String getName() {
        for (int i =0; i< nameList().length; i++)
            try {
            if (classList()[i].getName().equals(className))
                return nameList()[i];
            } catch(java.lang.NullPointerException npe){
		        log.error("Caught Null Pointer Exception while searching for " +className+" with index "+i);
            }
        return null;
    }

    public void setName(String n) {
        for (int i =0; i< nameList().length; i++)
            if (nameList()[i].equals(n))
                className = classList()[i].getName();
    }

    public void setClassName(String n) {
        className = n;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    static public String[] nameList() {
        if (names==null) loadArrays();
        return names;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"EI_EXPOSE_REP", "MS_EXPOSE_REP"}) // OK until Java 1.6 allows return of cheap array copy
    static public Class<?>[] classList() {
        if (classes==null) loadArrays();
        return classes;
    }

    static private void loadArrays() {
        ResourceBundle rb = ResourceBundle.getBundle("apps.ActionListBundle");
        // count entries (not entirely efficiently!)
        int count = 0;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            count++;
            e.nextElement();
        }
        // create ordered array of names
        names = new String[count];
        int index = 0;
        e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            names[index] = rb.getString(key);
            index++;
        }
        jmri.util.StringUtil.sort(names);
                
        classes = new Class[count];
        // load them
        for (index = 0; index < names.length; index++) {
            // find the key corresponding to this name
            e = rb.getKeys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                if (names[index].equals(rb.getString(key))) {
                    // this is a hit,
                    // get class for key
                    try {
                        classes[index] = Class.forName(key);
                    } catch (ClassNotFoundException ex) {
                        log.error("Did not find class "+key);
                    }
                    break;
                }
            }
        }
    }

    static private String[] names = null;
    static private Class<?>[] classes = null;

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractActionModel.class.getName());

}



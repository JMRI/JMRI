// AbstractActionModel.java

package apps;

import java.util.ResourceBundle;
import java.util.Enumeration;

/**
 * Provide services for invoking actions during configuration
 * and startup.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.2 $
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
            if (classList()[i].getName().equals(className))
                return nameList()[i];
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

    static public String[] nameList() {
        if (names==null) loadArrays();
        return names;
    }
    static public Class[] classList() {
        if (classes==null) loadArrays();
        return classes;
    }

    static private void loadArrays() {
        ResourceBundle rb = ResourceBundle.getBundle("apps.ActionListBundle");
        // count entries (not entirely efficiently!)
        int count = 0;
        Enumeration e = rb.getKeys();
        while (e.hasMoreElements()) {
            count++;
            e.nextElement();
        }
        // create arrays
        classes = new Class[count];
        names = new String[count];
        // load them
        int index = 0;
        e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            names[index] = rb.getString(key);
            // get class for key
            try {
                classes[index] = Class.forName(key);
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
            index++;
        }
    }

    static private String[] names = null;
    static private Class[] classes = null;

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractActionModel.class.getName());

}



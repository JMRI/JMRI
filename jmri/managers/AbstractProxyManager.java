// AbstractProxyManager.java

package jmri.managers;

import jmri.Manager;
import jmri.NamedBean;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * Implementation of a Manager that can serves as a proxy
 * for multiple system-specific implementations.  The first to
 * be added is the "Primary".
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.5 $
 */
public class AbstractProxyManager implements Manager {

    public void dispose() {
        for (int i=0; i<mgrs.size(); i++)
            ( (Manager)mgrs.get(i)).dispose();
        mgrs.clear();
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * Forwards the register request to the matching system
     */
    public void register(NamedBean s) {
        String systemName = s.getSystemName();
        char systemLetter = systemName.charAt(0);

        for (int i = 0; i<mgrs.size(); i++)
             if ( systemLetter == ((Manager)mgrs.get(i)).systemLetter()) {
                ((Manager)mgrs.get(i)).register(s);
                return;
            }
    }

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i<mgrs.size(); i++)
            ((Manager)mgrs.get(i)).addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        for (int i = 0; i<mgrs.size(); i++)
            ((Manager)mgrs.get(i)).removePropertyChangeListener(l);
    }

    /**
     * @return The system-specific prefix letter for the primary implementation
     */
    public char systemLetter() {
	try {
          return ((Manager)mgrs.get(0)).systemLetter();
        } catch(IndexOutOfBoundsException ie) {
          return '\0';
        }
    }

    /**
     * @return The type letter for turnouts
     */
    public char typeLetter() {
        return ((Manager)mgrs.get(0)).typeLetter();
    }

    /**
     * @return A system name from a user input, typically a number.
     */
    public String makeSystemName(String s) {
        return ((Manager)mgrs.get(0)).makeSystemName(s);
    }

    /**
     * Get a list of all system names.
     */
    public List getSystemNameList() {
        ArrayList result = new ArrayList();
        for (int i = 0; i<mgrs.size(); i++)
            result.addAll( ((Manager)mgrs.get(i)).getSystemNameList() );
        return result;
    }

    List mgrs = new ArrayList();

    public void addManager(Manager m) {
        mgrs.add(m);
        log.debug("added manager");
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractProxyManager.class.getName());
}

/* @(#)AbstractProxyManager.java */

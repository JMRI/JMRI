package jmri.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.implementation.DefaultConditional;
import jmri.implementation.SensorGroupConditional;
import jmri.jmrit.beantable.LRouteTableAction;
import jmri.jmrit.sensorgroup.SensorGroupFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a ConditionalManager.
 * <P>
 * Note that Conditionals always have an associated parent Logix.
 * <P>
 * Logix system names must begin with IX, and be followed by a string, usually,
 * but not always, a number. The system names of Conditionals always begin with
 * the parent Logix's system name, then there is a capital C and a number.
 * <P>
 * Conditional system names are set automatically when the Conditional is
 * created. All alphabetic characters in a Conditional system name must be upper
 * case. This is enforced when a new Conditional is created via
 * {@link jmri.jmrit.beantable.LogixTableAction}
 * <p>
 * Conditional user names have specific requirements that are
 * addressed in the {@link jmri.Conditional} class.
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Pete Cresman Copyright (C) 2009
 */
public class DefaultConditionalManager extends AbstractManager
        implements ConditionalManager, java.beans.PropertyChangeListener {

    public DefaultConditionalManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.CONDITIONALS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'X';
    }

    /**
     * Method to create a new Conditional if the Conditional does not exist If
     * the parent Logix cannot be found, the userName cannot be checked, but the
     * Conditional is still created. The scenario can happen when a Logix is
     * loaded from a file after its Conditionals.
     *
     * @param systemName properly formatted system name for the new Conditional
     * @param userName must not be null, use "" instead
     * @return null if a Conditional with the same systemName or userName
     *         already exists, or if there is trouble creating a new Conditional
     */
    @Override
    public Conditional createNewConditional(String systemName, String userName) {
        Conditional c = null;

        // Check system name
        if (systemName != null && systemName.length() > 0) {
            c = getBySystemName(systemName);
            if (c != null) {
                return null;        // Conditional already exists
            }
        }

        // Get the potential parent Logix
        Logix lgx = getParentLogix(systemName);
        if (lgx == null) {
            log.error("Unable to find the parent logix for condtional '{}'", systemName);
            return null;
        }

        // Check the user name
        if (userName != null && userName.length() > 0) {
            c = getByUserName(lgx, userName);
            if (c != null) {
                return null;        // Duplicate user name within the parent Logix
            }
        }

        // Conditional does not exist, create a new Conditional
        if (systemName.startsWith(SensorGroupFrame.ConditionalSystemPrefix)) {
            c = new SensorGroupConditional(systemName, userName);
        } else {
            c = new DefaultConditional(systemName, userName);
        }
        // save in the maps
//@        register(c);

        boolean addCompleted = lgx.addConditional(systemName, c);
        if (!addCompleted) {
            return null;
        }

        return c;
    }

    /**
     * Do not insist that Conditional user names are unique,
     * unlike the usual NamedBean support
     */
    @Override
    protected void handleUserNameUniqueness(jmri.NamedBean s) {
        // eventually needs error checking and reporting
    }

    /**
     * Parses the Conditional system name to get the parent Logix system name,
     * then gets the parent Logix, and returns it.  For sensor groups, the parent
     * Logix name is 'SYS'.  LRoutes and exported Routes (RTX prefix) require
     * special logic
     *
     * @param name - system name of Conditional (must be trimmed and upper case)
     * @return the parent Logix or null
     */
    @Override
    public Logix getParentLogix(String name) {
        if (name.length() < 4) {
            return null;
        }

        String lgxName = "";
        if (name.startsWith(SensorGroupFrame.ConditionalSystemPrefix)) {
            lgxName = "SYS";
        } else {
            String pattern = "(.*?)(C\\d+)";                            // Default pattern: ???Cn
            if (name.startsWith(LRouteTableAction.LOGIX_SYS_NAME)) {    // LRoutes and exported Routes
                pattern = "(.*?)(\\d+[ALT])";                           // Pattern: ???nA, nL or nT
            }
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(name);
            if (m.find()) {
                lgxName = m.group(1);
            } else {
                //Unable to match parent logix name
                return null;
            }
        }
        return InstanceManager.getDefault(jmri.LogixManager.class).getBySystemName(lgxName);
    }

    /**
     * Remove an existing Conditional. Parent Logix must have been deactivated
     * before invoking this.
     */
    @Override
    public void deleteConditional(Conditional c) {
//@        deregister(c);
    }

    /**
     * Method to get an existing Conditional. First looks up assuming that name
     * is a User Name. Note: the parent Logix must be passed in x for user name
     * lookup. If this fails, or if x == null, looks up assuming that name is a
     * System Name. If both fail, returns null.
     *
     * @param x    - parent Logix (may be null)
     * @param name - name to look up
     * @return null if no match found
     */
    @Override
    public Conditional getConditional(Logix x, String name) {
        Conditional c = null;
        if (x != null) {
            c = getByUserName(x, name);
            if (c != null) {
                return c;
            }
        }
        return getBySystemName(name);
    }

    @Override
    public Conditional getConditional(String name) {
        Conditional c = getBySystemName(name);
        if (c == null) {
            c = getByUserName(name);
        }
        return c;
    }

    /*
     * Conditional user names are NOT unique.
     * @param key The user name
     * @return the conditional or null when not found or a duplicate
     */
    @Override
    public Conditional getByUserName(String key) {
        if (key == null) {
            return null;
        }

        Conditional c = null;
        Conditional chkC = null;

        for (String cName : getSystemNameList()) {
            chkC = getBySystemName(cName);
            if (chkC == null) {
                continue;
            }
            if (key.equals(chkC.getUserName())) {
                if (c == null) {
                    // Save first match
                    c = getBySystemName(chkC.getSystemName());
                    continue;
                }
                // Found a second match, give up
                log.warn("Duplicate conditional user names found, key = {}", key);
                return null;
            }
        }
        return c;
    }

    @Override
    public Conditional getByUserName(Logix x, String key) {
        if (x == null) {
            return null;
        }
        for (int i = 0; i < x.getNumConditionals(); i++) {
            Conditional c = getBySystemName(x.getConditionalByNumberOrder(i));
            if (c != null) {
                String uName = c.getUserName();
                if (key.equals(uName)) {
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    public Conditional getBySystemName(String name) {
        if (name == null) {
            return null;
        }
        Logix lgx = getParentLogix(name);
        if (lgx == null) {
            return null;
        }
        return lgx.getConditional(name);
//@        return (Conditional) _tsys.get(name);
    }

    /**
     * Get a list of all Conditional system names with the specified Logix
     * parent
     */
    @Override
    public List<String> getSystemNameListForLogix(Logix x) {
//        log.error("getSystemNameListForLogix - Not implemented yet.");
//        return null;
        if (x == null) {
            return null;
        }
        List<String> nameList = new ArrayList<>();

        for (int i = 0; i < x.getNumConditionals(); i++) {
            nameList.add(x.getConditionalByNumberOrder(i));
        }
        Collections.sort(nameList);
        return nameList;
    }

    /**
     * Get a list of all Conditional system names
     * Overrides the bean method
     * @since 4.7.4
     * @return a list of conditional system names regardless of parent Logix
     */
    @Override
    public List<String> getSystemNameList() {
        List<String> nameList = new ArrayList<>();

        jmri.LogixManager logixManager = InstanceManager.getDefault(jmri.LogixManager.class);
        for (String xName : logixManager.getSystemNameList()) {
            Logix lgx = logixManager.getLogix(xName);
            for (int i = 0; i < lgx.getNumConditionals(); i++) {
                nameList.add(lgx.getConditionalByNumberOrder(i));
            }
        }
        Collections.sort(nameList);
        return nameList;
    }

    static DefaultConditionalManager _instance = null;

    static public DefaultConditionalManager instance() {
        if (_instance == null) {
            _instance = new DefaultConditionalManager();
        }
        return (_instance);
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameConditional");
    }

    // --- Conditional Where Used processes ---

    /**
     * Maintain a list of conditionals that refer to a particular conditional.
     * @since 4.7.4
     */
    private HashMap<String, ArrayList<String>> conditionalWhereUsed = new HashMap<>();

    /**
     * Return a copy of the entire map.  Used by {@link LogixTableAction#buildWhereUsedListing}
     * @since 4.7.4
     */
    @Override
    public HashMap<String, ArrayList<String>> getWhereUsedMap() {
        return conditionalWhereUsed;
    }

    /**
     * Add a conditional reference to the array indicated by the target system name.
     * @since 4.7.4
     * @param target The system name for the target conditional
     * @paran reference The system name of the conditional that contains the conditional reference
     */
    @Override
    public void addWhereUsed(String target, String reference) {
        if (target == null || target.equals("")) {
            log.error("Invalid target name for addWhereUsed");
            return;
        }
        if (reference == null || reference.equals("")) {
            log.error("Invalid reference name for addWhereUsed");
            return;
        }

        if (conditionalWhereUsed.containsKey(target)) {
            ArrayList refList = conditionalWhereUsed.get(target);
            if (!refList.contains(reference)) {
                refList.add(reference);
                conditionalWhereUsed.replace(target, refList);
            }
        } else {
            ArrayList refList = new ArrayList<String>();
            refList.add(reference);
            conditionalWhereUsed.put(target, refList);
        }
    }

    /**
     * Get a list of conditional references for the indicated conditional
     * @since 4.7.4
     * @param target The target conditional for a conditional reference
     * @return an ArrayList or null if none
     */
    @Override
    public ArrayList<String> getWhereUsed(String target) {
        if (target == null || target.equals("")) {
            log.error("Invalid target name for getWhereUsed");
            return null;
        }
        return conditionalWhereUsed.get(target);
    }

    /**
     * Remove a conditional reference from the array indicated by the target system name.
     * @since 4.7.4
     * @param target The system name for the target conditional
     * @paran reference The system name of the conditional that contains the conditional reference
     */
    @Override
    public void removeWhereUsed(String target, String reference) {
        if (target == null || target.equals("")) {
            log.error("Invalid target name for removeWhereUsed");
            return;
        }
        if (reference == null || reference.equals("")) {
            log.error("Invalid reference name for removeWhereUsed");
            return;
        }

        if (conditionalWhereUsed.containsKey(target)) {
            ArrayList refList = conditionalWhereUsed.get(target);
            refList.remove(reference);
            if (refList.size() == 0) {
                conditionalWhereUsed.remove(target);
            }
        }
    }

    /**
     * Display the complete structure, used for debugging purposes.
     * @since 4.7.4
     */
    @Override
    public void displayWhereUsed() {
        log.info("- Display Conditional Where Used     ");
        SortedSet<String> keys = new TreeSet<>(conditionalWhereUsed.keySet());
        for (String key : keys) {
        log.info("    Target: {}                  ", key);
            ArrayList<String> refList = conditionalWhereUsed.get(key);
            for (String ref : refList) {
            log.info("      Reference: {}             ", ref);
            }
        }
    }

    /**
     * Get the target system names used by this conditional
     * @since 4.7.4
     * @param reference The system name of the conditional the refers to other conditionals.
     * @return a list of the target conditionals
     */
    @Override
    public ArrayList<String> getTargetList(String reference) {
        ArrayList<String> targetList = new ArrayList();
        SortedSet<String> keys = new TreeSet<>(conditionalWhereUsed.keySet());
        for (String key : keys) {
            ArrayList<String> refList = conditionalWhereUsed.get(key);
            for (String ref : refList) {
                if (ref.equals(reference)) {
                    targetList.add(key);
                }
            }
        }
        return targetList;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultConditionalManager.class.getName());
}

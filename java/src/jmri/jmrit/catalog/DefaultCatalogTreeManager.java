package jmri.jmrit.catalog;

import java.util.Set;
import jmri.CatalogTree;
import jmri.CatalogTreeManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.managers.AbstractManager;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide the concrete implementation for the Internal CatalogTree Manager.
 * <P>
 * Control of the systemName is internal so the more casual approach of
 * SignalHeadManager is used rather than the ProxyManager style.
 *
 * @author Pete Cressman Copyright (C) 2009
 *
 */
public class DefaultCatalogTreeManager extends AbstractManager<CatalogTree> implements CatalogTreeManager {

    public DefaultCatalogTreeManager() {
    }

    /**
     * Override parent method to not register this object to be stored
     * automatically as part of the general storage mechanism.
     */
    @Override
    protected void registerSelf() {
        log.debug("not registering");
    }

    @Override
    public int getXMLOrder() {
        return 65400;
    }

    /**
     * This is a bogus systemPrefix. Naming is enforced in method
     * createNewCatalogTree below.
     */
    @Override
    public String getSystemPrefix() {
        return "0";
    }

    /**
     * Bogus typeLetter
     */
    @Override
    public char typeLetter() {
        return '0';
    }

    @Override
    public CatalogTree getCatalogTree(String name) {
        CatalogTree t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public CatalogTree getBySystemName(String key) {
        String name = key.toUpperCase();
        if (log.isDebugEnabled()) {
            log.debug("getBySystemName: systemName= " + name);
            CatalogTree tree = _tsys.get(name);
            if (tree != null) {
                CatalogTreeNode root = tree.getRoot();
                log.debug("root= " + root.toString()
                        + ", has " + root.getChildCount() + " children");
            }
        }
        return _tsys.get(name);
    }

    @Override
    public CatalogTree getByUserName(String key) {
        return _tuser.get(key);
    }

    @Override
    public CatalogTree newCatalogTree(String sysName, String userName) {
        if (log.isDebugEnabled()) {
            log.debug("new CatalogTree: systemName= " + sysName
                    + ", userName= " + userName);
        }
        if (sysName == null) {
            log.error("SystemName cannot be null. UserName= " + userName);
            return null;
        }
        String systemName = sysName.toUpperCase();

        // return existing if there is one
        CatalogTree s;
        if ((userName != null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != s) {
                log.error("inconsistent user (" + userName + ") and system name (" + systemName + ") results; userName related to (" + s.getSystemName() + ")");
            }
            return s;
        }
        if ((s = getBySystemName(systemName)) != null) {
            if ((s.getUserName() == null) && (userName != null)) {
                s.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found memory via system name (" + systemName
                        + ") with non-null user name (" + userName + ")");
            }
            return s;
        }

        // doesn't exist, make a new one
        s = createNewCatalogTree(systemName, userName);

        // save in the maps
        register(s);
        return s;
    }

    /**
     * Create a CatalogTree.
     * <P>
     * Naming convention is:
     * <PRE>
     *   IF... - filter for image files from the file system
     *   SF... - filter for sound files from the file system
     *   TF... - filter for script files from the file system
     *   NF... - no filter for files from the file system
     *   IX... - index for image files stored in XML config file
     *   SX... - index for sound files stored in XML config file
     *   TX... - index for script files stored in XML config file
     *   NX... - index for files stored in XML config file
     * </PRE>
     *
     * @param systemName system name for catalog tree
     * @param userName   user name for catalog tree
     * @return the new catalog tree or null if unable to create
     */
    protected CatalogTree createNewCatalogTree(String systemName, String userName) {
        if (systemName == null || systemName.length() == 0) {
            log.error("Null systemName!");
            return null;
        }
        if (userName == null || userName.length() == 0) {
            log.error("Null userName!");
            return null;
        }
        if (systemName.charAt(1) == CatalogTree.XML) {
            switch (systemName.charAt(0)) {
                case CatalogTree.IMAGE:
                case CatalogTree.SOUND:
                case CatalogTree.SCRIPT:
                case CatalogTree.NOFILTER:
                    return new CatalogTreeIndex(systemName, userName);
                default:
                    log.error("Bad systemName: " + systemName + " (userName= " + userName + ")");
            }
        } else if (systemName.charAt(1) == CatalogTree.FILESYS) {
            CatalogTreeFS catTree;
            switch (systemName.charAt(0)) {
                case CatalogTree.IMAGE:
                    catTree = new CatalogTreeFS(systemName, userName);
                    catTree.setFilter(IMAGE_FILTER);
                    return catTree;
                case CatalogTree.SOUND:
                    catTree = new CatalogTreeFS(systemName, userName);
                    catTree.setFilter(SOUND_FILTER);
                    return catTree;
                case CatalogTree.SCRIPT:
                    catTree = new CatalogTreeFS(systemName, userName);
                    catTree.setFilter(SCRIPT_FILTER);
                    return catTree;
                case CatalogTree.NOFILTER:
                    return new CatalogTreeFS(systemName, userName);
                default:
                    log.error("Bad systemName: " + systemName + " (userName= " + userName + ")");
            }
        }
        return null;
    }

    /**
     *
     * @return the managed instance
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static DefaultCatalogTreeManager instance() {
        return InstanceManager.getDefault(DefaultCatalogTreeManager.class);
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameCatalog");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultCatalogTreeManager.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(CatalogTreeManager.class)) {
                return new DefaultCatalogTreeManager();
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(CatalogTreeManager.class);
            return set;
        }

    }
}

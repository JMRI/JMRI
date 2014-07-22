// DefaultCatalogTreeManager.java
package jmri.jmrit.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.CatalogTree;

/**
 * Provide the concrete implementation for the Internal CatalogTree Manager.
 * <P>
 * Control of the systemName is internal so the more casual approach of
 * SignalHeadManager is used rather than the ProxyManager style.
 *
 * @author			Pete Cressman Copyright (C) 2009
 *
 */
public class DefaultCatalogTreeManager extends jmri.managers.AbstractManager
                    implements jmri.CatalogTreeManager {

    public DefaultCatalogTreeManager() {
    }
  
    /**
     * Override parent method to not register this object to
     * be stored automatically as part of the general storage mechanism.
     **/
    protected void registerSelf() {
        log.debug("not registering");
    }
    
    public int getXMLOrder(){
        return 65400;
    }

    /**
    * This is a bogus systemPrefix.  Naming is enforced in method
    * createNewCatalogTree below.
    */
    public String getSystemPrefix() { return "0"; }

    /**
    *  Bogus typeLetter 
    */
    public char typeLetter() { return '0'; }

    public CatalogTree getCatalogTree(String name) {
        CatalogTree t = getByUserName(name);
        if (t!=null) return t;

        return getBySystemName(name);
    }

    public CatalogTree getBySystemName(String key) {
		String name = key.toUpperCase();
        if (log.isDebugEnabled()) {
            log.debug("getBySystemName: systemName= "+name);
            CatalogTree tree = (CatalogTree)_tsys.get(name);
            if (tree != null) {
                CatalogTreeNode root = (CatalogTreeNode)tree.getRoot();
                log.debug("root= "+root.toString()+
                          ", has "+root.getChildCount()+" children");
            }
        }
        return (CatalogTree)_tsys.get(name);
    }

    public CatalogTree getByUserName(String key) {
        return (CatalogTree)_tuser.get(key);
    }

    public CatalogTree newCatalogTree(String sysName, String userName) {
        if (log.isDebugEnabled()) log.debug("new CatalogTree: systemName= "+sysName
                                            +", userName= "+userName);
        if (sysName == null) {
        	log.error("SystemName cannot be null. UserName= "+userName);
        	return null;
        }
		String systemName = sysName.toUpperCase();

        // return existing if there is one
        CatalogTree s;
        if ( (userName!=null) && ((s = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName)!=s)
                log.error("inconsistent user ("+userName+") and system name ("+systemName+") results; userName related to ("+s.getSystemName()+")");
            return s;
        }
        if ( (s = getBySystemName(systemName)) != null) {
			if ((s.getUserName() == null) && (userName != null))
				s.setUserName(userName);
            else if (userName != null) log.warn("Found memory via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
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
            switch (systemName.charAt(0) ) {
                case CatalogTree.IMAGE:
                case CatalogTree.SOUND:
                case CatalogTree.SCRIPT:
                case CatalogTree.NOFILTER:
                    return new CatalogTreeIndex(systemName, userName);
                default:
                    log.error("Bad systemName: "+systemName+" (userName= "+userName+")");
            }
        } else if (systemName.charAt(1) == CatalogTree.FILESYS) {
            CatalogTreeFS catTree = null;
            switch (systemName.charAt(0) ) {
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
                    log.error("Bad systemName: "+systemName+" (userName= "+userName+")");
            }
        }
        return null;
    }

    public static DefaultCatalogTreeManager instance() {
     if (_instance == null) _instance = new DefaultCatalogTreeManager();
     return _instance;
    }
    private static DefaultCatalogTreeManager _instance;
    
    public String getBeanTypeHandled(){
        return Bundle.getMessage("BeanNameCatalog");
    }

    static Logger log = LoggerFactory.getLogger(DefaultCatalogTreeManager.class.getName());
}

/* @(#)CatalogTreeFSManager.java */

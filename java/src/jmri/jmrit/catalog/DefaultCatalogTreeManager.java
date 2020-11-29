package jmri.jmrit.catalog;

import java.util.Set;
import jmri.CatalogTree;
import jmri.CatalogTreeLeaf;
import jmri.CatalogTreeNode;
import jmri.CatalogTreeManager;
import jmri.InstanceInitializer;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Provide the concrete implementation for the Internal CatalogTree Manager.
 * <p>
 * Control of the systemName is internal so the more casual approach like that of
 * SignalHeadManager is used rather than the ProxyManager style.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class DefaultCatalogTreeManager extends AbstractManager<CatalogTree> implements CatalogTreeManager {

    private boolean _indexChanged = false;
    private boolean _indexLoaded = false;
    private ShutDownTask _shutDownTask;

    public DefaultCatalogTreeManager() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
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
     * Bogus typeLetter
     */
    @Override
    public char typeLetter() {
        return '0';
    }

    @Override
    public CatalogTree getCatalogTree(@Nonnull String name) {
        CatalogTree t = getByUserName(name);
        if (t != null) {
            return t;
        }
        return getBySystemName(name);
    }

    @Override
    public CatalogTree getBySystemName(@Nonnull String key) {
        if (log.isDebugEnabled()) {
            log.debug("getBySystemName: systemName= {}", key);
            CatalogTree tree = _tsys.get(key);
            if (tree != null) {
                CatalogTreeNode root = tree.getRoot();
                log.debug("root= {}, has {} children", root,root.getChildCount());
            }
        }
        return _tsys.get(key);
    }

    @Override
    public CatalogTree getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CatalogTree newCatalogTree(@Nonnull String systemName, String userName) {
        log.debug("new CatalogTree: systemName= {}, userName= {}", systemName, userName);
        if (systemName.length() == 0) {
            log.error("Empty systemName!");
            return null;
        }
        // return existing if there is one
        CatalogTree t;
        if ((userName != null) && ((t = getByUserName(userName)) != null)) {
            if (getBySystemName(systemName) != t) {
                log.error("inconsistent user ({}) and system name ({}) results; userName related to ({})",
                        userName, systemName, t.getSystemName());
            }
            return t;
        }
        if ((t = getBySystemName(systemName)) != null) {
            if ((t.getUserName() == null) && (userName != null)) {
                t.setUserName(userName);
            } else if (userName != null) {
                log.warn("Found memory via system name ({}) with non-null userName ({})",
                        systemName, userName);
            }
            return t;
        }
        // doesn't exist, make a new one
        t = createNewCatalogTree(systemName, userName);
        // save in the maps
        register(t);
        return t;
    }

    /**
     * Create a CatalogTree.
     * <p>
     * Naming convention is:
     * <pre>
     *   IF... - filter for image files from the file system
     *   SF... - filter for sound files from the file system
     *   TF... - filter for script files from the file system
     *   NF... - no filter for files from the file system
     *   IX... - index for image files stored in XML config file
     *   SX... - index for sound files stored in XML config file
     *   TX... - index for script files stored in XML config file
     *   NX... - index for files stored in XML config file
     * </pre>
     *
     * @param systemName system name for catalog tree, never null/empty
     * @param userName   user name for catalog tree
     * @return the new catalog tree or null if unable to create
     */
    protected CatalogTree createNewCatalogTree(@Nonnull String systemName, String userName) {
        if (systemName.length() == 0) {
            log.error("Empty systemName!");
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
                    log.error("Bad systemName: {} (userName= {})", systemName, userName);
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
                    log.error("Bad systemName: {} (userName= {})", systemName, userName);
            }
        }
        return null;
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameCatalogs" : "BeanNameCatalog");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<CatalogTree> getNamedBeanClass() {
        return CatalogTree.class;
    }

    @Override
    public void storeImageIndex() {
        jmri.jmrit.display.palette.ItemPalette.storeIcons();

        log.debug("Start writing CatalogTree info");
        try {
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().writeCatalogTrees();
            indexChanged(false);
        } catch (java.io.IOException ioe) {
            log.error("Exception writing CatalogTrees: ", ioe);
        }
    }

    /**
     * Load the index file, one time per session.
     */
    @Override
    public void loadImageIndex() {
        if (!isIndexLoaded()) {
            new jmri.jmrit.catalog.configurexml.DefaultCatalogTreeManagerXml().readCatalogTrees();
            _indexLoaded = true;
            log.debug("loadImageIndex: catalog file loaded");
        }
    }

    @Override
    public boolean isIndexChanged() {
        return _indexChanged;
    }

    @Override
    public boolean isIndexLoaded() {
        return _indexLoaded;
    }

    @Override
    public final synchronized void indexChanged(boolean changed) {
        _indexChanged = changed;
        jmri.ShutDownManager sdm = InstanceManager.getDefault(jmri.ShutDownManager.class);
        if (changed) {
            if (_shutDownTask == null) {
                _shutDownTask = new SwingShutDownTask("PanelPro Save default icon check",
                        Bundle.getMessage("IndexChanged"),
                        Bundle.getMessage("SaveAndQuit"), null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        return !_indexChanged;
                    }

                    @Override
                    public void didPrompt() {
                        storeImageIndex();
                    }
                };
                sdm.register(_shutDownTask);
            }
        } else if (_shutDownTask != null) {
            sdm.deregister(_shutDownTask);
            _shutDownTask = null;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultCatalogTreeManager.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        @Nonnull
        public <T> Object getDefault(Class<T> type) {
            if (type.equals(CatalogTreeManager.class)) {
                return new DefaultCatalogTreeManager();
            }
            return super.getDefault(type);
        }

        @Override
        @Nonnull
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(CatalogTreeManager.class);
            return set;
        }

    }

}

package jmri.jmrit.logix;

import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import jmri.InstanceManagerAutoDefault;
import jmri.ProvidingManager;
import jmri.SignalSystem;
import jmri.managers.AbstractManager;

/**
 * Basic Implementation of an OBlockManager.
 * <p>
 * Note that this does not enforce any particular system naming convention.
 * <p>
 * Note this is a concrete class, there are now 2 types of Blocks (LayoutBlocks
 * use a Block member and inheritance). Perhaps now the proxyManager
 * strategy of interface/implementation pairs like other Managers should be
 * implemented.
 *
 * @author Bob Jacobsen Copyright (C) 2006
 * @author Pete Cressman Copyright (C) 2009
 */
public class OBlockManager extends AbstractManager<OBlock>
        implements ProvidingManager<OBlock>, InstanceManagerAutoDefault {

    @SuppressWarnings("deprecation")
    public OBlockManager() {
        super(new jmri.jmrix.ConflictingSystemConnectionMemo("O", "OBlocks")); // NOI18N
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.OBLOCKS;
    }

    @Override
    public char typeLetter() {
        return 'B';
    }

    /**
     * Create a new OBlock if it does not exist Returns null if a
     * OBlock with the same systemName or userName already exists, or if there
     * is trouble creating a new OBlock.
     *
     * @param systemName System name
     * @param userName   User name
     * @return newly created OBlock
     */
    public OBlock createNewOBlock(String systemName, String userName) {
        // Check that OBlock does not already exist
        OBlock r;
        if (userName != null && (userName.trim().length() > 0)) {
            r = getByUserName(userName);
            if (r != null) {
                return null;
            }
        }
        if (!systemName.startsWith("OB")) {
            return null;
        }
        if (systemName.length() < 3) {
            return null;
        }
        r = getBySystemName(systemName);
        if (r != null) {
            return null;
        }
        // OBlock does not exist, create a new OBlock
        r = new OBlock(systemName, userName);
        // save in the maps
        register(r);
        return r;
    }

    /**
     * Get an existing OBlock. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     *
     * @param name OBlock name
     * @return OBlock, if found
     */
    public OBlock getOBlock(String name) {
        OBlock r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    public OBlock getBySystemName(String name) {
        return _tsys.get(name);
    }

    public OBlock getByUserName(String key) {
        return _tuser.get(key);
    }

    @Override
    public OBlock provide(String name) throws IllegalArgumentException {
        return provideOBlock(name);
    }

    @Nonnull
    public OBlock provideOBlock(String name) throws IllegalArgumentException {
        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name \"" + name + "\" invalid");
        }
        OBlock ob = getByUserName(name);
        if (ob == null) {
            ob = getBySystemName(name);
        }
        if (ob == null) {
            ob = createNewOBlock(name, null);
            if (ob == null) {
                throw new IllegalArgumentException("could not create OBlock \"" + name + "\"");
            }
        }
        return ob;
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameOBlocks" : "BeanNameOBlock");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<OBlock> getNamedBeanClass() {
        return OBlock.class;
    }

}

package jmri.jmrix.openlcb;

import java.util.*;

import javax.annotation.Nonnull;
import jmri.BooleanPropertyDescriptor;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanPropertyDescriptor;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;
import org.openlcb.OlcbInterface;

/**
 * OpenLCB implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 * @since 2.3.1
 */
public class OlcbTurnoutManager extends AbstractTurnoutManager {

    public OlcbTurnoutManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    // Whether we accumulate partially loaded turnouts in pendingTurnouts.
    private boolean isLoading = false;
    // Turnouts that are being loaded from XML.
    private final ArrayList<OlcbTurnout> pendingTurnouts = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    @Override
    @Nonnull
    public List<NamedBeanPropertyDescriptor<?>> getKnownBeanProperties() {
        List<NamedBeanPropertyDescriptor<?>> l = new ArrayList<>();
        l.add(new BooleanPropertyDescriptor(OlcbUtils.PROPERTY_IS_AUTHORITATIVE, OlcbTurnout
                .DEFAULT_IS_AUTHORITATIVE) {
            @Override
            public String getColumnHeaderText() {
                return Bundle.getMessage("OlcbStateAuthHeader");
            }

            @Override
            public boolean isEditable(NamedBean bean) {
                return OlcbUtils.isOlcbBean(bean);
            }
        });
        l.add(new BooleanPropertyDescriptor(OlcbUtils.PROPERTY_LISTEN, OlcbTurnout
                .DEFAULT_LISTEN) {
            @Override
            public String getColumnHeaderText() {
                return Bundle.getMessage("OlcbStateListenHeader");
            }

            @Override
            public boolean isEditable(NamedBean bean) {
                return OlcbUtils.isOlcbBean(bean);
            }
        });
        return l;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return never null
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        OlcbTurnout t = new OlcbTurnout(getSystemPrefix(), addr, memo.get(OlcbInterface.class));
        t.setUserName(userName);
        synchronized (pendingTurnouts) {
            if (isLoading) {
                pendingTurnouts.add(t);
            } else {
                t.finishLoad();
            }
        }
        return t;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created turnouts until finishLoad because the feedback type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        synchronized (pendingTurnouts) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all turnouts are instantiated
     * and their feedback type is read in. We use this hook to finalize the construction of the
     * OpenLCB objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        synchronized (pendingTurnouts) {
            pendingTurnouts.forEach(OlcbTurnout::finishLoad);
            pendingTurnouts.clear();
            isLoading = false;
        }
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return false;
    }

    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        // don't check for integer; should check for validity here
        try {
            OlcbAddress.validateSystemNameFormat(curAddress,Locale.getDefault(),getSystemNamePrefix());
        } catch (jmri.NamedBean.BadSystemNameException e) {
            throw new JmriException(e.getMessage());
        }
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {
        // always return this (the current) name without change
        try {
            OlcbAddress.validateSystemNameFormat(curAddress,Locale.getDefault(),prefix+"T");
        } catch (jmri.NamedBean.BadSystemNameException e) {
            throw new JmriException(e.getMessage());
        }
        return curAddress;
    }
    
    /**
     * Validates to OpenLCB format.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        name = super.validateSystemNameFormat(name,locale);
        return OlcbAddress.validateSystemNameFormat(name,locale,getSystemNamePrefix());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddTurnoutEntryToolTip");
    }

}

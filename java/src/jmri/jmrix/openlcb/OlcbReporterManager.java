package jmri.jmrix.openlcb;

import jmri.BooleanPropertyDescriptor;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanPropertyDescriptor;
import jmri.Reporter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.openlcb.OlcbInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Manage the OpenLCB-specific Reporter implementation.
 *
 * System names are "MRaa.aa.aa.aa.aa.aa.00.00", where M is the user configurable system prefix,
 * aa.aa....aa is an OpenLCB Event ID with the last two bytes as zero.
 *
 * Typical event IDs for reporters come out of the range 06.4* and 06.5*.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 * @author Balazs Racz Copyright (C) 2023
 * @since 5.3.5
 */
public class OlcbReporterManager extends jmri.managers.AbstractReporterManager {

    // Whether we accumulate loaded objects in pendingReporters.
    private boolean isLoading = false;
    // Turnouts that are being loaded from XML.
    private final ArrayList<OlcbReporter> pendingReporters = new ArrayList<>();

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

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    // Implemented ready for new system connection memo
    public OlcbReporterManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "SLF4J_FORMAT_SHOULD_BE_CONST",
        justification = "passing exception text")
    protected Reporter createNewReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        String addr = systemName.substring(getSystemNamePrefix().length());
        // first, check validity
        try {
            validateSystemNameFormat(systemName,Locale.getDefault());
        } catch (NamedBean.BadSystemNameException e) {
            log.error(e.getMessage());
            throw e;
        }
        // OK, make
        OlcbReporter s = new OlcbReporter(getSystemPrefix(), addr, memo.get(OlcbInterface.class));
        s.setUserName(userName);

        synchronized (pendingReporters) {
            if (isLoading) {
                pendingReporters.add(s);
            } else {
                s.finishLoad();
            }
        }
        return s;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created Reporters until finishLoad. This avoids certain quadratic run-time
     * operations due to update listeners.
     */
    public void startLoad() {
        log.debug("Reporter manager : start load");
        synchronized (pendingReporters) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all Reporters are instantiated
     * and their feedback type is read in. We use this hook to finalize the construction of the
     * OpenLCB objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        log.debug("Reporter manager : finish load");
        synchronized (pendingReporters) {
            pendingReporters.forEach(OlcbReporter::finishLoad);
            pendingReporters.clear();
            isLoading = false;
        }
    }

    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        String tmpPrefix = prefix + typeLetter();
        String tmpSName  = tmpPrefix + curAddress;
        try {
            OlcbAddress.validateSystemNameFormat(tmpSName,Locale.getDefault(),tmpPrefix);
        }
        catch ( NamedBean.BadSystemNameException ex ){
            throw new JmriException(ex.getMessage());
        }
        // don't check for integer; should check for validity here
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    @Override
    @Nonnull
    @javax.annotation.CheckReturnValue
    public String getNextValidSystemName(@Nonnull NamedBean currentBean) throws JmriException {
        String currentName = currentBean.getSystemName();
        return incrementSystemName(currentName);
    }

    /**
     * Computes the system name for the next block sensor. This increments the unique ID
     * of the manufacturer-assigned range (bytes 4-5-6) by one.
     * @param currentName system name for a reporter of a given block
     * @return next block's system name.
     */
    public String incrementSystemName(String currentName) {
        String oAddr = currentName.substring(getSystemNamePrefix().length());
        OlcbAddress a = new OlcbAddress(oAddr);
        // Increments address elements 4-5-6 with overflow.
        int[] e = a.elements();
        int idx = 5;
        while(idx > 2) {
            e[idx]++;
            if (e[idx] > 255) {
                e[idx] = 0;
                --idx;
            } else {
                break;
            }
        }
        // Render new value.
        String newValue = a.toDottedString();
        return getSystemNamePrefix() + newValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddReporterEntryToolTip");
    }

    /**
     * Validates to OpenLCB format.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) throws NamedBean.BadSystemNameException {
        name = super.validateSystemNameFormat(name,locale);
        name = OlcbAddress.validateSystemNameFormat(name,locale,getSystemNamePrefix());
        return name;
    }

    private static final Logger log = LoggerFactory.getLogger(OlcbReporterManager.class);

}



package jmri.jmrix.openlcb;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.StringIO;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Implement a StringIOManager for OpenLCB StringIOs.
 *
 * @author Bob Jacobsen      Copyright (C) 2024
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class OlcbStringIOManager extends jmri.managers.AbstractStringIOManager {

    // Whether we accumulate partially loaded objects in pendingStringIOs.
    private boolean isLoading = false;
    // Turnouts that are being loaded from XML.
    private final ArrayList<OlcbStringIO> pendingStringIOs = new ArrayList<>();

    public OlcbStringIOManager(CanSystemConnectionMemo memo) {
        super(memo);
    }

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
    public StringIO provideStringIO(@Nonnull String sName) throws IllegalArgumentException {
        String name = sName.substring(getSystemPrefix().length()+1);
        return new OlcbStringIO(getSystemPrefix(), name, (CanSystemConnectionMemo) memo);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public StringIO provide(@Nonnull String name) throws IllegalArgumentException { 
        return provideStringIO(name); 
    }

    @Override
    @Nonnull
    public StringIO createNewStringIO(String sName, String uName) {
        String name = sName.substring(getSystemPrefix().length()+1);  // plus one for type letter
        var s = new OlcbStringIO(getSystemPrefix(), name, (CanSystemConnectionMemo) memo);
        if (uName != null) s.setUserName(uName);
        synchronized (pendingStringIOs) {
            if (isLoading) {
                pendingStringIOs.add(s);
            } else {
                s.finishLoad();
            }
        }
        return s;
    }
    
    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created Sensors until finishLoad because the feedback type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        log.debug("StringIO manager : start load");
        synchronized (pendingStringIOs) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all Sensors are instantiated
     * and their feedback type is read in. We use this hook to finalize the construction of the
     * OpenLCB objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        log.debug("StringIO manager : finish load");
        synchronized (pendingStringIOs) {
            pendingStringIOs.forEach(OlcbStringIO::finishLoad);
            pendingStringIOs.clear();
            isLoading = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddStringIOEntryToolTip");
    }

    /**
     * Validates to OpenLCB format.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        name = super.validateSystemNameFormat(name,locale);
        name = OlcbAddress.validateSystemNameFormat(name,locale,getSystemNamePrefix(), (CanSystemConnectionMemo) memo);
        return name;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbStringIOManager.class);
}

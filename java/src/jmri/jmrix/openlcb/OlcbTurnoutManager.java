package jmri.jmrix.openlcb;

import java.util.ArrayList;
import java.util.List;
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
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    @Override
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
     */
    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
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
            pendingTurnouts.forEach((t) -> {
                t.finishLoad();
            });
            pendingTurnouts.clear();
            isLoading = false;
        }
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        // don't check for integer; should check for validity here
        try {
            validateAddressFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        // always return this (the current) name without change
        try {
            validateAddressFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return curAddress;
    }

    void validateAddressFormat(String address) throws IllegalArgumentException {
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("Did not find usable system name: " + address + " to a valid Olcb turnout address");
        }
        switch (v.length) {
            case 1:
                if (address.startsWith("+") || address.startsWith("-")) {
                    break;
                }
                throw new IllegalArgumentException("can't make 2nd event from systemname " + address);
            case 2:
                break;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddTurnoutEntryToolTip");
    }

}

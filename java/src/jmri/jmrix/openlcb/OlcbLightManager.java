/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.openlcb;

import java.util.ArrayList;
import java.util.List;
import jmri.BooleanPropertyDescriptor;
import jmri.Light;
import jmri.NamedBean;
import jmri.NamedBeanPropertyDescriptor;
import jmri.managers.AbstractLightManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.openlcb.OlcbInterface;

/**
 *
 * @author jcollell
 */
public class OlcbLightManager extends AbstractLightManager {

    public OlcbLightManager(CanSystemConnectionMemo memo) {
        super(memo);
    }
    
    // Whether we accumulate partially loaded lights in pendingLights.
    private boolean isLoading = false;
    // Lights that are being loaded from XML.
    private final ArrayList<OlcbLight> pendingLights = new ArrayList<>();

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
        l.add(new BooleanPropertyDescriptor(OlcbUtils.PROPERTY_IS_AUTHORITATIVE, OlcbLight
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
        l.add(new BooleanPropertyDescriptor(OlcbUtils.PROPERTY_LISTEN, OlcbLight
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
    protected Light createNewLight(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        OlcbLight l = new OlcbLight(getSystemPrefix(), addr, memo.get(OlcbInterface.class));
        l.setUserName(userName);
        synchronized (pendingLights) {
            if (isLoading) {
                pendingLights.add(l);
            } else {
                l.finishLoad();
            }
        }
        return l;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created lights until finishLoad because the feedback type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        synchronized (pendingLights) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all lights are instantiated
     * and their feedback type is read in. We use this hook to finalize the construction of the
     * OpenLCB objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        synchronized (pendingLights) {
            for (OlcbLight l : pendingLights) {
                l.finishLoad();
            }
            pendingLights.clear();
            isLoading = false;
        }
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public boolean validSystemNameConfig(String address) throws IllegalArgumentException {
        String withoutPrefix = address.replace("ML", "");
        OlcbAddress a = new OlcbAddress(withoutPrefix);
        OlcbAddress[] v = a.split();
        switch (v.length) {
            case 1:
                if (address.startsWith("+") || address.startsWith("-")) {
                    return false;
                }
                throw new IllegalArgumentException("can't make 2nd event from systemname " + address);
            case 2:
                return true;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddLightEntryToolTip");
    }
}

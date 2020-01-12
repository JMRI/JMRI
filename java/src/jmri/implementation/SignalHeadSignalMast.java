package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.SignalHead;
import jmri.SignalMast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignalMast implemented via one SignalHead object.
 * <p>
 * System name specifies the creation information:
 * <pre>
 * IF$shsm:basic:one-searchlight:(IH1)(IH2)
 * </pre>
 * The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(IH1)(IH2) - colon-separated list of names for SignalHeads
 * </ul>
 * There was an older form where the SignalHead names were also colon separated:
 * IF$shsm:basic:one-searchlight:IH1:IH2 This was deprecated because colons appear in
 * e.g. SE8c system names.
 * <ul>
 * <li>IF$shsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>IH1:IH2 - colon-separated list of names for SignalHeads
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class SignalHeadSignalMast extends AbstractSignalMast {

    public SignalHeadSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public SignalHeadSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
    }

    private static final String mastType = "IF$shsm";

    private void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: {}", systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals(mastType)) {
            log.warn("SignalMast system name should start with {} but is {}", mastType, systemName);
        }
        String prefix = parts[0];
        String system = parts[1];
        String mast = parts[2];

        // if "mast" contains (, it's a new style
        if (mast.indexOf('(') == -1) {
            // old style
            setMastType(mast);
            configureSignalSystemDefinition(system);
            configureAspectTable(system, mast);
            configureHeads(parts, 3);
        } else {
            // new style
            mast = mast.substring(0, mast.indexOf("("));
            setMastType(mast);
            String interim = systemName.substring(prefix.length() + 1 + system.length() + 1);
            String parenstring = interim.substring(interim.indexOf("("), interim.length());
            java.util.List<String> parens = jmri.util.StringUtil.splitParens(parenstring);
            configureSignalSystemDefinition(system);
            configureAspectTable(system, mast);
            String[] heads = new String[parens.size()];
            int i = 0;
            for (String p : parens) {
                heads[i] = p.substring(1, p.length() - 1);
                i++;
            }
            configureHeads(heads, 0);
        }
    }

    private void configureHeads(String parts[], int start) {
        heads = new ArrayList<NamedBeanHandle<SignalHead>>();
        for (int i = start; i < parts.length; i++) {
            String name = parts[i];
            // check head exists
            SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);
            if (head == null) {
                log.warn("Attempting to create Mast from non-existant signal head {}", name);
                continue;
            }
            NamedBeanHandle<SignalHead> s
                    = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, head);
            heads.add(s);
        }
    }

    @Override
    public void setAspect(@Nonnull String aspect) {
        // check it's a choice
        if (!map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
        } else if (disabledAspects.contains(aspect)) {
            log.warn("attempting to set an aspect that has been disabled: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
        }

        // set the outputs
        if (log.isDebugEnabled()) {
            log.debug("setAspect \"{}\", numHeads= {}", aspect, heads.size());
        }
        setAppearances(aspect);
        // do standard processing
        super.setAspect(aspect);
    }

    @Override
    public void setHeld(boolean state) {
        // set all Heads to state
        for (NamedBeanHandle<SignalHead> h : heads) {
            try {
                h.getBean().setHeld(state);
            } catch (java.lang.NullPointerException ex) {
                log.error("NPE caused when trying to set Held due to missing signal head in mast {}", getDisplayName());
            }
        }
        super.setHeld(state);
    }

    @Override
    public void setLit(boolean state) {
        // set all Heads to state
        for (NamedBeanHandle<SignalHead> h : heads) {
            try {
                h.getBean().setLit(state);
            } catch (java.lang.NullPointerException ex) {
                log.error("NPE caused when trying to set Lit due to missing signal head in mast {}", getDisplayName());
            }
        }
        super.setLit(state);
    }

    private List<NamedBeanHandle<SignalHead>> heads;

    public List<NamedBeanHandle<SignalHead>> getHeadsUsed() {
        return heads;
    }

    // taken out of the defaultsignalappearancemap
    public void setAppearances(String aspect) {
        if (map == null) {
            log.error("No appearance map defined, unable to set appearance {}", getDisplayName());
            return;
        }
        if (map.getSignalSystem() != null && map.getSignalSystem().checkAspect(aspect) && map.getAspectSettings(aspect) != null) {
            log.warn("Attempt to set {} to undefined aspect: {}", getSystemName(), aspect);
        } else if ((map.getAspectSettings(aspect) != null) && (heads.size() > map.getAspectSettings(aspect).length)) {
            log.warn("setAppearance to \"{}\" finds {} heads but only {} settings", aspect, heads.size(), map.getAspectSettings(aspect).length);
        }

        int delay = 0;
        try {
            if (map.getProperty(aspect, "delay") != null) {
                delay = Integer.parseInt(map.getProperty(aspect, "delay"));
            }
        } catch (Exception e) {
            log.debug("No delay set");
            //can be considered normal if does not exists or is invalid
        }
        HashMap<SignalHead, Integer> delayedSet = new HashMap<SignalHead, Integer>(heads.size());
        for (int i = 0; i < heads.size(); i++) {
            // some extensive checking
            boolean error = false;
            if (heads.get(i) == null) {
                log.error("Head {} unexpectedly null in setAppearances while setting aspect \"{}\" for {}", i, aspect, getSystemName());
                error = true;
            }
            if (heads.get(i).getBean() == null) {
                log.error("Head {} getBean() unexpectedly null in setAppearances while setting aspect \"{}\" for {}", i, aspect, getSystemName());
                error = true;
            }
            if (map.getAspectSettings(aspect) == null) {
                log.error("Couldn't get table array for aspect \"{}\" in setAppearances for {}", aspect, getSystemName());
                error = true;
            }

            if (!error) {
                SignalHead head = heads.get(i).getBean();
                int[] dsam = map.getAspectSettings(aspect);
                if (i < dsam.length) {
                    int toSet = dsam[i];
                    if (delay == 0) {
                        head.setAppearance(toSet);
                        log.debug("Setting {} to {}", head.getSystemName(),
                                head.getAppearanceName(toSet));
                    } else {
                        delayedSet.put(head, toSet);
                    }
                } else {
                    log.error("     head '{}' appearance not set for aspect '{}'", head.getSystemName(), aspect);
                }
            } else {
                log.error("     head appearance not set due to above error");
            }
        }
        if (delay != 0) {
            // If a delay is required we will fire this off into a seperate thread and let it get on with it.
            final HashMap<SignalHead, Integer> thrDelayedSet = delayedSet;
            final int thrDelay = delay;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    setDelayedAppearances(thrDelayedSet, thrDelay);
                }
            };
            Thread thr = new Thread(r);
            thr.setName(getDisplayName() + " delayed set appearance");
            thr.setDaemon(true);
            try {
                thr.start();
            } catch (java.lang.IllegalThreadStateException ex) {
                log.error(ex.toString());
            }
        }
    }

    private void setDelayedAppearances(final HashMap<SignalHead, Integer> delaySet, final int delay) {
        for (SignalHead head : delaySet.keySet()) {
            final SignalHead thrHead = head;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        thrHead.setAppearance(delaySet.get(thrHead));
                        if (log.isDebugEnabled()) {
                            log.debug("Setting {} to {}", thrHead.getSystemName(),
                                    thrHead.getAppearanceName(delaySet.get(thrHead)));
                        }
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            };

            Thread thr = new Thread(r);
            thr.setName(getDisplayName());
            thr.setDaemon(true);
            try {
                thr.start();
                thr.join();
            } catch (java.lang.IllegalThreadStateException | InterruptedException ex) {
                log.error(ex.toString());
            }
        }
    }

    public static List<SignalHead> getSignalHeadsUsed() {
        List<SignalHead> headsUsed = new ArrayList<SignalHead>();
        for (SignalMast mast : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet()) {
            if (mast instanceof jmri.implementation.SignalHeadSignalMast) {
                java.util.List<NamedBeanHandle<SignalHead>> masthead = ((jmri.implementation.SignalHeadSignalMast) mast).getHeadsUsed();
                for (NamedBeanHandle<SignalHead> bean : masthead) {
                    headsUsed.add(bean.getBean());
                }
            }
        }
        return headsUsed;
    }

    public static String isHeadUsed(SignalHead head) {
        for (SignalMast mast : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet()) {
            if (mast instanceof jmri.implementation.SignalHeadSignalMast) {
                java.util.List<NamedBeanHandle<SignalHead>> masthead = ((jmri.implementation.SignalHeadSignalMast) mast).getHeadsUsed();
                for (NamedBeanHandle<SignalHead> bean : masthead) {
                    if ((bean.getBean()) == head) {
                        return mast.getDisplayName();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        NamedBean nb = (NamedBean) evt.getOldValue();
        if ("CanDelete".equals(evt.getPropertyName())) { //IN18N
            if (nb instanceof SignalHead) {
                for (NamedBeanHandle<SignalHead> bean : getHeadsUsed()) {
                    if (bean.getBean().equals(nb)) {
                        java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                        throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseSignalHeadSignalMastVeto", getDisplayName()), e);
                    }
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadSignalMast.class);

}

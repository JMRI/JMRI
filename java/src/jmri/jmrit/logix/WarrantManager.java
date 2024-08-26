package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ShutDownTask;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Basic Implementation of a WarrantManager.
 * <p>
 * Note this is a concrete class.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class WarrantManager extends AbstractManager<Warrant>
        implements jmri.InstanceManagerAutoDefault {

    private HashMap<String, RosterSpeedProfile> _mergeProfiles = new HashMap<>();
    private ShutDownTask _shutDownTask = null;
    private boolean _suppressWarnings = false;

    public WarrantManager() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.WARRANTS;
    }

    @Override
    public char typeLetter() {
        return 'W';
    }

    /**
     * Method to create a new Warrant if it does not exist.
     * <p>
     * Returns null if a Warrant with the same systemName or userName already 
     * exists, or if there is trouble creating a new Warrant.
     *
     * @param systemName the system name.
     * @param userName   the user name.
     * @param sCWa       true for a new SCWarrant, false for a new Warrant.
     * @param tTP        the time to platform.
     * @return an existing warrant if found or a new warrant, may be null.
     */
    public Warrant createNewWarrant(String systemName, String userName, boolean sCWa, long tTP) {
        log.debug("createNewWarrant {} SCWa= {}",systemName,sCWa);
        // Check that Warrant does not already exist
        Warrant r;
        if (userName != null && userName.trim().length() > 0) {
            r = getByUserName(userName);
            if (r == null) {
                r = getBySystemName(systemName);
            }
            if (r != null) {
                log.warn("Warrant {}  exits.",r.getDisplayName());
                return null;
            }
        }
        if (!systemName.startsWith(getSystemNamePrefix()) || systemName.length() < getSystemNamePrefix().length()+1) {
            log.error("Warrant system name \"{}\" must begin with \"{}\".",
                    systemName, getSystemNamePrefix());
            return null;
        }
        // Warrant does not exist, create a new Warrant
        if (sCWa) {
            r = new SCWarrant(systemName, userName, tTP);
        } else {
            r = new Warrant(systemName, userName);
        }
        // save in the maps
        register(r);
        return r;
    }

    /**
     * Method to get an existing Warrant. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     *
     * @param name the system name or user name
     * @return the warrant if found or null
     */
    public Warrant getWarrant(String name) {
        Warrant r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    public Warrant provideWarrant(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        Warrant w = getByUserName(name);
        if (w == null) {
            w = getBySystemName(name);
        }
        if (w == null) {
            w = createNewWarrant(name, null, false, 0);
        }
        return w;
    }

    protected boolean okToRemoveBlock( @Nonnull OBlock block) {
        String name = block.getDisplayName();
        List<Warrant> list = warrantsUsing(block);
        boolean ok = true;
        if (!list.isEmpty()) {
//            ok = false;   Last setting was OK = true when _suppressWarnings was set to true
            if (!_suppressWarnings) {
                StringBuilder sb = new StringBuilder();
                for (Warrant w : list) {
                    sb.append(Bundle.getMessage("DeleteWarrantBlock", name, w.getDisplayName()));
                }
                sb.append(Bundle.getMessage("DeleteConfirm", name));
                ok = okToRemove(name, sb.toString());
            }
        }
        if (ok) {
            removeWarrants(list);
        }
        return ok;
    }
    
    protected boolean okToRemovePortal(Portal portal) {
        String name = portal.getName();
        boolean ok = true;
        List<Warrant> wList = warrantsUsing(portal);
        if (!wList.isEmpty()) {
//          ok = false;   Last setting was OK = true when _suppressWarnings was set to true
            if (!_suppressWarnings) {
                StringBuilder sb = new StringBuilder();
                for (Warrant w : wList) {
                    sb.append(Bundle.getMessage("DeleteWarrantPortal", name, w.getDisplayName()));
                 }
                sb.append(Bundle.getMessage("DeleteConfirm", name));
                ok = okToRemove(name, sb.toString());
            }
        }
        List<NamedBean> sList = signalsUsing(portal);
        if (!sList.isEmpty()) {
//          ok = false;   Last setting was OK = true when _suppressWarnings was set to true
            if (!_suppressWarnings) {
                StringBuilder sb = new StringBuilder();
                for (NamedBean s : sList) {
                    sb.append(Bundle.getMessage("DeletePortalSignal", 
                            name, s.getDisplayName(), portal.getProtectedBlock(s)));
                 }
                sb.append(Bundle.getMessage("DeleteConfirmSignal", name));
                ok = okToRemove(name, sb.toString());
            }
        }
        
        if (ok) {
            removeWarrants(wList);
            for (NamedBean s : sList) {
                portal.deleteSignal(s);
            }
        }
        return ok;
    }

    protected boolean okToRemoveBlockPath(OBlock block, OPath path) {
        String pathName = path.getName();
        String blockName = block.getDisplayName();
        boolean ok = true;
        List<Warrant> list = warrantsUsing(block, path);
        if (!list.isEmpty()) {
//          ok = false;   Last setting was OK = true when _suppressWarnings was set to true
            if (!_suppressWarnings) {
                StringBuilder sb = new StringBuilder();
                for (Warrant w : list) {
                    sb.append(Bundle.getMessage("DeleteWarrantPath", 
                            pathName, blockName, w.getDisplayName()));
                 }
                sb.append(Bundle.getMessage("DeleteConfirm", pathName));
                ok = okToRemove(pathName, sb.toString());
            }
        }
        if (ok) {
            removeWarrants(list);
        }
        return ok;
    }

    private void removeWarrants(List<Warrant> list) {
        for (Warrant w : list) {
            if (w.getRunMode() != Warrant.MODE_NONE) {
                w.controlRunTrain(Warrant.ABORT);
            }
            deregister(w);
            w.dispose();
        }
    }

    private boolean okToRemove(String name, String message) {
        if (!ThreadingUtil.isLayoutThread()) {  //need GUI
            log.warn("Cannot delete portal \"{}\" from this thread", name);
            return false;
        }
        int val = JmriJOptionPane.showOptionDialog(null, message,
                Bundle.getMessage("WarningTitle"), JmriJOptionPane.DEFAULT_OPTION,
                JmriJOptionPane.QUESTION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonYesPlus"),
                        Bundle.getMessage("ButtonNo"),},
                Bundle.getMessage("ButtonNo")); // default NO
        if (val == 2 || val == JmriJOptionPane.CLOSED_OPTION ) { // array position 2 No, or Dialog closed
            return false;
        }
        if (val == 1) { // array position 1 ButtonYesPlus suppress future warnings
            _suppressWarnings = true;
        }
        return true;
    }

    protected synchronized void portalNameChange(String oldName, String newName) {
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            for (BlockOrder bo : orders) {
                if (oldName.equals(bo.getEntryName())) {
                    bo.setEntryName(newName);
                }
                if (oldName.equals(bo.getExitName())) {
                    bo.setExitName(newName);
                }
            }
        }
    }

    protected List<Warrant> warrantsUsing(OBlock block) {
        ArrayList<Warrant> list = new ArrayList<>();
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            Iterator<BlockOrder> it = orders.iterator();
            while (it.hasNext()) {
                if (block.equals(it.next().getBlock())) {
                    list.add(w);
                }
            }
        }
        return list;
    }

    protected List<Warrant> warrantsUsing(Portal portal) {
        ArrayList<Warrant> list = new ArrayList<>();
        String name = portal.getName();
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            for (BlockOrder bo : orders) {
                if (( name.equals(bo.getEntryName()) && !list.contains(w))
                    || ( name.equals(bo.getExitName()) && !list.contains(w))) {
                    list.add(w);
                }
            }
        }
        return list;
    }

    protected List<NamedBean> signalsUsing(Portal portal) {
        ArrayList<NamedBean> list = new ArrayList<>();
        NamedBean signal = portal.getToSignal();
        if (signal != null) {
            list.add(signal);
        }
        signal = portal.getFromSignal();
        if (signal != null) {
            list.add(signal);
        }
        return list;
    }

    protected List<Warrant> warrantsUsing(OBlock block, OPath path) {
        ArrayList<Warrant> list = new ArrayList<>();
        String name = path.getName();
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            for (BlockOrder bo : orders) {
                if (block.equals(bo.getBlock()) && name.equals(bo.getPathName())) {
                    list.add(w);
                }
            }
        }
        return list;
    }

    protected synchronized void pathNameChange(OBlock block, String oldName, String newName) {
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            for (BlockOrder bo : orders) {
                if (bo.getBlock().equals(block) && bo.getPathName().equals(oldName)) {
                    bo.setPathName(newName);
                }
            }
        }
    }

    /**
     * Get the default WarrantManager.
     *
     * @return the default WarrantManager, creating it if necessary
     */
    public static WarrantManager getDefault() {
        return InstanceManager.getOptionalDefault(WarrantManager.class).orElseGet(() -> 
            InstanceManager.setDefault(WarrantManager.class, new WarrantManager())
        );
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameWarrants" : "BeanNameWarrant");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Warrant> getNamedBeanClass() {
        return Warrant.class;
    }

    protected void setMergeProfile(String id, RosterSpeedProfile merge) {
        if (_shutDownTask == null) {
            if (!WarrantPreferences.getDefault().getShutdown().equals((WarrantPreferences.Shutdown.NO_MERGE))) {
                _shutDownTask = new WarrantShutdownTask("WarrantRosterSpeedProfileCheck");
                InstanceManager.getDefault(jmri.ShutDownManager.class).register(_shutDownTask);
            }
        }
        log.debug("setMergeProfile id = {}", id);
        if (id != null && merge != null) {
            _mergeProfiles.remove(id);
            _mergeProfiles.put(id, merge);
        }
    }

    /**
     * Return a copy of the RosterSpeedProfile for Roster entry
     * @param id roster id
     * @return RosterSpeedProfile
     */
    protected RosterSpeedProfile getMergeProfile(String id) {
        log.debug("getMergeProfile id = {}", id);
        return _mergeProfiles.get(id);
    }

    protected RosterSpeedProfile makeProfileCopy(@CheckForNull RosterSpeedProfile mergeProfile, @Nonnull RosterEntry re) {
        RosterSpeedProfile profile = new RosterSpeedProfile(re);
        if (mergeProfile == null) {
            mergeProfile = re.getSpeedProfile();
            if (mergeProfile == null) {
                mergeProfile = new RosterSpeedProfile(re);
                re.setSpeedProfile(mergeProfile);
            }
        }
        // make copy of mergeProfile
        TreeMap<Integer, SpeedStep> rosterTree = mergeProfile.getProfileSpeeds();
        for (Map.Entry<Integer, SpeedStep> entry : rosterTree.entrySet()) {
            profile.setSpeed(entry.getKey(), entry.getValue().getForwardSpeed(), entry.getValue().getReverseSpeed());
        }
        return profile;
    }

    protected HashMap<String, RosterSpeedProfile> getMergeProfiles() {
        return _mergeProfiles;
    }

    @Override
    public void dispose(){
        for(Warrant w:_beans){
            w.dispose();
        }
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WarrantManager.class);

}

package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ShutDownTask;
import jmri.SignalSystem;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a WarrantManager.
 * <p>
 * Note this is a concrete class.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class WarrantManager extends AbstractManager<Warrant>
        implements jmri.InstanceManagerAutoDefault {
    
    private HashMap<String, RosterSpeedProfile> _mergeProfiles;
    private HashMap<String, RosterSpeedProfile> _sessionProfiles;
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
     * Method to create a new Warrant if it does not exist Returns null if a
     * Warrant with the same systemName or userName already exists, or if there
     * is trouble creating a new Warrant.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return an existing warrant if found or a new warrant
     */
    public Warrant createNewWarrant(String systemName, String userName, boolean SCWa, long TTP) {
        log.debug("createNewWarrant {} SCWa= {}",systemName,SCWa);
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
        if (SCWa) {
            r = new SCWarrant(systemName, userName, TTP);
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

    public Warrant getBySystemName(String name) {
        return _tsys.get(name);
    }

    public Warrant getByUserName(String key) {
        return _tuser.get(key);
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

    protected boolean okToRemoveBlock(OBlock block) {
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
        int val = JOptionPane.showOptionDialog(null, message,
                Bundle.getMessage("WarningTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonYes"),
                        Bundle.getMessage("ButtonYesPlus"),
                        Bundle.getMessage("ButtonNo"),},
                Bundle.getMessage("ButtonNo")); // default NO
        if (val == 2) {
            return false;
        }
        if (val == 1) { // suppress future warnings
            _suppressWarnings = true;
        }
        return true;
    }

    synchronized protected void portalNameChange(String oldName, String newName) {
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            Iterator<BlockOrder> it = orders.iterator();
            while (it.hasNext()) {
                BlockOrder bo = it.next();
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
                if (block.equals(it.next().getBlock()))
                    list.add(w);
            }
        }
        return list;
    }

    protected List<Warrant> warrantsUsing(Portal portal) {
        ArrayList<Warrant> list = new ArrayList<>();
        String name = portal.getName();
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            Iterator<BlockOrder> it = orders.iterator();
            while (it.hasNext()) {
                BlockOrder bo = it.next();
                if (name.equals(bo.getEntryName()) && !list.contains(w)) {
                    list.add(w);
                } else if (name.equals(bo.getExitName()) && !list.contains(w)) {
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
            Iterator<BlockOrder> it = orders.iterator();
            while (it.hasNext()) {
                BlockOrder bo = it.next();
                if (block.equals(bo.getBlock()) && name.equals(bo.getPathName())) {
                    list.add(w);
                }
            }
        }
        return list;
    }

    synchronized protected void pathNameChange(OBlock block, String oldName, String newName) {
        for (Warrant w : getNamedBeanSet()) {
            List<BlockOrder> orders = w.getBlockOrders();
            Iterator<BlockOrder> it = orders.iterator();
            while (it.hasNext()) {
                BlockOrder bo = it.next();
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
        return InstanceManager.getOptionalDefault(WarrantManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(WarrantManager.class, new WarrantManager());
        });
    }

    @Override
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
    
    protected void setSpeedProfiles(String id, RosterSpeedProfile merge, RosterSpeedProfile session) {
        if (_mergeProfiles == null) {
            _mergeProfiles = new HashMap<>();
            _sessionProfiles = new HashMap<>();
            if (!WarrantPreferences.getDefault().getShutdown().equals((WarrantPreferences.Shutdown.NO_MERGE))) {
                ShutDownTask shutDownTask = new WarrantShutdownTask("WarrantRosterSpeedProfileCheck");
                jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(shutDownTask);
            }
        }
        if (id != null) {
            _mergeProfiles.put(id, merge);
            _sessionProfiles.put(id, session);
        }
    }
    
    protected RosterSpeedProfile getMergeProfile(String id) {
        if (_mergeProfiles == null) {
            return null;
        }
        return _mergeProfiles.get(id);
    }
    protected RosterSpeedProfile getSessionProfile(String id) {
        if (_sessionProfiles == null) {
            return null;
        }
        return _sessionProfiles.get(id);
    }
    
    protected HashMap<String, RosterSpeedProfile> getMergeProfiles() {
        return _mergeProfiles;
    }
    protected HashMap<String, RosterSpeedProfile> getSessionProfiles() {
        return _sessionProfiles;
    }

    @Override
    public void dispose(){
        for(Warrant w:_beans){
            w.stopWarrant(true);
        }
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(WarrantManager.class);
}

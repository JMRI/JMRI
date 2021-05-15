package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.NamedBean;
import jmri.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WarrantAction contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination. WarrantTableAction
 * provides the menu for panels to List, Edit and Create Warrants. It launches
 * the appropriate frame for each action.
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010
 **/
public class WarrantTableAction extends AbstractAction {

    static int STRUT_SIZE = 10;
    private JMenu _warrantMenu;
    
    private boolean _hasErrors = false;
    private JDialog _errorDialog;
    private WarrantFrame _openFrame;
    private NXFrame _nxFrame;
    private boolean _logging = false;
    private Runnable _shutDownTask = null;

    private WarrantTableAction(String menuOption) {
        super(Bundle.getMessage(menuOption));
    }

    public static WarrantTableAction getDefault() {
        return InstanceManager.getOptionalDefault(WarrantTableAction.class).orElseGet(() -> {
            WarrantTableAction wta = new WarrantTableAction("ShowWarrants"); // NOI18N
            wta.errorCheck();
            return InstanceManager.setDefault(WarrantTableAction.class, wta);
        });
    }

    @Override
    @InvokeOnGuiThread
    public void actionPerformed(ActionEvent e) {
        WarrantTableFrame.getDefault();
    }

    /**
     * @param edit true if portal errors should be shown in window created from
     *             menu item
     * @return a menu containing warrant actions
     */
    public JMenu makeWarrantMenu(boolean edit) {
        if (jmri.InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().size() > 1) {
            synchronized (this) {
                _warrantMenu = new JMenu(Bundle.getMessage("MenuWarrant"));
                updateWarrantMenu();
                return _warrantMenu;
            }
        }
        return null;
    }

    @InvokeOnGuiThread
    synchronized protected void updateWarrantMenu() {
        _warrantMenu.removeAll();
        _warrantMenu.add(getDefault());
        JMenu editWarrantMenu = new JMenu(Bundle.getMessage("EditWarrantMenu"));
        _warrantMenu.add(editWarrantMenu);
        ActionListener editWarrantAction = (ActionEvent e) -> openWarrantFrame(e.getActionCommand());
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        if (manager.getObjectCount() == 0) { // when there are no Warrants, enter the word "None" to the submenu
            JMenuItem _noWarrants = new JMenuItem(Bundle.getMessage("None"));
            editWarrantMenu.add(_noWarrants);
            // disable it
            _noWarrants.setEnabled(false);
        } else { // when there are warrants, add them to the submenu
            for (Warrant warrant : manager.getNamedBeanSet()) {
                // Warrant warrent = (Warrant) object;
                JMenuItem mi = new JMenuItem(warrant.getDisplayName());
                mi.setActionCommand(warrant.getDisplayName());
                mi.addActionListener(editWarrantAction);
                editWarrantMenu.add(mi);
            }
        }
         _warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateWarrant")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeWarrantFrame(null, null);
            }
         });
        _warrantMenu.add(InstanceManager.getDefault(TrackerTableAction.class));
        _warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateNXWarrant")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeNXFrame();
            }
        });
        _warrantMenu.add(makeLogMenu());

        log.debug("updateMenu to {} warrants.", manager.getObjectCount());
    }

    protected JMenuItem makeLogMenu() {
        JMenuItem mi;
        if (!_logging) {
            mi = new JMenuItem(Bundle.getMessage("startLog"));
            mi.addActionListener((ActionEvent e) -> {
                if (!OpSessionLog.makeLogFile(WarrantTableFrame.getDefault())) {
                    return;
                }
                _logging = true;
                _shutDownTask = () -> {
                    OpSessionLog.close();
                    _logging = false;
                };
                jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(_shutDownTask);
                updateWarrantMenu();
            });
        } else {
            mi = new JMenuItem(Bundle.getMessage("flushLog"));
            mi.addActionListener((ActionEvent e) -> OpSessionLog.flush());
            _warrantMenu.add(mi);
            mi = new JMenuItem(Bundle.getMessage("stopLog"));
            mi.addActionListener((ActionEvent e) -> {
                OpSessionLog.close();
                jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(_shutDownTask);
                _shutDownTask = null;
                _logging = false;
                updateWarrantMenu();
            });
        }
        return mi;
    }

    synchronized protected void writetoLog(String text) {
        if (_logging) {
            OpSessionLog.writeLn(text);
        }
    }

    @InvokeOnGuiThread
    protected void closeNXFrame() {
        if (_nxFrame != null) {
            _nxFrame.clearTempWarrant();
            _nxFrame.dispose();
            _nxFrame = null;
        }
    }

    @InvokeOnGuiThread
    protected void makeNXFrame() {
        if (warrantFrameRunning()) {
            return;
        }
        if (_nxFrame == null) {
            _nxFrame = new NXFrame();
        }
        _nxFrame.setState(java.awt.Frame.NORMAL);
        _nxFrame.setVisible(true);
        _nxFrame.toFront();
    }

    protected void closeWarrantFrame() {
        if (_openFrame != null) {
            _openFrame.close();
            _openFrame.dispose();
            _openFrame = null;
        }
    }

    // check if edited warrant is running test
    private boolean warrantFrameRunning() {
        if (_openFrame != null) {
            if (_openFrame.isRunning()) {
                _openFrame.toFront();
                return true;
            } else {
                closeWarrantFrame();
            }
        }
        return false;
    }

    protected void makeWarrantFrame(Warrant startW, Warrant endW) {
        if (warrantFrameRunning()) {
            return;
        }
        closeNXFrame();
        _openFrame = new WarrantFrame(startW, endW);
        _openFrame.setState(java.awt.Frame.NORMAL);
        _openFrame.toFront();            
    }

    protected void editWarrantFrame(Warrant w) {
        if (warrantFrameRunning()) {
            return;
        }
        closeNXFrame();
        _openFrame = new WarrantFrame(w);
        _openFrame.setState(java.awt.Frame.NORMAL);
        _openFrame.toFront();            
    }

    private void openWarrantFrame(String key) {
        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant(key);
        if (w != null) {
            editWarrantFrame(w);
        }
    }

    synchronized public void mouseClickedOnBlock(OBlock block) {
        if (block == null) {
            return;
        }

        if (_openFrame != null) {
            _openFrame.mouseClickedOnBlock(block);
            return;
        }

        if (_nxFrame != null && _nxFrame.isVisible() && _nxFrame.isRouteSeaching()) {
            _nxFrame.mouseClickedOnBlock(block);
            return;
        }

        InstanceManager.getDefault(TrackerTableAction.class).mouseClickedOnBlock(block);
    }

    /* ****************** Error checking ************************/
    public boolean errorCheck() {
        _hasErrors = false;
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(10, 50);
        textArea.setEditable(false);
        textArea.setTabSize(4);
        textArea.append(Bundle.getMessage("ErrWarnAreaMsg"));
        textArea.append("\n\n");
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        for (OBlock block : manager.getNamedBeanSet()) {
            textArea.append(checkPathPortals(block));
        }
        return showPathPortalErrors(textArea);
    }

    /**
     * Validation of paths within a block. Gathers messages in a text area that
     * can be displayed after all are written.
     *
     * @param b the block to validate
     * @return error/warning message, if any
     */
    @Nonnull
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public String checkPathPortals(OBlock b) {
        if (log.isDebugEnabled()) {
            log.debug("checkPathPortals for {}", b.getDisplayName());
        }
        StringBuilder sb = new StringBuilder();
        List<Path> pathList = b.getPaths();
        if (pathList.isEmpty()) {
            if (b.getPortals().isEmpty()) {
                sb.append(Bundle.getMessage("NoPortals"));
                sb.append(" ");
            }
            sb.append(Bundle.getMessage("NoPaths", b.getDisplayName()));
            sb.append("\n");
            _hasErrors = true;
            return sb.toString();
        }
        List<Portal> portalList = b.getPortals();
        // make list of names of all portals.  Then remove those we check, leaving the orphans
        ArrayList<String> portalNameList = new ArrayList<>();
        for (Portal portal : portalList) {
            if (portal.getFromPaths().isEmpty()) {
                sb.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(), portal.getFromBlockName()));
                sb.append("\n");
                _hasErrors = true;
                return sb.toString();
            }
            if (portal.getToPaths().isEmpty()) {
                sb.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(), portal.getToBlockName()));
                sb.append("\n");
                _hasErrors = true;
                return sb.toString();
            }
            portalNameList.add(portal.getName());
        }
        for (Path value : pathList) {
            OPath path = (OPath) value;
            OBlock block = (OBlock) path.getBlock();
            if (block == null || !block.equals(b)) {
                sb.append(Bundle.getMessage("PathWithBadBlock", path.getName(), b.getDisplayName()));
                sb.append("\n");
                _hasErrors = true;
                return sb.toString();
            }
            String msg = null;
            boolean hasPortal = false;
            Portal fromPortal = path.getFromPortal();
            if (fromPortal != null) {
                if (!fromPortal.isValid()) {
                    msg = fromPortal.getName();
                }
                hasPortal = true;
                portalNameList.remove(fromPortal.getName());
            }
            Portal toPortal = path.getToPortal();
            if (toPortal != null) {
                if (!toPortal.isValid()) {
                    msg = toPortal.getName();
                }
                hasPortal = true;
                portalNameList.remove(toPortal.getName());
                if (fromPortal != null && fromPortal.equals(toPortal)) {
                    sb.append(Bundle.getMessage("PathWithDuplicatePortal", path.getName(), b.getDisplayName()));
                    sb.append("\n");
                }
            }
            if (msg != null) {
                sb.append(Bundle.getMessage("PortalNeedsBlock", msg));
                sb.append("\n");
                _hasErrors = true;
            } else if (!hasPortal) {
                sb.append(Bundle.getMessage("PathNeedsPortal", path.getName(), b.getDisplayName()));
                sb.append("\n");
                _hasErrors = true;
            }
            // check that the path's portals have the path in their lists
            boolean validPath;
            if (toPortal != null) {
                if (fromPortal != null) {
                    validPath = toPortal.isValidPath(path) && fromPortal.isValidPath(path);
                } else {
                    validPath = toPortal.isValidPath(path);
                }
            } else {
                if (fromPortal != null) {
                    validPath = fromPortal.isValidPath(path);
                } else {
                    validPath = false;
                }
            }
            if (!validPath) {
                sb.append(Bundle.getMessage("PathNotConnectedToPortal", path.getName(), b.getDisplayName()));
                sb.append("\n");
                _hasErrors = true;
            }
        }
        for (String s : portalNameList) {
            sb.append(Bundle.getMessage("BlockPortalNoPath", s, b.getDisplayName()));
            sb.append("\n");
            _hasErrors = true;
        }
        // check whether any turnouts are shared between two blocks;
        checkSharedTurnouts(b);
        return sb.toString();
    }

    public boolean showPathPortalErrors(JTextArea textArea) {
        if (_errorDialog != null) {
            _errorDialog.dispose();
        }
        if (!_hasErrors) {
            return false;
        }
        JScrollPane scrollPane = new JScrollPane(textArea);
        _errorDialog = new JDialog();
        _errorDialog.setTitle(Bundle.getMessage("ErrorDialogTitle"));
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
        class myListener extends java.awt.event.WindowAdapter implements ActionListener {

            /*  java.awt.Window _w;
             myListener(java.awt.Window w) {
                 _w = w;
             }  */
            @Override
            public void actionPerformed(ActionEvent e) {
                _errorDialog.dispose();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                _errorDialog.dispose();
            }
        }
        ok.addActionListener(new myListener());
        ok.setMaximumSize(ok.getPreferredSize());

        java.awt.Container contentPane = _errorDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(Box.createVerticalStrut(5));
        contentPane.add(Box.createVerticalGlue());
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(ok);
        contentPane.add(panel, BorderLayout.SOUTH);
        _errorDialog.addWindowListener(new myListener());
        _errorDialog.pack();
        _errorDialog.setVisible(true);
        return true;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public boolean checkSharedTurnouts(OBlock block) {
        boolean hasShared = false;
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        List<Path> pathList = block.getPaths();
        for (Path value : pathList) {
            OPath path = (OPath) value;
            for (OBlock b : manager.getNamedBeanSet()) {
                if (block.getSystemName().equals(b.getSystemName())) {
                    continue;
                }
                for (Path item : b.getPaths()) {
                    boolean shared = sharedTO(path, (OPath) item);
                    if (shared) {
                        hasShared = true;
                        break;
                    }
                }
            }
        }
        return hasShared;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OBlock extends Block")
    private boolean sharedTO(OPath myPath, OPath path) {
        List<BeanSetting> myTOs = myPath.getSettings();
        Iterator<BeanSetting> iter = myTOs.iterator();
        List<BeanSetting> tos = path.getSettings();
        boolean ret = false;
        while (iter.hasNext()) {
            BeanSetting mySet = iter.next();
            NamedBean myTO = mySet.getBean();
            int myState = mySet.getSetting();
            for (BeanSetting set : tos) {
                NamedBean to = set.getBean();
                if (myTO.equals(to)) {
                    // turnouts are equal.  check if settings are compatible.
                    OBlock myBlock = (OBlock) myPath.getBlock();
                    int state = set.getSetting();
                    OBlock block = (OBlock) path.getBlock();
                    if (myState != state) {
                        ret = myBlock.addSharedTurnout(myPath, block, path);
                    }
                }
            }
        }
        return ret;
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantTableAction.class);

}

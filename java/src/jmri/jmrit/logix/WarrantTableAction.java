package jmri.jmrit.logix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.NamedBean;
import jmri.Path;
import jmri.ShutDownTask;
import jmri.implementation.swing.SwingShutDownTask;
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
 */
public class WarrantTableAction extends AbstractAction {

    static int STRUT_SIZE = 10;
    static JMenu _warrantMenu;
    private static final HashMap<String, Warrant> _warrantMap = new HashMap<>();
    
    private static JTextArea _textArea;
    private static boolean _hasErrors = false;
    private static JDialog _errorDialog;
    private static WarrantFrame _openFrame;
    private static NXFrame _nxFrame;
    private static boolean _logging = false;
    private static boolean _edit;
    static ShutDownTask _shutDownTask = null;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "until deprecated TrackerTableAction.getInstance removed")
    protected WarrantTableAction(String menuOption) {
        super(Bundle.getMessage(menuOption));
    }

    public static WarrantTableAction getDefault() {
        return InstanceManager.getOptionalDefault(WarrantTableAction.class).orElseGet(() -> {
            return InstanceManager.setDefault(WarrantTableAction.class, new WarrantTableAction("ShowWarrants")); // NOI18N
        });
    }

    @Override
    @InvokeOnGuiThread
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (Bundle.getMessage("ShowWarrants").equals(command)) {
            WarrantTableFrame.getDefault();
        } else if (Bundle.getMessage("CreateWarrant").equals(command)) {
            CreateWarrantFrame f = new CreateWarrantFrame();
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.error("During initComponents", ex);
            }
            f.setVisible(true);
        }
        initPathPortalCheck();
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        for (OBlock block : manager.getNamedBeanSet()) {
            checkPathPortals(block);
        }
        if (_edit) {
            showPathPortalErrors();
        }
    }

    /**
     * Note: _warrantMenu is static
     *
     * @param edit true if portal errors should be shown in window created from
     *             menu item
     * @return a menu containing warrant actions
     */
    synchronized public static JMenu makeWarrantMenu(boolean edit) {
        if (jmri.InstanceManager.getDefault(OBlockManager.class).getNamedBeanSet().size() > 1) {
            _edit = edit;
            _warrantMenu = new JMenu(Bundle.getMessage("MenuWarrant"));
            updateWarrantMenu();
            return _warrantMenu;
        }
        return null;
    }

    @InvokeOnGuiThread
    synchronized protected static void updateWarrantMenu() {
        _warrantMenu.removeAll();
        _warrantMenu.add(getDefault());
        JMenu editWarrantMenu = new JMenu(Bundle.getMessage("EditWarrantMenu"));
        _warrantMenu.add(editWarrantMenu);
        ActionListener editWarrantAction = (ActionEvent e) -> {
            openWarrantFrame(e.getActionCommand());
        };
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        if (manager.getObjectCount() == 0) { // when there are no Warrants, enter the word "None" to the submenu
            JMenuItem _noWarrants = new JMenuItem(Bundle.getMessage("None"));
            editWarrantMenu.add(_noWarrants);
            // disable it
            _noWarrants.setEnabled(false);
        } else { // when there are Warrents, add them to the submenu
            for (Warrant warrant : manager.getNamedBeanSet()) {
                // Warrant warrent = (Warrant) object;
                JMenuItem mi = new JMenuItem(warrant.getDisplayName());
                mi.setActionCommand(warrant.getDisplayName());
                mi.addActionListener(editWarrantAction);
                editWarrantMenu.add(mi);
            }
        }
        _warrantMenu.add(new WarrantTableAction("CreateWarrant")); // NOI18N
        _warrantMenu.add(InstanceManager.getDefault(TrackerTableAction.class));
        _warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateNXWarrant")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                WarrantTableFrame.nxAction();
            }
        });
        _warrantMenu.add(makeLogMenu());

        log.debug("updateMenu to {} warrants.", manager.getObjectCount());
    }

    protected static JMenuItem makeLogMenu() {
        JMenuItem mi;
        if (!_logging) {
            mi = new JMenuItem(Bundle.getMessage("startLog"));
            mi.addActionListener((ActionEvent e) -> {
                if (!OpSessionLog.makeLogFile(WarrantTableFrame.getDefault())) {
                    return;
                }
                _logging = true;
                _shutDownTask = new SwingShutDownTask("PanelPro Save default icon check",
                        null, null, null) {
                    @Override
                    public boolean checkPromptNeeded() {
                        OpSessionLog.close();
                        _logging = false;
                        return true;
                    }
                };
                jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(_shutDownTask);
                updateWarrantMenu();
            });
        } else {
            mi = new JMenuItem(Bundle.getMessage("flushLog"));
            mi.addActionListener((ActionEvent e) -> {
                OpSessionLog.flush();
            });
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

    synchronized protected static void writetoLog(String text) {
        if (_logging) {
            OpSessionLog.writeLn(text);
        }
    }

    @InvokeOnGuiThread
    synchronized protected static void closeNXFrame(NXFrame frame) {
        if (frame != null) {
            if (frame.equals(_nxFrame)) {
                _nxFrame = null;
            }
            frame.dispose();
        }
    }

    synchronized protected static boolean setNXFrame(NXFrame frame) {
        if (_nxFrame !=null && _nxFrame != frame) {
            return false;
        }
        _nxFrame = frame;
        return true;
    }

    synchronized protected static NXFrame getNXFrame() {
        return _nxFrame;
    }

    synchronized protected static void closeWarrantFrame(WarrantFrame frame) {
        if (frame != null) {
            if (frame.equals(_openFrame)) {
                _openFrame = null;
            }
            frame.dispose();
        }
    }

    synchronized protected static void setWarrantFrame(WarrantFrame frame) {
        closeWarrantFrame(_openFrame);
        _openFrame = frame;
    }

    synchronized protected static WarrantFrame getWarrantFrame() {
        return _openFrame;
    }

    synchronized protected static void openWarrantFrame(String key) {
        if (_openFrame != null) {
            _openFrame.dispose();
        }
        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant(key);
        if (w != null) {
            _warrantMap.put(key, w);
            _openFrame = new WarrantFrame(w);
        }
        if (log.isDebugEnabled()) {
            log.debug("openWarrantFrame for " + key + ", size= " + _warrantMap.size());
        }
        if (_openFrame != null) {
            _openFrame.setVisible(true);
            _openFrame.toFront();
        }
    }

    /*    synchronized public static WarrantFrame getWarrantFrame(String key) {
        return _frameMap.get(key);
    }*/
    synchronized static public void mouseClickedOnBlock(OBlock block) {
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
    public static void initPathPortalCheck() {
        if (_errorDialog != null) {
            _hasErrors = false;
            _textArea = null;
            _errorDialog.dispose();
        }
    }

    /**
     * Validation of paths within a block. Gathers messages in a text area that
     * can be displayed after all are written.
     *
     * @param b the block to validate
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public static void checkPathPortals(OBlock b) {
        if (log.isDebugEnabled()) {
            log.debug("checkPathPortals for " + b.getDisplayName());
        }
        // warn user of incomplete blocks and portals
        if (_textArea == null) {
            _textArea = new javax.swing.JTextArea(10, 50);
            _textArea.setEditable(false);
            _textArea.setTabSize(4);
            _textArea.append(Bundle.getMessage("ErrWarnAreaMsg"));
            _textArea.append("\n\n");
        }
        List<Path> pathList = b.getPaths();
        if (pathList.isEmpty()) {
            _textArea.append(Bundle.getMessage("NoPaths", b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
            return;
        }
        List<Portal> portalList = b.getPortals();
        // make list of names of all portals.  Then remove those we check, leaving the orphans
        ArrayList<String> portalNameList = new ArrayList<>();
        for (int i = 0; i < portalList.size(); i++) {
            Portal portal = portalList.get(i);
            if (portal.getFromPaths().isEmpty()) {
                _textArea.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(),
                        portal.getFromBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            if (portal.getToPaths().isEmpty()) {
                _textArea.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(),
                        portal.getToBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            portalNameList.add(portal.getName());
        }
        Iterator<Path> iter = pathList.iterator();
        while (iter.hasNext()) {
            OPath path = (OPath) iter.next();
            OBlock block = (OBlock) path.getBlock();
            if (block == null || !block.equals(b)) {
                _textArea.append(Bundle.getMessage("PathWithBadBlock", path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
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
                    _textArea.append(Bundle.getMessage("PathWithDuplicatePortal",
                            path.getName(), b.getDisplayName()));
                    _textArea.append("\n");
                }
            }
            if (msg != null) {
                _textArea.append(Bundle.getMessage("PortalNeedsBlock", msg));
                _textArea.append("\n");
                _hasErrors = true;
            } else if (!hasPortal) {
                _textArea.append(Bundle.getMessage("PathNeedsPortal",
                        path.getName(), b.getDisplayName()));
                _textArea.append("\n");
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
                _textArea.append(Bundle.getMessage("PathNotConnectedToPortal",
                        path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors = true;
            }
        }
        for (int i = 0; i < portalNameList.size(); i++) {
            _textArea.append(Bundle.getMessage("BlockPortalNoPath",
                    portalNameList.get(i), b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
        }
        // check whether any turnouts are shared between two blocks;
        checkSharedTurnouts(b);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "OPath extends Path")
    public static boolean checkSharedTurnouts(OBlock block) {
        boolean hasShared = false;
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        List<Path> pathList = block.getPaths();
        Iterator<Path> iter = pathList.iterator();
        while (iter.hasNext()) {
            OPath path = (OPath) iter.next();
            for (OBlock b : manager.getNamedBeanSet()) {
                if (block.getSystemName().equals(b.getSystemName())) {
                    continue;
                }
                Iterator<Path> it = b.getPaths().iterator();
                while (it.hasNext()) {
                    boolean shared = sharedTO(path, (OPath) it.next());
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
    private static boolean sharedTO(OPath myPath, OPath path) {
        List<BeanSetting> myTOs = myPath.getSettings();
        Iterator<BeanSetting> iter = myTOs.iterator();
        List<BeanSetting> tos = path.getSettings();
        boolean ret = false;
        while (iter.hasNext()) {
            BeanSetting mySet = iter.next();
            NamedBean myTO = mySet.getBean();
            int myState = mySet.getSetting();
            Iterator<BeanSetting> it = tos.iterator();
            while (it.hasNext()) {
                BeanSetting set = it.next();
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

    public static boolean showPathPortalErrors() {
        if (!_hasErrors) {
            return false;
        }
        if (_textArea == null) {
            log.error("_textArea is null!.");
            return true;
        }
        JScrollPane scrollPane = new JScrollPane(_textArea);
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
                _hasErrors = false;
                _textArea = null;
                _errorDialog.dispose();
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                _hasErrors = false;
                _textArea = null;
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

    /* ***************** CreateWarrant ***********************/
    static class CreateWarrantFrame extends JFrame {

        JTextField _sysNameBox;
        JTextField _userNameBox;

        private Warrant _startW;
        private Warrant _endW;

        public CreateWarrantFrame() {
            super.setTitle(Bundle.getMessage("TitleCreateWarrant"));
        }

        public void initComponents() {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(10, 10));
            JLabel prompt = new JLabel(Bundle.getMessage("CreateWarrantPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("LabelSystemName")));
            _sysNameBox = new JTextField(15);
            p.add(_sysNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("LabelUserName")));
            _userNameBox = new JTextField(15);
            p.add(_userNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            contentPane.add(panel, BorderLayout.CENTER);

            panel = new JPanel();
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener((ActionEvent e) -> {
                makeWarrant();
            });
            doneButton.setPreferredSize(doneButton.getPreferredSize());
            panel.add(doneButton);
            contentPane.add(panel, BorderLayout.SOUTH);
            contentPane.add(Box.createVerticalStrut(STRUT_SIZE), BorderLayout.EAST);
            contentPane.add(Box.createVerticalStrut(STRUT_SIZE), BorderLayout.WEST);

            setContentPane(contentPane);
            setLocationRelativeTo(null);
            setVisible(true);
            pack();
        }

        protected void concatenate(Warrant startW, Warrant endW) {
            _startW = startW;
            _endW = endW;
        }

        /**
         * Does 3 cases: create new warrant, copy a warrant, concatenate two
         * warrants warrant w is unregistered
         *
         */
        private void doConcatenate(Warrant w) {
            if (_startW != null) {
                List<BlockOrder> orders = _startW.getBlockOrders();
                int limit = orders.size() - 1;
                for (int i = 0; i < limit; i++) {
                    w.addBlockOrder(new BlockOrder(orders.get(i)));
                }
                w.setViaOrder(_startW.getViaOrder());
                w.setAvoidOrder(_startW.getAvoidOrder());
                if (log.isDebugEnabled()) {
                    log.debug("doConcatenate: limit= " + limit + ",  orders.size()= " + orders.size());
                }
                BlockOrder bo = new BlockOrder(orders.get(limit));
                if (_endW != null) {
                    orders = _endW.getBlockOrders();
                    bo.setExitName(orders.get(0).getExitName());
                    w.addBlockOrder(bo);
                    for (int i = 1; i < orders.size(); i++) {
                        w.addBlockOrder(new BlockOrder(orders.get(i)));
                    }
                    BlockOrder boo = w.getViaOrder();
                    if (boo == null) {
                        w.setViaOrder(_endW.getViaOrder());
                    }
                    boo = w.getAvoidOrder();
                    if (boo == null) {
                        w.setAvoidOrder(_endW.getAvoidOrder());
                    }
                } else {
                    w.addBlockOrder(bo);        // copy only
                }
                List<ThrottleSetting> commands = _startW.getThrottleCommands();
                for (int i = 0; i < commands.size(); i++) {
                    w.addThrottleCommand(new ThrottleSetting(commands.get(i)));
                }
                if (_endW != null) {
                    commands = _endW.getThrottleCommands();
                    for (int i = 0; i < commands.size(); i++) {
                        w.addThrottleCommand(new ThrottleSetting(commands.get(i)));
                    }
                }
                _warrantMap.put(w.getDisplayName(), w);
                setWarrantFrame(new WarrantFrame(w, false)); // copy/concat warrant/s
            } else {
                setWarrantFrame(new WarrantFrame(w, true));  // create new warrant
            }
            _startW = null;
            _endW = null;
            dispose();
        }

        private void makeWarrant() {
            String sysName = _sysNameBox.getText();
            String userName = _userNameBox.getText();
            if (!sysName.startsWith("IW")) {
                sysName = "IW" + sysName;
            }
            _sysNameBox.setText(sysName);
            if (sysName.length() < 3) {
                return;
            }
            if (userName.length() == 0) {
                userName = null;
            }
            boolean failed = false;
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            Warrant w = manager.getBySystemName(sysName);
            if (w != null) {
                failed = true;
            } else {
                w = manager.getByUserName(userName);
                if (w != null) {
                    failed = true;
                } else {
                    // register warrant if user saves this instance
                    w = new Warrant(sysName, userName);
                    doConcatenate(w);
                }
            }
            if (failed) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("WarrantExists",
                        userName, sysName), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(WarrantTableAction.class);

}

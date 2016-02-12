package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.EventObject;
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
import jmri.NamedBean;
import jmri.Path;
import jmri.ShutDownTask;
import jmri.implementation.swing.SwingShutDownTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WarrantAction contains the operating permissions and directives needed for
 * a train to proceed from an Origin to a Destination.
 * WarrantTableAction provides the menu for panels to List, Edit and Create
 * Warrants.  It launches the appropriate frame for each action.
 * <P>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author  Pete Cressman  Copyright (C) 2009, 2010
 */
public class WarrantTableAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -4761753768022771535L;
    static int STRUT_SIZE = 10;
    static JMenu _warrantMenu;
    private static WarrantTableAction _instance;
    private static HashMap <String, Warrant> _warrantMap = new HashMap <String, Warrant> ();
    protected static TrackerTableAction _trackerTable;
    private static JTextArea _textArea;
    private static boolean _hasErrors = false;
    private static JDialog _errorDialog;
    protected static WarrantFrame _openFrame;
    protected static NXFrame _nxFrame;
    private static OpSessionLog _log;
    private static boolean _edit;
    static ShutDownTask     _shutDownTask;

    protected WarrantTableAction(String menuOption) {
        super(Bundle.getMessage(menuOption));
        _trackerTable = TrackerTableAction.getInstance();
    }
    
    static WarrantTableAction getInstance() {
        if (_instance==null) {
            _instance = new WarrantTableAction("ShowWarrants");
        }
        return _instance;
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (Bundle.getMessage("ShowWarrants").equals(command)){
            WarrantTableFrame.getInstance();
        } else if (Bundle.getMessage("CreateWarrant").equals(command)){
            CreateWarrantFrame f = new CreateWarrantFrame();
            try {
                f.initComponents();
            } catch (Exception ex ) {/*bogus*/ }
            f.setVisible(true);
        }
        initPathPortalCheck();
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        for (int i = 0; i < sysNames.length; i++) {
            OBlock block = manager.getBySystemName(sysNames[i]);
            checkPathPortals(block);
        }
        if (_edit) {
            showPathPortalErrors();            
        }
    }
    
    /**
    *  Note: _warrantMenu is static
    */
    synchronized public static JMenu makeWarrantMenu(boolean edit) {
         if (jmri.InstanceManager.getDefault(OBlockManager.class).getSystemNameList().size() > 1) {
             _edit = edit;
             _warrantMenu = new JMenu(Bundle.getMessage("MenuWarrant"));
             updateWarrantMenu();
             return _warrantMenu;
         }
         return null;
    }

    synchronized protected static void updateWarrantMenu() {
        _warrantMenu.removeAll();
        _warrantMenu.add(getInstance());
        JMenu editWarrantMenu = new JMenu(Bundle.getMessage("EditWarrantMenu"));
        _warrantMenu.add(editWarrantMenu);
        ActionListener editWarrantAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                openWarrantFrame(e.getActionCommand());
            }
        };
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        String[] sysNames = manager.getSystemNameArray();
         
        for (int i = 0; i < sysNames.length; i++) {
            Warrant warrant = manager.getBySystemName(sysNames[i]);
            JMenuItem mi = new JMenuItem(warrant.getDisplayName());
            mi.setActionCommand(warrant.getDisplayName());
            mi.addActionListener(editWarrantAction);
            editWarrantMenu.add(mi);                                                  
        }

        _warrantMenu.add(new jmri.jmrit.logix.WarrantTableAction("CreateWarrant"));
        _warrantMenu.add(_trackerTable);
        _warrantMenu.add(new AbstractAction(Bundle.getMessage("CreateNXWarrant")) {
            private static final long serialVersionUID = 4129760191508866189L;

            public void actionPerformed(ActionEvent e) {
                WarrantTableFrame.nxAction();
            }           
        });
        _warrantMenu.add(makeLogMenu());
        
        if (log.isDebugEnabled()) log.debug("updateMenu to "+sysNames.length+" warrants.");
    }
    
    protected static JMenuItem makeLogMenu() {
        JMenuItem mi;
        if (_log==null) {
            mi = new JMenuItem(Bundle.getMessage("startLog"));          
            mi.addActionListener( new ActionListener()
                {
                    public void actionPerformed(ActionEvent e) {
                        _log = OpSessionLog.getInstance();
                        if (!_log.showFileChooser(WarrantTableFrame.getInstance())) {
                            _log = null;
                            return;
                        }
                        if (_shutDownTask == null) {
                            _shutDownTask = new SwingShutDownTask("PanelPro Save default icon check",
                                    null, null, null)
                            {
                                public boolean checkPromptNeeded() {
                                    _log.close();
                                    _log = null;
                                    return true;
                                }
                            };
                            jmri.InstanceManager.shutDownManagerInstance().register(_shutDownTask);
                        }
                        updateWarrantMenu();
                    }
                });
        } else {
            mi = new JMenuItem(Bundle.getMessage("stopLog"));           
            mi.addActionListener( new ActionListener()
                {
                    public void actionPerformed(ActionEvent e) {
                        _log.close();
                        jmri.InstanceManager.shutDownManagerInstance().deregister(_shutDownTask);
                        _shutDownTask = null;
                        _log = null;
                        updateWarrantMenu();
                    }
                });
        }
        return mi;
    }
    
    synchronized protected static void writetoLog(String text) {
        if (_log!=null) {
            _log.writeLn(text);
        }       
    }
    
    synchronized protected static void closeWarrantFrame(WarrantFrame frame) {
        if (frame!=null) {
            if  (frame.equals(_openFrame)) {
                _openFrame = null;                      
            }
            frame.dispose();
        }
    }
    synchronized protected static void newWarrantFrame(WarrantFrame frame) {
        closeWarrantFrame(_openFrame);
        _openFrame = frame;
    }

    synchronized protected static void openWarrantFrame(String key) {
        if (_openFrame!=null) {
            _openFrame.dispose();
        }
        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant(key);
        if (w!=null) {
            _warrantMap.put(key, w);
            _openFrame = new WarrantFrame(w);
        }
        if (log.isDebugEnabled()) log.debug("openWarrantFrame for "+key+", size= "+_warrantMap.size());
        if (_openFrame!=null) {
            _openFrame.setVisible(true);
            _openFrame.toFront();           
        }
    }
    
    synchronized protected static void portalNameChange(String oldName, String newName) {
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        List <String> systemNameList = manager.getSystemNameList();
        Iterator <String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            Warrant w =manager.getBySystemName(iter.next());
            List <BlockOrder> orders = w.getBlockOrders();
            Iterator <BlockOrder> it = orders.iterator();
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

    synchronized protected static void pathNameChange(OBlock block, String oldName, String newName) {
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        List <String> systemNameList = manager.getSystemNameList();
        Iterator <String> iter = systemNameList.iterator();
        while (iter.hasNext()) {
            Warrant w =manager.getBySystemName(iter.next());
            List <BlockOrder> orders = w.getBlockOrders();
            Iterator <BlockOrder> it = orders.iterator();
            while (it.hasNext()) {
                BlockOrder bo = it.next();
                if (bo.getBlock().equals(block) && bo.getPathName().equals(oldName)) {
                    bo.setPathName(newName);
                }
            }
        }
    }

/*    synchronized public static WarrantFrame getWarrantFrame(String key) {
        return _frameMap.get(key);
    }*/
    
    synchronized static public void mouseClickedOnBlock(OBlock block) {
        if (block==null) {
            return;
        }
        
        NXFrame nxFrame = NXFrame.getInstance();
        if (nxFrame.isVisible()) {
            nxFrame.mouseClickedOnBlock(block);
            return;
        }
           
        if (_trackerTable!=null && TrackerTableAction.mouseClickedOnBlock(block)) {
            return;
        }
        if (_openFrame!=null) {
            _openFrame.mouseClickedOnBlock(block);
            return;
        }
    }
    
    /******************** Error checking ************************/

    public static void initPathPortalCheck() {
        if (_errorDialog!=null) {
            _hasErrors = false;
            _textArea = null;
            _errorDialog.dispose();
        }        
    }
    /**
    *  Validation of paths within a block.
    *  Gathers messages in a text area that can be displayed after all
    * are written.
    */
    public static void checkPathPortals(OBlock b) {
        if (log.isDebugEnabled()) log.debug("checkPathPortals for "+b.getDisplayName());
        // warn user of incomplete blocks and portals
        if (_textArea==null) {
            _textArea = new javax.swing.JTextArea(10, 50);
            _textArea.setEditable(false);
            _textArea.setTabSize(4);
            _textArea.append(Bundle.getMessage("ErrWarnAreaMsg"));
            _textArea.append("\n\n");
        }
        List <Path> pathList = b.getPaths();
        if (pathList.size()==0) {
            _textArea.append(Bundle.getMessage("NoPaths", b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
            return;
        }
        List <Portal> portalList = b.getPortals();
        // make list of names of all portals.  Then remove those we check, leaving the orphans
        ArrayList <String> portalNameList =new ArrayList <String>();
        for (int i=0; i<portalList.size(); i++) {
            Portal portal = portalList.get(i);
            if (portal.getFromPaths().size()==0) {
                _textArea.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(),
                                     portal.getFromBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            if (portal.getToPaths().size()==0) {
                _textArea.append(Bundle.getMessage("BlockPortalNoPath", portal.getName(),
                                     portal.getToBlockName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            portalNameList.add(portal.getName());
        }
        Iterator <Path> iter = pathList.iterator();
        while (iter.hasNext()) {
            OPath path = (OPath)iter.next();
            OBlock block = (OBlock)path.getBlock();
            if  (block==null || !block.equals(b)) {
                _textArea.append(Bundle.getMessage("PathWithBadBlock", path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors = true;
                return;
            }
            String msg = null;
            boolean hasPortal = false;
            Portal fromPortal = path.getFromPortal();
            if (fromPortal!=null) {
                if (!fromPortal.isValid()){
                    msg = fromPortal.getName();
                }
                hasPortal = true;
                portalNameList.remove(fromPortal.getName());
            }
            Portal toPortal = path.getToPortal();
            if (toPortal!=null) {
                 if (!toPortal.isValid()) {
                     msg = toPortal.getName();
                 }
                 hasPortal = true;
                 portalNameList.remove(toPortal.getName());
                 if (fromPortal!=null && fromPortal.equals(toPortal)) {
                     _textArea.append(Bundle.getMessage("PathWithDuplicatePortal",
                             path.getName(), b.getDisplayName()));
                     _textArea.append("\n");
                 }
            }
            if (msg != null ) {
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
            if (toPortal!=null) {
                if (fromPortal!=null) {
                    validPath = toPortal.isValidPath(path) && fromPortal.isValidPath(path);
                } else {
                    validPath = toPortal.isValidPath(path);
                }
            }else {
                if (fromPortal!=null) {
                    validPath = fromPortal.isValidPath(path);
                } else {
                    validPath = false;
                }               
            }
            if (!validPath) {               
                _textArea.append(Bundle.getMessage("PathNotConnectedToPortal", 
                        path.getName(), b.getDisplayName()));
                _textArea.append("\n");
                _hasErrors =true;
            }
        }
        for (int i=0; i<portalNameList.size(); i++) {
            _textArea.append(Bundle.getMessage("BlockPortalNoPath", 
                    portalNameList.get(i), b.getDisplayName()));
            _textArea.append("\n");
            _hasErrors = true;
        }
        // check whether any turnouts are shared between two blocks;
        checkSharedTurnouts(b);
    }
    
    public static boolean checkSharedTurnouts(OBlock block) {
        boolean hasShared = false;
        OBlockManager manager = InstanceManager.getDefault(OBlockManager.class);
        String[] sysNames = manager.getSystemNameArray();
        List <Path> pathList = block.getPaths();
        Iterator <Path> iter = pathList.iterator();
        while (iter.hasNext()) {
            OPath path = (OPath)iter.next();
            for (int i=0; i < sysNames.length; i++) {
                if (block.getSystemName().equals(sysNames[i])) {
                    continue;
                }
                OBlock b = manager.getBySystemName(sysNames[i]);
                Iterator <Path> it = b.getPaths().iterator();
                while (it.hasNext()) {
                    boolean shared = sharedTO(path, (OPath)it.next());
                    if (shared) {
                        hasShared =true;
                        break;
                    }
                }
            }
        }
        return hasShared;
    }
    private static boolean sharedTO(OPath myPath, OPath path) {
        List<BeanSetting> myTOs = myPath.getSettings();
        Iterator <BeanSetting> iter = myTOs.iterator();
        List<BeanSetting> tos = path.getSettings();
        boolean ret = false;
        while (iter.hasNext()) {
            BeanSetting mySet = iter.next();
            NamedBean myTO = mySet.getBean();
            int myState = mySet.getSetting();
            Iterator <BeanSetting> it = tos.iterator();
            while (it.hasNext()) {
                BeanSetting set = it.next();
                NamedBean to = set.getBean();
                if(myTO.equals(to)) {
                    // turnouts are equal.  check if settings are compatible.
                    OBlock myBlock = (OBlock)myPath.getBlock();
                    int state = set.getSetting();
                    OBlock block = (OBlock)path.getBlock();
//                  String note = "WARNING: ";
                    if (myState!=state) {
                       ret = myBlock.addSharedTurnout(myPath, block, path);
/*                       _textArea.append(note+Bundle.getMessage("sharedTurnout", myPath.getName(), myBlock.getDisplayName(), 
                             myTO.getDisplayName(), (myState==jmri.Turnout.CLOSED ? "Closed":"Thrown"),
                             path.getName(), block.getDisplayName(), to.getDisplayName(), 
                             (state==jmri.Turnout.CLOSED ? "Closed":"Thrown")));
                      _textArea.append("\n");
                    } else {
                        note = "Note: "; */
                    }                   
                }
            }
        }
        return ret;
    }
    
    public static boolean showPathPortalErrors() {
        if (!_hasErrors) { return false; }
        if (_textArea==null) {
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
             public void actionPerformed(ActionEvent e) {
                 _hasErrors = false;
                 _textArea = null;
                 _errorDialog.dispose();
             }
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
        _errorDialog.addWindowListener( new myListener());
        _errorDialog.pack();
        _errorDialog.setVisible(true);
        return true;
    }

    /******************* CreateWarrant ***********************/

    static class CreateWarrantFrame extends JFrame {

        /**
         * 
         */
        private static final long serialVersionUID = -1815517166489246531L;
        JTextField _sysNameBox;
        JTextField _userNameBox;

        private Warrant _startW;
        private Warrant _endW;

        public CreateWarrantFrame() {
            setTitle(Bundle.getMessage("TitleCreateWarrant"));
        }

        public void initComponents() {
            JPanel contentPane = new JPanel();
            contentPane.setLayout(new BorderLayout(10,10));
            JLabel prompt = new JLabel(Bundle.getMessage("CreateWarrantPrompt"));
            contentPane.add(prompt, BorderLayout.NORTH);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("SystemName")));
            _sysNameBox = new JTextField(15);
            p.add(_sysNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("UserName")));
            _userNameBox = new JTextField(15);
            p.add(_userNameBox);
            panel.add(p);
            panel.add(Box.createHorizontalStrut(STRUT_SIZE));
            contentPane.add(panel, BorderLayout.CENTER);

            panel = new JPanel();
            JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
            doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    makeWarrant();
                }
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
         * Does 3 cases: create new warrant, copy a warrant, concatenate two warrants
         * warrant w is unregistered
         * */
        private void doConcatenate(Warrant w) {
            if (_startW!=null) {
                List <BlockOrder> orders = _startW.getOrders();
                int limit = orders.size()-1;
                for (int i=0; i<limit; i++) {
                    w.addBlockOrder(new BlockOrder(orders.get(i)));
                }
                w.setViaOrder(_startW.getViaOrder());
                w.setAvoidOrder(_startW.getAvoidOrder());
                if (log.isDebugEnabled()) log.debug("doConcatenate: limit= "+limit+",  orders.size()= "+ orders.size());
                BlockOrder bo = new BlockOrder(orders.get(limit));
                if (_endW!=null) {
                    orders = _endW.getOrders();
                    bo.setExitName(orders.get(0).getExitName());
                    w.addBlockOrder(bo);
                    for (int i=1; i<orders.size(); i++) {
                        w.addBlockOrder(new BlockOrder(orders.get(i)));
                    }
                    BlockOrder boo = w.getViaOrder();
                    if (boo==null) {
                        w.setViaOrder(_endW.getViaOrder());                     
                    }
                    boo = w.getAvoidOrder();
                    if (boo==null) {
                        w.setAvoidOrder(_endW.getAvoidOrder()); 
                    }                       
                } else {
                    w.addBlockOrder(bo);        // copy only                
                }
                List <ThrottleSetting> commands = _startW.getThrottleCommands();
                for (int i=0; i<commands.size(); i++) {
                    w.addThrottleCommand(new ThrottleSetting(commands.get(i)));
                }
                if (_endW!=null) {
                    commands = _endW.getThrottleCommands();
                    for (int i=0; i<commands.size(); i++) {
                        w.addThrottleCommand(new ThrottleSetting(commands.get(i)));
                    }                   
                }
                _warrantMap.put(w.getDisplayName(), w);
                newWarrantFrame(new WarrantFrame(w, false)); // copy/concat warrant/s
            } else {
                newWarrantFrame(new WarrantFrame(w, true));  // create new warrant
            }
            _startW = null;
            _endW =null;
            dispose();
        }

        private void makeWarrant() {
            String sysName = _sysNameBox.getText().trim();
            String userName = _userNameBox.getText().trim();
            sysName = sysName.toUpperCase();
            if (!sysName.startsWith("IW")) {
                sysName = "IW"+sysName;
            }
            _sysNameBox.setText(sysName);
            if (sysName.length()<3) {
                return;
            }
            if (userName.length()==0) {
                userName = null;
            }
            boolean failed = false;
            Warrant w = null;
            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            w = manager.getBySystemName(sysName);
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

    private final static Logger log = LoggerFactory.getLogger(WarrantTableAction.class.getName());
}

package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

import jmri.jmrit.logixng.FemaleSocket;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.UserPreferencesManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Editor of ConditionalNG
 * 
 * @author Daniel Bergqvist 2020
 */
public class ClipboardEditor extends TreeViewer {

    
    /**
     * Maintain a list of listeners -- normally only one.
     */
    private final List<ClipboardEventListener> listenerList = new ArrayList<>();
    
    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    final HashMap<String, String> clipboardData = new HashMap<>();
    
    /**
     * Construct a ConditionalEditor.
     */
    public ClipboardEditor() {
        super(InstanceManager.getDefault(LogixNG_Manager.class).getClipboard().getRoot());
        
        setTitle(Bundle.getMessage("TitleClipboardEditor"));
    }
    
    @Override
    public void initComponents() {
        super.initComponents();
        
        // The menu is created in parent class TreeViewer
        JMenuBar menuBar = getJMenuBar();
        
        JMenu toolsMenu = new JMenu(Bundle.getMessage("MenuTools"));
        JMenuItem openClipboardItem = new JMenuItem(Bundle.getMessage("MenuOpenClipboard"));
        openClipboardItem.addActionListener((ActionEvent e) -> {
            openClipboard();
        });
        toolsMenu.add(openClipboardItem);
        menuBar.add(toolsMenu);
        
        
        PopupMenu popup = new PopupMenu(tree, femaleSocketTreeModel);
        popup.init();
        
        // The JTree can get big, so allow it to scroll
        JScrollPane scrollpane = new JScrollPane(tree);

        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));
        
        // Display it all in a window and make the window appear
        pPanel.add(scrollpane, "Center");

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pPanel);
        
//        initMinimumSize(new Dimension(panelWidth700, panelHeight500));
    }

    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        clipboardData.clear();
        clipboardData.put("Finish", "Clipboard");  // NOI18N
        fireClipboardEvent();
    }
    
    public void openClipboard() {
        
    }
    
    public void addLogixNGEventListener(ClipboardEventListener listener) {
        listenerList.add(listener);
    }
    
    /**
     * Notify the listeners to check for new data.
     */
    void fireClipboardEvent() {
        for (ClipboardEventListener l : listenerList) {
            l.clipboardEventOccurred();
        }
    }
    
    
    public interface ClipboardEventListener extends EventListener {
        
        public void clipboardEventOccurred();
    }
    
    
    
    private final class PopupMenu extends JPopupMenu implements ActionListener {
        
        private static final String ACTION_COMMAND_REMOVE = "remove";
        private static final String ACTION_COMMAND_CUT = "cut";
        private static final String ACTION_COMMAND_COPY = "copy";
        
        private final JTree _tree;
        private FemaleSocket _currentFemaleSocket;
        private TreePath _currentPath;
        
        private JMenuItem menuItemRemove;
        private JMenuItem menuItemCut;
        private JMenuItem menuItemCopy;
        
        PopupMenu(JTree tree, FemaleSocketTreeModel model) {
            _tree = tree;
        }
        
        private void init() {
            menuItemRemove = new JMenuItem("Remove");
            menuItemRemove.addActionListener(this);
            menuItemRemove.setActionCommand(ACTION_COMMAND_REMOVE);
            add(menuItemRemove);
            addSeparator();
            menuItemCut = new JMenuItem("Cut");
            menuItemCut.addActionListener(this);
            menuItemCut.setActionCommand(ACTION_COMMAND_CUT);
            add(menuItemCut);
            menuItemCopy = new JMenuItem("Copy");
            menuItemCopy.addActionListener(this);
            menuItemCopy.setActionCommand(ACTION_COMMAND_COPY);
            add(menuItemCopy);
            setOpaque(true);
            setLightWeightPopupEnabled(true);
            
            _tree.addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseReleased(MouseEvent e) {
                            if (e.isPopupTrigger()) {
                                // Get the row the user has clicked on
                                TreePath path = _tree.getClosestPathForLocation(e.getX(), e.getY());
                                if (path != null) {
                                    // Check that the user has clicked on a row.
                                    Rectangle rect = _tree.getPathBounds(path);
                                    if ((e.getY() >= rect.y) && (e.getY() <= rect.y + rect.height)) {
                                        FemaleSocket femaleSocket = (FemaleSocket) path.getLastPathComponent();
                                        _tree.getLocationOnScreen();
                                        _tree.getX();
                                        showPopup(e.getX(), e.getY(), femaleSocket, path);
                                    }
                                }
                            }
                        }
                    }
            );
        }
        
        private void showPopup(int x, int y, FemaleSocket femaleSocket, TreePath path) {
            _currentFemaleSocket = femaleSocket;
            _currentPath = path;
            
            boolean isConnected = femaleSocket.isConnected();
            
            menuItemRemove.setEnabled(isConnected);
            menuItemCut.setEnabled(isConnected);
            menuItemCopy.setEnabled(isConnected);
            
            show(_tree, x, y);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case ACTION_COMMAND_REMOVE:
                    break;
                    
                case ACTION_COMMAND_CUT:
                    if (_currentFemaleSocket.isConnected()) {
                        Clipboard clipboard =
                                InstanceManager.getDefault(LogixNG_Manager.class).getClipboard();
                        clipboard.add(_currentFemaleSocket.getConnectedSocket());
                        _currentFemaleSocket.disconnect();
                        updateTree();
                    } else {
                        log.error("_currentFemaleSocket is not connected");
                    }
                    break;
                    
                case ACTION_COMMAND_COPY:
                    break;
                    
                default:
                    log.error("e.getActionCommand() returns unknown value {}", e.getActionCommand());
            }
        }
        
        private void updateTree() {
            for (TreeModelListener l : femaleSocketTreeModel.listeners) {
                TreeModelEvent tme = new TreeModelEvent(
                        _currentFemaleSocket,
                        _currentPath.getPath()
                );
                l.treeNodesChanged(tme);
            }
            tree.updateUI();
        }
        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(ClipboardEditor.class);

}

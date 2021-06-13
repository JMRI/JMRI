package jmri.jmrit.logixng.tools.swing;

import java.awt.Color;
import java.awt.*;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.VariableData;
import jmri.util.FileUtil;
import jmri.util.ThreadingUtil;

/**
 * Show the action/expression tree.
 * <P>
 * Base class for ConditionalNG editors
 * 
 * @author Daniel Bergqvist 2018
 */
public class TreePane extends JPanel implements PropertyChangeListener {

    private boolean _rootVisible = true;
    
    private static final Map<String, Color> FEMALE_SOCKET_COLORS = new HashMap<>();
    
    JTree _tree;
    
    protected final FemaleSocket _femaleRootSocket;
    protected FemaleSocketTreeModel femaleSocketTreeModel;
    
    
    /**
     * Construct a ConditionalEditor.
     *
     * @param femaleRootSocket the root of the tree
     */
    public TreePane(FemaleSocket femaleRootSocket) {
        _femaleRootSocket = femaleRootSocket;
        // Note!! This must be made dynamic, so that new socket types are recognized automaticly and added to the list
        // and the list must be saved between runs.
        FEMALE_SOCKET_COLORS.put("jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket", Color.RED);
        FEMALE_SOCKET_COLORS.put("jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket", Color.BLUE);
        
        _femaleRootSocket.forEntireTree((Base b) -> {
            b.addPropertyChangeListener(TreePane.this);
        });
    }
    
    public void initComponents() {
        initComponents((FemaleSocket femaleSocket, JPanel panel) -> panel);
    }
    
    public void initComponents(FemaleSocketDecorator decorator) {
        
        femaleSocketTreeModel = new FemaleSocketTreeModel(_femaleRootSocket);
        
        // Create a JTree and tell it to display our model
        _tree = new JTree();
        _tree.setRowHeight(0);
        ToolTipManager.sharedInstance().registerComponent(_tree);
        _tree.setModel(femaleSocketTreeModel);
        _tree.setCellRenderer(new FemaleSocketTreeRenderer(decorator));
        
        _tree.setRootVisible(_rootVisible);
        _tree.setShowsRootHandles(true);
        
        // Expand the entire tree
        for (int i = 0; i < _tree.getRowCount(); i++) {
            FemaleSocket femaleSocket = (FemaleSocket) _tree.getPathForRow(i).getLastPathComponent();
            if (femaleSocket.isConnected() && femaleSocket.getConnectedSocket().isEnabled()) {
                _tree.expandRow(i);
            }
        }
        
        // The JTree can get big, so allow it to scroll
        JScrollPane scrollpane = new JScrollPane(_tree);
        
        // create panel
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Display it all in a window and make the window appear
        add(scrollpane, "Center");
    }

    public boolean getRootVisible() {
        return _rootVisible;
    }
    
    public void setRootVisible(boolean rootVisible) {
        _rootVisible = rootVisible;
    }
    
    /**
     * Get the path for the item base.
     * 
     * @param base the item to look for
     * @param list a list of the female sockets that makes up the path
     */
    protected void getPath(Base base, List<FemaleSocket> list) {
        for (Base b = base; b != null; b = b.getParent()) {
            if (b instanceof FemaleSocket) list.add(0, (FemaleSocket)b);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        
        if (Base.PROPERTY_CHILD_COUNT.equals(evt.getPropertyName())) {
            
            // Remove myself as listener from sockets that has been removed
            if (evt.getOldValue() != null) {
                if (! (evt.getOldValue() instanceof List)) throw new RuntimeException("Old value is not a list");
                for (FemaleSocket socket : (List<FemaleSocket>)evt.getOldValue()) {
                    socket.removePropertyChangeListener(this);
                }
            }
            
            // Add myself as listener to sockets that has been added
            if (evt.getNewValue() != null) {
                if (! (evt.getNewValue() instanceof List)) throw new RuntimeException("New value is not a list");
                for (FemaleSocket socket : (List<FemaleSocket>)evt.getNewValue()) {
                    socket.addPropertyChangeListener(this);
                }
            }
            
//            tree.addTreeWillExpandListener(tel);
//            tree.addTreeExpansionListener(tel);
//            tree.expandPath(path);
//            tree.expandRow(row);
//            tree.getPathForRow(row)
//            tree.isCollapsed(row)
//            tree.isCollapsed(path)
//            tree.isExpanded(path)
//            tree.isExpanded(row)
//            tree.isVisible(path)
//            tree.makeVisible(path);
            
            // Update the tree
            Base b = (Base)evt.getSource();
            
            List<FemaleSocket> list = new ArrayList<>();
            getPath(b, list);
            
            ThreadingUtil.runOnGUIEventually(() -> {
                FemaleSocket femaleSocket = list.get(list.size()-1);
                updateTree(femaleSocket, list.toArray());
            });
        }
        
        
        if (Base.PROPERTY_CHILD_REORDER.equals(evt.getPropertyName())) {
            
            if (! (evt.getNewValue() instanceof List)) throw new RuntimeException("New value is not a list");
            for (FemaleSocket socket : (List<FemaleSocket>)evt.getNewValue()) {
                // Update the tree
                List<FemaleSocket> list = new ArrayList<>();
                getPath(socket, list);
                ThreadingUtil.runOnGUIEventually(() -> {
                    updateTree(socket, list.toArray());
                });
            }
        }
        
        
        if (Base.PROPERTY_SOCKET_CONNECTED.equals(evt.getPropertyName())
                || Base.PROPERTY_SOCKET_DISCONNECTED.equals(evt.getPropertyName())) {
            
            FemaleSocket femaleSocket = ((FemaleSocket)evt.getSource());
            List<FemaleSocket> list = new ArrayList<>();
            getPath(femaleSocket, list);
            ThreadingUtil.runOnGUIEventually(() -> {
                updateTree(femaleSocket, list.toArray());
            });
        }
    }
    
    protected void updateTree(FemaleSocket currentFemaleSocket, Object[] currentPath) {
        for (TreeModelListener l : femaleSocketTreeModel.listeners) {
            TreeModelEvent tme = new TreeModelEvent(
                    currentFemaleSocket,
                    currentPath
            );
            l.treeNodesChanged(tme);
        }
        _tree.updateUI();
    }
    
    public void updateTree(Base item) {
            List<FemaleSocket> list = new ArrayList<>();
            getPath(item, list);
            
            FemaleSocket femaleSocket = list.get(list.size()-1);
            updateTree(femaleSocket, list.toArray());
    }
    
    public void dispose() {
        _femaleRootSocket.forEntireTree((Base b) -> {
            b.addPropertyChangeListener(TreePane.this);
        });
    }
    
    
    /**
     * The methods in this class allow the JTree component to traverse the
     * female sockets of the ConditionalNG tree.
     */
    public static class FemaleSocketTreeModel implements TreeModel {

        private final FemaleSocket _root;
        protected final List<TreeModelListener> listeners = new ArrayList<>();
        
        
        public FemaleSocketTreeModel(FemaleSocket root) {
            this._root = root;
        }
        
        @Override
        public Object getRoot() {
            return _root;
        }

        @Override
        public boolean isLeaf(Object node) {
            FemaleSocket socket = (FemaleSocket) node;
            if (!socket.isConnected()) {
                return true;
            }
            return socket.getConnectedSocket().getChildCount() == 0;
        }

        @Override
        public int getChildCount(Object parent) {
            FemaleSocket socket = (FemaleSocket) parent;
            if (!socket.isConnected()) {
                return 0;
            }
            return socket.getConnectedSocket().getChildCount();
        }

        @Override
        public Object getChild(Object parent, int index) {
            FemaleSocket socket = (FemaleSocket) parent;
            if (!socket.isConnected()) {
                return null;
            }
            return socket.getConnectedSocket().getChild(index);
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            FemaleSocket socket = (FemaleSocket) parent;
            if (!socket.isConnected()) {
                return -1;
            }
            
            MaleSocket connectedSocket = socket.getConnectedSocket();
            for (int i = 0; i < connectedSocket.getChildCount(); i++) {
                if (child == connectedSocket.getChild(i)) {
                    return i;
                }
            }
            return -1;
        }

        // This method is invoked by the JTree only for editable trees.  
        // This TreeModel does not allow editing, so we do not implement 
        // this method.  The JTree editable property is false by default.
        @Override
        public void valueForPathChanged(TreePath path, Object newvalue) {
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

    }
    
    
    private static final class FemaleSocketTreeRenderer implements TreeCellRenderer {

        private final FemaleSocketDecorator _decorator;
        private static ImageIcon _lockIcon;
        
        
        public FemaleSocketTreeRenderer(FemaleSocketDecorator decorator) {
            this._decorator = decorator;
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            FemaleSocket socket = (FemaleSocket)value;
            
            JPanel mainPanel = new JPanel();
            
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setOpaque(false);
            
            JPanel commentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            mainPanel.add(commentPanel);
            
            JPanel panel = new JPanel();
            panel.setAlignmentX(LEFT_ALIGNMENT);
            mainPanel.add(panel);
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setOpaque(false);
            
            JLabel socketLabel = new JLabel(socket.getShortDescription());
            Font font = socketLabel.getFont();
            socketLabel.setFont(font.deriveFont((float)(font.getSize2D()*1.7)));
            socketLabel.setForeground(FEMALE_SOCKET_COLORS.get(socket.getClass().getName()));
//            socketLabel.setForeground(Color.red);
            panel.add(socketLabel);
            
            panel.add(javax.swing.Box.createRigidArea(new Dimension(5,0)));
            
            JLabel socketNameLabel = new JLabel(socket.getName());
            socketNameLabel.setForeground(FEMALE_SOCKET_COLORS.get(socket.getClass().getName()));
//            socketNameLabel.setForeground(Color.red);
            panel.add(socketNameLabel);
            
            panel.add(javax.swing.Box.createRigidArea(new Dimension(5,0)));
            
            JLabel connectedItemLabel = new JLabel();
            if (socket.isConnected()) {
                
                MaleSocket connectedSocket = socket.getConnectedSocket();
                
                if (connectedSocket.isLocked()) {
                    if (_lockIcon == null) {
                        _lockIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/logixng/lock.png", FileUtil.Location.INSTALLED));
                    }
                    JLabel icLabel = new JLabel(_lockIcon, JLabel.CENTER);
                    panel.add(icLabel);
                }
                
                String comment = connectedSocket.getComment();
                if (comment != null) {
                    JLabel commentLabel = new JLabel();
                    commentLabel.setText("<html><pre>"+comment+"</pre></html>");
                    commentLabel.setForeground(Color.GRAY);
                    Font font2 = commentLabel.getFont();
                    commentLabel.setFont(font2.deriveFont(Font.ITALIC));
                    commentPanel.setOpaque(false);
                    commentPanel.add(commentLabel);
                    commentPanel.setAlignmentX(LEFT_ALIGNMENT);
                    commentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
                }
                
                String label = connectedSocket.getLongDescription();
                if (connectedSocket.getUserName() != null) {
                    label += " ::: " + connectedSocket.getUserName();
                }
                if (!connectedSocket.isEnabled()) {
                    label = "<html><strike>" + label + "</strike></html>";
                }
                connectedItemLabel.setText(label);
                
                mainPanel.setToolTipText(connectedSocket.getShortDescription());
                
                for (VariableData variableData : connectedSocket.getLocalVariables()) {
                    JLabel variableLabel = new JLabel(Bundle.getMessage(
                            "PrintLocalVariable",
                            variableData._name,
                            variableData._initalValueType,
                            variableData._initialValueData));
                    variableLabel.setAlignmentX(LEFT_ALIGNMENT);
                    mainPanel.add(variableLabel);
                }
            }
            
            panel.add(connectedItemLabel);
            
            return _decorator.decorate(socket, mainPanel);
        }
        
    }
    
    
    public interface FemaleSocketDecorator {
        public JPanel decorate(FemaleSocket femaleSocket, JPanel panel);
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeViewer.class);

}

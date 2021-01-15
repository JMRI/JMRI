package jmri.jmrit.logixng.tools.swing;

import java.awt.Color;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.SymbolTable.VariableData;
import jmri.util.JmriJFrame;

/**
 * Show the action/expression tree.
 * <P>
 * Base class for ConditionalNG editors
 * 
 * @author Daniel Bergqvist 2018
 */
public class TreeViewer extends JmriJFrame implements PropertyChangeListener {

    private static final int panelWidth = 700;
    private static final int panelHeight = 500;
    
    private boolean _rootVisible = true;
    
    public final TreePane _treePane;
    
    
    /**
     * Construct a ConditionalEditor.
     *
     * @param femaleRootSocket the root of the tree
     */
    public TreeViewer(FemaleSocket femaleRootSocket) {
        _treePane = new TreePane(femaleRootSocket);
    }
    
    @Override
    public void initComponents() {
        super.initComponents();
        
        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        JMenuItem closeWindowItem = new JMenuItem(Bundle.getMessage("CloseWindow"));
        closeWindowItem.addActionListener((ActionEvent e) -> {
            dispose();
        });
        fileMenu.add(closeWindowItem);
        menuBar.add(fileMenu);
        
/*        
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new TimeDiagram.CreateNewLogixNGAction("Create a LogixNG"));
        toolMenu.add(new CreateNewLogixNGAction(Bundle.getMessage("TitleOptions")));
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
        toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
        toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
        toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
        toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));
        menuBar.add(toolMenu);
*/

        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N
        
        _treePane.initComponents();
        
        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(_treePane);
        
        initMinimumSize(new Dimension(panelWidth, panelHeight));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
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
            
            FemaleSocket femaleSocket = list.get(list.size()-1);
            updateTree(femaleSocket, list.toArray());
        }
        
        
        if (Base.PROPERTY_CHILD_REORDER.equals(evt.getPropertyName())) {
            
            if (! (evt.getNewValue() instanceof List)) throw new RuntimeException("New value is not a list");
            for (FemaleSocket socket : (List<FemaleSocket>)evt.getNewValue()) {
                // Update the tree
                List<FemaleSocket> list = new ArrayList<>();
                getPath(socket, list);
                updateTree(socket, list.toArray());
            }
        }
        
        
        if (Base.PROPERTY_SOCKET_CONNECTED.equals(evt.getPropertyName())
                || Base.PROPERTY_SOCKET_DISCONNECTED.equals(evt.getPropertyName())) {
            
            FemaleSocket femaleSocket = ((FemaleSocket)evt.getSource());
            List<FemaleSocket> list = new ArrayList<>();
            getPath(femaleSocket, list);
            updateTree(femaleSocket, list.toArray());
        }
    }
    
    protected void updateTree(FemaleSocket currentFemaleSocket, Object[] currentPath) {
        _treePane.updateTree(currentFemaleSocket, currentPath);
    }
    
    @Override
    public void dispose() {
        _treePane.dispose();
        super.dispose();
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TreeViewer.class);

}

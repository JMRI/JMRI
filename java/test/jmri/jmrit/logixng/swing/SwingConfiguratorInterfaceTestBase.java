package jmri.jmrit.logixng.swing;

import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.tools.swing.ConditionalNGEditor;

import org.netbeans.jemmy.operators.*;

/**
 * Base class for SwingConfiguratorInterface classes
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class SwingConfiguratorInterfaceTestBase {
    
    protected JDialogOperator editItem(
            ConditionalNG conditionalNG,
            String title1, String title2, int row) {
        
        ConditionalNGEditor treeEdit = new ConditionalNGEditor(conditionalNG);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        treeEdit.initComponents();
        treeEdit.setVisible(true);
        
        JFrameOperator treeFrame = new JFrameOperator(title1);
        JTreeOperator jto = new JTreeOperator(treeFrame);
        
        TreePath tp = jto.getPathForRow(row);
        
        JPopupMenu jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Edit");
        
        JDialogOperator editItemDialog = new JDialogOperator(title2);  // NOI18N
        
        return editItemDialog;
    }
    
}

package jmri.util.swing;

import java.beans.PropertyChangeEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.tree.DefaultTreeModel;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for JCheckBoxTree
 * @author Steve Young Copyright (C) 2025
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class JCheckBoxTreeTest {

    private JFrame f;

    @Test
    public void testTree() {

        f.setSize(500, 500);
        JCheckBoxTree cbt = new JCheckBoxTree();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Top Level 1");
        DefaultMutableTreeNode level2 = new DefaultMutableTreeNode("Level 2 in Level 1");
        DefaultMutableTreeNode level3 = new DefaultMutableTreeNode("Level 3 in Level 2");
        DefaultMutableTreeNode level2again = new DefaultMutableTreeNode("Level 2 Again");
        level2.add(level3);
        root.add(level2);
        root.add(level2again);

        DefaultTreeModel model = new DefaultTreeModel(root);
        cbt.setModel(model);

        TreePcl listener = new TreePcl();
        cbt.addPropertyChangeListener(listener);

        f.add(cbt);
        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle());

        JTreeOperator jto = new JTreeOperator(jfo);
        Assertions.assertNotNull(jto);

        Assertions.assertEquals(0, cbt.getCheckedPaths().size());


        cbt.treePathClicked(jto.getPathForRow(0), true);
        Assertions.assertEquals(4, cbt.getCheckedPaths().size());

        cbt.treePathClicked(jto.getPathForRow(0), true);

        expandAllNodes(0, cbt.getRowCount(), cbt);

        Assertions.assertEquals("[Top Level 1]", jto.getPathForRow(0).toString());
        Assertions.assertEquals("[Top Level 1, Level 2 in Level 1]", jto.getPathForRow(1).toString());
        Assertions.assertEquals("[Top Level 1, Level 2 in Level 1, Level 3 in Level 2]", jto.getPathForRow(2).toString());
        Assertions.assertEquals("[Top Level 1, Level 2 Again]", jto.getPathForRow(3).toString());

        cbt.treePathClicked(jto.getPathForRow(0), true);
        Assertions.assertEquals(4, cbt.getCheckedPaths().size());

        cbt.treePathClicked(jto.getPathForRow(0), true);
        Assertions.assertEquals(0, cbt.getCheckedPaths().size());

        cbt.treePathClicked(jto.getPathForRow(1), true);
        Assertions.assertEquals(2, cbt.getCheckedPaths().size());

        cbt.treePathClicked(jto.getPathForRow(2), true);
        Assertions.assertEquals(0, cbt.getCheckedPaths().size());

        cbt.treePathClicked(jto.getPathForRow(3), true);
        Assertions.assertEquals(1, cbt.getCheckedPaths().size());
        Assertions.assertEquals(7, listener.getTriggered());

        cbt.removePropertyChangeListener(listener);

        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    public static void expandAllNodes( int startingIndex, int rowCount, JCheckBoxTree tree){
        for(int i=startingIndex;i<rowCount;++i){
            tree.expandRow(i);
        }
        if(tree.getRowCount()!=rowCount){
            expandAllNodes( rowCount, tree.getRowCount(), tree);
        }
    }

    private static class TreePcl implements java.beans.PropertyChangeListener {

        private int triggered = 0;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ( JCheckBoxTree.PROPERTY_CHANGE_CHECKBOX_STATUS.equals(evt.getPropertyName()) ) {
                triggered++;
            }
        }

        int getTriggered(){
            return triggered;
        }

    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) {
        JUnitUtil.setUp();
        f = new JFrame("JCheckBoxTreeTestTestTree");
    }

    @AfterEach
    public void tearDown() {
        if ( f != null ) {
            JUnitUtil.dispose(f);
            f = null;
        }
        JUnitUtil.tearDown();
    }

}

// PanelProFrame.java

 package apps.gui3.paned;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.io.File;

import jmri.util.swing.*;
import jmri.util.swing.multipane.*;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Standalone, but paned, PanelPro window.
 *
 * Ignores WindowInterface, since standalone.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.1 $
 */
 
public class PanelProFrame extends MultiPaneWindow {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public PanelProFrame(String title) {
    	super(title, new File("xml/config/apps/panelpro/Gui3LeftTree.xml"), 
    	        new File("xml/config/apps/panelpro/Gui3Menus.xml"), new File("xml/config/apps/panelpro/Gui3MainToolBar.xml"));
    }
     
       
    /**
     * Make a PanelPro tree, and add the LocoNet stuff, because we assume those systems active here
     */
    @Override
    protected TreeNode makeNavTreeTopNode(File treeFile, PanedInterface rightTopWI) {
        DefaultMutableTreeNode top = JTreeUtil.loadTree(treeFile, rightTopWI, null);  // no context
        
        LocoNetSystemConnectionMemo lm1 = (LocoNetSystemConnectionMemo)jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class).get(0);
        DefaultMutableTreeNode l1 = JTreeUtil.loadTree(new File("xml/config/parts/jmri/jmrix/loconet/ToolsTree.xml"), rightTopWI, lm1);
        l1.setUserObject("LocoNet");
        top.add(l1);

        LocoNetSystemConnectionMemo lm2 = (LocoNetSystemConnectionMemo)jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class).get(1);
        DefaultMutableTreeNode l2 = JTreeUtil.loadTree(new File("xml/config/parts/jmri/jmrix/loconet/ToolsTree.xml"), rightTopWI, lm2);
        l2.setUserObject("LocoNet2");
        top.add(l2);

        return top;
    }
}

/* @(#)PanelProFrame.java */

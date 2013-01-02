// CombinedLocoSelTreePane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import java.util.List;

/**
 * Provide GUI controls to select a known loco and/or new decoder.
 * <P>
 * This is an extension of the CombinedLocoSelPane class to use
 * a JTree instead of a JComboBox for the decoder selection.
 * The loco selection (Roster manipulation) parts are unchanged.
 * <P>
 * The JComboBox implementation always had to have selected entries, so
 * we added dummy "select from .." items at the top & used those to
 * indicate that there was no selection in that box.
 * Here, the lack of a selection indicates there's no selection.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision$
 */
public class CombinedLocoSelTreePane extends CombinedLocoSelPane  {

    public CombinedLocoSelTreePane(JLabel s) {
            super(s);
    }

    public CombinedLocoSelTreePane() {
            super();
    }

    protected JTree dTree;
    InvisibleTreeModel dModel;
    DecoderTreeNode dRoot;
    protected TreeSelectionListener dListener;
    
    JRadioButton showAll;
    JRadioButton showMatched;
    protected JPanel viewButtons;
    /**
     * Create the panel used to select the decoder
     */
    protected JPanel layoutDecoderSelection() {
        JPanel pane1a = new JPanel(new BorderLayout());
        pane1a.add(new JLabel(rbt.getString("LabelDecoderInstalled")), BorderLayout.NORTH);
        // create the list of manufacturers; get the list of decoders, and add elements
        dRoot = new DecoderTreeNode("Root");
        dModel = new InvisibleTreeModel(dRoot);
        dTree = new JTree(dModel){
            public String getToolTipText(MouseEvent evt) {
                if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;
                TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
                return ((DecoderTreeNode)curPath.getLastPathComponent()).getToolTipText();
            }
        };
        dTree.setToolTipText("");

        List<DecoderFile> decoders = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, null, null);
        int len = decoders.size();
        DecoderTreeNode mfgElement = null;
        DecoderTreeNode familyElement = null;
        HashMap<String, DecoderTreeNode> familyNameNode = new HashMap<String, DecoderTreeNode>();
        for (int i = 0; i<len; i++) {
            DecoderFile decoder = decoders.get(i);
            String mfg = decoder.getMfg();
            String family = decoder.getFamily();
            String model = decoder.getModel();
            log.debug(" process "+mfg+"/"+family+"/"+model
                        +" on nodes "
                        +(mfgElement==null ? "<null>":mfgElement.toString()+"("+mfgElement.getChildCount()+")")+"/"
                        +(familyElement==null ? "<null>":familyElement.toString()+"("+familyElement.getChildCount()+")")
                    );
            // build elements
            if (mfgElement==null || !mfg.equals(mfgElement.toString()) ) {
                // need new mfg node
                mfgElement = new DecoderTreeNode(mfg,
                    "CV8 = "+DecoderIndexFile.instance().mfgIdFromName(mfg), "");
                dModel.insertNodeInto(mfgElement, dRoot, dRoot.getChildCount());
                familyNameNode = new HashMap<String, DecoderTreeNode>();
                familyElement = null;
            }
        	String famComment = decoders.get(i).getFamilyComment();
        	String verString = decoders.get(i).getVersionsAsString();
        	String hoverText = "";
        	if (famComment == "" || famComment == null) {
        		if (verString != "") {
            		hoverText = "CV7=" + verString;
        		}
        	} else {
        		if (verString == "") {
            		hoverText = famComment;
        		} else {
            		hoverText = famComment + "  CV7=" + verString;
        		}
        	}
            if (familyElement==null || (!family.equals(familyElement.toString()) && !familyNameNode.containsKey(family) )) {
                // need new family node - is there only one model? Expect the
                // family element, plus the model element, so check i+2
                // to see if its the same, or if a single-decoder family
                // appears to have decoder names separate from the family name
                if ( (i+2>=len) ||
                        decoders.get(i+2).getFamily().equals(family) ||
                        !decoders.get(i+1).getModel().equals(family)
                    ) {
                    // normal here; insert the new family element & exit
                    log.debug("normal family update case: "+family);
                    familyElement = new DecoderTreeNode(family,
                    									hoverText,
                                                        decoders.get(i).titleString());
                    dModel.insertNodeInto(familyElement, mfgElement, mfgElement.getChildCount());
                    familyNameNode.put(family, familyElement);
                    continue;
                } else {
                    // this is short case; insert decoder entry (next) here
                    log.debug("short case, i="+i+" family="+family+" next "+
                                decoders.get(i+1).getModel() );
                    if (i+1 > len) log.error("Unexpected single entry for family: "+family);
                    family = decoders.get(i+1).getModel();
                    familyElement = new DecoderTreeNode(family,
                    									hoverText,
                                                        decoders.get(i).titleString());
                    dModel.insertNodeInto(familyElement, mfgElement, mfgElement.getChildCount());
                    familyNameNode.put(family, familyElement);
                    i = i+1;
                    continue;
                }
            }
            // insert at the decoder level, except if family name is the same
            if (!family.equals(model)){
                if(familyNameNode.containsKey(family))
                    familyElement = familyNameNode.get(family);
                dModel.insertNodeInto(new DecoderTreeNode(model,
                                                        hoverText,
                                                        decoders.get(i).titleString()),
                                    familyElement, familyElement.getChildCount());
            }
        }  // end of loop over decoders

        // build the tree GUI
        pane1a.add(new JScrollPane(dTree), BorderLayout.CENTER);
        dTree.expandPath(new TreePath(dRoot));
        dTree.setRootVisible(false);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        // tree listener
        dTree.addTreeSelectionListener(dListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                if (!dTree.isSelectionEmpty() && dTree.getSelectionPath()!=null &&
                        // can't be just a mfg, has to be at least a family
                        dTree.getSelectionPath().getPathCount()>2 &&
                        // can't be a multiple decoder selection
                        dTree.getSelectionCount()<2) {
                    // decoder selected - reset and disable loco selection
                    log.debug("Selection event with "+dTree.getSelectionPath().toString());
                    if (locoBox != null) locoBox.setSelectedIndex(0);
                    go2.setEnabled(true);
                    go2.setRequestFocusEnabled(true);
                    go2.requestFocus();
                    go2.setToolTipText(rbt.getString("TipClickToOpen"));
                } else {
                    // decoder not selected - require one
                    go2.setEnabled(false);
                    go2.setToolTipText(rbt.getString("TipSelectLoco"));
                }
            }
        });
        
//      Mouselistener for doubleclick activation of proprammer   
        dTree.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent me){
             // Clear any status messages and ensure the tree is in single path select mode
             if (_statusLabel != null) _statusLabel.setText(rbt.getString("StateIdle"));
             dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
            
             if (me.getClickCount() == 2){
                 if (go2.isEnabled() && ((TreeNode)dTree.getSelectionPath().getLastPathComponent()).isLeaf()) go2.doClick();
                }
             }
            } );
        
        viewButtons = new JPanel();
        iddecoder = addDecoderIdentButton();
        if (iddecoder!=null) {
            viewButtons.add(iddecoder);
        }
        if (jmri.InstanceManager.programmerManagerInstance() != null &&
            jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            showAll = new JRadioButton(rbt.getString("LabelAll"));
            showAll.setSelected(true);
            showMatched = new JRadioButton(rbt.getString("LabelMatched"));
            ButtonGroup group = new ButtonGroup();
            group.add(showAll);
            group.add(showMatched);
            viewButtons.add(showAll);
            viewButtons.add(showMatched);
            

            
            pane1a.add(viewButtons, BorderLayout.SOUTH);
            showAll.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (dModel.isActivatedFilter()) {
                        dModel.activateFilter(false);
                        dModel.reload();
                        for(TreePath path:selectedPath){
                            dTree.expandPath(path);
                            dTree.addSelectionPath(path);
                            dTree.scrollPathToVisible(path);
                        }
                      }
                    }
                }
            );
            showMatched.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!dModel.isActivatedFilter()) {
                        dModel.activateFilter(true);
                        dModel.reload();
                        for(TreePath path:selectedPath){
                            dTree.expandPath(path);
                            dTree.scrollPathToVisible(path);
                        }
                      }
                    }
                }
            );
        }
        
        return pane1a;
    }
    
    @SuppressWarnings("unchecked")
    public void resetSelections(){
        Enumeration<DecoderTreeNode> e = dRoot.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            e.nextElement().setVisible(false);
        }
        dModel.activateFilter(false);
        dModel.reload();
        showAll.setSelected(true);
        selectedPath = new ArrayList<TreePath>();
        dTree.expandPath(new TreePath(dRoot));
        dTree.setExpandsSelectedPaths(true);
        int row = dTree.getRowCount() - 1;
        while (row >= 0) {
          dTree.collapseRow(row);
          row--;
          }
    }

    /**
     * Decoder identify has matched one or more specific types
     */
    @SuppressWarnings("unchecked")
	void updateForDecoderTypeID(List<DecoderFile> pList) {
        // find and select the first item
        if (log.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer("Identified "+pList.size()+" matches: ");
            for (int i = 0 ; i< pList.size(); i++)
                buf.append(pList.get(i).getModel()+":");
            log.debug(buf.toString());
        }
        if (pList.size()<=0) {
            log.error("Found empty list in updateForDecoderTypeID, should not happen");
            return;
        }
        dTree.clearSelection();
        // If there are multiple matches change tree to allow multiple selections by the program
        // and issue a warning instruction in the status bar
        if (pList.size()>1) {
        	dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        	_statusLabel.setText(rbt.getString("StateMultipleMatch"));
        }
        else dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        ArrayList<DecoderTreeNode> selectedNode = new ArrayList<DecoderTreeNode>();
        ArrayList<DecoderTreeNode> mfgNode = new ArrayList<DecoderTreeNode>();
        ArrayList<DecoderTreeNode> modelNode = new ArrayList<DecoderTreeNode>();
        ArrayList<DecoderTreeNode> familyNode = new ArrayList<DecoderTreeNode>();
        // Select the decoder(s) in the tree
        for (int i=0; i < pList.size(); i++) {
        	
        	DecoderFile f = pList.get(i);
        	String findMfg = f.getMfg();
        	String findFamily = f.getFamily();
        	String findModel = f.getModel();
        
	        Enumeration<DecoderTreeNode> e = dRoot.breadthFirstEnumeration();
	        while (e.hasMoreElements()) {
                //log.debug(node.getPath().toString());
	            DecoderTreeNode node = e.nextElement();
	            // convert path to comparison string
	            TreeNode[] list = node.getPath();
	            if (list.length == 3) {
                    node.setVisible(true);
	                // check for match to mfg, model, model
	                if (list[1].toString().equals(findMfg)
	                    && list[2].toString().equals(findModel))
	                        {
                                if(!mfgNode.contains(list[1]))
                                    mfgNode.add((DecoderTreeNode)list[1]);
                                if(!modelNode.contains(list[2]))
                                    modelNode.add((DecoderTreeNode)list[2]);
	                            TreePath path = new TreePath(node.getPath());
	                            dTree.expandPath(path);
	                            dTree.addSelectionPath(path);
	                            dTree.scrollPathToVisible(path);
                                selectedNode.add(node);
                                selectedPath.add(path);
	                            break;
	                        }
	            } else if (list.length == 4 ) {
	                // check for match to mfg, family, model
	                if (list[1].toString().equals(findMfg)
	                    && list[2].toString().equals(findFamily)
	                    && list[3].toString().equals(findModel))
	                        {
                                if(!mfgNode.contains(list[1]))
                                    mfgNode.add((DecoderTreeNode)list[1]);
                                if(!modelNode.contains(list[3]))
                                    modelNode.add((DecoderTreeNode)list[3]);
                                if(!familyNode.contains(list[2]))
                                    familyNode.add((DecoderTreeNode)list[2]);
	                            TreePath path = new TreePath(node.getPath());
	                            dTree.expandPath(path);
	                            dTree.addSelectionPath(path);
	                            dTree.scrollPathToVisible(path);
                                selectedNode.add(node);
                                selectedPath.add(path);
	                            break;
	                        }
	            } else {
                    node.setVisible(false);
                }
	        }
    	}

        for(DecoderTreeNode node:mfgNode){
            node.setVisible(true);
            Enumeration<DecoderTreeNode> e = node.breadthFirstEnumeration();
	        while (e.hasMoreElements()) {
                DecoderTreeNode subnode = e.nextElement();
                if(subnode!=node){
                    subnode.setVisible(false);
                }
            }
        }
        for(DecoderTreeNode node:familyNode){
            node.setVisible(true);
        }
        for(DecoderTreeNode node:modelNode){
            node.setVisible(true);
        }
        for(DecoderTreeNode node:selectedNode){
            node.setVisible(true);
        }
        
        if(showMatched.isSelected()){
            dModel.activateFilter(true);
            dModel.reload();
            for(TreePath path:selectedPath){
                dTree.expandPath(path);
                dTree.scrollPathToVisible(path);
            }
        }
    }

    /**
     * Decoder identify has not matched specific types, but did
     * find manufacturer match
     * @param pMfg Manufacturer name. This is passed to save time,
     *              as it has already been determined once.
     * @param pMfgID Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    @SuppressWarnings("unchecked")
	void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" ("+pMfg+") version "+pModelID+"; no such decoder defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        // find this mfg to select it
        dTree.clearSelection();
        Enumeration<DecoderTreeNode> e = dRoot.breadthFirstEnumeration();
        ArrayList<DecoderTreeNode> selected = new ArrayList<DecoderTreeNode>();
        selectedPath = new ArrayList<TreePath>();
        while (e.hasMoreElements()) {
            DecoderTreeNode node = e.nextElement();
            if(node.getParent()!=null && node.getParent().toString().equals("Root")){
                if (node.toString().equals(pMfg)) {
                    TreePath path = new TreePath(node.getPath());
                    dTree.expandPath(path);
                    dTree.addSelectionPath(path);
                    dTree.scrollPathToVisible(path);
                    selectedPath.add(path);
                    node.setVisible(true);
                    selected.add(node);
                }
            } else {
                node.setVisible(false);
            }
        }
        for(DecoderTreeNode node:selected){
            node.setVisible(true);
            Enumeration<DecoderTreeNode> es = node.breadthFirstEnumeration();
            while(es.hasMoreElements()){
                es.nextElement().setVisible(true);
            }
        }
        if(showMatched.isSelected()){
            dModel.activateFilter(true);
            dModel.reload();
        }
    }
    
    ArrayList<TreePath> selectedPath = new ArrayList<TreePath>();
    
    /**
     * Decoder identify did not match anything, warn and clear selection
     */
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" version "+pModelID+"; no such manufacterer defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        dTree.clearSelection();
    }

    /**
     *  Set the decoder selection to a specific decoder from a selected Loco.
     *  <P>
     *  This must not trigger an update event from the Tree selection, so
     *  we remove and replace the listener.
     */
    @SuppressWarnings("unchecked")
	void setDecoderSelectionFromLoco(String loco) {
        // if there's a valid loco entry...
        RosterEntry locoEntry = Roster.instance().entryFromTitle(loco);
        if ( locoEntry == null) return;
        dTree.removeTreeSelectionListener(dListener);
        dTree.clearSelection();
        // get the decoder type, it has to be there (assumption!),
        String modelString = locoEntry.getDecoderModel();
        String familyString = locoEntry.getDecoderFamily();
        String titleString = DecoderFile.titleString(modelString, familyString);

        // find the decoder mfg
        DecoderIndexFile.instance().fileFromTitle(titleString).getMfg();
        
        // close the entire GUI (not currently done, users want left open)
        //collapseAll();
        
        // find this one to select it
        Enumeration<DefaultMutableTreeNode> e = dRoot.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            TreeNode parentNode = node.getParent();
            if (node.toString().equals(modelString)
                && parentNode.toString().equals(familyString)) {
                TreePath path = new TreePath(node.getPath());
                dTree.addSelectionPath(path);
                dTree.scrollPathToVisible(path);
                break;
            }
        }
        // and restore the listener
        dTree.addTreeSelectionListener(dListener);
    }

    /**
     * Convert the decoder selection UI result into a name.
     * @return The selected decoder type name, or null if none selected.
     */
    protected String selectedDecoderType() {
        if (!isDecoderSelected()) return null;
        else return ((DecoderTreeNode)dTree.getLastSelectedPathComponent()).getTitle();
    }

    /**
     * Has the user selected a decoder type, either manually or
     * via a successful event?
     * @return true if a decoder type is selected
     */
    boolean isDecoderSelected() {
        return !dTree.isSelectionEmpty();
    }
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CombinedLocoSelTreePane.class.getName());

}

/**
* The following has been taken from an example given in.. 
* http://www.java2s.com/Code/Java/Swing-Components/DecoderTreeNodeTreeExample.htm
* with extracts from http://www.codeguru.com/java/articles/143.shtml
*
*/
class InvisibleTreeModel extends DefaultTreeModel {

    protected boolean filterIsActive;

    public InvisibleTreeModel(TreeNode root) {
        this(root, false);
    }

    public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren) {
        this(root, false, false);
    }

    public InvisibleTreeModel(TreeNode root, boolean asksAllowsChildren,
      boolean filterIsActive) {
        super(root, asksAllowsChildren);
        this.filterIsActive = filterIsActive;
    }

    public void activateFilter(boolean newValue) {
        filterIsActive = newValue;
    }

  public boolean isActivatedFilter() {
    return filterIsActive;
  }

    public Object getChild(Object parent, int index) {
        if (filterIsActive) {
            if (parent instanceof DecoderTreeNode) {
              return ((DecoderTreeNode) parent).getChildAt(index,
                  filterIsActive);
            }
        }
        return ((TreeNode) parent).getChildAt(index);
    }

    public int getChildCount(Object parent) {
        if (filterIsActive) {
            if (parent instanceof DecoderTreeNode) {
                return ((DecoderTreeNode) parent).getChildCount(filterIsActive);
            }
        }
        return ((TreeNode) parent).getChildCount();
    }
}

class DecoderTreeNode extends DefaultMutableTreeNode {

    protected boolean isVisible;

    private String toolTipText;
    private String title;

    public DecoderTreeNode(String str, String toolTipText, String title) {
        this(str);
        this.toolTipText = toolTipText;
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public String getToolTipText() {
        return toolTipText;
    }
    
    public DecoderTreeNode(Object userObject) {
        this(userObject, true, false);
    }
    
    public DecoderTreeNode(Object userObject, boolean allowsChildren,
        boolean isVisible) {
        super(userObject, allowsChildren);
        this.isVisible = isVisible;
    }

    public TreeNode getChildAt(int index, boolean filterIsActive) {
        if (!filterIsActive) {
            return super.getChildAt(index);
        }
        if (children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        
        int realIndex = -1;
        int visibleIndex = -1;
        Enumeration<?> e = children.elements();
        while (e.hasMoreElements()) {
            DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
            if (node.isVisible()) {
                visibleIndex++;
            }
            realIndex++;
            if (visibleIndex == index) {
                return (TreeNode) children.elementAt(realIndex);
            }
        }

        throw new ArrayIndexOutOfBoundsException("index unmatched");
        //return (TreeNode)children.elementAt(index);
    }

    public int getChildCount(boolean filterIsActive) {
        if (!filterIsActive) {
            return super.getChildCount();
        }
        if (children == null) {
            return 0;
        }

        int count = 0;
        Enumeration<?> e = children.elements();
        while (e.hasMoreElements()) {
            DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
            if (node.isVisible()) {
                count++;
            }
        }

        return count;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public boolean isVisible() {
        return isVisible;
    }
  
}


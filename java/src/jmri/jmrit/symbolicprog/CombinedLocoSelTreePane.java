// CombinedLocoSelTreePane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.BoxLayout;
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

    JTree dTree;
    DefaultTreeModel dModel;
    DefaultMutableTreeNode dRoot;
    TreeSelectionListener dListener;

    /**
     * Create the panel used to select the decoder
     */
    protected JPanel layoutDecoderSelection() {
        JPanel pane1a = new JPanel(new BorderLayout());
        pane1a.add(new JLabel(rbt.getString("LabelDecoderInstalled")), BorderLayout.WEST);
        // create the list of manufacturers; get the list of decoders, and add elements
        dRoot = new DefaultMutableTreeNode("Root");
        dModel = new DefaultTreeModel(dRoot);
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
        DefaultMutableTreeNode mfgElement = null;
        DefaultMutableTreeNode familyElement = null;
        HashMap<String, DefaultMutableTreeNode> familyNameNode = new HashMap<String, DefaultMutableTreeNode>();
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
                familyNameNode = new HashMap<String, DefaultMutableTreeNode>();
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
            if ((familyElement==null || !family.equals(familyElement.toString())) && !familyNameNode.containsKey(family) ) {
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
            	
             /* check for both double click and that it's a decoder 
             that is being clicked on.  If it's just a Family, the programmer
             button is enabled by the TreeSelectionListener, but we don't
             want to automatically open a programmer so a user has the opportunity
             to select an individual decoder
             */
             if (me.getClickCount() == 2){
                 if (go2.isEnabled() && ((TreeNode)dTree.getSelectionPath().getLastPathComponent()).isLeaf()) go2.doClick();
                }
             }
            } );

        // add button
        iddecoder = addDecoderIdentButton();
        if (iddecoder!=null) {
            JPanel buttonHolder = new JPanel();
            buttonHolder.setLayout(new BoxLayout(buttonHolder, BoxLayout.X_AXIS));
            buttonHolder.add(iddecoder);
            buttonHolder.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
            buttonHolder.setAlignmentY(JLabel.CENTER_ALIGNMENT);
            pane1a.add(buttonHolder, BorderLayout.EAST);
        }
        return pane1a;
    }

    /**
     * Decoder identify has matched one or more specific types
     */
    @SuppressWarnings("unchecked")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
	void updateForDecoderTypeID(List<DecoderFile> pList) {
        // find and select the first item
        if (log.isDebugEnabled()) {
            String msg = "Identified "+pList.size()+" matches: ";
            for (int i = 0 ; i< pList.size(); i++)
                msg = msg+pList.get(i).getModel()+":";
            log.debug(msg);
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
        // Select the decoder(s) in the tree
        for (int i=0; i < pList.size(); i++) {
        	
        	DecoderFile f = pList.get(i);
        	String findMfg = f.getMfg();
        	String findFamily = f.getFamily();
        	String findModel = f.getModel();
        
/*	        // if this is a family listing plus one specific decoder, take the decoder
	        if (pList.size()==2 && findFamily.equals(((DecoderFile)pList.get(0)).getFamily()) ) {
	            f = (DecoderFile)pList.get(1);
	            findMfg = f.getMfg();
	            findFamily = f.getFamily();
	            findModel = f.getModel();
	        }
	
	        dTree.clearSelection();
*/
	        Enumeration<DefaultMutableTreeNode> e = dRoot.breadthFirstEnumeration();
	        while (e.hasMoreElements()) {
	            DefaultMutableTreeNode node = e.nextElement();
	            
	            // convert path to comparison string
	            TreeNode[] list = node.getPath();           
	            if (list.length == 3) {
	                // check for match to mfg, model, model
	                if (list[1].toString().equals(findMfg)
	                    && list[2].toString().equals(findModel))
	                        {
	                            TreePath path = new TreePath(node.getPath());
	                            dTree.expandPath(path);
	                            dTree.addSelectionPath(path);
	                            dTree.scrollPathToVisible(path);
	                            break;
	                        }
	            } else if (list.length == 4 ) {
	                // check for match to mfg, family, model
	                if (list[1].toString().equals(findMfg)
	                    && list[2].toString().equals(findFamily)
	                    && list[3].toString().equals(findModel))
	                        {
	                            TreePath path = new TreePath(node.getPath());
	                            dTree.expandPath(path);
	                            dTree.addSelectionPath(path);
	                            dTree.scrollPathToVisible(path);
	                            break;
	                        }
	            }
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
        Enumeration<DefaultMutableTreeNode> e = dRoot.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equals(pMfg)) {
                TreePath path = new TreePath(node.getPath());
                dTree.expandPath(path);
                dTree.addSelectionPath(path);
                dTree.scrollPathToVisible(path);
                break;
            }
        }

    }
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
    String selectedDecoderType() {
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

    // from http://www.codeguru.com/java/articles/143.shtml
   static class DecoderTreeNode extends DefaultMutableTreeNode {
        private String toolTipText;
        private String title;

        public DecoderTreeNode(String str, String toolTipText, String title) {
            super(str);
            this.toolTipText = toolTipText;
            this.title = title;
        }
        public String getTitle() {
            return title;
        }
        public String getToolTipText() {
            return toolTipText;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CombinedLocoSelTreePane.class.getName());

}

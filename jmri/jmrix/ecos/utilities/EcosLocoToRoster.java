package jmri.jmrix.ecos.utilities;

import java.awt.BorderLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jmri.jmrit.DccLocoAddressSelector;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosTrafficController;

public class EcosLocoToRoster implements EcosListener {
    
    EcosTrafficController tc;
    EcosLocoAddressManager ecosManager;
    EcosLocoAddress ecosLoco;
    RosterEntry re;
    DccLocoAddressSelector addrSel = new DccLocoAddressSelector();
    String              filename        = null;    
    DecoderFile              pDecoderFile = null;
    DecoderIndexFile decoderind = DecoderIndexFile.instance();
    String _ecosObject;
    int _ecosObjectInt;
    Label _statusLabel = null;
    protected JComboBox locoBox = null;
    protected JToggleButton iddecoder;
    JFrame frame;
    
    public EcosLocoToRoster(){}
    //Same Name as the constructor need to sort it out!
    public void ecosLocoToRoster(String ecosObject){
        frame = new JFrame();
        _ecosObject = ecosObject;
        _ecosObjectInt = Integer.parseInt(_ecosObject);
        ecosManager = (EcosLocoAddressManager)jmri.InstanceManager.getDefault(EcosLocoAddressManager.class);
        ecosLoco = ecosManager.getByEcosObject(ecosObject);
        String rosterId=ecosLoco.getEcosDescription();
        if(checkDuplicate(rosterId)){
            int count = 0;
            String oldrosterId = rosterId;
            while (checkDuplicate(rosterId)) {
                rosterId=oldrosterId+"_"+count;
                count++;
            }
        }
        re = new RosterEntry();
        re.setId(rosterId);
        List<DecoderFile> decoder = decoderind.matchingDecoderList(null, null, ecosLoco.getCV8(), ecosLoco.getCV7(), null, null);
        if (decoder.size()==1){
            DecoderFile pDecoderFile;
            pDecoderFile=decoder.get(0);
            System.out.println(decoder.get(0));
            SelectedDecoder(pDecoderFile);
            
        } else {

        	ComboPanel();
        }
    }
    
    public void reply(EcosReply m) {
        int startval;
        int endval;
        
        
        //int addr;
        //String description;
        //String protocol;
        //String strde;
        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (lines[lines.length-1].contains("<END 0 (OK)>")){
                if (lines[0].startsWith("<REPLY get(")){
                startval=lines[0].indexOf("(")+1;
                endval=(lines[0].substring(startval)).indexOf(",")+startval;
                //The first part of the messages is always the object id.
                int object = Integer.parseInt(lines[0].substring(startval, endval));
                if (object==_ecosObjectInt){
                    for(int i =1; i<lines.length-1; i++) {
                            if(lines[i].contains("cv[")){
                            //int startcvnum = lines[i].indexOf("[")+1;
                            //int endcvnum = (lines[i].substring(startcvnum)).indexOf(",")+startcvnum;
                            //int cvnum = Integer.parseInt(lines[i].substring(startcvnum, endcvnum));
                            //int startcvval = (lines[i].substring(endcvnum)).indexOf(", ")+endcvnum+2;
                            //int endcvval = (lines[i].substring(startcvval)).indexOf("]")+startcvval;
                            //int cvval = Integer.parseInt(lines[i].substring(startcvval, endcvval));
                            //String strcvnum = "CV"+cvnum;
                        }
                     }
                }
            }
        }
    }
    
    public void message(EcosMessage m){
        
    }
    
    void storeloco(){
        Roster.instance().addEntry(re);
        ecosLoco.setRosterId(re.getId());
        re.ensureFilenameExists();
        

        re.writeFile(null, null, null);
        
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        
        Roster.instance().writeRosterFile();
    }

    JComboBox combo;
    
	public void ComboPanel()
	{
       	frame.setTitle( "Decoder Selection" );
		frame.getContentPane().setLayout( new BorderLayout() );
        
        JPanel topPanel = new JPanel();
    
        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        JPanel p3 = this.layoutDecoderSelection();

		// Create a panel to hold all other components
		topPanel.setLayout( new BorderLayout() );
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JLabel jLabel1 = new JLabel("Decoder installed can not be identified, please select from the list below");
        JButton okayButton = new JButton("Okay");
        p1.add(jLabel1);
        p2.add(okayButton);
        topPanel.add(p1);
        topPanel.add(p3);
        topPanel.add(p2);
        
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        
        frame.setLocationRelativeTo(null);
        frame.getContentPane().add( topPanel);
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.setFocusable(true);
        frame.setFocusableWindowState(true);
        frame.requestFocus();

        
        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);
        frame.setVisible( true );
        
        ActionListener okayButtonAction = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //System.out.println(combo.getSelectedItem());
                okayButton();
            }
        };
        okayButton.addActionListener(okayButtonAction);
	}
    
    String selectedDecoderType() {
        if (!isDecoderSelected()) return null;
        else return ((DecoderTreeNode)dTree.getLastSelectedPathComponent()).getTitle();
    }
    
    boolean isDecoderSelected() {
        return !dTree.isSelectionEmpty();
    }
    
    private void okayButton(){
                pDecoderFile=DecoderIndexFile.instance().fileFromTitle(selectedDecoderType());
                SelectedDecoder(pDecoderFile);
                frame.dispose();
    }
    
    private void SelectedDecoder(DecoderFile pDecoderFile) {
        re.setDecoderModel(pDecoderFile.getModel());
        re.setDecoderFamily(pDecoderFile.getFamily());    

        re.setDccAddress(Integer.toString(ecosLoco.getEcosLocoAddress()));
        re.setLongAddress(true);
        
        re.setRoadName("");
        re.setRoadNumber("");
        re.setMfg("");
        re.setModel("");
        if (RosterEntry.getDefaultOwner()==null)
            re.setOwner("");
        else
            re.setOwner(RosterEntry.getDefaultOwner());
        re.setComment("Automatically Imported from the Ecos");
        re.setDecoderComment("");    
        re.putAttribute("EcosObject", _ecosObject);
        re.ensureFilenameExists();
        storeloco();
        JOptionPane.showMessageDialog(frame, "Loco Added to the JMRI Roster");
    }
    
    /**
     *
     * @return true if the value in the Ecos Description
     * is a duplicate of some other RosterEntry in the roster
     */
    public boolean checkDuplicate(String id) {
        // check its not a duplicate
        List<RosterEntry> l = Roster.instance().matchingList(null, null, null, null, null, null, id);
        boolean oops = false;
        for (int i=0; i<l.size(); i++) {
            if (re!=l.get(i)) oops =true;
        }
        return oops;
    }
    
    JTree dTree;
    DefaultTreeModel dModel;
    DefaultMutableTreeNode dRoot;
    TreeSelectionListener dListener;
    
    protected JPanel layoutDecoderSelection() {
        
        JPanel pane1a = new JPanel();
        pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
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
            if (familyElement==null || !family.equals(familyElement.toString()) ) {
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
                    i = i+1;
                    continue;
                }
            }
            // insert at the decoder level, except if family name is the same
            if (!family.equals(model)){
                dModel.insertNodeInto(new DecoderTreeNode(model,
                                                        hoverText,
                                                        decoders.get(i).titleString()),
                                    familyElement, familyElement.getChildCount());
            }
        }  // end of loop over decoders

        // build the tree GUI
        pane1a.add(new JScrollPane(dTree));
        dTree.expandPath(new TreePath(dRoot));
        dTree.setRootVisible(false);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        jmri.util.JTreeUtil.setExpandsSelectedPaths(dTree,true);

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
                }
            }
        });
        
//      Mouselistener for doubleclick activation of proprammer   
        dTree.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent me){
                 // Clear any status messages and ensure the tree is in single path select mode
                 //if (_statusLabel != null) _statusLabel.setText("StateIdle");
                 dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
                    
                 /* check for both double click and that it's a decoder 
                 that is being clicked on.  If it's just a Family, the programmer
                 button is enabled by the TreeSelectionListener, but we don't
                 want to automatically open a programmer so a user has the opportunity
                 to select an individual decoder
                 */
                if (me.getClickCount() == 2){
                    if (((TreeNode)dTree.getSelectionPath().getLastPathComponent()).isLeaf()) okayButton();
                }
            }
        } );

        this.selectDecoder(ecosLoco.getCV8(), ecosLoco.getCV7());
        return pane1a;
    }
    JToggleButton addDecoderIdentButton() {
        JToggleButton iddecoder = new JToggleButton("ButtonReadType");
        iddecoder.setToolTipText("TipSelectType");
            if (jmri.InstanceManager.programmerManagerInstance()!= null
                    && jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer()!=null
                    && !jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer().getCanRead()) {
            // can't read, disable the button
            iddecoder.setEnabled(false);
            iddecoder.setToolTipText("TipNoRead");
        }
        iddecoder.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                        //startIdentifyDecoder();
                }
        });
        return iddecoder;
    }
        // from http://www.codeguru.com/java/articles/143.shtml
    class DecoderTreeNode extends DefaultMutableTreeNode {
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
    
    protected void selectDecoder(String mfgID, String modelID) {

        // locate a decoder like that.
        List<DecoderFile> temp = DecoderIndexFile.instance().matchingDecoderList(null, null, mfgID, modelID, null, null);
        if (log.isDebugEnabled()) log.debug("selectDecoder found "+temp.size()+" matches");
        // install all those in the JComboBox in place of the longer, original list
        if (temp.size() > 0) {
            updateForDecoderTypeID(temp);
        } else {
            String mfg = DecoderIndexFile.instance().mfgNameFromId(mfgID);
            int intMfgID = Integer.parseInt(mfgID);
            int intModelID = Integer.parseInt(modelID);
            if (mfg==null) {
                updateForDecoderNotID(intMfgID, intModelID);
            }
            else {
                updateForDecoderMfgID(mfg, intMfgID, intModelID);
            }
        }
    }
    
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" version "+pModelID+"; no such manufacterer defined";
        log.warn(msg);
        dTree.clearSelection();
    }
    
    @SuppressWarnings("unchecked")
	void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg "+pMfgID+" ("+pMfg+") version "+pModelID+"; no such decoder defined";
        log.warn(msg);
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

    
    @SuppressWarnings("unchecked")
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
        }
        else dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        // Select the decoder(s) in the tree
        for (int i=0; i < pList.size(); i++) {
        	
        	DecoderFile f = pList.get(i);
        	String findMfg = f.getMfg();
        	String findFamily = f.getFamily();
        	String findModel = f.getModel();
        
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosLocoToRoster.class.getName());
}
/*
cv8 - mfgIdFromName
cv7 - Version/family

tmp1 = jmri.jmrit.decoderdefn.DecoderIndexFile.instance()
print tmp1.matchingDecoderList(None, None, cv8, None, None, None)

matchingDecoderList(String mfg, String family, String decoderMfgID, String decoderVersionID, String decoderProductID, String model )

tmp1 = jmri.jmrit.decoderdefn.DecoderIndexFile.instance()
list = tmp1.matchingDecoderList(None, None, "153", "16", None, None
returns decoderfile.java
print list[0].getMfg()
print list[0].getFamily()
*/
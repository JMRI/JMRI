package jmri.jmrit.symbolicprog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.progsupport.ProgModeSelector;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI controls to select a known loco and/or new decoder.
 * <p>
 * This is an extension of the CombinedLocoSelPane class to use a JTree instead
 * of a JComboBox for the decoder selection. The loco selection (Roster
 * manipulation) parts are unchanged.
 * <p>
 * The JComboBox implementation always had to have selected entries, so we added
 * dummy "select from .." items at the top {@literal &} used those to indicate
 * that there was no selection in that box. Here, the lack of a selection
 * indicates there's no selection.
 * <p>
 * Internally, the "filter" is used to only show identified models (leaf nodes).
 * This is implemented in internal InvisibleTreeModel and DecoderTreeNode
 * classes.
 * <p>
 * The decoder definition {@link jmri.jmrit.decoderdefn.DecoderFile.Showable}
 * attribute also interacts with those.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2013
 */
public class CombinedLocoSelTreePane extends CombinedLocoSelPane {

    /**
     * The decoder selection tree.
     */
    protected JTree dTree;

    /**
     * A panel immediately below the decoder selection tree.
     * <br><br>
     * Used for tree action buttons.
     */
    protected JPanel viewButtons;

    /**
     * The listener for the decoder selection tree.
     */
    protected transient volatile TreeSelectionListener dListener;
    InvisibleTreeModel dModel;
    DecoderTreeNode dRoot;
    JRadioButton showAll;
    JRadioButton showMatched;
    ArrayList<TreePath> selectedPath = new ArrayList<>();

    /**
     * Provide GUI controls to select a known loco and/or new decoder.
     *
     * @param s        Reference to a JLabel that should be updated with status
     *                 information as identification happens.
     *
     * @param selector Reference to a
     *                 {@link jmri.jmrit.progsupport.ProgModeSelector} panel
     *                 that configures the programming mode.
     */
    public CombinedLocoSelTreePane(JLabel s, ProgModeSelector selector) {
        super(s, selector);
    }

    /**
     * Create the panel used to select the decoder.
     *
     * @return a JPanel for handling the decoder-selection GUI
     */
    @Override
    protected JPanel layoutDecoderSelection() {
        JPanel pane1a = new JPanel(new BorderLayout());
        pane1a.add(new JLabel(Bundle.getMessage("LabelDecoderInstalled")), BorderLayout.NORTH);
        // create the list of manufacturers; get the list of decoders, and add elements
        dRoot = new DecoderTreeNode("Root");
        dModel = new InvisibleTreeModel(dRoot);
        dTree = new JTree(dModel) {

            @Override
            public String getToolTipText(MouseEvent evt) {
                if (getRowForLocation(evt.getX(), evt.getY()) == -1) {
                    return null;
                }
                TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
                return ((DecoderTreeNode) curPath.getLastPathComponent()).getToolTipText();
            }
        };
        dTree.setToolTipText("");

        createDecoderTypeContents();

        // build the tree GUI
        pane1a.add(new JScrollPane(dTree), BorderLayout.CENTER);
        dTree.expandPath(new TreePath(dRoot));
        dTree.setRootVisible(false);
        dTree.setShowsRootHandles(true);
        dTree.setScrollsOnExpand(true);
        dTree.setExpandsSelectedPaths(true);

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        // tree listener
        dTree.addTreeSelectionListener(dListener = (TreeSelectionEvent e) -> {
            log.debug("selection changed {} {}", dTree.isSelectionEmpty(), dTree.getSelectionPath());
            if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null
                    && // can't be just a mfg, has to be at least a family
                    dTree.getSelectionPath().getPathCount() > 2
                    && // can't be a multiple decoder selection
                    dTree.getSelectionCount() < 2) {
                // decoder selected - reset and disable loco selection
                log.debug("Selection event with {}", dTree.getSelectionPath());
                if (locoBox != null) {
                    locoBox.setSelectedIndex(0);
                }
                go2.setEnabled(true);
                go2.setRequestFocusEnabled(true);
                go2.requestFocus();
                go2.setToolTipText(Bundle.getMessage("TipClickToOpen"));
            } else {
                // decoder not selected - require one
                go2.setEnabled(false);
                go2.setToolTipText(Bundle.getMessage("TipSelectLoco"));
            }
        });

//      Mouselistener for doubleclick activation of programmer
        dTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent me) {
                // Clear any status messages and ensure the tree is in single path select mode
                if (_statusLabel != null) {
                    _statusLabel.setText(Bundle.getMessage("StateIdle"));
                }
                dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

                if (me.getClickCount() == 2) {
                    if (go2.isEnabled() && ((TreeNode) dTree.getSelectionPath().getLastPathComponent()).isLeaf()) {
                        go2.doClick();
                    }
                }
            }
        });

        viewButtons = new JPanel();
        iddecoder = addDecoderIdentButton();
        if (iddecoder != null) {
            viewButtons.add(iddecoder);
        }
        showAll = new JRadioButton(Bundle.getMessage("LabelAll"));
        showAll.setSelected(true);
        showMatched = new JRadioButton(Bundle.getMessage("LabelMatched"));

        if (InstanceManager.getNullableDefault(GlobalProgrammerManager.class) != null
                && InstanceManager.getDefault(GlobalProgrammerManager.class).isGlobalProgrammerAvailable()) {
            ButtonGroup group = new ButtonGroup();
            group.add(showAll);
            group.add(showMatched);
            viewButtons.add(showAll);
            viewButtons.add(showMatched);

            pane1a.add(viewButtons, BorderLayout.SOUTH);
            showAll.addActionListener((ActionEvent e) -> {
                setShowMatchedOnly(false);
            });
            showMatched.addActionListener((ActionEvent e) -> {
                setShowMatchedOnly(true);
            });
        }

        return pane1a;
    }

    /**
     * Sets the Loco Selection Pane to "Matched Only" {@code (true)} or "Show
     * All" {@code (false)}.
     * <br><br>
     * Changes the Decoder Tree Display and the Radio Buttons.
     *
     * @param state the desired state
     */
    public void setShowMatchedOnly(boolean state) {
        showMatched.setSelected(state);
        showAll.setSelected(!state);
        dModel.activateFilter(state);
        dModel.reload();
        for (TreePath path : selectedPath) {
            log.debug("action selects path: {}", path);
            dTree.expandPath(path);
            dTree.addSelectionPath(path);
            dTree.scrollPathToVisible(path);
        }
    }

    /**
     * Reads the available decoders and loads them into the dModel tree model.
     */
    void createDecoderTypeContents() {
        List<DecoderFile> decoders = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, null);
        int len = decoders.size();
        DecoderTreeNode mfgElement = null;
        DecoderTreeNode familyElement = null;
        HashMap<String, DecoderTreeNode> familyNameNode = new HashMap<>();
        for (int i = 0; i < len; i++) {
            DecoderFile decoder = decoders.get(i);
            String mfg = decoder.getMfg();
            String family = decoder.getFamily();
            String model = decoder.getModel();
            log.debug(" process {}/{}/{} on nodes {}/{}", mfg, family, model, mfgElement == null ? "<null>" : mfgElement.toString() + "(" + mfgElement.getChildCount() + ")", familyElement == null ? "<null>" : familyElement.toString() + "(" + familyElement.getChildCount() + ")");

            // build elements
            if (mfgElement == null || !mfg.equals(mfgElement.toString())) {
                // need new mfg node
                mfgElement = new DecoderTreeNode(mfg,
                        "CV8 = " + InstanceManager.getDefault(DecoderIndexFile.class).mfgIdFromName(mfg), "");
                dModel.insertNodeInto(mfgElement, dRoot, dRoot.getChildCount());
                familyNameNode = new HashMap<>();
                familyElement = null;
            }
            String famComment = decoders.get(i).getFamilyComment();
            String verString = decoders.get(i).getVersionsAsString();
            if (familyElement == null || (!family.equals(familyElement.toString()) && !familyNameNode.containsKey(family))) {
                // need new family node - is there only one model? Expect the
                // family element, plus the model element, so check i+2
                // to see if its the same, or if a single-decoder family
                // appears to have decoder names separate from the family name
                if ((i + 2 >= len)
                        || decoders.get(i + 2).getFamily().equals(family)
                        || !decoders.get(i + 1).getModel().equals(family)) {
                    // normal here; insert the new family element & exit
                    log.debug("normal family update case: {}", family);
                    familyElement = new DecoderTreeNode(family,
                            getHoverText(verString, famComment),
                            decoders.get(i).titleString());
                    dModel.insertNodeInto(familyElement, mfgElement, mfgElement.getChildCount());
                    familyNameNode.put(family, familyElement);
                    continue;
                } else {
                    // this is short case; insert decoder entry (next) here
                    log.debug("short case, i={} family={} next {}", i, family, decoders.get(i + 1).getModel());
                    if (i + 1 > len) {
                        log.error("Unexpected single entry for family: {}", family);
                    }
                    family = decoders.get(i + 1).getModel();
                    familyElement = new DecoderTreeNode(family,
                            getHoverText(verString, famComment),
                            decoders.get(i).titleString());
                    dModel.insertNodeInto(familyElement, mfgElement, mfgElement.getChildCount());
                    familyNameNode.put(family, familyElement);
                    i = i + 1;
                    continue;
                }
            }
            // insert at the decoder level, except if family name is the same
            if (!family.equals(model)) {
                if (familyNameNode.containsKey(family)) {
                    familyElement = familyNameNode.get(family);
                }
                String modelComment = decoders.get(i).getModelComment();
                DecoderTreeNode decoderNameNode = new DecoderTreeNode(model,
                        getHoverText(verString, modelComment),
                        decoders.get(i).titleString());
                decoderNameNode.setShowable(decoder.getShowable());
                dModel.insertNodeInto(decoderNameNode, familyElement, familyElement.getChildCount());
            }
        }  // end of loop over decoders
    }

    /**
     * Provide tooltip text: Decoder comment, with CV version info, formatted as
     * best we can.
     *
     * @param verString version string, typically from
     *                  {@link jmri.jmrit.decoderdefn.DecoderFile#getVersionsAsString DecoderFile.getVersionsAsString()}
     * @param comment   version string, typically from
     *                  {@link jmri.jmrit.decoderdefn.DecoderFile#getModelComment DecoderFile.getModelComment()}
     *                  or
     *                  {@link jmri.jmrit.decoderdefn.DecoderFile#getFamilyComment DecoderFile.getFamilyComment()}
     * @return the combined formatted string.
     */
    String getHoverText(String verString, String comment) {
        if (comment == null || comment.equals("")) {
            if (!verString.equals("")) {
                return "CV7=" + verString;
            } else {
                return "";
            }
        } else {
            if (verString.equals("")) {
                return comment;
            } else {
                return StringUtil.concatTextHtmlAware(comment, " (CV7=" + verString + ")");
            }
        }
    }

    /**
     * Identify loco button pressed, start the identify operation. This defines
     * what happens when the identify is done.
     * <br><br>
     * This {@code @Override} method invokes
     * {@link #resetSelections resetSelections} before starting.
     */
    @Override
    protected synchronized void startIdentifyDecoder() {
        // start identifying a decoder
        resetSelections();
        super.startIdentifyDecoder();
    }

    /**
     * Resets the Decoder Tree Display selections and sets the state to "Show
     * All".
     */
    public void resetSelections() {
        Enumeration<TreeNode> e = dRoot.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            ((DecoderTreeNode) e.nextElement()).setIdentified(false);
        }
        setShowMatchedOnly(false);
        selectedPath = new ArrayList<>();
        dTree.expandPath(new TreePath(dRoot));
        dTree.setExpandsSelectedPaths(true);
        int row = dTree.getRowCount() - 1;
        while (row >= 0) {
            dTree.collapseRow(row);
            row--;
        }
    }

    /**
     * Decoder identify has matched one or more specific types.
     *
     * @param pList a list of decoders
     */
    @Override
    public void updateForDecoderTypeID(List<DecoderFile> pList) {
        // find and select the first item
        if (log.isDebugEnabled()) {
            StringBuilder buf = new StringBuilder("Identified " + pList.size() + " matches: ");
            for (int i = 0; i < pList.size(); i++) {
                buf.append(pList.get(i).getModel()).append(":");
            }
            log.debug(buf.toString());
        }
        if (pList.size() <= 0) {
            log.error("Found empty list in updateForDecoderTypeID, should not happen");
            return;
        }
        dTree.clearSelection();
        // If there are multiple matches change tree to allow multiple selections by the program
        // and issue a warning instruction in the status bar
        if (pList.size() > 1) {
            dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            _statusLabel.setText(Bundle.getMessage("StateMultipleMatch"));
        } else {
            dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
            _statusLabel.setText(Bundle.getMessage("StateIdle"));
        }

        // set everybody not identified
        Enumeration<TreeNode> e = dRoot.breadthFirstEnumeration();
        while (e.hasMoreElements()) { // loop over the tree
            DecoderTreeNode node = ((DecoderTreeNode) e.nextElement());
            node.setIdentified(false);
        }

        selectedPath = new ArrayList<>();

        // Find decoder nodes in tree and set selected
        for (int i = 0; i < pList.size(); i++) { // loop over selected decoders
            e = dRoot.breadthFirstEnumeration();

            DecoderFile f = pList.get(i);
            String findMfg = f.getMfg();
            String findFamily = f.getFamily();
            String findModel = f.getModel();

            while (e.hasMoreElements()) { // loop over the tree & find node
                DecoderTreeNode node = ((DecoderTreeNode) e.nextElement());
                // never match show=NO nodes
                if (node.getShowable() == DecoderFile.Showable.NO) {
                    continue;
                }
                // convert path to comparison string
                TreeNode[] list = node.getPath();
                if (list.length == 3) {
                    // check for match to mfg, model (as family)
                    if (list[1].toString().equals(findMfg)
                            && list[2].toString().equals(findModel)) {
                        log.debug("match length 3");
                        node.setIdentified(true);
                        dModel.reload();
                        ((DecoderTreeNode) list[1]).setIdentified(true);
                        ((DecoderTreeNode) list[2]).setIdentified(true);
                        TreePath path = new TreePath(node.getPath());
                        selectedPath.add(path);
                        break;
                    }
                } else if (list.length == 4) {
                    // check for match to mfg, family, model
                    if (list[1].toString().equals(findMfg)
                            && list[2].toString().equals(findFamily)
                            && list[3].toString().equals(findModel)) {
                        log.debug("match length 4");
                        node.setIdentified(true);
                        dModel.reload();
                        ((DecoderTreeNode) list[1]).setIdentified(true);
                        ((DecoderTreeNode) list[2]).setIdentified(true);
                        ((DecoderTreeNode) list[3]).setIdentified(true);
                        TreePath path = new TreePath(node.getPath());
                        selectedPath.add(path);
                        break;
                    }
                }
            }
        }
        // now select and show paths in tree
        for (TreePath path : selectedPath) {
            dTree.addSelectionPath(path);
            dTree.expandPath(path);
            dTree.scrollPathToVisible(path);
        }
    }

    /**
     * Decoder identify has not matched specific types, but did find
     * manufacturer match.
     *
     * @param pMfg     Manufacturer name. This is passed to save time, as it has
     *                 already been determined once.
     * @param pMfgID   Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    @Override
    void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg " + pMfgID + " (" + pMfg + ") version " + pModelID + "; no such decoder defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        // find this mfg to select it
        dTree.clearSelection();

        Enumeration<TreeNode> e = dRoot.breadthFirstEnumeration();

        ArrayList<DecoderTreeNode> selected = new ArrayList<>();
        selectedPath = new ArrayList<>();
        while (e.hasMoreElements()) {
            DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
            if (node.getParent() != null && node.getParent().toString().equals("Root")) {
                if (node.toString().equals(pMfg)) {
                    TreePath path = new TreePath(node.getPath());
                    dTree.expandPath(path);
                    dTree.addSelectionPath(path);
                    dTree.scrollPathToVisible(path);
                    selectedPath.add(path);
                    node.setIdentified(true);
                    selected.add(node);
                }
            } else {
                node.setIdentified(false);
            }
        }
        for (DecoderTreeNode node : selected) {
            node.setIdentified(true);

            Enumeration<TreeNode> es = dRoot.breadthFirstEnumeration();

            while (es.hasMoreElements()) {
                ((DecoderTreeNode) es.nextElement()).setIdentified(true);
            }
        }
        if (showMatched.isSelected()) {
            dModel.activateFilter(true);
            dModel.reload();
        }
    }

    /**
     * Decoder identify did not match anything, warn and clear selection.
     *
     * @param pMfgID   Manufacturer ID number (CV8)
     * @param pModelID Model ID number (CV7)
     */
    @Override
    void updateForDecoderNotID(int pMfgID, int pModelID) {
        String msg = "Found mfg " + pMfgID + " version " + pModelID + "; no such manufacturer defined";
        log.warn(msg);
        _statusLabel.setText(msg);
        dTree.clearSelection();
    }

    /**
     * Set the decoder selection to a specific decoder from a selected Loco.
     * <p>
     * This must not trigger an update event from the Tree selection, so we
     * remove and replace the listener.
     *
     * @param loco the loco name
     */
    @Override
    void setDecoderSelectionFromLoco(String loco) {
        // if there's a valid loco entry...
        RosterEntry locoEntry = Roster.getDefault().entryFromTitle(loco);
        if (locoEntry == null) {
            return;
        }
        dTree.removeTreeSelectionListener(dListener);
        dTree.clearSelection();
        // get the decoder type, it has to be there (assumption!),
        String modelString = locoEntry.getDecoderModel();
        String familyString = locoEntry.getDecoderFamily();

        // close the entire GUI (not currently done, users want left open)
        //collapseAll();
        // find this one to select it
        Enumeration<TreeNode> e = dRoot.breadthFirstEnumeration();

        while (e.hasMoreElements()) {
            DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
            DecoderTreeNode parentNode = (DecoderTreeNode) node.getParent();
            if (node.toString().equals(modelString)
                    && parentNode.toString().equals(familyString)) {
                TreePath path = new TreePath(node.getPath());
                node.setIdentified(true);
                parentNode.setIdentified(true);
                dModel.reload();
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
     *
     * @return The selected decoder type name, or null if none selected.
     */
    @Override
    protected String selectedDecoderType() {
        if (!isDecoderSelected()) {
            return null;
        } else {
            return ((DecoderTreeNode) dTree.getLastSelectedPathComponent()).getTitle();
        }
    }

    /**
     * Has the user selected a decoder type, either manually or via a successful
     * event?
     *
     * @return true if a decoder type is selected
     */
    @Override
    boolean isDecoderSelected() {
        return !dTree.isSelectionEmpty();
    }
    private final static Logger log = LoggerFactory.getLogger(CombinedLocoSelTreePane.class);

    /**
     * The following has been taken from an example given in..
     * http://www.java2s.com/Code/Java/Swing-Components/DecoderTreeNodeTreeExample.htm
     * with extracts from http://www.codeguru.com/java/articles/143.shtml
     */
    static class InvisibleTreeModel extends DefaultTreeModel {

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

        @Override
        public Object getChild(Object parent, int index) {
            if (parent instanceof DecoderTreeNode) {
                return ((DecoderTreeNode) parent).getChildAt(index,
                        filterIsActive);
            }
            return ((TreeNode) parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent instanceof DecoderTreeNode) {
                return ((DecoderTreeNode) parent).getChildCount(filterIsActive);
            }
            return ((TreeNode) parent).getChildCount();
        }
    }

    static class DecoderTreeNode extends DefaultMutableTreeNode {

        protected boolean isIdentified;
        private String toolTipText;
        private String title;
        DecoderFile.Showable showable = DecoderFile.Showable.YES;  // default

        public DecoderTreeNode(String str, String toolTipText, String title) {
            this(str);
            this.toolTipText = toolTipText;
            this.title = title;
        }

        @Override
        @SuppressWarnings("unchecked") // required because super.breadthFirstEnumeration not fully typed
        public Enumeration<TreeNode> breadthFirstEnumeration() { // JDK 9 typing
            return super.breadthFirstEnumeration();
        }

        public String getTitle() {
            return title;
        }

        public String getToolTipText() {
            return toolTipText;
        }

        public DecoderTreeNode(Object userObject) {
            this(userObject, true, false, DecoderFile.Showable.YES);
        }

        public DecoderTreeNode(Object userObject, boolean allowsChildren,
                boolean isIdentified, DecoderFile.Showable showable) {
            super(userObject, allowsChildren);
            this.isIdentified = isIdentified;
            this.showable = showable;
        }

        public TreeNode getChildAt(int index, boolean filterIsActive) {
            if (children == null) {
                throw new ArrayIndexOutOfBoundsException("node has no children");
            }

            int realIndex = -1;
            int visibleIndex = -1;
            Enumeration<?> e = children.elements();
            while (e.hasMoreElements()) {
                DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
                if (node.isVisible(filterIsActive)) {
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
            if (children == null) {
                return 0;
            }

            int count = 0;
            Enumeration<?> e = children.elements();
            while (e.hasMoreElements()) {
                DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
                if (node.isVisible(filterIsActive)) {
                    count++;
                }
            }

            return count;
        }

        public void setIdentified(boolean isIdentified) {
            this.isIdentified = isIdentified;
        }

        public void setShowable(DecoderFile.Showable showable) {
            this.showable = showable;
        }

        public DecoderFile.Showable getShowable() {
            return this.showable;
        }

        private boolean isVisible(boolean filterIsActive) {
            // if there are children, are any visible?
            if (children != null) {
                Enumeration<?> e = children.elements();
                while (e.hasMoreElements()) {
                    DecoderTreeNode node = (DecoderTreeNode) e.nextElement();
                    if (node.isVisible(filterIsActive)) {
                        return true;
                    }
                }
                return false;
            }
            // no children
            return isIdentified || (!filterIsActive && (showable == DecoderFile.Showable.YES));
        }
    }
}

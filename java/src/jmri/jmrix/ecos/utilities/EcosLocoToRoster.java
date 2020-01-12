package jmri.jmrix.ecos.utilities;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import jmri.InstanceManager;
import jmri.Programmer;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.ResetTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrix.ecos.EcosListener;
import jmri.jmrix.ecos.EcosLocoAddress;
import jmri.jmrix.ecos.EcosLocoAddressManager;
import jmri.jmrix.ecos.EcosMessage;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.jmrix.ecos.EcosReply;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcosLocoToRoster implements EcosListener {

    EcosLocoAddressManager ecosManager;
    EcosLocoAddress ecosLoco;
    RosterEntry re;
    String filename = null;
    DecoderFile pDecoderFile = null;
    String _ecosObject;
    int _ecosObjectInt;
    Label _statusLabel = null;
    CvTableModel cvModel = null;
    Programmer mProgrammer;
    JLabel progStatus;
//    Programmer pProg;
    protected JComboBox<?> locoBox = null;
    protected JToggleButton iddecoder;
    JFrame frame;
    EcosSystemConnectionMemo adaptermemo;
    EcosPreferences p;
    boolean suppressFurtherAdditions = false;

    public EcosLocoToRoster(EcosSystemConnectionMemo memo) {
        adaptermemo = memo;
        p = adaptermemo.getPreferenceManager();
    }

    public void addToQueue(EcosLocoAddress ecosObject) {
        locoList.add(ecosObject);
    }
    boolean waitingForComplete = false;
    boolean inProcess = false;

    public void processQueue() {
        if (inProcess) {
            return;
        }
        suppressFurtherAdditions = false;
        inProcess = true;
        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (locoList.size() != 0) {
                    final EcosLocoAddress tmploco = locoList.get(0);
                    waitingForComplete = false;
                    if (p.getAddLocoToJMRI() == EcosPreferences.YES) {
                        adaptermemo.getLocoAddressManager().setLocoToRoster();
                        ecosLocoToRoster(tmploco.getEcosObject());
                    } else if (!suppressFurtherAdditions && tmploco.addToRoster() && (tmploco.getRosterId() == null)) {
                        class WindowMaker implements Runnable {

                            EcosLocoAddress ecosObject;

                            WindowMaker(EcosLocoAddress o) {
                                ecosObject = o;
                            }

                            @Override
                            public void run() {
                                final JDialog dialog = new JDialog();
                                dialog.setTitle(Bundle.getMessage("AddRosterEntryQuestion"));
                                //dialog.setLocationRelativeTo(null);
                                dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                                JPanel container = new JPanel();
                                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                                container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                                JLabel question = new JLabel(Bundle.getMessage("LocoAddedJMessage", ecosObject.getEcosDescription(), adaptermemo.getUserName()));
                                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                                container.add(question);

                                question = new JLabel(Bundle.getMessage("AddToJMRIQuestion"));
                                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                                container.add(question);
                                final JCheckBox remember = new JCheckBox(Bundle.getMessage("MessageRememberSetting"));
                                remember.setFont(remember.getFont().deriveFont(10f));
                                remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                                //user preferences do not have the save option, but once complete the following line can be removed
                                //Need to get the method to save connection configuration.
                                remember.setVisible(true);
                                JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));
                                JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));
                                JPanel button = new JPanel();
                                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                                button.add(yesButton);
                                button.add(noButton);
                                container.add(button);

                                noButton.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        ecosObject.doNotAddToRoster();
                                        waitingForComplete = true;
                                        if (remember.isSelected()) {
                                            suppressFurtherAdditions = true;
                                            p.setAddLocoToJMRI(EcosPreferences.NO);
                                        }
                                        dialog.dispose();
                                    }
                                });

                                yesButton.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        if (remember.isSelected()) {
                                            p.setAddLocoToJMRI(EcosPreferences.YES);
                                        }
                                        ecosLocoToRoster(ecosObject.getEcosObject());
                                        dialog.dispose();
                                    }
                                });
                                container.add(remember);
                                container.setAlignmentX(Component.CENTER_ALIGNMENT);
                                container.setAlignmentY(Component.CENTER_ALIGNMENT);
                                dialog.getContentPane().add(container);
                                dialog.pack();
                                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

                                int w = dialog.getSize().width;
                                int h = dialog.getSize().height;
                                int x = (dim.width - w) / 2;
                                int y = (dim.height - h) / 2;

                                // Move the window
                                dialog.setLocation(x, y);

                                dialog.setModal(true);
                                dialog.setVisible(true);
                            }
                        }
                        try {
                            WindowMaker t = new WindowMaker(tmploco);
                            javax.swing.SwingUtilities.invokeAndWait(t);
                        } catch (java.lang.reflect.InvocationTargetException | InterruptedException ex) {
                            log.warn("Exception, ending", ex);
                            return;
                        }
                    } else {
                        waitingForComplete = true;
                    }
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (!waitingForComplete) {
                                    Thread.sleep(500L);
                                }
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    };
                    Thread thr = new Thread(r);
                    thr.start();
                    thr.setName("Ecos Loco To Roster Inner thread"); // NOI18N
                    try {
                        thr.join();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    locoList.remove(0);
                }
                inProcess = false;
            }
        };
        Thread thread = new Thread(run);
        thread.setName("Ecos Loco To Roster"); // NOI18N
        thread.start();

    }

    ArrayList<EcosLocoAddress> locoList = new ArrayList<EcosLocoAddress>();

    //Same Name as the constructor need to sort it out!
    public void ecosLocoToRoster(String ecosObject) {
        frame = new JFrame();

        _ecosObject = ecosObject;
        _ecosObjectInt = Integer.parseInt(_ecosObject);
        ecosManager = adaptermemo.getLocoAddressManager();

        ecosLoco = ecosManager.getByEcosObject(ecosObject);
        String rosterId = ecosLoco.getEcosDescription();
        if (checkDuplicate(rosterId)) {
            int count = 0;
            String oldrosterId = rosterId;
            while (checkDuplicate(rosterId)) {
                rosterId = oldrosterId + "_" + count;
                count++;
            }
        }
        re = new RosterEntry();
        re.setId(rosterId);
        List<DecoderFile> decoder = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, ecosLoco.getCVAsString(8), ecosLoco.getCVAsString(7), null, null);
        if (decoder.size() == 1) {
            pDecoderFile = decoder.get(0);
            selectedDecoder(pDecoderFile);

        } else {

            class WindowMaker implements Runnable {

                WindowMaker() {
                }

                @Override
                public void run() {
                    comboPanel();
                }
            }
            WindowMaker t = new WindowMaker();
            javax.swing.SwingUtilities.invokeLater(t);

        }
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "CF_USELESS_CONTROL_FLOW", 
        justification = "TODO fill out the actions in these clauses")
    public void reply(EcosReply m) {
        int startval;
        int endval;

        String msg = m.toString();
        String[] lines = msg.split("\n");
        if (m.getResultCode() == 0) {
            // TODO use this if branch?
            //
            //            if (lines[0].startsWith("<REPLY get(" + _ecosObject + ", cv[")) {
            //                startval = lines[0].indexOf("(") + 1;
            //                endval = (lines[0].substring(startval)).indexOf(",") + startval;
            //                //The first part of the messages is always the object id.
            //                int object = Integer.parseInt(lines[0].substring(startval, endval));
            //                if (object == _ecosObjectInt) {
            //                    for (int i = 1; i < lines.length - 1; i++) {
            //                        if (lines[i].contains("cv[")) {
            //                            //int startcvnum = lines[i].indexOf("[")+1;
            //                            //int endcvnum = (lines[i].substring(startcvnum)).indexOf(",")+startcvnum;
            //                            //int cvnum = Integer.parseInt(lines[i].substring(startcvnum, endcvnum));
            //                            //int startcvval = (lines[i].substring(endcvnum)).indexOf(", ")+endcvnum+2;
            //                            //int endcvval = (lines[i].substring(startcvval)).indexOf("]")+startcvval;
            //                            //int cvval = Integer.parseInt(lines[i].substring(startcvval, endcvval));
            //                            //String strcvnum = "CV"+cvnum;
            //                        }
            //                    }
            //                }
            //            } else if (lines[0].startsWith("<REPLY get(" + _ecosObject + ", funcdesc")) {
            if (lines[0].startsWith("<REPLY get(" + _ecosObject + ", funcdesc")) {
                int functNo = 0;
                try {
                    startval = lines[1].indexOf("[") + 1;
                    endval = (lines[1].substring(startval)).indexOf(",") + startval;
                    boolean moment = true;
                    functNo = Integer.parseInt(lines[1].substring(startval, endval));
                    startval = endval + 1;
                    endval = (lines[1].substring(startval)).indexOf(",");//+startval;
                    if (endval == -1) {
                        endval = (lines[1].substring(startval)).indexOf("]");//+startval;
                        moment = false;
                    }
                    endval = endval + startval;
                    if (lines[1].contains("moment")) {
                        moment = true;
                    }

                    int functDesc = Integer.parseInt(lines[1].substring(startval, endval));

                    String functionLabel = "";
                    switch (functDesc) {
                        //Default descriptions for ESU function icons
                        case 2:
                            functionLabel = "function";
                            break;
                        case 3:
                            functionLabel = "light";
                            break;
                        case 4:
                            functionLabel = "light_0";
                            break;
                        case 5:
                            functionLabel = "light_1";
                            break;
                        case 7:
                            functionLabel = "sound";
                            break;
                        case 8:
                            functionLabel = "music";
                            break;
                        case 9:
                            functionLabel = "announce";
                            break;
                        case 10:
                            functionLabel = "routing_speed";
                            break;
                        case 11:
                            functionLabel = "abv";
                            break;
                        case 32:
                            functionLabel = "coupler";
                            break;
                        case 33:
                            functionLabel = "steam";
                            break;
                        case 34:
                            functionLabel = "panto";
                            break;
                        case 35:
                            functionLabel = "highbeam";
                            break;
                        case 36:
                            functionLabel = "bell";
                            break;
                        case 37:
                            functionLabel = "horn";
                            break;
                        case 38:
                            functionLabel = "whistle";
                            break;
                        case 39:
                            functionLabel = "door_sound";
                            break;
                        case 40:
                            functionLabel = "fan";
                            break;
                        case 42:
                            functionLabel = "shovel_work_sound";
                            break;
                        case 44:
                            functionLabel = "shift";
                            break;
                        case 260:
                            functionLabel = "interior_lighting";
                            break;
                        case 261:
                            functionLabel = "plate_light";
                            break;
                        case 263:
                            functionLabel = "brakesound";
                            break;
                        case 299:
                            functionLabel = "crane_raise_lower";
                            break;
                        case 555:
                            functionLabel = "hook_up_down";
                            break;
                        case 773:
                            functionLabel = "wheel_light";
                            break;
                        case 811:
                            functionLabel = "turn";
                            break;
                        case 1031:
                            functionLabel = "steam-blow";
                            break;
                        case 1033:
                            functionLabel = "radio_sound";
                            break;
                        case 1287:
                            functionLabel = "coupler_sound";
                            break;
                        case 1543:
                            functionLabel = "track_sound";
                            break;
                        case 1607:
                            functionLabel = "notch_up";
                            break;
                        case 1608:
                            functionLabel = "notch_down";
                            break;
                        case 2055:
                            functionLabel = "thunderer_whistle";
                            break;
                        case 3847:
                            functionLabel = "buffer_sound";
                            break;
                        default:
                            break;
                    }

                    re.setFunctionLabel(functNo, functionLabel);
                    re.setFunctionLockable(functNo, !moment);
                } catch (RuntimeException e) {
                    log.error("Error occurred while getting the function information : " + e.toString());
                }
                getFunctionDetails(functNo + 1);
            }
        }
    }

    @Override
    public void message(EcosMessage m) {

    }

    void storeloco() {
        Roster.getDefault().addEntry(re);
        ecosLoco.setRosterId(re.getId());
        re.ensureFilenameExists();

        re.writeFile(null, null);

        Roster.getDefault().writeRoster();
        ecosManager.clearLocoToRoster();
    }

    public void comboPanel() {
        frame.setTitle(Bundle.getMessage("DecoderSelectionXTitle", ecosLoco.getEcosDescription()));
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();

        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        JPanel p3 = this.layoutDecoderSelection();

        // Create a panel to hold all other components
        topPanel.setLayout(new BorderLayout());
        //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //frame.setDefaultCloseOperation(frameclosed());
        JLabel jLabel1 = new JLabel(Bundle.getMessage("DecoderNoIDWarning"));
        JButton okayButton = new JButton(Bundle.getMessage("ButtonOK"));
        p1.add(jLabel1);
        p2.add(okayButton);
        topPanel.add(p1);
        topPanel.add(p3);
        topPanel.add(p2);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        frame.getContentPane().add(topPanel);
        frame.pack();
        frame.setVisible(true);
        frame.toFront();
        frame.setFocusable(true);
        frame.setFocusableWindowState(true);
        frame.requestFocus();

        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        int w = frame.getSize().width;
        int h = frame.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        frame.setLocation(x, y);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent winEvt) {
                ecosManager.clearLocoToRoster();
            }
        });

        ActionListener okayButtonAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                okayButton();
            }
        };
        okayButton.addActionListener(okayButtonAction);
    }

    String selectedDecoderType() {
        if (!isDecoderSelected()) {
            return null;
        } else {
            return ((DecoderTreeNode) dTree.getLastSelectedPathComponent()).getTitle();
        }
    }

    boolean isDecoderSelected() {
        return !dTree.isSelectionEmpty();
    }

    private void okayButton() {
        pDecoderFile = InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(selectedDecoderType());
        selectedDecoder(pDecoderFile);
        frame.dispose();
    }

    private void selectedDecoder(DecoderFile pDecoderFile) {
        //pDecoderFile=InstanceManager.getDefault(DecoderIndexFile.class).fileFromTitle(selectedDecoderType());
        re.setDecoderModel(pDecoderFile.getModel());
        re.setDecoderFamily(pDecoderFile.getFamily());

        if (ecosLoco.getNumber() == 0) {
            re.setDccAddress(Integer.toString(EcosLocoAddress.MFX_DCCAddressOffset+ecosLoco.getEcosObjectAsInt()));
        } else {
            re.setDccAddress(Integer.toString(ecosLoco.getNumber()));
        }
        //re.setLongAddress(true);

        re.setRoadName("");
        re.setRoadNumber("");
        re.setMfg("");
        re.setModel("");
        re.setOwner(InstanceManager.getDefault(RosterConfigManager.class).getDefaultOwner());
        re.setComment(Bundle.getMessage("LocoAutoAdded"));
        re.setDecoderComment("");
        re.putAttribute(adaptermemo.getPreferenceManager().getRosterAttribute(), _ecosObject);
        re.ensureFilenameExists();
        if ((ecosLoco.getECOSProtocol().startsWith("DCC"))) {
            if (ecosLoco.getNumber() <= 127) {
                re.setProtocol(jmri.LocoAddress.Protocol.DCC_SHORT);
            } else {
                re.setProtocol(jmri.LocoAddress.Protocol.DCC_LONG);
            }
        } else if (ecosLoco.getECOSProtocol().equals("MMFKT") || ecosLoco.getECOSProtocol().equals("MFX")) {
            re.setProtocol(jmri.LocoAddress.Protocol.MFX);
        } else if (ecosLoco.getECOSProtocol().startsWith("MM")) {
            re.setProtocol(jmri.LocoAddress.Protocol.MOTOROLA);
        } else if (ecosLoco.getECOSProtocol().equals("SX32")) {
            re.setProtocol(jmri.LocoAddress.Protocol.SELECTRIX);
        }

        mProgrammer = null;
        cvModel = new CvTableModel(progStatus, mProgrammer);
        variableModel = new VariableTableModel(progStatus, new String[]{"CV", "Value"}, cvModel);
        resetModel = new ResetTableModel(progStatus, mProgrammer);
        storeloco();
        filename = "programmers" + File.separator + "Basic.xml";
        loadProgrammerFile(re);
        loadDecoderFile(pDecoderFile, re);

        variableModel.findVar("Speed Step Mode").setIntValue(0); // NOI18N
        if (ecosLoco.getECOSProtocol().equals("DCC128")) {
            variableModel.findVar("Speed Step Mode").setIntValue(1);
        }

        re.writeFile(cvModel, variableModel);
        getFunctionDetails(0);
        JOptionPane.showMessageDialog(frame, Bundle.getMessage("LocoAddedJDialog"));
        waitingForComplete = true;
    }

    /**
     *
     * @return true if the value in the Ecos Description is a duplicate of some
     *         other RosterEntry in the roster
     */
    public boolean checkDuplicate(String id) {
        // check its not a duplicate
        List<RosterEntry> l = Roster.getDefault().matchingList(null, null, null, null, null, null, id);
        boolean oops = false;
        for (int i = 0; i < l.size(); i++) {
            if (re != l.get(i)) {
                oops = true;
            }
        }
        return oops;
    }

    JTree dTree;
    DefaultTreeModel dModel;
    DefaultMutableTreeNode dRoot;
    TreeSelectionListener dListener;

    //@TODO this could do with being re-written so that it reuses the combined loco select tree code
    protected JPanel layoutDecoderSelection() {

        JPanel pane1a = new JPanel();
        pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
        // create the list of manufacturers; get the list of decoders, and add elements
        dRoot = new DefaultMutableTreeNode("Root");
        dModel = new DefaultTreeModel(dRoot);
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
        List<DecoderFile> decoders = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, null, null, null, null);
        int len = decoders.size();
        DefaultMutableTreeNode mfgElement = null;
        DefaultMutableTreeNode familyElement = null;
        for (int i = 0; i < len; i++) {
            DecoderFile decoder = decoders.get(i);
            String mfg = decoder.getMfg();
            String family = decoder.getFamily();
            String model = decoder.getModel();
            log.debug(" process " + mfg + "/" + family + "/" + model
                    + " on nodes "
                    + (mfgElement == null ? "<null>" : mfgElement.toString() + "(" + mfgElement.getChildCount() + ")") + "/"
                    + (familyElement == null ? "<null>" : familyElement.toString() + "(" + familyElement.getChildCount() + ")")
            );
            // build elements
            if (mfgElement == null || !mfg.equals(mfgElement.toString())) {
                // need new mfg node
                mfgElement = new DecoderTreeNode(mfg,
                        "CV8 = " + InstanceManager.getDefault(DecoderIndexFile.class).mfgIdFromName(mfg), "");
                dModel.insertNodeInto(mfgElement, dRoot, dRoot.getChildCount());
                familyElement = null;
            }
            String famComment = decoders.get(i).getFamilyComment();
            String verString = decoders.get(i).getVersionsAsString();
            String hoverText = "";
            if (famComment == null || famComment.equals("")) {
                if (verString != null && !verString.equals("")) {
                    hoverText = "CV7=" + verString;
                }
            } else {
                if (verString == null || verString.equals("")) {
                    hoverText = famComment;
                } else {
                    hoverText = famComment + "  CV7=" + verString;
                }
            }
            if (familyElement == null || !family.equals(familyElement.toString())) {
                // need new family node - is there only one model? Expect the
                // family element, plus the model element, so check i+2
                // to see if its the same, or if a single-decoder family
                // appears to have decoder names separate from the family name
                if ((i + 2 >= len)
                        || decoders.get(i + 2).getFamily().equals(family)
                        || !decoders.get(i + 1).getModel().equals(family)) {
                    // normal here; insert the new family element & exit
                    log.debug("normal family update case: " + family);
                    familyElement = new DecoderTreeNode(family,
                            hoverText,
                            decoders.get(i).titleString());
                    dModel.insertNodeInto(familyElement, mfgElement, mfgElement.getChildCount());
                    continue;
                } else {
                    // this is short case; insert decoder entry (next) here
                    log.debug("short case, i=" + i + " family=" + family + " next "
                            + decoders.get(i + 1).getModel());
                    if (i + 1 > len) {
                        log.error("Unexpected single entry for family: " + family);
                    }
                    family = decoders.get(i + 1).getModel();
                    familyElement = new DecoderTreeNode(family,
                            hoverText,
                            decoders.get(i).titleString());
                    dModel.insertNodeInto(familyElement, mfgElement, mfgElement.getChildCount());
                    i = i + 1;
                    continue;
                }
            }
            // insert at the decoder level, except if family name is the same
            if (!family.equals(model)) {
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
        dTree.setExpandsSelectedPaths(true);

        dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        // tree listener
        dTree.addTreeSelectionListener(dListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (!dTree.isSelectionEmpty() && dTree.getSelectionPath() != null
                        && // can't be just a mfg, has to be at least a family
                        dTree.getSelectionPath().getPathCount() > 2
                        && // can't be a multiple decoder selection
                        dTree.getSelectionCount() < 2) {
                    // decoder selected - reset and disable loco selection
                    log.debug("Selection event with " + dTree.getSelectionPath().toString());
                    if (locoBox != null) {
                        locoBox.setSelectedIndex(0);
                    }
                }
            }
        });

//      Mouselistener for doubleclick activation of proprammer
        dTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                // Clear any status messages and ensure the tree is in single path select mode
                //if (_statusLabel != null) _statusLabel.setText("StateIdle");
                dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);

                /* check for both double click and that it's a decoder
                 that is being clicked on.  If it's just a Family, the programmer
                 button is enabled by the TreeSelectionListener, but we don't
                 want to automatically open a programmer so a user has the opportunity
                 to select an individual decoder
                 */
                if (me.getClickCount() == 2) {
                    if (((TreeNode) dTree.getSelectionPath().getLastPathComponent()).isLeaf()) {
                        okayButton();
                    }
                }
            }
        });

        this.selectDecoder(ecosLoco.getCVAsString(8), ecosLoco.getCVAsString(7));
        return pane1a;
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

    protected void selectDecoder(String mfgID, String modelID) {

        // locate a decoder like that.
        List<DecoderFile> temp = InstanceManager.getDefault(DecoderIndexFile.class).matchingDecoderList(null, null, mfgID, modelID, null, null);
        if (log.isDebugEnabled()) {
            log.debug("selectDecoder found " + temp.size() + " matches");
        }
        // install all those in the JComboBox in place of the longer, original list
        if (temp.size() > 0) {
            updateForDecoderTypeID(temp);
        } else {
            String mfg = InstanceManager.getDefault(DecoderIndexFile.class).mfgNameFromId(mfgID);
            int intMfgID = Integer.parseInt(mfgID);
            int intModelID = Integer.parseInt(modelID);
            if (mfg == null) {
                updateForDecoderNotID(intMfgID, intModelID);
            } else {
                updateForDecoderMfgID(mfg, intMfgID, intModelID);
            }
        }
    }

    void updateForDecoderNotID(int pMfgID, int pModelID) {
        String msg = "Found mfg " + pMfgID + " version " + pModelID + "; no such manufacterer defined";
        log.warn(msg);
        dTree.clearSelection();
    }

    @SuppressWarnings("unchecked")
    void updateForDecoderMfgID(String pMfg, int pMfgID, int pModelID) {
        String msg = "Found mfg " + pMfgID + " (" + pMfg + ") version " + pModelID + "; no such decoder defined";
        log.warn(msg);
        dTree.clearSelection();
        Enumeration<TreeNode> e = dRoot.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
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
            //String msg = "Identified "+pList.size()+" matches: ";
            StringBuilder buf = new StringBuilder();
            buf.append("Identified "); // NOI18N
            buf.append(pList.size());
            buf.append(" matches: ");
            for (int i = 0; i < pList.size(); i++) {
                buf.append(pList.get(i).getModel());
                buf.append(":");
                //msg = msg+pList.get(i).getModel()+":";
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
        } else {
            dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        // Select the decoder(s) in the tree
        for (int i = 0; i < pList.size(); i++) {

            DecoderFile f = pList.get(i);
            String findMfg = f.getMfg();
            String findFamily = f.getFamily();
            String findModel = f.getModel();

            Enumeration<TreeNode> e = dRoot.breadthFirstEnumeration();
            while (e.hasMoreElements()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();

                // convert path to comparison string
                TreeNode[] list = node.getPath();
                if (list.length == 3) {
                    // check for match to mfg, model, model
                    if (list[1].toString().equals(findMfg)
                            && list[2].toString().equals(findModel)) {
                        TreePath path = new TreePath(node.getPath());
                        dTree.expandPath(path);
                        dTree.addSelectionPath(path);
                        dTree.scrollPathToVisible(path);
                        break;
                    }
                } else if (list.length == 4) {
                    // check for match to mfg, family, model
                    if (list[1].toString().equals(findMfg)
                            && list[2].toString().equals(findFamily)
                            && list[3].toString().equals(findModel)) {
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

    Element modelElem = null;

    Element decoderRoot = null;
    VariableTableModel variableModel;
    Element programmerRoot = null;
    ResetTableModel resetModel = null;

    protected void loadDecoderFile(DecoderFile df, RosterEntry re) {
        if (df == null) {
            log.error("loadDecoder file invoked with null object");
            return;
        }
        log.debug("loadDecoderFile from " + DecoderFile.fileLocation
                + " " + df.getFileName());

        try {
            decoderRoot = df.rootFromName(DecoderFile.fileLocation + df.getFileName());
        } catch (org.jdom2.JDOMException e) {
            log.error("JDOM Exception while loading decoder XML file: " + df.getFileName());
        } catch (java.io.IOException e) {
            log.error("IO Exception while loading decoder XML file: " + df.getFileName());
        }
        // load variables from decoder tree
        df.getProductID();
        df.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);

        df.loadResetModel(decoderRoot.getChild("decoder"), resetModel);

        // load function names
        re.loadFunctions(decoderRoot.getChild("decoder").getChild("family").getChild("functionlabels"));

        // get the showEmptyPanes attribute, if yes/no update our state
        if (decoderRoot.getAttribute("showEmptyPanes") != null) {
            log.debug("Found in decoder " + decoderRoot.getAttribute("showEmptyPanes").getValue());
        }

        // save the pointer to the model element
        modelElem = df.getModelElement();
    }

    // From PaneProgFrame
    protected void loadProgrammerFile(RosterEntry r) {
        // Open and parse programmer file
        XmlFile pf = new XmlFile() {
        };  // XmlFile is abstract
        try {
            programmerRoot = pf.rootFromName(filename);

            readConfig(programmerRoot, r);

        } catch (IOException | JDOMException e) {
            log.error("exception reading programmer file: {}", filename, e);
        }
    }

    void readConfig(Element root, RosterEntry r) {
        // check for "programmer" element at start
        if (root.getChild("programmer") == null) {
            log.error("xml file top element is not programmer");
            return;
        }
    }

    boolean getFunctionSupported = true;

    void getFunctionDetails(int func) {
        //Only gets information for function numbers upto 28
        if (func >= 29) {
            return;
        }
        String message = "get(" + _ecosObject + ", funcdesc[" + func + "])";
        EcosMessage m = new EcosMessage(message);
        adaptermemo.getTrafficController().sendEcosMessage(m, this);
    }

    private final static Logger log = LoggerFactory.getLogger(EcosLocoToRoster.class);

}
/*
 cv8 - mfgIdFromName
 cv7 - Version/family

 tmp1 = jmri.jmrit.decoderdefn.InstanceManager.getDefault(DecoderIndexFile.class)
 print tmp1.matchingDecoderList(None, None, cv8, None, None, None)

 matchingDecoderList(String mfg, String family, String decoderMfgID, String decoderVersionID, String decoderProductID, String model )

 tmp1 = jmri.jmrit.decoderdefn.InstanceManager.getDefault(DecoderIndexFile.class)
 list = tmp1.matchingDecoderList(None, None, "153", "16", None, None
 returns decoderfile.java
 print list[0].getMfg()
 print list[0].getFamily()
 */

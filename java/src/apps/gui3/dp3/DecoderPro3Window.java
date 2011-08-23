// DecoderPro3Window.java

 package apps.gui3.dp3;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.File;

import java.util.List;
import java.util.ResourceBundle;
import jmri.util.JmriJFrame;
import jmri.util.swing.ResizableImagePanel;
import jmri.jmrit.decoderdefn.DecoderFile;

// for ugly code
import jmri.Programmer;
import jmri.progdebugger.*;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.roster.swing.*;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;


/**
 * Standalone DecoderPro3 Window (new GUI)
 *
 * Ignores WindowInterface.
 *
 * TODO:
 * Several methods are copied from PaneProgFrame and should be refactored
 * No programmer support yet (dummy object below)
 * Color only covering borders
 * No reset toolbar support yet
 * No glass pane support (See DecoderPro3Panes class and usage below)
 * Special panes (Roster entry, attributes, graphics) not included
 * How do you pick a programmer file? (hardcoded)
 * Initialization needs partial deferal, too for 1st pane to appear
 * 
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneSet
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision$
 */
 
public class DecoderPro3Window 
        extends jmri.util.swing.multipane.TwoPaneTBWindow {

    public DecoderPro3Window() {
        super("DecoderPro", 
    	        new File("xml/config/apps/decoderpro/Gui3Menus.xml"), 
    	        new File("xml/config/apps/decoderpro/Gui3MainToolBar.xml"));  // no toolbar
    	add(createToolBarPanel(), BorderLayout.NORTH);
    	getTop().add(createTop());
        getBottom().add(createBottom());
        getToolBar().add(createToolBarPanel());
        statusField.setFont(statusField.getFont().deriveFont(10f));
        statusField.setText("idle");
        getStatus().add(statusField);
        systemsMenu();
        helpMenu(getMenu(), this);
        setSize(getMaximumSize());
        setVisible(true);
    }
    
     /**
     * Show only active systems in the menu bar.
     * <P>
     * Alternately, you might want to do
     * <PRE>
     *    menuBar.add(new jmri.jmrix.SystemsMenu());
     * </PRE>
     * @param menuBar
     * @param frame
     */
    protected void systemsMenu() {
        jmri.jmrix.ActiveSystemsMenu.addItems(getMenu());
        getMenu().add(new jmri.util.WindowMenu(this));
    }
    
    
    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        try {

            // create menu and standard items
            JMenu helpMenu = jmri.util.HelpUtil.makeHelpMenu("package.apps.Apps", true);
            
            // tell help to use default browser for external types
            javax.help.SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
                
            // use as main help menu 
            menuBar.add(helpMenu);
            
        } catch (java.lang.Throwable e3) {
            log.error("Unexpected error creating help: "+e3);
        }

    }
    
    jmri.jmrit.roster.swing.RosterTable rtable;
    ResourceBundle rb = ResourceBundle.getBundle("apps.gui3.dp3.DecoderPro3Bundle");
    
    JComponent createTop() {
        JPanel retval = new JPanel();
        retval.setLayout(new BorderLayout());
        //retval.add(createToolBarPanel(), BorderLayout.NORTH);
        // set up roster table
         
        rtable = new RosterTable(true);
        retval.add(rtable, BorderLayout.CENTER);
        // add selection listener
        rtable.getTable().getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (! e.getValueIsAdjusting()) {
                        for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                            if (rtable.getTable().getSelectionModel().isSelectedIndex(i)) {
                                locoSelected(rtable.getModel().getValueAt(i, RosterTableModel.IDCOL).toString());
                                break;
                            }
                        }    
                    }
                }
            }
        );

        return retval;
    }
    
    /**
     * An entry has been selected in the Roster Table, 
     * activate the bottom part of the window
     */
    void locoSelected(String id) {
        log.debug("locoSelected ID "+id);
        // convert to roster entry
        RosterEntry re = Roster.instance().entryFromTitle(id);
        
        updateDetails(re);
        repaint();
        rosterDetailPanel.revalidate();
        sp.revalidate();
    }
    
    JPanel paneSpace = new JPanel(); // place where the panes go
    JScrollPane sp;
    
    ProgDebugger programmer = new ProgDebugger();
        
    JPanel rosterDetailPanel = new JPanel();
        
    JComponent createBottom(){
        
        locoImage = new ResizableImagePanel(null, 320, 240);
        locoImage.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        locoImage.setOpaque(true);
		locoImage.setRespectAspectRatio(true);
        rosterDetailPanel.setLayout(new BorderLayout());
        rosterDetailPanel.add(locoImage, BorderLayout.WEST);
        rosterDetailPanel.add(rosterDetails(), BorderLayout.CENTER);
        rosterDetailPanel.add(bottomRight(), BorderLayout.EAST);
        sp = new JScrollPane(rosterDetailPanel);
        
        return sp;
    }
    
    JComponent createToolBarPanel(){
        ((JToolBar) getToolBar()).setFloatable(false);
        JPanel retval = new JPanel();
        retval.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));

        retval.add(identifyButton = new JButton(rb.getString("IdentifyButton"), new ImageIcon("resources/icons/misc/gui3/IdentifyButton.png")));
        identifyButton.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Start Identify Pressed");
                    startIdentifyLoco();
                }
            });
        identifyButton.setHorizontalAlignment(JButton.LEFT);
        identifyButton.setAlignmentX(0.0f);
        return retval;
    }
    
    JButton identifyButton;
    JButton newLocoButton;
    
    JLabel statusField = new JLabel();
    
    final ResourceBundle rbroster = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
    
    JLabel filename 		= new JLabel();
    JLabel dateUpdated   	= new JLabel();
    JLabel decoderModel 	= new JLabel();
    JLabel decoderFamily 	= new JLabel();
    
    JLabel id 		= new JLabel();
    JLabel roadName 	= new JLabel();
    JLabel maxSpeed		= new JLabel();
 
    JLabel roadNumber 	= new JLabel();
    JLabel mfg 		= new JLabel();
    JLabel model		= new JLabel();
    JLabel owner		= new JLabel();
    
    ResizableImagePanel locoImage;
    
    JPanel rosterDetails(){
        JPanel panel = new JPanel();
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(150,20);
        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.EAST;
        cL.insets = new Insets (0,0,0,15);
        JLabel row0Label = new JLabel(rbroster.getString("FieldID"), JLabel.LEFT);
        gbLayout.setConstraints(row0Label,cL);
        panel.setLayout(gbLayout);
        panel.add(row0Label);

        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id,cR);
        panel.add(id);

        cL.gridy = 1;
        JLabel row1Label = new JLabel(rbroster.getString("FieldRoadName"), JLabel.LEFT);
        gbLayout.setConstraints(row1Label,cL);
        panel.add(row1Label);

        cR.gridy = 1;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName,cR);
        panel.add(roadName);

        cL.gridy = 2;
        JLabel row2Label = new JLabel(rbroster.getString("FieldRoadNumber"));
        gbLayout.setConstraints(row2Label,cL);
        panel.add(row2Label);

        cR.gridy = 2;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber,cR);
        panel.add(roadNumber);

        cL.gridy = 3;
        JLabel row3Label = new JLabel(rbroster.getString("FieldManufacturer"));
        gbLayout.setConstraints(row3Label,cL);
        panel.add(row3Label);

        cR.gridy = 3;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg,cR);
        panel.add(mfg);

        cL.gridy = 4;
        JLabel row4Label = new JLabel(rbroster.getString("FieldOwner"));
        gbLayout.setConstraints(row4Label,cL);
        panel.add(row4Label);

        cR.gridy = 4;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner,cR);
        panel.add(owner);

        cL.gridy = 5;
        JLabel row5Label = new JLabel(rbroster.getString("FieldModel"));
        gbLayout.setConstraints(row5Label,cL);
        panel.add(row5Label);

        cR.gridy = 5;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model,cR);
        panel.add(model);

        cL.gridy = 6;
        /*JLabel row6Label = new JLabel(rbroster.getString("FieldDCCAddress"));
        gbLayout.setConstraints(row6Label,cL);
        add(row6Label);*/

        cR.gridy = 6;
        /*gbLayout.setConstraints(selPanel,cR);
        add(selPanel);*/

        cL.gridy = 7;

        cR.gridy = 7;

        
        cL.gridy = 8;


        cR.gridy = 8;


        cL.gridy = 9;
        JLabel row9Label = new JLabel(rbroster.getString("FieldDecoderFamily"));
        gbLayout.setConstraints(row9Label,cL);
        panel.add(row9Label);

        cR.gridy = 9;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily,cR);
        panel.add(decoderFamily);

        cL.gridy = 10;
        JLabel row10Label = new JLabel(rbroster.getString("FieldDecoderModel"));
        gbLayout.setConstraints(row10Label,cL);
        panel.add(row10Label);

        cR.gridy = 10;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel,cR);
        panel.add(decoderModel);

        cL.gridy = 11;

        cR.gridy = 11;

        cL.gridy = 12;
        JLabel row12Label = new JLabel(rbroster.getString("FieldFilename"));
        gbLayout.setConstraints(row12Label,cL);
        panel.add(row12Label);

        cR.gridy = 12;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(filename,cR);
        panel.add(filename);

        cL.gridy = 13;
        JLabel row13Label = new JLabel(rbroster.getString("FieldDateUpdated"));
        gbLayout.setConstraints(row13Label,cL);
        panel.add(row13Label);

        cR.gridy = 13;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dateUpdated,cR);
        panel.add(dateUpdated);
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retval.add(panel);
        return retval;
    }
    
    void updateDetails(RosterEntry _re){
        re=_re;
    
        filename.setText(re.getFileName());
        dateUpdated.setText(re.getDateUpdated());
        decoderModel.setText(re.getDecoderModel());
        decoderFamily.setText(re.getDecoderFamily());
        
        
        id.setText(re.getId());
        roadName.setText(re.getRoadName());
     
        roadNumber.setText(re.getRoadNumber());
        mfg.setText(re.getMfg());
        model.setText(re.getModel());
        owner.setText(re.getOwner());
        locoImage.setImagePath(re.getImagePath());
        if(re!=null){
            basicProg.setEnabled(true);
            compProg.setEnabled(true);
            throttleLabels.setEnabled(true);
            rosterMedia.setEnabled(true);
            throttleLaunch.setEnabled(true);
            modePane.setEnabled(true);
            service.setEnabled(true);
            ops.setEnabled(true);
            edit.setEnabled(true);
        }
    
    }
    
    JRadioButton service = new JRadioButton("<HTML>Service Mode<br>(programming track)</HTML>");
    JRadioButton ops = new JRadioButton("<HTML>Operations Mode<br>(Programming On Main)</HTML>");
    JRadioButton edit = new JRadioButton("Edit Only");
    
    jmri.jmrit.progsupport.ProgModeSelector modePane = new jmri.jmrit.progsupport.ProgDeferredServiceModePane();
    
    JButton basicProg = new JButton("Basic Programmer");
    JButton compProg = new JButton("Comprehensive Programmer");
    JButton throttleLabels = new JButton("Throttle Labels");
    JButton rosterMedia = new JButton("Roster Media");
    JButton throttleLaunch = new JButton("Launch Throttle");
    
    ActionListener programModeListener = new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    updateProgMode();
                }
            };
            
    void updateProgMode(){
        modePane.setEnabled(false);
        if(service.isSelected()){
            modePane.setEnabled(true);
        }
    }
    
    JPanel bottomRight(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup progMode = new ButtonGroup();
        progMode.add(service);
        progMode.add(ops);
        progMode.add(edit);
        
        service.setEnabled(false);
        ops.setEnabled(false);
        edit.setEnabled(false);

        JPanel progModePanel = new JPanel();
        progModePanel.add(service);
        progModePanel.add(ops);
        progModePanel.add(edit);
        
        service.addActionListener(programModeListener);
        ops.addActionListener(programModeListener);
        edit.addActionListener(programModeListener);
        
        service.setSelected(true);
        
        panel.add(progModePanel);
        panel.add(modePane);
        
        JPanel buttonHolder = new JPanel();
        GridLayout buttonLayout = new GridLayout(3, 2, 5, 5);
        buttonHolder.setLayout(buttonLayout);
        
        buttonHolder.add(basicProg);
        buttonHolder.add(compProg);
        buttonHolder.add(throttleLabels);
        buttonHolder.add(rosterMedia);
        buttonHolder.add(throttleLaunch);
        
        panel.add(buttonHolder);
        
        basicProg.setEnabled(false);
        basicProg.addActionListener( new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                        startProgrammer(null, re, "Basic");
                }
            });
        compProg.setEnabled(false);
        compProg.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    startProgrammer(null, re, "Comprehensive");
            }
        });
        throttleLabels.setEnabled(false);
        throttleLabels.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    edit.setSelected(true);
                    startProgrammer(null, re, "ThrottleLabels");
            }
        });
        rosterMedia.setEnabled(false);
        rosterMedia.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                    edit.setSelected(true);
                    startProgrammer(null, re, "MediaPane");
            }
        });
        
        throttleLaunch.setEnabled(false);
        throttleLaunch.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Launch Throttle pressed");
                ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
                tf.toFront();
                tf.getAddressPanel().setRosterEntry(re);
            }
        });
        
        modePane.setEnabled(false);

        return panel;
    }
    
    //current selected loco
    RosterEntry re;

    
    /**
     * Identify loco button pressed, start the identify operation
     * This defines what happens when the identify is done.
     */
     //taken out of CombinedLocoSelPane
    protected void startIdentifyLoco() {
        // start identifying a loco
        final DecoderPro3Window me = this;
        IdentifyLoco id = new IdentifyLoco() {
                private DecoderPro3Window who = me;
                protected void done(int dccAddress) {
                    // if Done, updated the selected decoder
                    who.selectLoco(dccAddress);
                }
                protected void message(String m) {
                    statusField.setText(m);
                }
                protected void error() {
                    // raise the button again
                    //idloco.setSelected(false);
                }
            };
        id.start();
    }
    
        /**
     * Identify locomotive complete, act on it by setting the GUI.
     * This will fire "GUI changed" events which will reset the
     * decoder GUI.
     * @param dccAddress
     */
    protected void selectLoco(int dccAddress) {
        // raise the button again
        //idloco.setSelected(false);
        // locate that loco
        List<RosterEntry> l = Roster.instance().matchingList(null, null, Integer.toString(dccAddress),
                                                null, null, null, null);
        if (log.isDebugEnabled()) log.debug("selectLoco found "+l.size()+" matches");
        if (l.size() > 0) {
            RosterEntry r = l.get(0);
            updateDetails(r);
            JTable table = rtable.getTable();
            int entires = table.getRowCount();
            for (int i = 0; i<entires; i++){
                if(table.getValueAt(i, 0).equals(r.getId())){
                    table.addRowSelectionInterval(i,i);
                }
            }
        } else {
            log.warn("Read address "+dccAddress+", but no such loco in roster");
            JOptionPane.showMessageDialog(null, "No Such loco in the Roster");
        }
    }
    protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                    String filename) {
        //String title = rbt.getString("FrameNewEntryTitle");
        String title = re.getId();
        Programmer pProg = null;
        JFrame p = new PaneProgFrame(decoderFile, re,
                                         title, "programmers"+File.separator+filename+".xml",
                                         null, false){
                protected JPanel getModePane() { return null; }
            };
        
        if(service.isSelected()){
            pProg = jmri.InstanceManager.programmerManagerInstance()
                                    .getGlobalProgrammer();
            p = new PaneProgFrame(decoderFile, re,
                                         title, "programmers"+File.separator+filename+".xml",
                                         pProg, false){
                                        
                protected JPanel getModePane() {
                    return modePane;
                }
            };
        }
        if(ops.isSelected()){
            int address = Integer.parseInt(re.getDccAddress());
            boolean longAddr = re.isLongAddress();
            pProg = jmri.InstanceManager.programmerManagerInstance()
                                    .getAddressedProgrammer(longAddr, address);
            p = new PaneOpsProgFrame(decoderFile, re, title, "programmers"+File.separator+filename+".xml",
                    pProg);
        }
        p.pack();
        p.setVisible(true);

    }
    
    public void windowClosing(java.awt.event.WindowEvent e) {
        super.windowClosing(e);
        jmri.InstanceManager.shutDownManagerInstance().shutdown();
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderPro3Window.class.getName());
}

/* @(#)DecoderPro3Window.java */

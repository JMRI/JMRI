// DecoderPro3Window.java

package apps.gui3.dp3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;

import java.io.File;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import jmri.Programmer;
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.roster.swing.*;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.throttle.ThrottleFrameManager;
import jmri.util.swing.ResizableImagePanel;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.util.datatransfer.RosterEntrySelection;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.ConnectionConfig;

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
        extends jmri.util.swing.multipane.TwoPaneTBWindow{

    static int openWindowInstances = 0;

    /**
     * Loads Decoder Pro 3 with the default set of menus and toolbars
     */
    public DecoderPro3Window(){
        super("DecoderPro",
                new File("xml/config/apps/decoderpro/Gui3Menus.xml"),
                new File("xml/config/apps/decoderpro/Gui3MainToolBar.xml"));
        buildWindow();
    }

    /**
     * Loads Decoder Pro 3 with specific menu and toolbar files
     */
    public DecoderPro3Window(File menuFile, File toolbarFile) {
        super("DecoderPro",
                menuFile,
                toolbarFile);
        buildWindow();
    }

    protected void buildWindow(){
        //Additions to the toolbar need to be added first otherwise when trying to hide bits up during the initialisation they remain on screen
        additionsToToolBar();
        openWindowInstances++;
        if(openWindowInstances>1)
            firePropertyChange("closewindow", "setEnabled", true);
        else
            firePropertyChange("closewindow", "setEnabled", false);
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        getTop().add(createTop());
        
        getBottom().setMinimumSize(summaryPaneDim);

        getBottom().add(createBottom());
        statusBar();
        systemsMenu();
        helpMenu(getMenu(), this);
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            //System.out.println(jmri.managers.ManagerDefaultSelector.instance.getDefault(jmri.ProgrammerManager.class));
            //System.out.println(jmri.InstanceManager.programmerManagerInstance().getGlobalProgrammer());
        }
        if((!p.getSimplePreferenceState(DecoderPro3Window.class.getName()+".hideGroups")) && !Roster.instance().getRosterGroupList().isEmpty()){
            hideGroupsPane(false);
        } else {
            hideGroupsPane(true);
        }
        
        if(p.getSimplePreferenceState(DecoderPro3Window.class.getName()+".hideSummary")){
            //We have to set it to display first, then we can hide it.
            hideBottomPane(false);
            hideBottomPane(true);
        }
        
        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent changeEvent) {
            JSplitPane sourceSplitPane = (JSplitPane) changeEvent.getSource();
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
              int current = sourceSplitPane.getDividerLocation()+sourceSplitPane.getDividerSize();
              int panesize = (int) (sourceSplitPane.getSize().getHeight());
              if(panesize-current<=1)
                hideBottomPane=true;
              else
                hideBottomPane=false;
              //p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideSummary",hideSummary);
            }
          }
        };
        getSplitPane().addPropertyChangeListener(propertyChangeListener);
    }
    
    void additionsToToolBar(){
        //This value may return null if the DP3 window has been called from a the traditional JMRI menu frame
        if(apps.gui3.Apps3.buttonSpace()!=null)
            getToolBar().add(apps.gui3.Apps3.buttonSpace());
        getToolBar().add(modePanel);
        getToolBar().add(new jmri.jmrit.roster.SelectRosterGroupPanelAction("Select Group").makePanel());
    }

    jmri.UserPreferencesManager p;
    final String rosterGroupSelectionCombo = this.getClass().getName()+".rosterGroupSelected";

    JLabel serviceModeProgrammerLabel = new JLabel();
    JLabel operationsModeProgrammerLabel = new JLabel();
    ConnectionConfig opsModeProCon = null;
    ConnectionConfig serModeProCon = null;
    /*
     * This status bar needs sorting out properly
     */
    void statusBar(){
        JLabel programmerLabel = new JLabel();
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            /*Ideally we should probably have the programmer manager reference the username configured in the system connection memo.
            but as DP3 (jmri can not use mutliple programmers!) isn't designed for multi-connection enviroments this should be sufficient*/
            String serviceModeProgrammer = jmri.InstanceManager.programmerManagerInstance().getUserName();
            serviceModeProgrammerLabel.setText ("Service Mode Programmer " +serviceModeProgrammer + " Is Online");
            serviceModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            
            ArrayList<Object> connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
            if (connList!=null){
                for (int x = 0; x<connList.size(); x++){
                    ConnectionConfig conn = (jmri.jmrix.ConnectionConfig)connList.get(x);
                    if(conn.getConnectionName().equals(serviceModeProgrammer)){
                        serModeProCon = conn;
                    }
                }
            }
        } else {
            serviceModeProgrammerLabel.setText("No Service Mode Programmer Available");
            serviceModeProgrammerLabel.setForeground(Color.red);
        }
        
        addToStatusBox(serviceModeProgrammerLabel, null);
        
        operationsModeProgrammerLabel = new JLabel();
        if (jmri.InstanceManager.programmerManagerInstance()!=null &&
                        jmri.InstanceManager.programmerManagerInstance().isAddressedModePossible()){
            /*Ideally we should probably have the programmer manager reference the username configured in the system connection memo.
            but as DP3 (jmri can not use mutliple programmers!) isn't designed for multi-connection enviroments this should be sufficient*/
            String opsModeProgrammer = jmri.InstanceManager.programmerManagerInstance().getUserName();
            operationsModeProgrammerLabel.setText ("Operations Mode Programmer " +opsModeProgrammer + " Is Online");
            operationsModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            ArrayList<Object> connList = jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class);
            if (connList!=null){
                for (int x = 0; x<connList.size(); x++){
                    ConnectionConfig conn = (jmri.jmrix.ConnectionConfig)connList.get(x);
                    if(conn.getConnectionName().equals(opsModeProgrammer)){
                        opsModeProCon=conn;
                    }
                }
            }
        } else {
            operationsModeProgrammerLabel.setText("No Operations Mode Programmer Available");
            operationsModeProgrammerLabel.setForeground(Color.red);
        }
        
        ConnectionStatus.instance().addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("change")){
                    updateProgrammerStatus();
                } 
            }
        });
        
        addToStatusBox(operationsModeProgrammerLabel, null);
        
        JLabel programmerStatusLabel = new JLabel("Programmer Status : ");
        statusField.setText("idle");
        addToStatusBox(programmerStatusLabel, statusField);
        
        programmerLabel = new JLabel("Active Roster Group : ");
        addToStatusBox(programmerLabel, activeRosterGroupField);
    }
    
    protected void updateProgrammerStatus(){
        if(serModeProCon!=null){
            if(ConnectionStatus.instance().isConnectionOk(serModeProCon.getInfo())){
                serviceModeProgrammerLabel.setText ("Service Mode Programmer " + serModeProCon.getConnectionName() + " Is Online");
                serviceModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            } else {
                serviceModeProgrammerLabel.setText("Service Mode Programmer " + serModeProCon.getConnectionName() + " Is Offline");
                serviceModeProgrammerLabel.setForeground(Color.red);
            }
        }
        if(opsModeProCon!=null){
            if(ConnectionStatus.instance().isConnectionOk(opsModeProCon.getInfo())){
                operationsModeProgrammerLabel.setText ("Operations Mode Programmer " + opsModeProCon.getConnectionName() + " Is Online");
                operationsModeProgrammerLabel.setForeground(new Color(0, 128, 0));
            } else {
                operationsModeProgrammerLabel.setText("Operations Mode Programmer "+ opsModeProCon.getConnectionName() + " Is Offline");
                operationsModeProgrammerLabel.setForeground(Color.red);
            }
        }
    }
    
    protected void systemsMenu() {
        jmri.jmrix.ActiveSystemsMenu.addItems(getMenu());
        getMenu().add(new jmri.util.WindowMenu(this));
    }

    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        try {
            // create menu and standard items
            JMenu helpMenu = jmri.util.HelpUtil.makeHelpMenu("package.apps.gui3.dp3.DecoderPro3", true);

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
    JSplitPane rosterGroupSplitPane;

    JComponent createTop() {
        
        String rosterGroup = p.getComboBoxLastSelection(rosterGroupSelectionCombo);
        Roster.instance().setRosterGroup(rosterGroup);
        activeRosterGroupField.setText(Roster.getRosterGroupName());
        
        JPanel rosters = new JPanel();
        JPanel groups = new RosterGroupsPanel();
        rosters.setLayout(new BorderLayout());

        // set up roster table

        rtable = new RosterTable(false, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rosters.add(rtable, BorderLayout.CENTER);
        // add selection listener
        rtable.getTable().getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            if (rtable.getTable().getSelectedRowCount() == 1) {
                                locoSelected(rtable.getModel().getValueAt(rtable.getTable().getSelectedRow(), RosterTableModel.IDCOL).toString());
                            } else if (rtable.getTable().getSelectedRowCount() > 1) {
                                locoSelected(null);
                            } // leave last selected item visible if no selection
                        }
                    }
            }
        );

        TableModel jModel = rtable.getModel();
        int count = jModel.getColumnCount();
        
        for (int i = 0; i <count; i++){
            if(p.getProperty(getWindowFrameRef(), "rosterOrder:"+ jModel.getColumnName(i))!=null){
                int sort = (Integer) p.getProperty(getWindowFrameRef(), "rosterOrder:"+ jModel.getColumnName(i));
                rtable.getModel().setSortingStatus(i, sort);
            }
            
            if(p.getProperty(getWindowFrameRef(), "rosterWidth:"+ jModel.getColumnName(i))!=null){
                int width = (Integer) p.getProperty(getWindowFrameRef(), "rosterWidth:"+ jModel.getColumnName(i));
                rtable.getTable().getColumnModel().getColumn(i).setPreferredWidth(width);
            }
        }
        rtable.getTable().setDragEnabled(true);
        rtable.getTable().setTransferHandler(new TransferHandler() {

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            @Override
            public Transferable createTransferable(JComponent c) {
                ArrayList<String> Ids = new ArrayList<String>(rtable.getTable().getSelectedRowCount());
                for (int i = 0; i < rtable.getTable().getSelectedRowCount(); i++) {
                    Ids.add(rtable.getModel().getValueAt(rtable.getTable().getSelectedRows()[i], RosterTableModel.IDCOL).toString());
                }
                return new RosterEntrySelection(Ids);
            }

            @Override
            public void exportDone(JComponent c, Transferable t, int action) {
                // nothing to do
            }
        });

        // assemble roster/groups splitpane
        rosterGroupSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groups, rosters);
        rosterGroupSplitPane.setOneTouchExpandable(true);
        rosterGroupSplitPane.setResizeWeight(0); // emphasis rosters
       
        Object w = p.getProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation");
        if (w != null) {
            groupSplitPaneLocation = (Integer) w;
            rosterGroupSplitPane.setDividerLocation(groupSplitPaneLocation);
        }
        if (!Roster.instance().getRosterGroupList().isEmpty()) {
            if (p.getSimplePreferenceState(DecoderPro3Window.class.getName()+".hideGroups")){
                hideGroupsPane(true);
            }
        } else {
            enableRosterGroupMenuItems(false);
        }
        
        PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent changeEvent) {
            JSplitPane sourceSplitPane = (JSplitPane) changeEvent.getSource();
            String propertyName = changeEvent.getPropertyName();
            if (propertyName.equals(JSplitPane.LAST_DIVIDER_LOCATION_PROPERTY)) {
              int current = sourceSplitPane.getDividerLocation();
              if(current<=1){
                hideGroups=true;
              } else {
                hideGroups=false;
              }
              //p.setSimplePreferenceState(DecoderPro3Window.class.getName() + ".hideGroups", hideGroups);
              Integer last = (Integer) changeEvent.getNewValue();
              if(current>=2)
                groupSplitPaneLocation = current;
              else if(last>=2)
                groupSplitPaneLocation = last;
            }
          }
        };

        rosterGroupSplitPane.addPropertyChangeListener(propertyChangeListener);
        
        Roster.instance().addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("ActiveRosterGroup")){
                    String activeGroup = ((String)e.getNewValue());
                    p.addComboBoxLastSelection(rosterGroupSelectionCombo, activeGroup);
                    activeRosterGroupField.setText(activeGroup);
                } 
                if (e.getPropertyName().equals("RosterGroupAdded")
                        && Roster.instance().getRosterGroupList().size() == 1) {
                    // if the pane is hidden, show it when 1st group is created
                    hideGroupsPane(false);
                    enableRosterGroupMenuItems(true);
                }
            }
        });
        
        return rosterGroupSplitPane;
    }

    /**
     * An entry has been selected in the Roster Table, 
     * activate the bottom part of the window
     */
    void locoSelected(String id) {
        if (id != null) {
            log.debug("locoSelected ID "+id);

            if (re != null) {
                //We remove the propertychangelistener if we had a previoulsy selected entry;
                re.removePropertyChangeListener(rosterEntryUpdateListener);
            }
            // convert to roster entry
            re = Roster.instance().entryFromTitle(id);
            re.addPropertyChangeListener(rosterEntryUpdateListener);
        } else {
            log.debug("Multiple selection");
            re = null;
        }
        updateDetails();
    }

    ProgDebugger programmer = new ProgDebugger();

    JPanel rosterDetailPanel = new JPanel();

    JComponent createBottom(){

        locoImage = new ResizableImagePanel(null, 240, 160);
        locoImage.setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
        locoImage.setOpaque(true);
        locoImage.setRespectAspectRatio(true);
        rosterDetailPanel.setLayout(new BorderLayout());
        rosterDetailPanel.add(locoImage, BorderLayout.WEST);
        rosterDetailPanel.add(rosterDetails(), BorderLayout.CENTER);
        rosterDetailPanel.add(bottomRight(), BorderLayout.EAST);
        if(p.getSimplePreferenceState(DecoderPro3Window.class.getName()+".hideRosterImage")){
            locoImage.setVisible(false);
            hideRosterImage=true;
        }

        rosterEntryUpdateListener = new PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                updateDetails();
            }
        };

        return rosterDetailPanel;
    }

    JLabel statusField = new JLabel();
    JLabel activeRosterGroupField = new JLabel();

    final ResourceBundle rbroster = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

    JTextPane filename 		= new JTextPane();
    JTextPane dateUpdated   	= new JTextPane();
    JTextPane decoderModel 	= new JTextPane();
    JTextPane decoderFamily 	= new JTextPane();

    JTextPane id 		= new JTextPane();
    JTextPane roadName 	= new JTextPane();
    JTextPane maxSpeed		= new JTextPane();

    JTextPane roadNumber 	= new JTextPane();
    JTextPane mfg 		= new JTextPane();
    JTextPane model		= new JTextPane();
    JTextPane owner		= new JTextPane();

    ResizableImagePanel locoImage;

    boolean hideRosterImage = false;

    JPanel rosterDetails(){
        JPanel panel = new JPanel();
        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        GridBagConstraints cR = new GridBagConstraints();
        Dimension minFieldDim = new Dimension(30,20);
        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.EAST;
        cL.insets = new Insets (0,0,0,15);
        JLabel row0Label = new JLabel(rbroster.getString("FieldID")+":", JLabel.LEFT);
        gbLayout.setConstraints(row0Label,cL);
        panel.setLayout(gbLayout);
        panel.add(row0Label);

        cR.gridx = 1;
        cR.gridy = 0;
        cR.anchor = GridBagConstraints.WEST;
        id.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(id,cR);
        formatTextAreaAsLabel(id);
        panel.add(id);

        cL.gridy = 1;
        JLabel row1Label = new JLabel(rbroster.getString("FieldRoadName")+":", JLabel.LEFT);
        gbLayout.setConstraints(row1Label,cL);
        panel.add(row1Label);

        cR.gridy = 1;
        roadName.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadName,cR);
        formatTextAreaAsLabel(roadName);
        panel.add(roadName);

        cL.gridy = 2;
        JLabel row2Label = new JLabel(rbroster.getString("FieldRoadNumber")+":");
        gbLayout.setConstraints(row2Label,cL);
        panel.add(row2Label);

        cR.gridy = 2;
        roadNumber.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(roadNumber,cR);
        formatTextAreaAsLabel(roadNumber);
        panel.add(roadNumber);

        cL.gridy = 3;
        JLabel row3Label = new JLabel(rbroster.getString("FieldManufacturer")+":");
        gbLayout.setConstraints(row3Label,cL);
        panel.add(row3Label);

        cR.gridy = 3;
        mfg.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(mfg,cR);
        formatTextAreaAsLabel(mfg);
        panel.add(mfg);

        cL.gridy = 4;
        JLabel row4Label = new JLabel(rbroster.getString("FieldOwner")+":");
        gbLayout.setConstraints(row4Label,cL);
        panel.add(row4Label);

        cR.gridy = 4;
        owner.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(owner,cR);
        formatTextAreaAsLabel(owner);
        panel.add(owner);

        cL.gridy = 5;
        JLabel row5Label = new JLabel(rbroster.getString("FieldModel")+":");
        gbLayout.setConstraints(row5Label,cL);
        panel.add(row5Label);

        cR.gridy = 5;
        model.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(model,cR);
        formatTextAreaAsLabel(model);
        panel.add(model);

        cL.gridy = 6;

        cR.gridy = 6;

        cL.gridy = 7;

        cR.gridy = 7;


        cL.gridy = 8;


        cR.gridy = 8;


        cL.gridy = 9;
        JLabel row9Label = new JLabel(rbroster.getString("FieldDecoderFamily")+":");
        gbLayout.setConstraints(row9Label,cL);
        panel.add(row9Label);

        cR.gridy = 9;
        decoderFamily.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderFamily,cR);
        formatTextAreaAsLabel(decoderFamily);
        panel.add(decoderFamily);

        cL.gridy = 10;
        JLabel row10Label = new JLabel(rbroster.getString("FieldDecoderModel")+":");
        gbLayout.setConstraints(row10Label,cL);
        panel.add(row10Label);

        cR.gridy = 10;
        decoderModel.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(decoderModel,cR);
        formatTextAreaAsLabel(decoderModel);
        panel.add(decoderModel);

        cL.gridy = 11;

        cR.gridy = 11;

        cL.gridy = 12;
        JLabel row12Label = new JLabel(rbroster.getString("FieldFilename")+":");
        gbLayout.setConstraints(row12Label,cL);
        panel.add(row12Label);

        cR.gridy = 12;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(filename,cR);
        formatTextAreaAsLabel(filename);
        panel.add(filename);

        cL.gridy = 13;
        JLabel row13Label = new JLabel(rbroster.getString("FieldDateUpdated")+":");
        gbLayout.setConstraints(row13Label,cL);
        panel.add(row13Label);

        cR.gridy = 13;
        filename.setMinimumSize(minFieldDim);
        gbLayout.setConstraints(dateUpdated,cR);
        panel.add(dateUpdated);
        formatTextAreaAsLabel(dateUpdated);
        JPanel retval = new JPanel(new FlowLayout(FlowLayout.LEFT));
        retval.add(panel);
        return retval;
    }

    void formatTextAreaAsLabel(JTextPane pane){
        pane.setOpaque(false);
        pane.setEditable(false);
        pane.setBorder(null);
    }

    void updateDetails(){
        if(re==null){
            String value = (rtable.getTable().getSelectedRowCount() > 1) ? "Multiple Items Selected" : "";
            filename.setText(value);
            dateUpdated.setText(value);
            decoderModel.setText(value);
            decoderFamily.setText(value);


            id.setText(value);
            roadName.setText(value);

            roadNumber.setText(value);
            mfg.setText(value);
            model.setText(value);
            owner.setText(value);
            locoImage.setImagePath(value);
        } else {
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
            if(hideRosterImage)
                locoImage.setVisible(false);
            else
                locoImage.setVisible(true);

            prog1Button.setEnabled(true);
            prog2Button.setEnabled(true);
            throttleLabels.setEnabled(true);
            rosterMedia.setEnabled(true);
            throttleLaunch.setEnabled(true);

            updateProgMode();
        }
    }

    JRadioButton service = new JRadioButton("<HTML>Service Mode<br>(Programming Track)</HTML>");
    JRadioButton ops = new JRadioButton("<HTML>Operations Mode<br>(Programming On Main)</HTML>");
    JRadioButton edit = new JRadioButton("<HTML>Edit Only</HTML>");

    jmri.jmrit.progsupport.ProgModeSelector modePanel = new jmri.jmrit.progsupport.ProgServiceModeComboBox();

    JButton prog1Button = new JButton("Basic Programmer");
    JButton prog2Button = new JButton("Comprehensive Programmer");
    JButton throttleLabels = new JButton("Throttle Labels");
    JButton rosterMedia = new JButton("Roster Media");
    JButton throttleLaunch = new JButton("Launch Throttle");

    ActionListener programModeListener;

    PropertyChangeListener rosterEntryUpdateListener;

    void updateProgMode(){
        String progMode;
        if(service.isSelected()){
            progMode = "setprogservice";
        }
        else if(ops.isSelected()){
            progMode = "setprogops";
        }
        else{
            progMode = "setprogedit";
        }

        firePropertyChange(progMode, "setSelected", true);
    }
    /**
     * Simple method to change over the programmer buttons, this should be impliemented button
     * with the buttons in their own class etc, but this will work for now.
     * Basic button is button Id 1, comprehensive button is button id 2
     */
    public void setProgrammerLaunch(int buttonId, String programmer, String buttonText){
        if(buttonId == 1){
            programmer1 = programmer;
            prog1Button.setText(buttonText);
        } else if (buttonId == 2){
            programmer2 = programmer;
            prog2Button.setText(buttonText);
        }
    }

    String programmer1 = "Basic";
    String programmer2 = "Comprehensive";

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
        firePropertyChange("setprogservice", "setEnabled", false);
        firePropertyChange("setprogops", "setEnabled", false);
        firePropertyChange("setprogedit", "setEnabled", false);

        JPanel progModePanel = new JPanel();
        progModePanel.add(service);
        progModePanel.add(ops);
        progModePanel.add(edit);

        programModeListener = new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateProgMode();
            }
        };

        service.addActionListener(programModeListener);
        ops.addActionListener(programModeListener);
        edit.addActionListener(programModeListener);
        
        service.setVisible(false);
        ops.setVisible(false);
        
        if (jmri.InstanceManager.programmerManagerInstance()!=null){
            if(jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
                service.setEnabled(true);
                service.setVisible(true);
                firePropertyChange("setprogservice", "setEnabled", true);
            }
            if (jmri.InstanceManager.programmerManagerInstance().isAddressedModePossible()){
                ops.setEnabled(true);
                ops.setVisible(true);
                firePropertyChange("setprogops", "setEnabled", true);
            }
        }
        
        edit.setEnabled(true);
        firePropertyChange("setprogedit", "setEnabled", true);
        
        String strProgMode;
        if(service.isEnabled()){
            service.setSelected(true);
            strProgMode = "setprogservice";
            modePanel.setVisible(true);
        }
        else if(ops.isEnabled()){
            ops.setSelected(true);
            strProgMode = "setprogops";
            modePanel.setVisible(false);
        }
        else{
            edit.setSelected(true);
            modePanel.setVisible(false);
            strProgMode = "setprogedit";
        }
        
        firePropertyChange(strProgMode, "setSelected", true);
        
        panel.add(progModePanel);

        JPanel buttonHolder = new JPanel();
        GridLayout buttonLayout = new GridLayout(3, 2, 5, 5);
        buttonHolder.setLayout(buttonLayout);

        buttonHolder.add(prog1Button);
        buttonHolder.add(prog2Button);
        buttonHolder.add(throttleLabels);
        buttonHolder.add(rosterMedia);
        buttonHolder.add(throttleLaunch);

        panel.add(buttonHolder);

        prog1Button.setEnabled(false);
        prog1Button.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                startProgrammer(null, re, programmer1);
            }
        });
        prog2Button.setEnabled(false);
        prog2Button.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                startProgrammer(null, re, programmer2);
            }
        });
        throttleLabels.setEnabled(false);
        throttleLabels.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                edit.setSelected(true);
                    startProgrammer(null, re, "dp3"+File.separator+"ThrottleLabels");
            }
        });
        rosterMedia.setEnabled(false);
        rosterMedia.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                edit.setSelected(true);
                    startProgrammer(null, re, "dp3"+File.separator+"MediaPane");
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

        return panel;
    }

    //current selected loco
    RosterEntry re;

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
        if(re!=null){
            //We remove the propertychangelistener if we had a previoulsy selected entry;
            re.removePropertyChangeListener(rosterEntryUpdateListener);
        }
        List<RosterEntry> l = Roster.instance().matchingList(null, null, Integer.toString(dccAddress),
                null, null, null, null);
        if (log.isDebugEnabled()) log.debug("selectLoco found "+l.size()+" matches");
        if (l.size() > 0) {
            re = l.get(0);
            re.addPropertyChangeListener(rosterEntryUpdateListener);
            updateDetails();
            JTable table = rtable.getTable();
            table.clearSelection();
          
            int entires = table.getRowCount();
            for (int i = 0; i<entires; i++){
                if(table.getValueAt(i, jmri.jmrit.roster.swing.RosterTableModel.IDCOL).equals(re.getId())){
                    table.addRowSelectionInterval(i,i);
                    table.scrollRectToVisible(new Rectangle(table.getCellRect(i, 0, true)));
                }
            }
        } else {
            log.warn("Read address "+dccAddress+", but no such loco in roster");  //"No roster entry found"
            JOptionPane.showMessageDialog(this, "No roster entry found", "Address " + dccAddress + " was read from the decoder\nbut has not been found in the Roster", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
            String filename) {
        String title = re.getId();
        Programmer pProg = null;
        JFrame progFrame=null;
        if(edit.isSelected())
            progFrame = new PaneProgFrame(decoderFile, re,
                                         title, "programmers"+File.separator+filename+".xml",
                                         null, false){
                protected JPanel getModePane() { return null; }
            };

        else if(service.isSelected()){
            progFrame = new PaneServiceProgFrame(decoderFile, re,
                                         title, "programmers"+File.separator+filename+".xml",
                                         modePanel.getProgrammer()){
            };
        }
        else if(ops.isSelected()){
            int address = Integer.parseInt(re.getDccAddress());
            boolean longAddr = re.isLongAddress();
            pProg = jmri.InstanceManager.programmerManagerInstance()
                                    .getAddressedProgrammer(longAddr, address);
            progFrame = new PaneOpsProgFrame(decoderFile, re, title, "programmers"+File.separator+filename+".xml",
                    pProg);
        }
        if(progFrame==null){
            return;
        }
        progFrame.pack();
        progFrame.setVisible(true);
    }

    boolean allowQuit = true;

    /**
     * For use when the DP3 window is called from another JMRI instance, set this to prevent the DP3 from shutting down
     * JMRI when the window is closed.
     */
    protected void allowQuit(boolean allowQuit){
        this.allowQuit=allowQuit;
        firePropertyChange("quit", "setEnabled", allowQuit);
        //if we are not allowing quit, ie opened from JMRI classic
        //then we must at least allow the window to be closed
        if(!allowQuit)
            firePropertyChange("closewindow", "setEnabled", true);
    }

    public void windowClosing(java.awt.event.WindowEvent e) {
        closeWindow(e);
    }
    
    void closeWindow(java.awt.event.WindowEvent e){
        p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideSummary", hideBottomPane);
        p.setSimplePreferenceState(DecoderPro3Window.class.getName() + ".hideGroups", hideGroups);
        p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideRosterImage",hideRosterImage);
        //Method to save table sort status
        int count = rtable.getModel().getColumnCount();
        for (int i = 0; i <count; i++){
            //This should probably store the sort with real names rather than numbers
            //But conversion back on the headers is a pain.
            p.setProperty(getWindowFrameRef(), "rosterOrder:"+rtable.getTable().getColumnName(i), rtable.getModel().getSortingStatus(i));
            p.setProperty(getWindowFrameRef(), "rosterWidth:"+rtable.getTable().getColumnName(i), rtable.getTable().getColumnModel().getColumn(i).getPreferredWidth());
        }
        
        
        if(rosterGroupSplitPane.getDividerLocation()>2){
            p.setProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation", rosterGroupSplitPane.getDividerLocation());
        }
        else if (groupSplitPaneLocation>2){
            p.setProperty(getWindowFrameRef(), "rosterGroupPaneDividerLocation", groupSplitPaneLocation);
        }

        if (allowQuit){
            handleQuit(e);
        } else {
            //As we are not the last window open or we are not allowed to quit the application then we will just close the current window
            openWindowInstances--;
            super.windowClosing(e);
            dispose();
            if((openWindowInstances==1) && (allowQuit))
                firePropertyChange("closewindow", "setEnabled", false);
        }
    }
    
    void handleQuit(java.awt.event.WindowEvent e){
        if(openWindowInstances>1){
            if (JOptionPane.showConfirmDialog(null,
                        rb.getString("MessageLongCloseWarning"),
                        rb.getString("MessageShortCloseWarning"),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    apps.AppsBase.handleQuit();
                }
        } else {
            apps.AppsBase.handleQuit();
        }
    }

    //Matches the first argument in the array against a locally know method
    public void remoteCalls(String args[]){
        args[0] = args[0].toLowerCase();

        if(args[0].equals("identifyloco")){
            startIdentifyLoco();
        } else if(args[0].equals("printloco")){
            if (checkIfEntrySelected()) printLoco(false);
        } else if(args[0].equals("printpreviewloco")){
             if (checkIfEntrySelected()) printLoco(true);
        } else if(args[0].equals("exportloco")){
             if (checkIfEntrySelected()) exportLoco();
        } else if(args[0].equals("basicprogrammer")){
             if (checkIfEntrySelected()) startProgrammer(null, re, programmer1);
        } else if(args[0].equals("comprehensiveprogrammer")){
             if (checkIfEntrySelected()) startProgrammer(null, re, programmer2);
        } else if(args[0].equals("editthrottlelabels")){
             if (checkIfEntrySelected()) startProgrammer(null, re, "dp3"+File.separator+"ThrottleLabels");
        } else if(args[0].equals("editrostermedia")){
             if (checkIfEntrySelected()) startProgrammer(null, re, "dp3"+File.separator+"MediaPane");
        } else if(args[0].equals("hiderosterimage")){
            hideRosterImage();
        } else if(args[0].equals("summarypane")){
            hideSummary();
        } else if(args[0].equals("copyloco")){
            if (checkIfEntrySelected()) copyLoco();
        } else if(args[0].equals("deleteloco")){
            if (checkIfEntrySelected()) deleteLoco();
        } else if(args[0].equals("setprogservice")){
            service.setSelected(true);
        } else if(args[0].equals("setprogops")){
            ops.setSelected(true);
        } else if(args[0].equals("setprogedit")){
            edit.setSelected(true);
        } else if (args[0].equals("groupspane")) {
            hideGroups();
        } else if (args[0].equals("quit")){
            handleQuit(null);
        } else if (args[0].equals("closewindow")){
            closeWindow(null);
        } else if (args[0].equals("newwindow")){
            newWindow();
        }
        else
            log.error ("method " + args[0] + " not found");
    }

    boolean checkIfEntrySelected(){
        if (re == null){
            JOptionPane.showMessageDialog(null, "Please select a loco from the roster first");
            return false;
        }
        return true;
    }

    /**
     * Identify loco button pressed, start the identify operation
     * This defines what happens when the identify is done.
     */
    //taken out of CombinedLocoSelPane
    protected void startIdentifyLoco() {
        if (jmri.InstanceManager.programmerManagerInstance()==null ||
                        !jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
            log.error("Identify loco called when no service mode programmer is available");
            JOptionPane.showMessageDialog(null, "Identify loco called when no service mode programmer is available");
            return;
        }
        // start identifying a loco
        final DecoderPro3Window me = this;
        IdentifyLoco ident = new IdentifyLoco() {
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
        ident.start();
    }

    protected void hideRosterImage(){
        hideRosterImage=!hideRosterImage;
        //p.setSimplePreferenceState(DecoderPro3Window.class.getName()+".hideRosterImage",hideRosterImage);
        if(hideRosterImage){
            locoImage.setVisible(false);
        } else {
            locoImage.setVisible(true);
        }
    }

    protected void exportLoco(){
        ExportRosterItem act = new ExportRosterItem("Export", this, re);
        act.actionPerformed(null);
    }

    static class ExportRosterItem extends jmri.jmrit.roster.ExportRosterItemAction{
        ExportRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            setExistingEntry(re);
        }
        @Override
        protected boolean selectFrom(){ return true;}
        }

    protected void copyLoco(){
        CopyRosterItem act = new CopyRosterItem("Copy", this, re);
        act.actionPerformed(null);
    }

    static class CopyRosterItem extends jmri.jmrit.roster.CopyRosterItemAction{
        CopyRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            setExistingEntry(re);
        }
        @Override
        protected boolean selectFrom(){ return true;}
        }

    protected void deleteLoco(){
        DeleteRosterItem act = new DeleteRosterItem("Delete", this, re);
        act.actionPerformed(null);
    }

    static class DeleteRosterItem extends jmri.jmrit.roster.DeleteRosterItemAction{
        DeleteRosterItem(String pName, Component pWho, RosterEntry re) {
            super(pName, pWho);
            this.re = re;
        }
        RosterEntry re;
        @Override
        protected String selectRosterEntry(){
            return re.getId();
        }
    }

    protected void newWindow(){
        DecoderPro3Action act = new DecoderPro3Action(getTitle(), allowQuit);
        act.actionPerformed(null);
        firePropertyChange("closewindow", "setEnabled", true);
    }
    protected void printLoco(boolean boo){
        PrintRosterEntry pre = new PrintRosterEntry(re, this, "programmers"+File.separator+programmer2+".xml");
        pre.printPanes(boo);

    }

    protected void hideSummary(){
        boolean boo =!hideBottomPane;
        hideBottomPane(boo);
    }
    
    Dimension summaryPaneDim = new Dimension(0, 170);
    
    protected void enableRosterGroupMenuItems(boolean enable){
        firePropertyChange("groupspane", "setEnabled", enable);
        firePropertyChange("grouptable", "setEnabled", enable);
        firePropertyChange("activegroup", "setEnabled", enable);
        firePropertyChange("deletegroup", "setEnabled", enable);
    }
    int groupSplitPaneLocation = 0;
    
    boolean hideGroups = false;
    protected void hideGroups() {
        boolean boo = !hideGroups;
        hideGroupsPane(boo);
    }
    
    public void hideGroupsPane(boolean hide) {
        if(hideGroups==hide){
            return;
        }
        hideGroups = hide;
        if (hide) {
            groupSplitPaneLocation = rosterGroupSplitPane.getDividerLocation();
            rosterGroupSplitPane.setDividerLocation(1);
            rosterGroupSplitPane.getLeftComponent().setMinimumSize(new Dimension());
            if (Roster.instance().getRosterGroupList().isEmpty()) {
                rosterGroupSplitPane.setOneTouchExpandable(false);
                rosterGroupSplitPane.setDividerSize(0);
            }
        } else {
            rosterGroupSplitPane.setDividerSize(UIManager.getInt("SplitPane.dividerSize"));
            rosterGroupSplitPane.setOneTouchExpandable(true);
            if (groupSplitPaneLocation >= 2) {
                rosterGroupSplitPane.setDividerLocation(groupSplitPaneLocation);
            } else {
                rosterGroupSplitPane.resetToPreferredSizes();
            }
        }
    }

    public Object getRemoteObject(String value){
        value=value.toLowerCase();
        if(value.equals("hidesummary")){
            return hideBottomPane;
        }
        return null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderPro3Window.class.getName());
}

/* @(#)DecoderPro3Window.java */

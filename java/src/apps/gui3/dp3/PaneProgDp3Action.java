// PaneProgAction.java

package apps.gui3.dp3;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.*;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriPanel;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import  jmri.jmrit.symbolicprog.tabbedframe.*;

import javax.swing.BoxLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;

/**
 * Swing action to create and register a
 * frame for selecting the information needed to
 * open a PaneProgFrame in service mode.
 * <P>
 * The name is a historical accident, and probably should have
 * included "ServiceMode" or something.
 * <P>
 * The resulting JFrame
 * is constructed on the fly here, and has no specific type.
 *
 * @see  jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgAction
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 17977 $
 */
public class PaneProgDp3Action 			extends jmri.util.swing.JmriAbstractAction {

    Object o1, o2, o3, o4;
    JLabel statusLabel;
    jmri.jmrit.progsupport.ProgModeSelector modePane = new jmri.jmrit.progsupport.ProgServiceModeComboBox();

    static final java.util.ResourceBundle rbt = jmri.jmrit.symbolicprog.SymbolicProgBundle.bundle();

    public PaneProgDp3Action(String s, jmri.util.swing.WindowInterface wi) {
    	super(s, wi);
        init();
    }
     
 	public PaneProgDp3Action(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
    	super(s, i, wi);
        init();
    }
    
    public PaneProgDp3Action() {
        this("DecoderPro service programmer");
    }

    public PaneProgDp3Action(String s) {
        super(s);
        init();

    }
    
    void init(){
        statusLabel = new JLabel(rbt.getString("StateIdle"));
    }
    
    JmriJFrame f;
    
    public void actionPerformed(ActionEvent e) {

        if (log.isDebugEnabled()) log.debug("Pane programmer requested");

        if(f==null){
            // create the initial frame that steers
            f = new JmriJFrame("Create New Loco"); //rbt.getString("FrameServiceProgrammerSetup")
            f.getContentPane().setLayout(new BorderLayout());
            // ensure status line is cleared on close so it is normal if re-opened
            f.addWindowListener(new WindowAdapter(){
                @Override
                public void windowClosing(WindowEvent we){
                    statusLabel.setText(rbt.getString("StateIdle"));
                    f.windowClosing(we);}});

            // add the Roster menu
            JMenuBar menuBar = new JMenuBar();
            JMenu j = new JMenu(rbt.getString("MenuFile"));
            j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(rbt.getString("MenuPrintDecoderDefinitions"), f, false));
            j.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(rbt.getString("MenuPrintPreviewDecoderDefinitions"), f, true));
            menuBar.add(j);
            menuBar.add(new jmri.jmrit.roster.swing.RosterMenu(rbt.getString("MenuRoster"), jmri.jmrit.roster.swing.RosterMenu.MAINMENU, f));
            f.setJMenuBar(menuBar);

            // new Loco on programming track
            JPanel pane1 = new CombinedLocoSelTreePane(statusLabel){
                @Override
                    protected void startProgrammer(DecoderFile decoderFile, RosterEntry re,
                                                    String filename) {
                        String title = java.text.MessageFormat.format(rbt.getString("FrameServiceProgrammerTitle"),
                                                            new Object[]{"new decoder"});
                        if (re!=null) title = java.text.MessageFormat.format(rbt.getString("FrameServiceProgrammerTitle"),
                                                            new Object[]{re.getId()});
                        JFrame p = new PaneServiceProgFrame(decoderFile, re,
                                                     title, "programmers"+File.separator+filename+".xml",
                                                     modePane.getProgrammer());
                        if(editModeProg.isSelected()){
                            p = new PaneProgFrame(decoderFile, re,
                                             title, "programmers"+File.separator+filename+".xml",
                                             null, false){
                                protected JPanel getModePane() { return null; }
                            };
                        }
                        p.pack();
                        p.setVisible(true);
                    }

                @Override
                    protected JPanel layoutRosterSelection() { return null; }
                    
                    JRadioButton serviceModeProg;
                    JRadioButton editModeProg;
                    
                    jmri.UserPreferencesManager p;
                    
                @Override
                    protected JPanel createProgrammerSelection(){
                        p=jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
                        serviceModeProg = new JRadioButton("<HTML>Service Mode<br>(programming track)</HTML>");
                        editModeProg = new JRadioButton("Edit Only");
                        JPanel pane3a = new JPanel();
                        pane3a.setLayout(new BoxLayout(pane3a, BoxLayout.Y_AXIS));
                        // create the programmer box
                        
                        ButtonGroup modeGroup = new ButtonGroup();
                        modeGroup.add(serviceModeProg);
                        modeGroup.add(editModeProg);
                        
                        JPanel progModePane = new JPanel();
                        progModePane.add(serviceModeProg);
                        progModePane.add(editModeProg);
                        serviceModeProg.setSelected(true);
                        
                        if (jmri.InstanceManager.programmerManagerInstance()==null ||
                            !jmri.InstanceManager.programmerManagerInstance().isGlobalProgrammerAvailable()){
                            editModeProg.setSelected(true);
                            serviceModeProg.setEnabled(false);
                            iddecoder.setVisible(false);
                            modePane.setVisible(false);
                        }
                        
                        serviceModeProg.addActionListener(new ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                if(serviceModeProg.isSelected())
                                    iddecoder.setVisible(true);
                            }
                        });
                        
                        editModeProg.addActionListener(new ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                if(editModeProg.isSelected())
                                    iddecoder.setVisible(false);
                            }
                        });
                        
                        JPanel progFormat = new JPanel();
                        progFormat.setLayout(new BoxLayout(progFormat, BoxLayout.X_AXIS));
                        progFormat.add(new JLabel(rbt.getString("ProgrammerFormat")));
                        progFormat.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
                        
                        programmerBox = new JComboBox();
                        programmerBox.addItem("Basic");
                        programmerBox.addItem("Comprehensive");
                        programmerBox.setSelectedIndex(0);
                        if(p.getComboBoxLastSelection(lastSelectedProgrammer)!=null)
                            programmerBox.setSelectedItem(p.getComboBoxLastSelection(lastSelectedProgrammer));
                        if (ProgDefault.getDefaultProgFile()!=null) programmerBox.setSelectedItem(ProgDefault.getDefaultProgFile());
                        progFormat.add(programmerBox);
                        go2 = new JButton(rbt.getString("OpenProgrammer"));
                        go2.addActionListener( new ActionListener() {
                                public void actionPerformed(java.awt.event.ActionEvent e) {
                                    if (log.isDebugEnabled()) log.debug("Open programmer pressed");
                                    openButton();
                                    p.addComboBoxLastSelection(lastSelectedProgrammer, (String) programmerBox.getSelectedItem());
                                }
                            });
                        go2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
                        go2.setEnabled(false);
                        go2.setToolTipText(rbt.getString("SELECT A LOCOMOTIVE OR DECODER TO ENABLE"));
                        pane3a.add(progModePane);
                        pane3a.add(progFormat);
                        pane3a.add(go2);
                        return pane3a;
                    }
                };

            // load primary frame
            JPanel topPanel = new JPanel();
            topPanel.add(modePane);
            topPanel.add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            f.getContentPane().add(topPanel, BorderLayout.NORTH);
            //f.getContentPane().add(modePane);
            //f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));

            pane1.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            f.getContentPane().add(pane1, BorderLayout.CENTER);
            //f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
            
            statusLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            f.getContentPane().add(statusLabel, BorderLayout.SOUTH);

            f.pack();
            if (log.isDebugEnabled()) log.debug("Tab-Programmer setup created");
            }
        f.setVisible(true);
    }
    
    String lastSelectedProgrammer = this.getClass().getName()+".SelectedProgrammer";

    // never invoked, because we overrode actionPerformed above
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PaneProgAction.class.getName());

}

/* @(#)PaneProgAction.java */


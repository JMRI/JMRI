package jmri.jmrit.roster.swing.rostergroup;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import jmri.jmrit.roster.swing.RosterGroupComboBox;

    /**
     * Swing action to create and register a Roster Group Table.
     * <P>
     * @author	Bob Jacobsen   Copyright (C) 2003
     * @author	Kevin Dickerson   Copyright (C) 2009
     * @version	$Revision$
     */

public class RosterGroupTableAction extends jmri.util.swing.JmriAbstractAction {


    public RosterGroupTableAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public RosterGroupTableAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */

   public RosterGroupTableAction(String s) {
        super(s);
        
    }
    public RosterGroupTableAction() { this(Bundle.getMessage("RosterGroupTable"));}
        
    RosterGroupTableModel m;
    RosterGroupTableFrame f;
    void createModel() {
    
        m = new RosterGroupTableModel();
    }

    public void actionPerformed() {
        // create the JTable model, with changes for specific NamedBean
        createModel();

        // create the frame
        f = new RosterGroupTableFrame(m, helpTarget()){
            /**
             * Include an "add" button
             */
            void extras() {
                final JComboBox selectCombo = new RosterGroupComboBox();
                selectCombo.insertItemAt("",0);
                selectCombo.setSelectedIndex(-1);
                JPanel p25 = new JPanel();
                p25.add(new JLabel(Bundle.getMessage("SelectRosterGroup")));
                p25.add(selectCombo);
                addToTopBox(p25);
                selectCombo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        comboSelected(e, selectCombo.getSelectedItem().toString());
                    }
                });
                selectCombo.setVisible(true);
                
            }
        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);
    }
    

    public void actionPerformed(ActionEvent e) {
        actionPerformed();
        // create the JTable model, with changes for specific NamedBean
        //createModel();
        //final Roster roster = Roster.instance();
        // create the frame
        //f = new RosterGroupTableFrame(m, helpTarget()){
            /**
             * Include an "save" button
             */
        /*    void extras() {
                
                final JComboBox selectCombo = roster.rosterGroupBox();
                //addToTopBox(selectCombo);
                selectCombo.insertItemAt("",0);
                selectCombo.setSelectedIndex(-1);
                JPanel p25 = new JPanel();
                p25.add(new JLabel("Select Roster Group:"));
                p25.add(selectCombo);
                addToTopBox(p25);
                selectCombo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        comboSelected(e, selectCombo.getSelectedItem().toString());
                    }
                });
            }
        };
        setTitle();
        addToFrame(f);
        f.pack();
        f.setVisible(true);*/
    }

    public void addToFrame(RosterGroupTableFrame f) {
    }
    
    void setTitle() {
        f.setTitle(Bundle.getMessage("RosterGroupTable"));
    }
    
    String helpTarget() {
        return "package.jmri.jmrit.roster.swing"; // NOI18N
    }
    
    void comboSelected(ActionEvent e, String group){
        jmri.jmrit.roster.Roster roster = jmri.jmrit.roster.Roster.instance();
        m.setGroup(roster.getRosterGroupPrefix()+group);
        m.fireTableDataChanged();
    
    }
    
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
}
// TurnoutTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.Light;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Swing action to create and register a
 * TurnoutTable GUI.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.14 $
 */

public class TurnoutTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param actionName
     */
    public TurnoutTableAction(String actionName) { 
	super(actionName);

        // disable ourself if there is no primary turnout manager available
        if (jmri.InstanceManager.turnoutManagerInstance()==null ||
            (((jmri.managers.AbstractProxyManager)jmri.InstanceManager
                                                 .turnoutManagerInstance())
                                                 .systemLetter()=='\0')) {
            setEnabled(false);
        }

    }

    public TurnoutTableAction() { this("Turnout Table");}

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of Turnouts
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.turnoutManagerInstance().getBySystemName(name).getKnownState();
                switch (val) {
                case Turnout.CLOSED: return rbean.getString("TurnoutStateClosed");
                case Turnout.THROWN: return rbean.getString("TurnoutStateThrown");
                case Turnout.UNKNOWN: return rbean.getString("BeanStateUnknown");
                case Turnout.INCONSISTENT: return rbean.getString("BeanStateInconsistent");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.turnoutManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.turnoutManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                int state = ((Turnout)t).getKnownState();
                if (state==Turnout.CLOSED) ((Turnout)t).setCommandedState(Turnout.THROWN);
                else ((Turnout)t).setCommandedState(Turnout.CLOSED);
            }
            public JButton configureButton() {
                return new JButton("Thrown");
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleTurnoutTable"));
    }
    JFrame addFrame = null;
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(rb.getString("LabelSystemName"));
    JLabel userNameLabel = new JLabel(rb.getString("LabelUserName"));

    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddTurnout"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            p.add(sysName);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            addFrame.getContentPane().add(p);

            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.pack();
        addFrame.show();
    }

    void okPressed(ActionEvent e) {
        String user = userName.getText();
        if (user.equals("")) user=null;
        // Test if bit already in use as a light
        String sName = sysName.getText();
        if (sName.charAt(1)=='T') {
            // probably standard format turnout system name
            String testSN = sName.substring(0,1)+"L"+sName.substring(2,sName.length());
            Light testLight = InstanceManager.lightManagerInstance().
                                        getBySystemName(testSN);
            if (testLight != null) {
                // Bit is already used as a Light
                log.error("Requested Turnout "+sName+" uses same bit as Light "+testSN);
                return;
            }
        }
        InstanceManager.turnoutManagerInstance().newTurnout(sName, user);
    }

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutTableAction.class.getName());
}

/* @(#)TurnoutTableAction.java */

// SignalHeadTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.Turnout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.5 $
 */

public class SignalHeadTableAction extends AbstractTableAction {

    public SignalHeadTableAction() {
        super();
    }
    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame.  Perhaps this should be changed?
     * @param s
     */
    public SignalHeadTableAction(String s) {
        super(s);
    }

    /**
     * Create the JTable DataModel, along with the changes
     * for the specific case of SignalHeads
     */
    void createModel() {
        m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.signalHeadManagerInstance().getBySystemName(name).getAppearance();
                switch (val) {
                case SignalHead.RED: return rbean.getString("SignalHeadStateRed");
                case SignalHead.YELLOW: return rbean.getString("SignalHeadStateYellow");
                case SignalHead.GREEN: return rbean.getString("SignalHeadStateGreen");
                case SignalHead.FLASHRED: return rbean.getString("SignalHeadStateFlashingRed");
                case SignalHead.FLASHYELLOW: return rbean.getString("SignalHeadStateFlashingYellow");
                case SignalHead.FLASHGREEN: return rbean.getString("SignalHeadStateFlashingGreen");
                case SignalHead.DARK: return rbean.getString("SignalHeadStateDark");
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.signalHeadManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.signalHeadManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead)t).getAppearance();
                int newState;
                switch (oldState) {
                case SignalHead.RED: newState = SignalHead.YELLOW; break;
                case SignalHead.YELLOW: newState = SignalHead.GREEN; break;
                case SignalHead.GREEN: newState = SignalHead.DARK; break;
                case SignalHead.FLASHRED: newState = SignalHead.DARK; break;
                case SignalHead.FLASHYELLOW: newState = SignalHead.DARK; break;
                case SignalHead.FLASHGREEN: newState = SignalHead.DARK; break;
                case SignalHead.DARK: newState = SignalHead.RED; break;
                default: newState = SignalHead.DARK; this.log.warn("Unexpected state "+oldState+" becomes DARK");break;
                }
               ((SignalHead)t).setAppearance(newState);
            }
            public JButton configureButton() {
                return new JButton("Yellow");
            }
        };
    }

    void setTitle() {
        f.setTitle(f.rb.getString("TitleSignalTable"));
    }

    JFrame addFrame = null;
    JComboBox typeBox;
    JTextField name = new JTextField(5);
    JTextField to1 = new JTextField(5);
    JTextField to2 = new JTextField(5);
    JTextField to3 = new JTextField(5);
    JLabel nameLabel = new JLabel("Name:");
    JLabel v1Label = new JLabel("Value1:");
    JLabel v2Label = new JLabel("Value2:");
    JLabel v3Label = new JLabel("Value3:");

    String SE8c4Aspect = "SE8c 4 aspect";
    String TripleTurnout = "Triple Turnout";
    void addPressed(ActionEvent e) {
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddSignal"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            addFrame.getContentPane().add(typeBox = new JComboBox(new String[]{
                SE8c4Aspect, TripleTurnout
            }));
            typeBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    typeChanged();
                }
            });
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(nameLabel);
            p.add(name);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v1Label);
            p.add(to1);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v2Label);
            p.add(to2);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(v3Label);
            p.add(to3);
            addFrame.getContentPane().add(p);
            JButton ok;
            addFrame.getContentPane().add(ok = new JButton(rb.getString("ButtonOK")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        typeBox.setSelectedIndex(1);  // force GUI status consistent
        addFrame.pack();
        addFrame.show();
    }

    void typeChanged() {
        if (SE8c4Aspect.equals(typeBox.getSelectedItem())) {
            nameLabel.setText("User name:");
            v1Label.setText("Turnout number");
            v2Label.setText("");to2.setVisible(false);
            v3Label.setText("");to3.setVisible(false);

        } else if (TripleTurnout.equals(typeBox.getSelectedItem())) {
            nameLabel.setText("System name:");
            v1Label.setText("Green turnout ID:");
            v2Label.setText("Yellow turnout ID:");to2.setVisible(true);
            v3Label.setText("Red Turnout ID:");to3.setVisible(true);
        } else log.error("Unexpected type in typeChanged: "+typeBox.getSelectedItem());
    }

    void okPressed(ActionEvent e) {
        SignalHead s;
        if (SE8c4Aspect.equals(typeBox.getSelectedItem())) {
            s = new jmri.jmrix.loconet.SE8cSignalHead(Integer.parseInt(to1.getText()),name.getText());
            InstanceManager.signalHeadManagerInstance().register(s);
        } else if (TripleTurnout.equals(typeBox.getSelectedItem())) {
            Turnout t1 = InstanceManager.turnoutManagerInstance().getTurnout(to1.getText());
            Turnout t2 = InstanceManager.turnoutManagerInstance().getTurnout(to2.getText());
            Turnout t3 = InstanceManager.turnoutManagerInstance().getTurnout(to3.getText());
            if (t1==null) log.warn("Could not find turnout "+to1.getText());
            if (t2==null) log.warn("Could not find turnout "+to2.getText());
            if (t3==null) log.warn("Could not find turnout "+to3.getText());
            if (t3==null || t2==null || t1==null) return;
            s = new jmri.TripleTurnoutSignalHead(name.getText(),t1, t2, t3);
            InstanceManager.signalHeadManagerInstance().register(s);
        } else log.error("Unexpected type: "+typeBox.getSelectedItem());
    }

    static final org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadTableAction.class.getName());
}
/* @(#)SignalHeadTableAction.java */

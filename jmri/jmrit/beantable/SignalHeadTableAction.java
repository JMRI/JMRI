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

import javax.swing.AbstractAction;
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
 * @version     $Revision: 1.4 $
 */

public class SignalHeadTableAction extends AbstractAction {

    public SignalHeadTableAction(String s) { super(s);}
    public SignalHeadTableAction() { this("SignalHead Table");}

    public void actionPerformed(ActionEvent e) {
        final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

        // create the model, with modifications for SignalHeads
        BeanTableDataModel m = new BeanTableDataModel() {
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
        // create the frame
        BeanTableFrame f = new BeanTableFrame(m){
            /**
             * Include an "add" button
             */
            void extras() {
                JButton addButton = new JButton(rb.getString("ButtonAdd"));
                this.getContentPane().add(addButton);
                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addPressed(e);
                    }
                });
            }
        };
        f.setTitle(f.rb.getString("TitleSignalTable"));
        f.show();
    }

    JFrame addFrame = null;
    JComboBox typeBox;
    JTextField name = new JTextField();
    JTextField to1 = new JTextField();
    JTextField to2 = new JTextField();
    JTextField to3 = new JTextField();

    String SE8c4Aspect = "SE8c 4 aspect";
    String TripleTurnout = "Triple Turnout";
    void addPressed(ActionEvent e) {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
        if (addFrame==null) {
            addFrame = new JFrame(rb.getString("TitleAddSignal"));
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            addFrame.getContentPane().add(typeBox = new JComboBox(new String[]{
                SE8c4Aspect, TripleTurnout
            }));
            JPanel p;
            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(new JLabel("Name:"));
            p.add(name);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(new JLabel("Value1:"));
            p.add(to1);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(new JLabel("Value2:"));
            p.add(to2);
            addFrame.getContentPane().add(p);

            p = new JPanel(); p.setLayout(new FlowLayout());
            p.add(new JLabel("Value3:"));
            p.add(to3);
            addFrame.getContentPane().add(p);
            JButton ok;
            addFrame.getContentPane().add(ok = new JButton("OK"));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });
        }
        addFrame.show();
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

// BoosterProgFrame.java

 package jmri.jmrix.nce.boosterprog;

import java.util.ResourceBundle;

import javax.swing.*;

import jmri.*;

/**
 * Frame for configuring a NCE booster
 *
 * @author		Bob Jacobsen   Copyright (C) 2004
 * @version             $Revision: 1.3 $
 */
public class BoosterProgFrame extends javax.swing.JFrame {
    JTextField start = new JTextField(6);
    JTextField length = new JTextField(12);

    JLabel status = new JLabel();
    
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.nce.boosterprog.BoosterProgBundle");

    public BoosterProgFrame() {
        super(ResourceBundle.getBundle("jmri.jmrix.nce.boosterprog.BoosterProgBundle").getString("TitleBoosterProg"));
        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // install items in GUI, one line at a time
        
        // box of entries
        
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        
        getContentPane().add(new JLabel(res.getString("Warn1")));
        getContentPane().add(new JLabel(res.getString("Warn2")));
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton b = new JButton(res.getString("ButtonSet"));
        p.add(new JLabel(res.getString("LabelStart")));
        start.setText("30");
        p.add(start);
        p.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setStartPushed();
            }
        });
        box.add(p);
        
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        b = new JButton(res.getString("ButtonSet"));
        p.add(new JLabel(res.getString("LabelDuration")));
        length.setText("420");
        p.add(length);
        p.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setDurationPushed();
            }
        });
        box.add(p);
        
        getContentPane().add(box);
        
        getContentPane().add(status);
        status.setText(res.getString("StatusOK"));
        
        pack();
    }

    static Programmer p = null;
    
    static void getProgrammer() {
        p = InstanceManager.programmerManagerInstance().
                            getOpsModeProgrammer(true, 0);
    }
    
    static void releaseProgrammer() {
        if (p!=null)
            InstanceManager.programmerManagerInstance().
                            releaseOpsModeProgrammer(p);
        p = null;
    }
    
    void setStartPushed() {
        getProgrammer();
        status.setText(res.getString("StatusProgramming"));
        int val = Integer.parseInt(start.getText());
        
        try {
           p.writeCV(255, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    status.setText(res.getString("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(res.getString("StatusError")+e);
        } finally { releaseProgrammer(); }
    }
    
    static public void setStart(int val) {
        getProgrammer();
        
        try {
           p.writeCV(255, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                }
            });
        } catch (ProgrammerException e) {
        } finally { releaseProgrammer(); }
    }
    
    static public void setDuration(final int val) {
        getProgrammer();
        
        try {
           p.writeCV(253, val/256, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {}
                    }
                    try {
                        p.writeCV(254, val % 256, new ProgListener() {
                            public void programmingOpReply(int value, int retval) {}
                        });
                    } catch (ProgrammerException e) {
                    } finally { releaseProgrammer(); }
                }
            });
        } catch (ProgrammerException e) {
            releaseProgrammer();
        } 
    }
    
    void setDurationPushed() {
        getProgrammer();
        status.setText(res.getString("StatusProgramming"));
        int val = Integer.parseInt(length.getText())/256;
        
        try {
           p.writeCV(253, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {}
                    }
                    durationPart2();
                }
            });
        } catch (ProgrammerException e) {
            status.setText(res.getString("StatusError")+e);
            releaseProgrammer();
        } 
    }
    
    void durationPart2() {
        status.setText(res.getString("StatusProgramming"));
        int val = Integer.parseInt(length.getText()) % 256;
        
        try {
           p.writeCV(254, val, new ProgListener() {
                public void programmingOpReply(int value, int retval) {
                    status.setText(res.getString("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(res.getString("StatusError")+e);
        } finally { releaseProgrammer(); }
    }
    
    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
    }
}

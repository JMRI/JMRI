package jmri.util.swing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;

import javax.annotation.Nonnull;
import javax.swing.*;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

import jmri.util.JmriJFrame;

/**
 * Utility Methods for Jemmy Tests.
 * 
 * @author Paul Bender Copyright (C) 2018
 */

public class JemmyUtil {
    static public void pressDialogButton(JmriJFrame f, String buttonName) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo, 1); // wait for the first dialog.
        NameComponentChooser bChooser = new NameComponentChooser(buttonName);
        JButtonOperator jbo = new JButtonOperator(jdo, bChooser);
        // Click button
        jbo.push();
    }
    
    static public void pressDialogButton(String dialogTitle, String buttonName) {
        JDialogOperator jdo = new JDialogOperator(dialogTitle); // wait for the first dialog.
        JButtonOperator jbo = new JButtonOperator(jdo, buttonName);
        // Click button
        jbo.push();
    }

    static public void pressDialogButton(JmriJFrame f, String dialogTitle, String buttonName) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo, dialogTitle); // wait for the first dialog.
        JButtonOperator jbo = new JButtonOperator(jdo, buttonName);
        // Click button
        jbo.push();
    }

    static public void enterClickAndLeave(JButton comp) {
        JButtonOperator jbo = new JButtonOperator(comp);
        jbo.push();
    }
    
    static public void enterClickAndLeaveThreadSafe(JButton comp) {
        // test can hang if button isn't enabled
        jmri.util.JUnitUtil.waitFor(() -> {
            return comp.isEnabled();
        }, "wait for button to be enabled");
        
        Thread t = new Thread(() -> {
            JButtonOperator jbo = new JButtonOperator(comp);
            jbo.push();
        });
        t.start();
    }

    static public void enterClickAndLeave(JCheckBox comp) {
        JCheckBoxOperator jbo = new JCheckBoxOperator(comp);
        jbo.doClick();
    }

    static public void enterClickAndLeave(JRadioButton comp) {
        JRadioButtonOperator jbo = new JRadioButtonOperator(comp);
        jbo.doClick();
    }

    static public void enterClickAndLeave(JToggleButton comp) {
        JToggleButtonOperator jtbo = new JToggleButtonOperator(comp);
        jtbo.doClick();
    }

    static public void pressButton(WindowOperator frame, String text) {
        JButtonOperator jbo = new JButtonOperator(frame, text);
        jbo.push();
    }
    
    static public void clickOnCellThreadSafe(JTableOperator tbl, int row, String columnName) {
        new Thread(() -> {
            tbl.clickOnCell(row, tbl.findColumn(columnName));
        }).start();
    }

    static public void confirmJOptionPane(WindowOperator wo, String title, String message, String buttonLabel) {
        // the previous version of this message verified the text string
        // if the dialog matched the passed message value.  We need to
        // determine how to do that using Jemmy.
        JDialogOperator jdo = new JDialogOperator(wo, title);
        JButtonOperator jbo = new JButtonOperator(jdo, buttonLabel);
        jbo.push();
    }

    /**
     * Create a Modal Dialog Operator Thread.
     * Button action is complete when Thread terminates.
     * @param dialogTitle The Dialog title
     * @param buttonText the text of the Button to press.
     * @return the Thread.
     */
    public static Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.push(); // push waits for the button action to complete.
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    static public JLabel getLabelWithText(String frameName, String text) {
        // Find window by name
        JmriJFrame frame = JmriJFrame.getFrame(frameName);

        // find label within that
        JLabel jl = JLabelOperator.findJLabel(frame,new ComponentChooser(){
               @Override
            public boolean checkComponent(Component comp){
                   if(comp == null){
                      return false;
                   } else if (comp instanceof JLabel ) {
                      return ((JLabel)comp).getText().equals(text);
                   } else {
                      return false;
                   }
               }
               @Override
            public String getDescription(){
                  return "find JLabel with text: " + text;
               }
        });
        return jl;
    }

    /**
     * Get a JLabelOperator for a Named JLabel.
     * @param jfo the JFrameOperator containing the JLabel.
     * @param name the name given to the JLabel
     * @return a new JLabelOperator.
     * Fails test if JLabel is not located.
     */
    public static JLabelOperator getLabelOperatorByName(@Nonnull JFrameOperator jfo, @Nonnull String name) {
        return new JLabelOperator(jfo,new ComponentChooser(){
            @Override
            public boolean checkComponent(Component comp){
                if(comp == null){
                    return false;
                } else if (comp instanceof JLabel ) {
                    return name.equals(comp.getName());
                } else {
                    return false;
                }
            }
            @Override
            public String getDescription(){
                return "find JLabel with Name: " + name;
            }
        });
    }

    /**
     * Get a JButtonOperator for a Named Button.
     * @param jfo the JFrameOperator containing the JButton.
     * @param name the name given to the JButton
     * @return a new JButtonOperator.
     * Fails test if JButton is not located.
     */
    public static JButtonOperator getButtonOperatorByName(@Nonnull JFrameOperator jfo, @Nonnull String name) {
        return new JButtonOperator(jfo,new ComponentChooser(){
            @Override
            public boolean checkComponent(Component comp){
                if(comp == null){
                    return false;
                } else if (comp instanceof JButton ) {
                    return name.equals(comp.getName());
                } else {
                    return false;
                }
            }
            @Override
            public String getDescription(){
                return "find JButton with Name: " + name;
            }
        });
    }

    /**
     * Get a JButtonOperator for a JButton with a specific Action Command.
     * @param jfo the JFrameOperator containing the JButton.
     * @param action  the ActionCommand of the JButton
     * @return a new JButtonOperator.
     * Fails test if JButton is not located.
     */
    public static JButtonOperator getButtonOperatorByActionComnmand(@Nonnull JFrameOperator jfo, @Nonnull String action ) {
        return new JButtonOperator(jfo,new ComponentChooser(){
            @Override
            public boolean checkComponent(Component comp){
                if(comp == null){
                    return false;
                } else if (comp instanceof JButton ) {
                    return action.equals(((JButton)comp).getActionCommand());
                } else {
                    return false;
                }
            }
            @Override
            public String getDescription(){
                return "find JButton with Action: " + action ;
            }
        });
    }

    /**
     * Wait for a specified {@link JmriJFrame} to become active.
     * @param f The non-null {@link JmriJFrame} to wait for and activate.
     * @throws AssertionError if the frame does not become active.
     */
    public static void waitFor( @Nonnull JmriJFrame f) {
        int count = 5;
        boolean active = false;
        f.requestFocus();
        while (!active && count > 0) {
            active = jmri.util.JUnitUtil.waitFor(() -> {
                return f.isActive();
            });
            count--;
            f.requestFocusInWindow();
        }
        assertTrue( f.isActive(), () -> "frame " + f.getTitle() +" should be active");
    }

}

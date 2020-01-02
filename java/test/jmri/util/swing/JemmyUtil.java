package jmri.util.swing;

import java.awt.Component;
import javax.swing.*;
import jmri.util.JmriJFrame;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Utility Methods for Jemmy Tests.
 *
 * @author Paul Bender Copyright (C) 2018
 */
public class JemmyUtil {

    /**
     * wait for a frame with the title and then request to close it
     *
     * @param frameTitle the title of the frame to wait for
     */
    public static void waitAndCloseFrame(String frameTitle) {
        waitAndCloseFrame(new JFrameOperator(frameTitle));
    }

    /**
     * wait for a frame with the title and then press button to close it
     *
     * @param frameTitle the title of the frame to wait for
     * @param buttonText the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(String frameTitle, String buttonText) {
        waitAndCloseFrame(new JFrameOperator(frameTitle), buttonText);
    }

    /**
     * wait for a frame with the title and then press button to close it
     *
     * @param jfo        the JFrameOperator
     * @param buttonText the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(JFrameOperator jfo, String buttonText) {
        new JButtonOperator(jfo, buttonText).pushNoBlock();
        jfo.waitClosed(); //make sure the frame closed
    }

    /**
     * wait for a frame and then request to close it
     *
     * @param jfo the JFrameOperator
     */
    public static void waitAndCloseFrame(JFrameOperator jfo) {
        jfo.requestClose();
        jfo.waitClosed(); //make sure the frame closed
    }

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

    static public void confirmJOptionPane(WindowOperator wo, String title, String message, String buttonLabel) {
        // the previous version of this message verified the text string
        // if the dialog matched the passed message value.  We need to
        // determine how to do that using Jemmy.
        JDialogOperator jdo = new JDialogOperator(wo, title);
        JButtonOperator jbo = new JButtonOperator(jdo, buttonLabel);
        jbo.push();
    }

    public static Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    /**
     * get the JLabel with text labelText on the named frame
     *
     * @param frameName
     * @param labelText
     * @return JLabel or null if not found
     */
    public static JLabel getLabelWithText(String frameName, String labelText) {
        // Find window by name
        JmriJFrame frame = JmriJFrame.getFrame(frameName);

        // find label within that
        JLabel jl = JLabelOperator.findJLabel(frame, new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                if (comp == null) {
                    return false;
                } else if (comp instanceof JLabel) {
                    return ((JLabel) comp).getText().equals(labelText);
                } else {
                    return false;
                }
            }

            public String getDescription() {
                return "find JLabel with text: " + labelText;
            }
        });
        return jl;
    }

    /**
     * wait for a dialog with the title and message and then press button to
     * close it
     *
     * @param dialogTitle the title of the dialog
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(String dialogTitle, String messageText, String buttonText) {
        waitAndCloseDialog(new JDialogOperator(dialogTitle), messageText, buttonText);
    }

    /**
     * wait for the message in the dialog and then press the named button to
     * close it
     *
     * @param jdo         the JDialogOperator
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(JDialogOperator jdo, String messageText, String buttonText) {
        waitForLabels(jdo, messageText);
        JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
        jbo.pushNoBlock();
        jdo.waitClosed(); //make sure the dialog closed
    }

    /**
     * wait for labels based on label text (delimited by /n's)
     *
     * @param jdo       the dialog operator these labels are in
     * @param labelText the label text to wait for
     */
    public static void waitForLabels(JDialogOperator jdo, String labelText) {
//        try {
        if ((labelText != null) && !labelText.isEmpty()) {
            //note: the JOptionPane.showMessageDialog method splits the message
            //by the new line character ('\n') and creates JLables for
            //each line... so that's what we have to match here
            String[] lines = labelText.split("\\n");
            for (String line : lines) {
                // if (debugFlag) {
                //      System.out.println("line: " + line);
                // }
                //wait for this label
                new JLabelOperator(jdo, line);
            }
        }
//        } catch (JemmyException ex) {
//            if (debugFlag) {
//                dumpToXML();
//                captureScreenshot();
//            }
//            throw ex;
//        }
    }

    /**
     * create a modal dialog operator thread
     *
     * @param dialogTitle the title for the dialog
     * @param messageText the message for the dialog
     * @param buttonText  the name of the button to press to dismiss
     * @return the thread that is waiting to dismiss this dialog
     */
    public static Thread createModalDialogOperatorThread(String dialogTitle, String messageText, String buttonText) {
        Thread t = new Thread(() -> {
//            try {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            waitForLabels(jdo, messageText);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
//            } catch (JemmyException ex) {
//                if (debugFlag) {
//                    dumpToXML();
//                    captureScreenshot();
//                }
//                throw ex;
//            }
        });
        t.setName(dialogTitle + " '" + messageText + "' Close Dialog Thread");
        t.start();
        return t;
    }

    /*
     * a component chooser that matches by ToolTipText
     */
    public static class ToolTipComponentChooser implements ComponentChooser {

        private String buttonTooltip;
        private Operator.StringComparator comparator = Operator.getDefaultStringComparator();

        public ToolTipComponentChooser(String buttonTooltip) {
            this.buttonTooltip = buttonTooltip;
        }

        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent) comp).getToolTipText(), buttonTooltip);
        }

        public String getDescription() {
            return "Component with tooltip \"" + buttonTooltip + "\".";
        }
    }
}

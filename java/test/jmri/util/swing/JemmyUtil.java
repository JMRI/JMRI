package jmri.util.swing;

import java.awt.Component;
import java.io.FileNotFoundException;
import javax.swing.*;
import jmri.util.JmriJFrame;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.*;

/**
 * Utility Methods for Jemmy Tests.
 *
 * @author Paul Bender Copyright (C) 2018
 * @author George Warner Copyright (C) 2019
 */
public class JemmyUtil {

    public static boolean debugFlag = false;

    public static void pressDialogButton(JmriJFrame f, String buttonLabel) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo, 1); // wait for the first dialog.
        NameComponentChooser bChooser = new NameComponentChooser(buttonLabel);
        JButtonOperator jbo = new JButtonOperator(jdo, bChooser);
        jbo.push();
    }

    public static void pressDialogButton(String dialogTitle, String buttonLabel) {
        JDialogOperator jdo = new JDialogOperator(dialogTitle); // wait for the first dialog.
        pressButton(jdo, buttonLabel);
    }

    public static void pressDialogButton(JmriJFrame f, String dialogTitle, String buttonLabel) {
        JFrameOperator jfo = new JFrameOperator(f);
        JDialogOperator jdo = new JDialogOperator(jfo, dialogTitle); // wait for the first dialog.
        pressButton(jdo, buttonLabel);
    }

    public static void enterClickAndLeave(JButton comp) {
        new JButtonOperator(comp).push();
    }

    public static void enterClickAndLeave(JCheckBox comp) {
        new JCheckBoxOperator(comp).push();
    }

    public static void enterClickAndLeave(JRadioButton comp) {
        new JRadioButtonOperator(comp).push();
    }

    public static void enterClickAndLeave(JToggleButton comp) {
        new JToggleButtonOperator(comp).push();
    }

    public static void pressButton(WindowOperator frame, String text) {
        new JButtonOperator(frame, text).push();
    }

    public static void confirmJOptionPane(WindowOperator wo, String dialogTitle, String message, String buttonLabel) {
        // the previous version of this message verified the text string
        // if the dialog matched the passed message value.  We need to
        // determine how to do that using Jemmy.
        //(hint: see note in waitForLabels method below)
        JDialogOperator jdo = new JDialogOperator(wo, dialogTitle);
        pressButton(jdo, buttonLabel);
    }

    public static Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            try {
                // constructor for jdo will wait until the dialog is visible
                JDialogOperator jdo = new JDialogOperator(dialogTitle);
                JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
                jbo.pushNoBlock();
            } catch (Exception ex) {
                if (debugFlag) {
                    dumpToXML();
                    captureScreenshot();
                }
                throw ex;
            }
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

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
     * @param dialogTitle the title of the dialog to wait for
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(String dialogTitle, String messageText, String buttonText) {
        waitAndCloseDialog(new JDialogOperator(dialogTitle), messageText, buttonText);
    }

    /**
     * wait for a dialog with the title and message and then press button to
     * close it
     *
     * @param dialogTitle the title of the dialog to wait for
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(JDialogOperator jdo, String messageText, String buttonText) {
        waitForLabels(jdo, messageText);
        JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
        jbo.pushNoBlock();
        jdo.waitClosed();    //make sure the dialog closed
    }

    /**
     * wait for a dialog with the title and then press button to close it
     *
     * @param dialogTitle the title of the dialog to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(String dialogTitle, String buttonText) {
        waitAndCloseDialog(new JDialogOperator(dialogTitle), buttonText);
    }

    /**
     * wait for a dialog with the title and then press button to close it
     *
     * @param dialogTitle the title of the dialog to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(JDialogOperator jdo, String buttonText) {
        JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
        jbo.pushNoBlock();
        jdo.waitClosed();    //make sure the dialog closed
    }

    /**
     * wait for a dialog with the title and then request close
     *
     * @param dialogTitle the title of the dialog to wait for
     */
    public static void waitAndCloseDialog(String dialogTitle) {
        waitAndCloseDialog(new JDialogOperator(dialogTitle));
    }

    /**
     * wait for a dialog with the title and then request close
     *
     * @param dialogTitle the title of the dialog to wait for
     */
    public static void waitAndCloseDialog(JDialogOperator jdo) {
        jdo.requestClose();
        jdo.waitClosed();    //make sure the dialog closed
    }

    /**
     * wait for labels based on label text (delimited by /n's)
     *
     * @param jdo       the dialog operator these labels are in
     * @param labelText the label text to wait for
     */
    public static void waitForLabels(JDialogOperator jdo, String labelText) {
        try {
            if ((labelText != null) && !labelText.isEmpty()) {
                //note: the JOptionPane.showMessageDialog method splits the message
                //by the new line character ('\n') and creates JLables for
                //each line... so that's what we have to match here
                String[] lines = labelText.split("\\n");
                for (String line : lines) {
                    //wait for this label
                    new JLabelOperator(jdo, line);
                }
            }
        } catch (Exception ex) {
            if (debugFlag) {
                dumpToXML();
                captureScreenshot();
            }
            throw ex;
        }
    }

    /**
     * wait for a frame with the title and message and then press button to
     * close it
     *
     * @param frameTitle  the title of the frame to wait for
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(String frameTitle, String messageText, String buttonText) {
        waitAndCloseFrame(new JFrameOperator(frameTitle), messageText, buttonText);
    }

    /**
     * wait for a frame with the title and message and then press button to
     * close it
     *
     * @param frameTitle  the title of the frame to wait for
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(JFrameOperator jfo, String messageText, String buttonText) {
        waitForLabels(jfo, messageText);
        JButtonOperator jbo = new JButtonOperator(jfo, buttonText);
        jbo.pushNoBlock();
        jfo.waitClosed();    //make sure the frame closed
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
     * @param frameTitle the title of the frame to wait for
     * @param buttonText the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(JFrameOperator jfo, String buttonText) {
        new JButtonOperator(jfo, buttonText).pushNoBlock();
        jfo.waitClosed();    //make sure the frame closed
    }

    /**
     * wait for a frame with the title and then request close
     *
     * @param frameTitle the title of the frame to wait for
     */
    public static void waitAndCloseFrame(String frameTitle) {
        waitAndCloseFrame(new JFrameOperator(frameTitle));
    }

    /**
     * wait for a frame with the title and message and then press button to
     * close it
     *
     * @param frameTitle  the title of the frame to wait for
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(JFrameOperator jfo) {
        jfo.requestClose();
        jfo.waitClosed();    //make sure the frame closed
    }

    /**
     * wait for labels based on label text (delimited by /n's)
     *
     * @param jfo       the frame operator these labels are in
     * @param labelText the label text to wait for
     */
    public static void waitForLabels(JFrameOperator jfo, String labelText) {
        if ((labelText != null) && !labelText.isEmpty()) {
            try {
                //note: the JOptionPane.showMessageDialog method splits the message
                //by the new line character ('\n') and creates JLables for
                //each line... so that's what we have to match here
                String[] lines = labelText.split("\\n");
                for (String line : lines) {
                    //wait for this label
                    new JLabelOperator(jfo, line);
                }
            } catch (Exception ex) {
                if (debugFlag) {
                    dumpToXML();
                    captureScreenshot();
                }
                throw ex;
            }
        }
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
            try {
                // constructor for jdo will wait until the dialog is visible
                JDialogOperator jdo = new JDialogOperator(dialogTitle);
                waitForLabels(jdo, messageText);
                JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
                jbo.pushNoBlock();
            } catch (Exception ex) {
                if (debugFlag) {
                    dumpToXML();
                    captureScreenshot();
                }
                throw ex;
            }
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

//TODO: finish this!
//WIP:delay for s seconds
//    private void waitSeconds(int s) {
//        //waits until queue has been empty for X milliseconds
//        //new QueueTool().waitEmpty(s * 1000);
//
//        //wait until no event is registered for a given number of milliseconds
//        new EventTool().waitNoEvent(s * 1000);
//    }
    /**
     * utility method to save screenshot to desktop file
     */
    public static void captureScreenshot() {
        //grab image
        PNGEncoder.captureScreen(System.getProperty("user.home")
                + System.getProperty("file.separator")
                + "Desktop"
                + System.getProperty("file.separator")
                + "JemmyScreenshot.png");
    }

    /**
     * utility method to save jemmy xml to desktop file
     */
    public static void dumpToXML() {
        //grab component state
        try {
            Dumper.dumpAll(System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + "Desktop"
                    + System.getProperty("file.separator")
                    + "JemmyDump.xml");
        } catch (FileNotFoundException e) {
        }
    }

}

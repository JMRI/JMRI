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

    public static boolean debugFlag = false;    //TODO: get rid of this for production

    /**
     * press the named button in the specified frame
     *
     * @param frame      the specified frame
     * @param buttonText the name of the button to press to dismiss
     */
    public static void pressDialogButton(JmriJFrame frame, String buttonText) {
        JFrameOperator jfo = new JFrameOperator(frame);
        JDialogOperator jdo = new JDialogOperator(jfo, 1); // wait for the first dialog.
        NameComponentChooser bChooser = new NameComponentChooser(buttonText);
        JButtonOperator jbo = new JButtonOperator(jdo, bChooser);
        jbo.push();
    }

    /**
     * press the named button in the named dialog
     *
     * @param dialogTitle the title of the dialog
     * @param buttonText  the name of the button to press to dismiss
     */
    public static void pressDialogButton(String dialogTitle, String buttonText) {
        JDialogOperator jdo = new JDialogOperator(dialogTitle); // wait for the first dialog.
        pressButton(jdo, buttonText);
    }

    /**
     * press the named button in the named dialog in the specified frame
     *
     * @param frame       the specified frame
     * @param dialogTitle the title of the dialog
     * @param buttonText  the name of the button to press to dismiss
     */
    public static void pressDialogButton(JmriJFrame frame, String dialogTitle, String buttonText) {
        JFrameOperator jfo = new JFrameOperator(frame);
        JDialogOperator jdo = new JDialogOperator(jfo, dialogTitle); // wait for the first dialog.
        pressButton(jdo, buttonText);
    }

    /**
     * push the JButton
     *
     * @param comp the JButton to push
     */
    public static void enterPushAndLeave(JButton comp) {
        new JButtonOperator(comp).push();
    }

    /**
     * push the JCheckBox
     *
     * @param comp the JCheckBox to push
     */
    public static void enterPushAndLeave(JCheckBox comp) {
        new JCheckBoxOperator(comp).push();
    }

    /**
     * click the JButton
     *
     * @param comp the JButton
     */
    public static void enterClickAndLeave(JButton comp) {
        new JButtonOperator(comp).doClick();
    }

    /**
     * click the JCheckBox
     *
     * @param comp the JCheckBox
     */
    public static void enterClickAndLeave(JCheckBox comp) {
        new JCheckBoxOperator(comp).doClick();
    }

    /**
     * click the JRadioButton
     *
     * @param comp the JRadioButton
     */
    public static void enterClickAndLeave(JRadioButton comp) {
        new JRadioButtonOperator(comp).doClick();
    }

    /**
     * click the JToggleButton
     *
     * @param comp the JToggleButton
     */
    public static void enterClickAndLeave(JToggleButton comp) {
        new JToggleButtonOperator(comp).doClick();
    }

    /**
     * press the named button in the specified frame
     *
     * @param wo         the window operator
     * @param buttonText the name of the button to press to dismiss
     */
    public static void pressButton(WindowOperator wo, String buttonText) {
        new JButtonOperator(wo, buttonText).push();
    }

    /**
     *
     * @param wo          the window operator
     * @param dialogTitle the title of the dialog
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void confirmJOptionPane(WindowOperator wo, String dialogTitle, String messageText, String buttonText) {
        JDialogOperator jdo = new JDialogOperator(wo, dialogTitle);
        waitForLabels(jdo, messageText);
        pressButton(jdo, buttonText);
    }

    /**
     * create a thread to wait for a dialog with title and then press named
     * button to close
     *
     * @param dialogTitle the title of the dialog
     * @param buttonText  the name of the button to press to dismiss this dialog
     * @return the thread
     */
    public static Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            try {
                // constructor for jdo will wait until the dialog is visible
                JDialogOperator jdo = new JDialogOperator(dialogTitle);
                JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
                jbo.pushNoBlock();
            } catch (JemmyException ex) {
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
     * wait for a dialog with the title and then press button to close it
     *
     * @param dialogTitle the title of the dialog
     * @param buttonText  the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(String dialogTitle, String buttonText) {
        waitAndCloseDialog(new JDialogOperator(dialogTitle), buttonText);
    }

    /**
     * wait for a dialog with the title and then press button to close it
     *
     * @param jdo        the JDialogOperator
     * @param buttonText the name of the button to press to dismiss this dialog
     */
    public static void waitAndCloseDialog(JDialogOperator jdo, String buttonText) {
        JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
        jbo.pushNoBlock();
        jdo.waitClosed(); //make sure the dialog closed
    }

    /**
     * wait for a dialog with the title and then request close
     *
     * @param dialogTitle the title of the dialog
     */
    public static void waitAndCloseDialog(String dialogTitle) {
        waitAndCloseDialog(new JDialogOperator(dialogTitle));
    }

    /**
     * wait for a dialog with the title and then request close
     *
     * @param jdo the JDialogOperator
     */
    public static void waitAndCloseDialog(JDialogOperator jdo) {
        jdo.requestClose();
        jdo.waitClosed(); //make sure the dialog closed
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
                    // if (debugFlag) {
                    //      System.out.println("line: " + line);
                    // }
                    //wait for this label
                    new JLabelOperator(jdo, line);
                }
            }
        } catch (JemmyException ex) {
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
     * @param buttonText  the name of the button to press to dismiss
     */
    public static void waitAndCloseFrame(String frameTitle, String messageText, String buttonText) {
        waitAndCloseFrame(new JFrameOperator(frameTitle), messageText, buttonText);
    }

    /**
     * wait for a frame and message and then press button to close it
     *
     * @param jfo         the JFrameOperator
     * @param messageText the message (JLable(s)) to wait for
     * @param buttonText  the name of the button to press to dismiss
     */
    public static void waitAndCloseFrame(JFrameOperator jfo, String messageText, String buttonText) {
        waitForLabels(jfo, messageText);
        JButtonOperator jbo = new JButtonOperator(jfo, buttonText);
        jbo.pushNoBlock();
        jfo.waitClosed(); //make sure the frame closed
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
     * @param jfo         the JFrameOperator
     * @param buttonText the name of the button to press to dismiss this frame
     */
    public static void waitAndCloseFrame(JFrameOperator jfo, String buttonText) {
        new JButtonOperator(jfo, buttonText).pushNoBlock();
        jfo.waitClosed(); //make sure the frame closed
    }

    /**
     * wait for a frame with the title and then request to close it
     *
     * @param frameTitle the title of the frame to wait for
     */
    public static void waitAndCloseFrame(String frameTitle) {
        waitAndCloseFrame(new JFrameOperator(frameTitle));
    }

    /**
     * wait for a frame and then request to close it
     *
     * @param jfo         the JFrameOperator
     */
    public static void waitAndCloseFrame(JFrameOperator jfo) {
        jfo.requestClose();
        jfo.waitClosed(); //make sure the frame closed
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
            } catch (JemmyException ex) {
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
            } catch (JemmyException ex) {
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

    /**
     * delay for N milliseconds
     *
     * @param milliseconds how log to delay
     */
    //REMOVED: use JUnitUtil.waitFor() instead (if at all!)

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

package jmri.jmrit.throttle;

import javax.swing.JButton;
import java.awt.event.*;

/**
 * A JButton to activate functions on the decoder. FunctionButtons
 * have a right-click popup menu with several configuration options:
 * <ul>
 * <li> Set the text
 * <li> Set an icon
 * <li>
 * </ul>
 */
public class FunctionButton extends JButton implements ActionListener
{
    private FunctionListener listener;
    private int identity;
    private boolean isOn;

    /**
     * Construct the FunctionButton.
     * @param id An integer identifier for the button, probably relates
     * to a Function number (i.e. F0, F1)
     * @param isOn The state of the function. True if the function is
     * activated.
     */
    public FunctionButton(int id, boolean isOn)
    {
        this.identity = id;
        this.isOn = isOn;
        this.addActionListener(this);
    }

    /**
     * Set the state of the function
     * @param isOn True if the function should be active.
     */
    public void setState(boolean isOn)
    {
        this.isOn = isOn;
    }

    /**
     * Button is clicked
     * @param e The ActionEvent causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        isOn = !isOn;
        if (listener != null)
        {
            listener.notifyFunctionStateChanged(identity, isOn);
        }
    }

    /**
     * Add a listener to this button, probably some sort of keypad panel.
     * @param l The FunctionListener that wants notifications via the
     * FunctionListener.notifyFunctionStateChanged.
     */
    public void setFunctionListener(FunctionListener l)
    {
        this.listener = l;
    }
}
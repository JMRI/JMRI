package jmri.jmrit.throttle;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;

import java.awt.event.*;

/**
 * A JButton to activate functions on the decoder. FunctionButtons
 * have a right-click popup menu with several configuration options:
 * <ul>
 * <li> Set the text
 * <li> Set the locking state
 * <li>
 * </ul>
 */
public class FunctionButton extends JButton implements ActionListener
{
    private FunctionListener listener;
    private int identity;
    private boolean isOn;
    private boolean isLockable = true;

    private JPopupMenu popup;
    private JMenuItem setTextItem;
    private JCheckBoxMenuItem changeLockStateItem;

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

        popup = new JPopupMenu();

        setTextItem = new JMenuItem("Change Text");
        setTextItem.addActionListener(this);
        popup.add(setTextItem);

        changeLockStateItem = new JCheckBoxMenuItem("Lockable");
        changeLockStateItem.addActionListener(this);
        changeLockStateItem.setSelected(true);
        popup.add(changeLockStateItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        this.addMouseListener(popupListener);
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
     * Set the locking state of the button
     * @param isLockable True if the a clicking and releasing the button
     * changes the function state. False if the state is changed
     * back when the button is released
     */
    public void setIsLockable(boolean isLockable)
    {
        this.isLockable = isLockable;
        changeLockStateItem.setSelected(isLockable);
    }

    /**
     * Get the locking state of the function
     * @return True if the a clicking and releasing the button
     * changes the function state. False if the state is changed
     * back when the button is released
     */
    public boolean getIsLockable()
    {
        return isLockable;
    }

    /**
     * Handle the selection from the popup menu.
     * @param e The ActionEvent causing the action.
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == setTextItem)
        {
            editButtonText();
        }
        else if (e.getSource() == changeLockStateItem)
        {
            this.setIsLockable(!isLockable);
            if (!getIsLockable())
            {
                isOn = false;
            }
        }
    }

    /**
     * Change the state of the function.
     * @parem newState The new state. True = Is on, False = Is off.
     */
    private void changeState(boolean newState)
    {
        isOn = newState;
        if (listener != null)
        {
            listener.notifyFunctionStateChanged(identity, isOn);
        }

    }

    /**
     * Present a dialog to allow the user to change the text
     * of the button.
     */
    private void editButtonText()
    {
        Object input = JOptionPane.showInputDialog(this, "Enter text for this button",
                                    "Change button text", JOptionPane.PLAIN_MESSAGE,
                                    null, null, this.getText());
        if (input != null)
        {
            this.setText(input.toString());
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

    /**
     * A PopupListener to handle mouse clicks and releases. Handles
     * the popup menu.
     */
    class PopupListener extends MouseAdapter
    {
        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mousePressed(MouseEvent e)
        {
            JButton button = (JButton)e.getSource();
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            /* Must check button mask since some platforms wait
            for mouse release to do popup. */
            else if (button.isEnabled() &&
                    ((e.getModifiers() & e.BUTTON1_MASK) > 0)
                     && !getIsLockable())
            {
                changeState(true);
            }
        }

        /**
         * If the event is the popup trigger, which is dependent on
         * the platform, present the popup menu. Otherwise change
         * the state of the function depending on the locking state
         * of the button.
         * @param e The MouseEvent causing the action.
         */
        public void mouseReleased(MouseEvent e)
        {
            JButton button = (JButton)e.getSource();
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            else if (button.isEnabled())
            {
                if (!getIsLockable())
                {
                    changeState(false);
                }
                else
                {
                    changeState(!isOn);
                }
            }
        }

    }

}
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
 * <li> Set an icon
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

    public void setIsLockable(boolean isLockable)
    {
        this.isLockable = isLockable;
        changeLockStateItem.setSelected(isLockable);
    }


    public boolean getIsLockable()
    {
        return isLockable;
    }

    /**
     * Button is clicked
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

    private void changeState(boolean newState)
    {
        isOn = newState;
        if (listener != null)
        {
            listener.notifyFunctionStateChanged(identity, isOn);
        }

    }

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


    class PopupListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            else if (((e.getModifiers() & e.BUTTON1_MASK) > 0)
                     && !getIsLockable())
            {
                changeState(true);
            }
        }

        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
            else if (!getIsLockable())
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
package jmri.jmrit.throttle;

import javax.swing.JButton;
import java.awt.event.*;

public class FunctionButton extends JButton implements ActionListener
{
    private FunctionListener listener;
    private int identity;
    private boolean isOn;

    public FunctionButton(int number, boolean isOn)
    {
        this.identity = number;
        this.isOn = isOn;
        this.addActionListener(this);
    }

    public void setState(boolean isOn)
    {
        this.isOn = isOn;
    }

    public void actionPerformed(ActionEvent e)
    {
        isOn = !isOn;
        if (listener != null)
        {
            listener.notifyFunctionStateChanged(identity, isOn);
        }
    }

    public void setFunctionListener(FunctionListener l)
    {
        this.listener = l;
    }
}
package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;

public class FunctionPanel extends JInternalFrame
{
    //private FunctionListener listener;
    private FunctionButton functionButton[];
    public static final int NUM_FUNCTION_BUTTONS = 10;

    public FunctionPanel()
    {
        initGUI();
    }

    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setEnabled(isEnabled);
        }
    }

    public void setFunctionStates(boolean [] states )
    {
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setState(states[i]);
        }

    }

    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new GridLayout(4, 3));
        functionButton = new FunctionButton[NUM_FUNCTION_BUTTONS];
        for (int i=0; i<NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i] = new FunctionButton(i, false);
            functionButton[i].setText("F"+String.valueOf(i));

            if (i > 0)
            {
                mainPanel.add(functionButton[i]);
            }
        }
        mainPanel.add(new JLabel(""));
        mainPanel.add(functionButton[0]);
    }

    public void setFunctionListener(FunctionListener l)
    {
        //this.listener = l;
        for (int i=0; i < NUM_FUNCTION_BUTTONS; i++)
        {
            functionButton[i].setFunctionListener(l);
        }
    }
}
package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;

public class FunctionPanel extends JInternalFrame
{
    //private FunctionListener listener;
    private FunctionButton functionButton[];

    public FunctionPanel()
    {
        initGUI();
    }

    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new GridLayout(4, 3));
        functionButton = new FunctionButton[10];
        for (int i=0; i<10; i++)
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
        for (int i=0; i < 10; i++)
        {
            functionButton[i].setFunctionListener(l);
        }
    }
}
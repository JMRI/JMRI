package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

public class ControlPanel extends JInternalFrame
{
    private ControlPanelListener listener;
    private JSlider speedSlider;
    private JRadioButton forwardButton, reverseButton;
    private JButton stopButton;
    private int speedIncrement;

    private static int MAX_SPEED = 127; //TODO: correct always?

    public ControlPanel()
    {
        speedSlider = new JSlider(0, MAX_SPEED);
        speedSlider.setValue(0);
        forwardButton = new JRadioButton("Forward");
        reverseButton = new JRadioButton("Reverse");

        initGUI();
    }

    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        forwardButton.setEnabled(isEnabled);
        reverseButton.setEnabled(isEnabled);
        stopButton.setEnabled(isEnabled);
        speedSlider.setEnabled(isEnabled);
    }

    public void setIsForward(boolean isForward)
    {
        forwardButton.setSelected(isForward);
        reverseButton.setSelected(!isForward);
    }

    public void setSpeedValues(int speedIncrement, int speed)
    {
        this.speedIncrement = speedIncrement;
        speedSlider.setValue(speed*speedIncrement);
    }

    private void initGUI()
    {
        JPanel mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.gridheight = 1;
        constraints.gridwidth = 1;
        constraints.ipadx = 0;
        constraints.ipady = 0;
        Insets insets = new Insets(2, 2, 2, 2);
        constraints.insets = insets;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        this.getContentPane().add(speedSlider, constraints);
        speedSlider.setOrientation(JSlider.VERTICAL);
        speedSlider.addChangeListener(
                new ChangeListener()
                {
                   public void stateChanged(ChangeEvent e)
                   {
                       if (listener != null)
                       {
                           listener.notifySpeedChanged(speedSlider.getValue());
                       }
                   }
                });

        ButtonGroup directionButtons = new ButtonGroup();
        directionButtons.add(forwardButton);
        directionButtons.add(reverseButton);
        constraints.gridy = 1;
        this.getContentPane().add(forwardButton, constraints);
        constraints.gridy = 2;
        this.getContentPane().add(reverseButton, constraints);
        forwardButton.addActionListener(
                new ActionListener()
                {
                   public void actionPerformed(ActionEvent e)
                   {
                       if (listener != null)
                       {
                           listener.notifyDirectionChanged(true);
                       }
                   }
                });
        reverseButton.addActionListener(
                 new ActionListener()
                 {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (listener != null)
                        {
                            listener.notifyDirectionChanged(false);
                        }
                    }
                 });

        stopButton = new JButton("STOP!");
        constraints.gridy = 3;
        this.getContentPane().add(stopButton, constraints);
        stopButton.addActionListener(
                 new ActionListener()
                 {
                    public void actionPerformed(ActionEvent e)
                    {
                        speedSlider.setValue(0);
                    }
                 });
     }

    public void setControlPanelListener(ControlPanelListener l)
    {
        this.listener = l;
    }
}
package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AddressPanel extends JInternalFrame
        implements ActionListener
{

    private AddressListener listener;
    private JTextField addressField;
    private String previousAddress = "";

    public AddressPanel()
    {
        initGUI();
    }

    public void setAddressListener(AddressListener l)
    {
        listener = l;
    }

    private void initGUI()
     {
         JPanel mainPanel = new JPanel();
         this.setContentPane(mainPanel);

         mainPanel.setLayout(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.CENTER;
         constraints.fill = GridBagConstraints.NONE;
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

         addressField = new JTextField();
         addressField.setColumns(4);
         addressField.setFont(new Font("", Font.PLAIN, 32));
         mainPanel.add(addressField, constraints);

         JButton setButton = new JButton("Set");
         constraints.gridx = 1;
         mainPanel.add(setButton, constraints);

         setButton.addActionListener(this);

     }

     public void actionPerformed(ActionEvent e)
     {
         try
         {
             Integer input = new Integer(addressField.getText());
             previousAddress = addressField.getText();
             if (listener != null)
             {
                 listener.notifyAddressChanged(input.intValue());
             }
         }
         catch (NumberFormatException ex)
         {
             addressField.setText(previousAddress);
         }

     }


}
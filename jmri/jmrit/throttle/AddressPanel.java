package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import org.jdom.Element;

public class AddressPanel extends JInternalFrame
{

    private ArrayList listeners;
    private JTextField addressField;
    private int previousAddress;
    private int currentAddress;

    /**
     * Constructor
     */
    public AddressPanel()
    {
        initGUI();
    }

    public void addAddressListener(AddressListener l)
    {
        if (listeners == null)
        {
            listeners = new ArrayList(2);
        }
        if (!listeners.contains(l))
        {
            listeners.add(l);
        }
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

         setButton.addActionListener(
                 new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 changeOfAddress();
             }
         });

    }

     public void changeOfAddress()
     {
         try
         {
             Integer value = new Integer(addressField.getText());
             previousAddress = currentAddress;
             currentAddress = value.intValue();
             if (currentAddress != previousAddress){
                 // send notification of new address
                 if (listeners != null)
                 {
                     for (int i=0; i<listeners.size(); i++)
                     {
                         AddressListener l = (AddressListener)listeners.get(i);
                         l.notifyAddressChanged(previousAddress, currentAddress);
                     }
                 }
             }
         }
         catch (NumberFormatException ex)
         {
             addressField.setText(String.valueOf(currentAddress));
         }

     }

     public Element getXml()
     {
         Element me = new Element("AddressPanel");
         Element window = new Element("window");
         WindowPreferences wp = new WindowPreferences();
         com.sun.java.util.collections.ArrayList children =
                 new com.sun.java.util.collections.ArrayList(1);
         children.add(wp.getPreferences(this));
         Element address = new Element("address");
         address.addAttribute("value", String.valueOf(addressField.getText()));
         children.add(address);
         me.setChildren(children);
         return me;
     }

     public void setXml(Element e)
     {
         Element window = e.getChild("window");
         WindowPreferences wp = new WindowPreferences();
         wp.setPreferences(this, window);

         Element addressElement = e.getChild("address");
         String address = addressElement.getAttribute("value").getValue();
         addressField.setText(address);
         changeOfAddress();

     }


}
package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.beans.*;

import jmri.DccThrottle;
import jmri.jmrit.roster.*;
import org.jdom.Element;

/**
 * A JInternalFrame that provides a way for the user to enter a
 * decoder address. This class also store AddressListeners and
 * notifies them when the user enters a new address.
 */
public class AddressPanel extends JInternalFrame
{

    private DccThrottle throttle;

    private ArrayList listeners;
    private JTextField addressField;
    private int currentAddress;

	private JButton releaseButton;
	private JButton dispatchButton;
	private JButton setButton;
	private JComboBox rosterBox;

    /** The longest 4 character string. Used for resizing. */
    private static final String LONGEST_STRING = "mmmm";

    /**
     * Constructor
     */
    public AddressPanel()
    {
        initGUI();
    }

	public void dipose()
	{
		if (throttle != null)
		{
			throttle.release();
		}
		super.dispose();
	}
	
    /**
     * Add an AddressListener. AddressListeners are notified when the
     * user selects a new address.
     * @param l
     */
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


    /**
     * Get notification that a throttle has been found as we requested.
     * @param t An instantiation of the DccThrottle with the address requested.
     */
    public void notifyThrottleFound(DccThrottle t)
    {
        this.throttle = t;
        releaseButton.setEnabled(true);
        dispatchButton.setEnabled(true);
		setButton.setEnabled(false);
		addressField.setEditable(false);
		rosterBox.setEnabled(false);
    }


	/**
	 * Receive notification that an address has been release or dispatched.
	 * @param address The address released/dispatched
	 */
	public void notifyThrottleDisposed()
	{
		dispatchButton.setEnabled(false);
		releaseButton.setEnabled(false);
		setButton.setEnabled(true);
		addressField.setEditable(true);
		rosterBox.setEnabled(true);
		throttle = null;
	}


    /**
     * Create, initialize and place the GUI objects.
     */
    private void initGUI()
     {
         JPanel mainPanel = new JPanel();
         this.setContentPane(mainPanel);
		 this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

         mainPanel.setLayout(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         constraints.anchor = GridBagConstraints.CENTER;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.insets = new Insets(2, 2, 2, 2);
         constraints.weightx = 1;
		 constraints.weighty = 0;
         constraints.gridx = 0;
         constraints.gridy = 0;

		 constraints.ipadx = constraints.ipady = -16;
         addressField = new JTextField();
         addressField.setColumns(4);
         addressField.setFont(new Font("", Font.PLAIN, 32));
         mainPanel.add(addressField, constraints);

         setButton = new JButton("Set");
         constraints.gridx = GridBagConstraints.RELATIVE;
         constraints.fill = GridBagConstraints.NONE;
         constraints.weightx = 0;
		 constraints.ipadx = constraints.ipady = 0;
         mainPanel.add(setButton, constraints);

         setButton.addActionListener(
                 new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 changeOfAddress();
             }
         });
		 
		 rosterBox = Roster.instance().matchingComboBox(null,null,null,null,null,null,null);
		 rosterBox.insertItemAt(new NullComboBoxItem(), 0);
		 rosterBox.setSelectedIndex(0);
		 rosterBox.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 rosterItemSelected();
             }
         });
		 constraints.gridx=0;
		 constraints.gridy= GridBagConstraints.RELATIVE;
		 constraints.fill = GridBagConstraints.HORIZONTAL;
		 constraints.weightx = 1;
		 constraints.weighty = 1;
		 mainPanel.add(rosterBox, constraints);
		 
		 JPanel buttonPanel = new JPanel();
		 buttonPanel.setLayout(new FlowLayout());
         dispatchButton = new JButton("Dispatch");
         buttonPanel.add(dispatchButton);
		 dispatchButton.setEnabled(false);
         dispatchButton.addActionListener(
                 new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 dispatchAddress();
             }
         });

         releaseButton = new JButton("Release");
         buttonPanel.add(releaseButton);
		 releaseButton.setEnabled(false);
         releaseButton.addActionListener(
                 new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 releaseAddress();
             }
         });
		 
		 constraints.gridx=0;
		 constraints.gridy=GridBagConstraints.RELATIVE;
		 constraints.gridwidth=2;
		 constraints.weighty = 0;
		 constraints.insets = new Insets(0,0,0,0);
		 mainPanel.add(buttonPanel, constraints);

         this.addComponentListener(
                 new ComponentAdapter()
         {
             public void componentResized(ComponentEvent e)
             {
                 changeFontSizes();
             }
         });
    }

	private void rosterItemSelected()
	{
		if (!(rosterBox.getSelectedItem() instanceof NullComboBoxItem))
		{
			String rosterEntryTitle = rosterBox.getSelectedItem().toString();
			RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
			addressField.setText(entry.getDccAddress());
			changeOfAddress();
		}
	}
	
    /**
     * A resizing has occurred, so determine the optimum font size
     * for the addressField.
     */
    private void changeFontSizes()
    {
        double fieldWidth = addressField.getSize().getWidth();
        int stringWidth = addressField.getFontMetrics(addressField.getFont()).
                          stringWidth(LONGEST_STRING);
        int fontSize = addressField.getFont().getSize();
        if (stringWidth > fieldWidth) // component has shrunk.
        {
            while (stringWidth > fieldWidth)
            {
                fontSize -= 2;
                Font f = new Font("", Font.PLAIN, fontSize);
                addressField.setFont(f);
                stringWidth = addressField.getFontMetrics(addressField.getFont()).
                              stringWidth(LONGEST_STRING);
            }
        }
        else // component has grown
        {
            while (fieldWidth - stringWidth > 10)
            {
                fontSize += 2;
                Font f = new Font("", Font.PLAIN, fontSize);
                addressField.setFont(f);
                stringWidth = addressField.getFontMetrics(addressField.getFont()).
                              stringWidth(LONGEST_STRING);
            }
        }

    }

    /**
     * The user has selected a new address. Notify all listeners.
     */
     public void changeOfAddress()
     {
         try
         {
             Integer value = new Integer(addressField.getText());
			 // send notification of new address
			 if (listeners != null)
			 {
				 for (int i=0; i<listeners.size(); i++)
				 {
					 AddressListener l = (AddressListener)listeners.get(i);
					 log.debug("Notify address listener "+l);
					 currentAddress = value.intValue();
					 l.notifyAddressChosen(currentAddress);
				 }
			 }
         }
         catch (NumberFormatException ex)
         {
             addressField.setText(String.valueOf(currentAddress));
         }
     }

	 /**
	  * Dispatch the current address for use by other throttles
	  */
	 private void dispatchAddress()
	 {
		throttle.dispatch();
		notifyListenersOfThrottleRelease();
	 }
	 
	 /**
	  * Release the current address.
	  */
	 private void releaseAddress()
	 {
		 throttle.release();
		 notifyListenersOfThrottleRelease();
	 }
	 
	 private void notifyListenersOfThrottleRelease()
	 {
		if (listeners != null)
		 {
			 for (int i=0; i<listeners.size(); i++)
			 {
				 AddressListener l = (AddressListener)listeners.get(i);
				 log.debug("Notify address listener "+l);
				 l.notifyAddressReleased(currentAddress);
			 }
		 }
	 }
	 
     /**
      * Create an Element of this object's preferences.
      * <ul>
      * <li> Window Preferences
      * <li> Address value
      * </ul>
      *
      * @return org.jdom.Element for this objects preferences. Defined
      * in DTD/throttle-config
      */
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

     /**
      * Use the Element passed to initialize based on user prefs.
      * @param The Element containing prefs as defined
      * in DTD/throttle-config
      */
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
	 
	 class NullComboBoxItem
	 {
		 public String toString()
		 {
			 return "<No Item Selected>";
		 }
	 }
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AddressPanel.class.getName());

}
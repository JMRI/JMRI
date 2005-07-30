package jmri.jmrit.throttle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.sun.java.util.collections.*;
import java.beans.*;
import javax.swing.BoxLayout;

import jmri.DccThrottle;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.roster.*;
import jmri.jmrit.DccLocoAddressSelector;
import org.jdom.Element;

/**
 * A JInternalFrame that provides a way for the user to enter a
 * decoder address. This class also store AddressListeners and
 * notifies them when the user enters a new address.
 *
 * @author     glen   Copyright (C) 2002
 * @version    $Revision: 1.24 $
 */
public class AddressPanel extends JInternalFrame
{

    private DccThrottle throttle;

    private DccLocoAddressSelector addrSelector = new DccLocoAddressSelector();
    private DccLocoAddress         currentAddress;
    private ArrayList listeners;

	private JButton releaseButton;
	private JButton dispatchButton;
	private JButton setButton;
	private JComboBox rosterBox;

    /**
     * Constructor
     */
    public AddressPanel()
    {
        initGUI();
    }

	public void destroy()
	{
		if (throttle != null)
		{
			throttle.release();
		}
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
     	currentAddress= (DccLocoAddress)t.getLocoAddress();
	    addrSelector.setAddress(currentAddress);

    	if(InstanceManager.throttleManagerInstance()
			       .hasDispatchFunction()) {
        	dispatchButton.setEnabled(true);
    	}

		setButton.setEnabled(false);
		addrSelector.setEnabled(false);
		rosterBox.setEnabled(false);
    }


	/**
	 * Receive notification that an address has been release or dispatched.
	 */
	public void notifyThrottleDisposed()
	{
		dispatchButton.setEnabled(false);
		releaseButton.setEnabled(false);
		setButton.setEnabled(true);
		addrSelector.setEnabled(true);
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
		 addrSelector.setVariableSize(true);
         mainPanel.add(addrSelector.getCombinedJPanel(), constraints);

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

		 rosterBox = Roster.instance().fullRosterComboBox();
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
		 constraints.weighty = 0;
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
         
         pack();
    }

	private void rosterItemSelected()
	{
		if (!(rosterBox.getSelectedItem() instanceof NullComboBoxItem))
		{
			String rosterEntryTitle = rosterBox.getSelectedItem().toString();
			RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
			
			addrSelector.setAddress(entry.getDccLocoAddress());
			
			changeOfAddress();
		}
	}


    /**
     * The user has selected a new address. Notify all listeners.
     */
     public void changeOfAddress()
     {
	    // send notification of new address
	    if (listeners != null) {
	        for (int i=0; i<listeners.size(); i++) {
                AddressListener l = (AddressListener)listeners.get(i);
                log.debug("Notify address listener "+l);
				currentAddress = addrSelector.getAddress();
			    l.notifyAddressChosen(currentAddress.getNumber(), currentAddress.isLongAddress());
            }
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
				 l.notifyAddressReleased(currentAddress.getNumber(), currentAddress.isLongAddress());
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
         children.add((new jmri.configurexml.LocoAddressXml()).store(addrSelector.getAddress()));
         me.setChildren(children);
         return me;
     }

     /**
      * Use the Element passed to initialize based on user prefs.
      * @param e The Element containing prefs as defined
      * in DTD/throttle-config
      */
     public void setXml(Element e)
     {
         Element window = e.getChild("window");
         WindowPreferences wp = new WindowPreferences();
         wp.setPreferences(this, window);

         Element addressElement = e.getChild("address");
         if (addressElement != null) {
            String address = addressElement.getAttribute("value").getValue();
            addrSelector.setAddress(new DccLocoAddress(Integer.parseInt(address), false));  // guess at the short/long
            changeOfAddress();
         }
         addressElement = e.getChild("locoaddress");
         if (addressElement != null) {
            addrSelector.setAddress((DccLocoAddress)(new jmri.configurexml.LocoAddressXml()).getAddress(addressElement));
            changeOfAddress();
         }
         
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

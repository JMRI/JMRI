// CarAttributeEditFrame.java

 package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.OperationsFrame;

/**
 * Frame for adding and editing the car roster for operations.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version             $Revision: 1.19 $
 */
public class CarAttributeEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener{
	
	final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	
	// labels
	JLabel textAttribute = new JLabel();
	JLabel textSep = new JLabel();
	JLabel quanity = new JLabel("0");

	// major buttons
	JButton addButton = new JButton();
	JButton deleteButton = new JButton();
	JButton replaceButton = new JButton();
	
	// combo box
	JComboBox comboBox;
	
	// text box
	JTextField addTextBox = new JTextField(10);
	
    public CarAttributeEditFrame() {}
    
    String _comboboxName;		// track which combo box is being edited
    
    public void initComponents(String comboboxName) {
    	
    	getContentPane().removeAll();
     	
        setTitle(MessageFormat.format(rb.getString("TitleCarEditAtrribute"),new Object[]{comboboxName}));
        
        // track which combo box is being edited 
        _comboboxName = comboboxName;
        loadCombobox();
        
        // general GUI config
        getContentPane().setLayout(new GridBagLayout());
        
        textAttribute.setText(comboboxName);

		addButton.setText(rb.getString("Add"));
		addButton.setVisible(true);
        deleteButton.setText(rb.getString("Delete"));
		deleteButton.setVisible(true);
        replaceButton.setText(rb.getString("Replace"));
        replaceButton.setVisible(true);
        
        quanity.setVisible(showQuanity);
        
		// row 1
		addItem(textAttribute,2,1);
		// row 2
		addItem(addTextBox, 2, 2);
        addItem(addButton, 3, 2);
        
        // row 3
        addItem(quanity, 1, 3);
        addItem(comboBox, 2, 3);
        addItem(deleteButton, 3, 3);
        
        // row 4 
        addItem(replaceButton, 3, 4);
        
		addButtonAction(addButton);
        addButtonAction(deleteButton);
		addButtonAction(replaceButton);
		
		addComboBoxAction(comboBox);
		manager.addPropertyChangeListener(this);
 
 		// build menu
		JMenuBar menuBar = new JMenuBar();
		JMenu toolMenu = new JMenu("Tools");
		toolMenu.add(new CarAttributeAction(rb.getString("CarQuanity"), this));
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);
		
    	pack();
    	if ((getWidth()<150)) 
    		setSize(200, getHeight()+10);
    	else
    		setSize(getWidth()+50, getHeight()+10);
    	setVisible(true);
    }
 
	// add or delete button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("edit frame button actived");
		if (ae.getSource() == addButton){
			String addItem = addTextBox.getText();
			if (addItem.equals(""))
				return;
			if (addItem.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotAdd"),new Object[]{_comboboxName}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			addItemToCombobox (addItem);
		}
		if (ae.getSource() == deleteButton){
			String deleteItem = (String)comboBox.getSelectedItem();
			deleteItemFromCombobox (deleteItem);
		}
		if (ae.getSource() == replaceButton){
			String newItem = addTextBox.getText();
			if (newItem.equals(""))
				return;
			if (newItem.length() > Control.MAX_LEN_STRING_ATTRIBUTE){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carAttribute"),new Object[]{Control.MAX_LEN_STRING_ATTRIBUTE}),
						MessageFormat.format(rb.getString("canNotReplace"),new Object[]{_comboboxName}),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String oldItem = (String) comboBox.getSelectedItem();
			if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(rb.getString("replaceMsg"),new Object[]{oldItem, newItem}),
						rb.getString("replaceAll"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
			if (newItem.equals(oldItem))
				return;
			addItemToCombobox (newItem);
			replaceItem(oldItem, newItem);
			deleteItemFromCombobox (oldItem);
		}
	}
	
	protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("Combo box action");
		updateCarQuanity();
	}

	private void deleteItemFromCombobox (String deleteItem){
		if(_comboboxName == CarEditFrame.ROAD){
			// purge train and locations by using replace
			CarRoads.instance().replaceName(deleteItem, null);
		}
		if(_comboboxName == CarEditFrame.TYPE){
			CarTypes.instance().deleteName(deleteItem);
		}
		if(_comboboxName == CarEditFrame.COLOR){
			CarColors.instance().deleteName(deleteItem);
		}
		if(_comboboxName == CarEditFrame.LENGTH){
			CarLengths.instance().deleteName(deleteItem);
		}
		if(_comboboxName == CarEditFrame.OWNER){
			CarOwners.instance().deleteName(deleteItem);
		}
		if(_comboboxName == CarEditFrame.KERNEL){
			manager.deleteKernel(deleteItem);
		}
	}
	
	private void addItemToCombobox (String addItem){
		if(_comboboxName == CarEditFrame.ROAD){
			CarRoads.instance().addName(addItem);
		}
		if(_comboboxName == CarEditFrame.TYPE){
			CarTypes.instance().addName(addItem);
		}
		if(_comboboxName == CarEditFrame.COLOR){
			CarColors.instance().addName(addItem);
		}
		if(_comboboxName == CarEditFrame.LENGTH){
			// convert from inches to feet if needed
			if (addItem.endsWith("\"")){
				addItem = addItem.substring(0, addItem.length()-1);
				try {
					double inches = Double.parseDouble(addItem);
					int feet = (int)(inches * Setup.getScaleRatio() / 12);
					addItem = Integer.toString(feet);
				} catch (NumberFormatException e){
					log.error("can not convert from inches to feet");
					JOptionPane.showMessageDialog(this,
							rb.getString("CanNotConvertFeet"), rb.getString("ErrorCarLength"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (addItem.endsWith("cm")){
				addItem = addItem.substring(0, addItem.length()-2);
				try {
					double cm = Double.parseDouble(addItem);
					int meter = (int)(cm * Setup.getScaleRatio() / 100);
					addItem = Integer.toString(meter);
				} catch (NumberFormatException e){
					log.error("Can not convert from cm to meters");
					JOptionPane.showMessageDialog(this,
							rb.getString("CanNotConvertMeter"), rb.getString("ErrorCarLength"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			// confirm that length is a number and less than 10000 feet
			try {
				int carLength = Integer.parseInt(addItem);
				if (carLength > 9999){
					log.error("car length must be less than 10,000 feet");
					JOptionPane.showMessageDialog(this,rb.getString("carAttribute5"),
							MessageFormat.format(rb.getString("canNotAdd"),new Object[]{_comboboxName}),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (NumberFormatException e){
				log.error("length not an integer");
				return;
			}
			CarLengths.instance().addName(addItem);
			comboBox.setSelectedItem(addItem);
		}
		if(_comboboxName == CarEditFrame.KERNEL){
			manager.newKernel(addItem);
		}
		if(_comboboxName == CarEditFrame.OWNER){
			CarOwners.instance().addName(addItem);
		}
	}
	
	private void replaceItem (String oldItem, String newItem){
		List<String> cars = manager.getCarsByNumberList();
		for (int i=0; i<cars.size(); i++){
			Car car = manager.getCarById(cars.get(i));

			if(_comboboxName == CarEditFrame.ROAD){
				if (car.getRoad().equals(oldItem))
					car.setRoad(newItem);
			}
			if(_comboboxName == CarEditFrame.TYPE){
				if (car.getType().equals(oldItem))
					car.setType(newItem);
			}
			if(_comboboxName == CarEditFrame.COLOR){
				if (car.getColor().equals(oldItem))
					car.setColor(newItem);
			}
			if(_comboboxName == CarEditFrame.LENGTH){
				if (car.getLength().equals(oldItem))
					car.setLength(newItem);
			}
			if(_comboboxName == CarEditFrame.OWNER){
				if (car.getOwner().equals(oldItem))
					car.setOwner(newItem);
			}
			if(_comboboxName == CarEditFrame.KERNEL){
				if (car.getKernelName().equals(oldItem)){
					Kernel kernel = manager.newKernel(newItem);
					car.setKernel(kernel);
				}
			}
		}
		//	now adjust locations and trains
		if(_comboboxName == CarEditFrame.TYPE){
			CarTypes.instance().replaceName(oldItem, newItem);
		}
		if(_comboboxName == CarEditFrame.ROAD){
			CarRoads.instance().replaceName(oldItem, newItem);
		}
	}
	
	private void loadCombobox(){ 
		if(_comboboxName == CarEditFrame.ROAD){
			comboBox = CarRoads.instance().getComboBox();
			CarRoads.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == CarEditFrame.TYPE){
			comboBox = CarTypes.instance().getComboBox();
			CarTypes.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == CarEditFrame.COLOR){
			comboBox = CarColors.instance().getComboBox();
			CarColors.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == CarEditFrame.LENGTH){
			comboBox = CarLengths.instance().getComboBox();
			CarLengths.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == CarEditFrame.OWNER){
			comboBox = CarOwners.instance().getComboBox();
			CarOwners.instance().addPropertyChangeListener(this);
		}
		if(_comboboxName == CarEditFrame.KERNEL){
			comboBox = manager.getKernelComboBox();
		}
	}
	
	boolean showQuanity = false;
	public void toggleShowQuanity(){
		if (showQuanity)
			showQuanity = false;		
		else
			showQuanity = true;
		quanity.setVisible(showQuanity);
		updateCarQuanity();
	}
	
	private void updateCarQuanity(){
		if(!showQuanity)
			return;
		int number = 0;
		String item = (String)comboBox.getSelectedItem();
		List<String> cars = manager.getCarsByIdList();
		for (int i=0; i<cars.size(); i++){
			Car car = manager.getCarById(cars.get(i));

			if(_comboboxName == CarEditFrame.ROAD){
				if (car.getRoad().equals(item))
					number++;
			}
			if(_comboboxName == CarEditFrame.TYPE){
				if (car.getType().equals(item))
					number++;
			}
			if(_comboboxName == CarEditFrame.COLOR){
				if (car.getColor().equals(item))
					number++;
			}
			if(_comboboxName == CarEditFrame.LENGTH){
				if (car.getLength().equals(item))
					number++;
			}
			if(_comboboxName == CarEditFrame.OWNER){
				if (car.getOwner().equals(item))
					number++;
			}
			if(_comboboxName == CarEditFrame.KERNEL){
				if (car.getKernelName().equals(item))
					number++;
			}
		}
		quanity.setText(Integer.toString(number));
	}

    public void dispose() {
    	CarRoads.instance().removePropertyChangeListener(this);
    	CarTypes.instance().removePropertyChangeListener(this);
		CarColors.instance().removePropertyChangeListener(this);
		CarLengths.instance().removePropertyChangeListener(this);
    	CarOwners.instance().removePropertyChangeListener(this);
    	manager.removePropertyChangeListener(this);
		firePcs ("dispose", null, _comboboxName);
        super.dispose();
    }
    
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("CarsAttributeFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(CarRoads.CARROADS_LENGTH_CHANGED_PROPERTY))
			CarRoads.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarTypes.CARTYPES_LENGTH_CHANGED_PROPERTY))
			CarTypes.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY))
			CarColors.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY))
			CarLengths.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY))
			CarOwners.instance().updateComboBox(comboBox);
		if (e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY))
			manager.updateKernelComboBox(comboBox);
		if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY))
			updateCarQuanity();
	}
	
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	//	note firePropertyChange occurs during frame creation
	private void firePcs(String p, Object old, Object n) {
		log.debug("CarAttribute firePropertyChange " + p +" " );
		pcs.firePropertyChange(p, old, n);
	}
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarAttributeEditFrame.class.getName());
}

final class CarAttributeAction extends AbstractAction {	
    public CarAttributeAction(String actionName, CarAttributeEditFrame caef) {
        super(actionName);
        this.caef = caef;
    }
    
    CarAttributeEditFrame caef;
    
    public void actionPerformed(ActionEvent ae) {
    	log.debug("Show attribute quanity");
    	caef.toggleShowQuanity();
    }
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarAttributeEditFrame.class.getName());
}

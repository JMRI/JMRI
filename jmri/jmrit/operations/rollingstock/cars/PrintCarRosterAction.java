// PrintCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;

import java.util.List;
import java.util.ResourceBundle;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * MacIntosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version     $Revision: 1.15 $
 */
public class PrintCarRosterAction  extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();

    public PrintCarRosterAction(String actionName, Frame frame, boolean preview, Component pWho) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
        panel = (CarsTableFrame)pWho;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    CarsTableFrame panel;
    CarPrintOptionFrame cpof = null;

    public void actionPerformed(ActionEvent e) {
    	if (cpof == null)
    		cpof = new CarPrintOptionFrame(this);
    	else
    		cpof.setVisible(true);
    	cpof.initComponents();
    }
    
    private static final int numberCharPerLine = 90;
    int ownerMaxLen = 5;
    
    private void printCars(){
 
        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, rb.getString("TitleCarRoster"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        
        // Loop through the Roster, printing as needed

        String location;
        String number;
        String road;
        String type;
        String length = "";
        String weight = "";
        String color = "";
        String owner = "";
        String built = "";
        String load = "";
        String kernel = "";
        
        boolean printOnly = printCarsWithLocation.isSelected();
        boolean printLength = printCarLength.isSelected();
        boolean printWeight = printCarWeight.isSelected();
        boolean printColor = printCarColor.isSelected();
        boolean printOwner = printCarOwner.isSelected();
        boolean printBuilt = printCarBuilt.isSelected();
        boolean printLoad = printCarLoad.isSelected();
        boolean printKernel = printCarKernel.isSelected();
        boolean space = printSpace.isSelected();
        boolean page = printPage.isSelected();
        

		ownerMaxLen = 5;
		if (!printLoad && !printKernel && !printColor)
			ownerMaxLen = Control.MAX_LEN_STRING_ATTRIBUTE;
        
        List<String> cars = panel.getSortByList();
        try {
        	printTitleLine(writer);
        	String previousLocation = "";
        	for (int i=0; i<cars.size(); i++){
        		Car car = manager.getById(cars.get(i));

        		location = "";     		
        		if (!car.getLocationName().equals("")){
        			location = car.getLocationName().trim() + " - " + car.getTrackName().trim();
        		} else if (printOnly)
        			continue;	// car doesn't have a location skip
        		
        		// Page break between locations?
        		if (!previousLocation.equals("") && !car.getLocationName().trim().equals(previousLocation) && page){
        			writer.pageBreak();
        			printTitleLine(writer);
        		}
           		// Add a line between locations?
        		else if (!previousLocation.equals("") && !car.getLocationName().trim().equals(previousLocation) && space){
        			writer.write(newLine);
        		}
        		previousLocation = car.getLocationName().trim();
        		
        		// car number
        		number = padAttribute(car.getNumber().trim(), 7);     		
        		// car road
        		road = padAttribute(car.getRoad().trim(), 7);       		
        		// car type
        		type = padAttribute(car.getType().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);
 
        		if (printLength)
        			length = padAttribute(car.getLength().trim(), Control.MAX_LEN_STRING_LENGTH_NAME);
        		if (printWeight)
        			weight = padAttribute(car.getWeight().trim(), Control.MAX_LEN_STRING_WEIGHT_NAME);
        		if (printColor)
        			color = padAttribute(car.getColor().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);        		
           		if (printLoad)
           			load = padAttribute(car.getLoad().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);          		
           		if (printKernel)
           			kernel = padAttribute(car.getKernelName().trim(), Control.MAX_LEN_STRING_ATTRIBUTE);
        		if (printOwner)
        			owner = padAttribute(car.getOwner().trim(), ownerMaxLen);
        		if (printBuilt)
        			built = padAttribute(car.getBuilt().trim(), Control.MAX_LEN_STRING_BUILT_NAME);

        		String s = number + road + type
        		+ length + weight + color + load + kernel
        		+ owner + built
        		+ location;
    			if (s.length() > numberCharPerLine)
    				s = s.substring(0, numberCharPerLine);
        		writer.write(s+newLine);
        	}

        	// and force completion of the printing
        	writer.close();
        } catch (IOException we) {
        	log.error("Error printing car roster");
        }
    }
    
    private void printTitleLine(HardcopyWriter writer) throws IOException{
       	String s = rb.getString("Number") + "\t" + rb.getString("Road")
    	+ "\t" + rb.getString("Type") + "\t  "
    	+ (printCarLength.isSelected()?rb.getString("Length")+ " ":"  ") 
    	+ (printCarWeight.isSelected()?"      ":" ")
    	+ (printCarColor.isSelected()?rb.getString("Color")+ "        ":"")
    	+ (printCarLoad.isSelected()?rb.getString("Load")+ "         ":"")
    	+ (printCarKernel.isSelected()?rb.getString("Kernel")+ "       ":"")
    	+ (printCarOwner.isSelected()?padAttribute(rb.getString("Owner"),ownerMaxLen):"")
    	+ (printCarBuilt.isSelected()?rb.getString("Built")+" ":"")
    	+ rb.getString("Location")
    	+ newLine;
    	writer.write(s);
    }
    
    private String padAttribute(String attribute, int length){
			if (attribute.length() > length)
				attribute = attribute.substring(0, length);
			StringBuffer buf = new StringBuffer(attribute);
			for (int i=attribute.length(); i<length+1; i++)
   				buf.append(" ");
			return buf.toString(); 	
    }
    
    JCheckBox printCarsWithLocation = new JCheckBox(rb.getString("PrintCarsWithLocation"));
    JCheckBox printCarLength = new JCheckBox(rb.getString("PrintCarLength"));
    JCheckBox printCarWeight = new JCheckBox(rb.getString("PrintCarWeight"));
    JCheckBox printCarColor = new JCheckBox(rb.getString("PrintCarColor"));
    JCheckBox printCarOwner = new JCheckBox(rb.getString("PrintCarOwner"));
    JCheckBox printCarBuilt = new JCheckBox(rb.getString("PrintCarBuilt"));
    JCheckBox printCarLoad = new JCheckBox(rb.getString("PrintCarLoad"));
    JCheckBox printCarKernel = new JCheckBox(rb.getString("PrintKernel"));
    JCheckBox printSpace = new JCheckBox(rb.getString("PrintSpace"));
    JCheckBox printPage = new JCheckBox(rb.getString("PrintPage"));
    
    JButton okayButton = new JButton(rb.getString("ButtonOkay"));
    
    String newLine = "\n";
    
    public class CarPrintOptionFrame extends OperationsFrame{
    	 PrintCarRosterAction pcr;
    	
    	public CarPrintOptionFrame(PrintCarRosterAction pcr){
    		super();
    		this.pcr = pcr;
    		// create panel
    		JPanel pPanel = new JPanel();
    		pPanel.setLayout(new BoxLayout(pPanel,BoxLayout.Y_AXIS));
    		pPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("PrintOptions")));
    		pPanel.add(printCarsWithLocation);
    		pPanel.add(printCarLength);
    		pPanel.add(printCarWeight);
    		pPanel.add(printCarColor);
    		pPanel.add(printCarLoad);
    		pPanel.add(printCarKernel);
    		pPanel.add(printCarOwner);
    		pPanel.add(printCarBuilt);
			pPanel.add(printSpace);
			pPanel.add(printPage);
    		    		
    		// set defaults
    		printCarsWithLocation.setSelected(false);
    		printCarLength.setSelected(true);
    		printCarWeight.setSelected(false);
    		printCarColor.setSelected(true);
    		printCarLoad.setSelected(false);
    		printCarKernel.setSelected(false);
       		printCarOwner.setSelected(false);
    		printCarBuilt.setSelected(false);
    		printSpace.setSelected(false);
    		printPage.setSelected(false);
    		
    		//add tool tips
    		printSpace.setToolTipText(rb.getString("TipSelectSortByLoc"));
    		printPage.setToolTipText(rb.getString("TipSelectSortByLoc"));
    		
    		JPanel pButtons = new JPanel();  
    		pButtons.setLayout(new GridBagLayout());
    		pButtons.add(okayButton);
    		addButtonAction(okayButton);
    	   		
    		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
    		getContentPane().add(pPanel);
    		getContentPane().add(pButtons);
    		setPreferredSize(null);
    		pack();
    		setVisible(true);
    	}
    	
    	public void initComponents() {
    		printSpace.setEnabled(panel.sortByLocation.isSelected());
    		printPage.setEnabled(panel.sortByLocation.isSelected());
    		if (!panel.sortByLocation.isSelected()){
    			printSpace.setSelected(false);
    			printPage.setSelected(false);
    		}
    	}
    	
    	public void buttonActionPerformed(java.awt.event.ActionEvent ae) { 		
    		setVisible(false);
    		pcr.printCars();  		
    	}
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PrintCarRosterAction.class.getName());
}

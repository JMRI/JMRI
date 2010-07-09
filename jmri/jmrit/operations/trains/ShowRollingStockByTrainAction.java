//ShowRollingStockByTrainAction.java

package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;

import javax.swing.AbstractAction;

/**
 * Swing action to create and register an object.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision: 1.1 $
 */
public class ShowRollingStockByTrainAction extends AbstractAction {
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.Trains.JmritOperationsTrainsBundle");

	public ShowRollingStockByTrainAction(String s, TrainEditFrame frame) {
		super(s);
		this.frame = frame;
	}
	
	TrainEditFrame frame;

	public void actionPerformed(ActionEvent e) {
		// create frame
		TrainByCarTypeFrame f = new TrainByCarTypeFrame();
		f.initComponents(frame._train);
	}
}

/* @(#)ShowRollingStockByTrainAction.java */

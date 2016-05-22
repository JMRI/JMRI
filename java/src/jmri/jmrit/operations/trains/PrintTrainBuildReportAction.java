// PrintTrainBuildReportAction.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Frame;
import java.awt.event.*;
import java.text.MessageFormat;
import javax.swing.*;

/**
 * Action to print a train's build report
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class PrintTrainBuildReportAction extends AbstractAction {

	public PrintTrainBuildReportAction(String actionName, boolean preview, Frame frame) {
		super(actionName);
		isPreview = preview;
		this.frame = frame;
	}

	/**
	 * Variable to set whether this is to be printed or previewed
	 */
	boolean isPreview;
	Frame frame;

	public void actionPerformed(ActionEvent e) {
		TrainEditFrame f = (TrainEditFrame) frame;
		Train train = f._train;
		if (train == null)
			return;
		if (!train.isBuilt()) {
			String printOrPreview = Bundle.getMessage("print");
			if (isPreview)
				printOrPreview = Bundle.getMessage("preview");
			String string = MessageFormat.format(Bundle.getMessage("DoYouWantToPrintPreviousBuildReport"),
					new Object[] { printOrPreview, train.getName() });
			int results = JOptionPane.showConfirmDialog(null, string, MessageFormat.format(
					Bundle.getMessage("PrintPreviousBuildReport"), new Object[] { printOrPreview }),
					JOptionPane.YES_NO_OPTION);
			if (results != JOptionPane.YES_OPTION)
				return;
		}
		if (!train.printBuildReport(isPreview)) {
			String string = MessageFormat.format(Bundle.getMessage("NeedToBuildTrainBeforePrinting"),
					new Object[] { train.getName() });
			JOptionPane.showMessageDialog(null, string, Bundle.getMessage("CanNotPrintBuildReport"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	static Logger log = LoggerFactory
			.getLogger(PrintTrainBuildReportAction.class.getName());
}

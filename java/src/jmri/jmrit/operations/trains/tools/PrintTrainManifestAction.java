// PrintTrainManifestAction.java
package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;

/**
 * Action to print a train's manifest
 *
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class PrintTrainManifestAction extends AbstractAction {

    public PrintTrainManifestAction(String actionName, boolean preview, TrainEditFrame frame) {
        super(actionName);
        isPreview = preview;
        trainEditFrame = frame;
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    TrainEditFrame trainEditFrame;

    @Override
    public void actionPerformed(ActionEvent e) {
        Train train = trainEditFrame._train;
        if (train == null) {
            return;
        }
        if (!train.isBuilt()) {
            String printOrPreview = Bundle.getMessage("print");
            if (isPreview) {
                printOrPreview = Bundle.getMessage("preview");
            }
            String string = MessageFormat.format(Bundle.getMessage("DoYouWantToPrintPreviousManifest"),
                    new Object[]{printOrPreview, train.getName()});
            int results = JOptionPane.showConfirmDialog(null, string, MessageFormat.format(
                    Bundle.getMessage("PrintPreviousManifest"), new Object[]{printOrPreview}),
                    JOptionPane.YES_NO_OPTION);
            if (results != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (!train.printManifest(isPreview)) {
            String string = MessageFormat.format(Bundle.getMessage("NeedToBuildTrainBeforePrinting"),
                    new Object[]{train.getName()});
            JOptionPane.showMessageDialog(null, string, MessageFormat.format(
                    Bundle.getMessage("CanNotPrintManifest"), new Object[]{Bundle.getMessage("print")}),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(PrintTrainManifestAction.class.getName());
}

package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.trains.Train;

/**
 * Action to print a train's manifest
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class PrintTrainManifestAction extends AbstractAction {

    public PrintTrainManifestAction(String actionName, boolean preview, Train train) {
        super(actionName);
        isPreview = preview;
        _train = train;
        setEnabled(train != null);
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    Train _train;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_train == null) {
            return;
        }
        if (!_train.isBuilt()) {
            String printOrPreview = Bundle.getMessage("print");
            if (isPreview) {
                printOrPreview = Bundle.getMessage("preview");
            }
            String string = MessageFormat.format(Bundle.getMessage("DoYouWantToPrintPreviousManifest"),
                    new Object[]{printOrPreview, _train.getName()});
            int results = JOptionPane.showConfirmDialog(null, string, MessageFormat.format(
                    Bundle.getMessage("PrintPreviousManifest"), new Object[]{printOrPreview}),
                    JOptionPane.YES_NO_OPTION);
            if (results != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (!_train.printManifest(isPreview)) {
            String string = MessageFormat.format(Bundle.getMessage("NeedToBuildTrainBeforePrinting"),
                    new Object[]{_train.getName()});
            JOptionPane.showMessageDialog(null, string, MessageFormat.format(
                    Bundle.getMessage("CanNotPrintManifest"), new Object[]{Bundle.getMessage("print")}),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(PrintTrainManifestAction.class);
}

package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.Train;
import jmri.util.swing.JmriJOptionPane;

/**
 * Action to print a train's manifest
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class PrintTrainManifestAction extends AbstractAction {

    public PrintTrainManifestAction(boolean isPreview, Train train) {
        super(isPreview ? Bundle.getMessage("MenuItemPreviewManifest") : Bundle.getMessage("MenuItemPrintManifest"));
        _isPreview = isPreview;
        _train = train;
        setEnabled(train != null);
    }

    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean _isPreview;
    Train _train;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_train == null) {
            return;
        }
        if (!_train.isBuilt()) {
            String printOrPreview = Bundle.getMessage("print");
            if (_isPreview) {
                printOrPreview = Bundle.getMessage("preview");
            }
            String string = Bundle.getMessage("DoYouWantToPrintPreviousManifest",
                    printOrPreview, _train.getName());
            int results = JmriJOptionPane.showConfirmDialog(null, string, 
                    Bundle.getMessage("PrintPreviousManifest", printOrPreview),
                    JmriJOptionPane.YES_NO_OPTION);
            if (results != JmriJOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            if (!_train.printManifest(_isPreview)) {
                String string = Bundle.getMessage("NeedToBuildTrainBeforePrinting",
                        _train.getName());
                JmriJOptionPane.showMessageDialog(null, string,
                        Bundle.getMessage("CanNotPrintManifest", Bundle.getMessage("print")),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        } catch (jmri.jmrit.operations.trains.BuildFailedException e1) {
        }
    }


//    private final static Logger log = LoggerFactory.getLogger(PrintTrainManifestAction.class);
}

package jmri.jmrit.operations.trains.tools;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.Train;
import jmri.util.swing.JmriJOptionPane;

/**
 * Action to print a train's build report
 *
 * @author Daniel Boudreau Copyright (C) 2010
 */
public class PrintTrainBuildReportAction extends AbstractAction {

    public PrintTrainBuildReportAction(boolean isPreview, Train train) {
        super(isPreview ? Bundle.getMessage("MenuItemPreviewBuildReport") : Bundle.getMessage("MenuItemPrintBuildReport"));
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
            String string = MessageFormat.format(Bundle.getMessage("DoYouWantToPrintPreviousBuildReport"),
                    new Object[]{printOrPreview, _train.getName()});
            int results = JmriJOptionPane.showConfirmDialog(null, string, MessageFormat.format(
                    Bundle.getMessage("PrintPreviousBuildReport"), new Object[]{printOrPreview}),
                    JmriJOptionPane.YES_NO_OPTION);
            if (results != JmriJOptionPane.YES_OPTION) {
                return;
            }
        }
        if (!_train.printBuildReport(_isPreview)) {
            String string = MessageFormat.format(Bundle.getMessage("NeedToBuildTrainBeforePrinting"),
                    new Object[]{_train.getName()});
            JmriJOptionPane.showMessageDialog(null, string, Bundle.getMessage("CanNotPrintBuildReport"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(PrintTrainBuildReportAction.class);
}


package apps.util.issuereporter.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;

/**
 * Action to report an issue to the JMRI developers.
 * 
 * @author Randall Wood Copyright 2020
 */
@API(status = API.Status.INTERNAL)
public class IssueReporterAction extends AbstractAction {

    public IssueReporterAction() {
        super(Bundle.getMessage("IssueReporterAction.title", "..."));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new IssueReporter().setVisible(true);
    }
    
}

package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Starts the ImportEngines thread
 *
 * @author Dan Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class ExportEngineRosterAction extends AbstractAction {

    public ExportEngineRosterAction() {
        super(Bundle.getMessage("MenuItemExport"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        ExportEngines ex = new ExportEngines();
        ex.writeOperationsEngineFile();
    }

//    private final static Logger log = LoggerFactory.getLogger(ExportEngineRosterAction.class);
}

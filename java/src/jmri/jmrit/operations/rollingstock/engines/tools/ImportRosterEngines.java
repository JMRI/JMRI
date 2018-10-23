package jmri.jmrit.operations.rollingstock.engines.tools;

import java.text.MessageFormat;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import engines from the jmri Roster
 *
 * @author Daniel Boudreau Copyright (C) 2008
 *
 */
public class ImportRosterEngines extends Thread {

    EngineManager manager = InstanceManager.getDefault(EngineManager.class);
    private static String defaultEngineLength = Bundle.getMessage("engineDefaultLength");
    private static String defaultEngineType = Bundle.getMessage("engineDefaultType");
    private static String defaultEngineHp = Bundle.getMessage("engineDefaultHp");

    javax.swing.JLabel textEngine = new javax.swing.JLabel();
    javax.swing.JLabel textId = new javax.swing.JLabel();

    // we use a thread so the status frame will work!
    @Override
    public void run() {

        // create a status frame
        JPanel ps = new JPanel();
        jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame(Bundle.getMessage("TitleImportEngines"));
        fstatus.setLocationRelativeTo(null);
        fstatus.setSize(200, 100);

        ps.add(textEngine);
        ps.add(textId);
        fstatus.getContentPane().add(ps);
        textEngine.setText(Bundle.getMessage("AddEngine"));
        textEngine.setVisible(true);
        textId.setVisible(true);
        fstatus.setVisible(true);

        // Now get engines from the JMRI roster 
        int enginesAdded = 0;

        List<RosterEntry> engines = Roster.getDefault().matchingList(null, null, null, null, null, null, null);

        for (RosterEntry re : engines) {
            // add engines that have a road name and number
            if (!re.getRoadName().equals("") && !re.getRoadNumber().equals("")) {
                String road = re.getRoadName();
                if (road.length() > Control.max_len_string_attibute) {
                    road = road.substring(0, Control.max_len_string_attibute);
                }
                textId.setText(road + " " + re.getRoadNumber());
                Engine engine = manager.getByRoadAndNumber(road, re.getRoadNumber());
                if (engine == null) {
                    engine = manager.newRS(road, re.getRoadNumber());
                    String model = re.getModel();
                    if (model.length() > Control.max_len_string_attibute) {
                        model = model.substring(0, Control.max_len_string_attibute);
                    }
                    engine.setModel(model);
                    // does this model already have a length?
                    if (engine.getLength().equals(Engine.NONE)) {
                        engine.setLength(defaultEngineLength);
                    }
                    // does this model already have a type?
                    if (engine.getTypeName().equals(Engine.NONE)) {
                        engine.setTypeName(defaultEngineType);
                    }
                    // does this model already have a hp?
                    if (engine.getHp().equals(Engine.NONE)) {
                        engine.setHp(defaultEngineHp);
                    }
                    String owner = re.getOwner();
                    if (owner.length() > Control.max_len_string_attibute) {
                        owner = owner.substring(0, Control.max_len_string_attibute);
                    }
                    engine.setOwner(owner);
                    enginesAdded++;
                } else {
                    log.info("Can not add, engine number (" + re.getRoadNumber() + ") road (" + re.getRoadName() + ") already exists");
                }
            }
        }

        // kill status panel
        fstatus.dispose();

        if (enginesAdded > 0) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"), new Object[]{enginesAdded}),
                    Bundle.getMessage("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"), new Object[]{enginesAdded}),
                    Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(ImportRosterEngines.class);
}

package jmri.jmrit.operations.rollingstock.engines.tools;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterGroupComboBox;
import jmri.util.JmriJFrame;

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

    JLabel textEngine = new JLabel(Bundle.getMessage("AddEngine"));
    JLabel textId = new JLabel();

    // we use a thread so the status frame will update!
    @Override
    public void run() {

        // create a status frame
        JmriJFrame fstatus = new JmriJFrame(Bundle.getMessage("TitleImportEngines"));
        fstatus.setSize(Control.panelWidth500, Control.panelHeight100);
        JPanel ps = new JPanel();
        ps.add(textEngine);
        ps.add(textId);
        fstatus.getContentPane().add(ps);
        fstatus.setVisible(true);

        // create dialog with roster group comboBox
        RosterGroupComboBox comboBox = new RosterGroupComboBox();
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
        JOptionPane.showMessageDialog(null, comboBox, rb.getString("SelectRosterGroup"), JOptionPane.QUESTION_MESSAGE);
        String groupName = comboBox.getSelectedItem();
        log.debug("User selected roster group: {}", groupName);

        // Now get engines from the JMRI roster
        int enginesAdded = 0;

        List<RosterEntry> engines = Roster.getDefault().getEntriesMatchingCriteria(null, null, null, null, null, null,
                null, groupName, null, null, null);

        for (RosterEntry re : engines) {
            // add engines that have a road name and number
            if (re.getRoadName().isEmpty() || re.getRoadNumber().isEmpty()) {
                log.error("Roster Id: {} doesn't have a road name and road number", re.getId());
                continue;
            }
            String road = re.getRoadName().trim();
            if (road.length() > Control.max_len_string_attibute) {
                road = road.substring(0, Control.max_len_string_attibute);
            }
            String number = re.getRoadNumber().trim();
            if (number.length() > Control.max_len_string_road_number) {
                number = number.substring(0, Control.max_len_string_road_number);
            }
            textId.setText(road + " " + number);
            Engine engine = manager.getByRoadAndNumber(road, number);
            if (engine == null) {
                engine = manager.newRS(road, number);
                String model = re.getModel().trim();
                if (model.length() > Control.max_len_string_attibute) {
                    model = model.substring(0, Control.max_len_string_attibute);
                }
                if (model.isEmpty()) {
                    log.warn("Roster Id: {} hasn't been assigned a model name", re.getId());
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
                String owner = re.getOwner().trim();
                if (owner.length() > Control.max_len_string_attibute) {
                    owner = owner.substring(0, Control.max_len_string_attibute);
                }
                engine.setOwner(owner);
                enginesAdded++;
            } else {
                log.info("Can not add roster Id: {}, engine road name ({}) road number ({}) already exists", re.getId(),
                        re.getRoadName(), re.getRoadNumber());
            }
        }

        // kill status panel
        fstatus.dispose();

        if (enginesAdded > 0) {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"), new Object[] { enginesAdded }),
                    Bundle.getMessage("SuccessfulImport"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    MessageFormat.format(Bundle.getMessage("ImportEnginesAdded"), new Object[] { enginesAdded }),
                    Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImportRosterEngines.class);
}

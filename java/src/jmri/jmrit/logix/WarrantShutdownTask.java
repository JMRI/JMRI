package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jmri.InstanceManager;
import jmri.implementation.AbstractShutDownTask;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Allows user to decide if (and which) SpeedProfiles to write to the Roster at
 * the end of a session. Locos running warrants have had their speeds measured
 * and this new data may or may not be merged into any existing SpeedProfiles in
 * the Roster.
 * <p>
 *
 * @author Pete cressman Copyright (C) 2017
 */
public class WarrantShutdownTask extends AbstractShutDownTask {

    HashMap<String, Boolean> _mergeCandidates;
    HashMap<String, RosterSpeedProfile> _mergeProfiles;
    Map<String, Map<Integer, Boolean>> _anomalies;
    
    /**
     * Constructor specifies the warning message and action to take
     *
     * @param name the name of the task (used in logs)
     */
    public WarrantShutdownTask(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean call() {
        WarrantPreferences preferences = WarrantPreferences.getDefault();
        switch (preferences.getShutdown()) {
            case MERGE_ALL:
                if (makeMergeCandidates()) {
                    if (_anomalies != null && _anomalies.size() > 0) {
                        makeMergeWindow();
                    }
                    setDoRun(true);
                }
                break;
            case PROMPT:
                if (makeMergeCandidates()) {
                    makeMergeWindow();
                    setDoRun(true);
                }
                break;
            case NO_MERGE:
                // do nothing
                break;
            default:
                log.warn("No choice made for warrant shutdown");
                break;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        if (isDoRun()) {
            merge();
        }
    }

    private boolean makeMergeCandidates() {
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _mergeProfiles = manager.getMergeProfiles();
        if (_mergeProfiles == null || _mergeProfiles.isEmpty()) {
            return false;
        }
        _anomalies = new HashMap<>();
        _mergeCandidates = new HashMap<>();
        Iterator<java.util.Map.Entry<String, RosterSpeedProfile>> iter = _mergeProfiles.entrySet().iterator();
        while (iter.hasNext()) {
            java.util.Map.Entry<String, RosterSpeedProfile> entry = iter.next();
            Map<Integer, Boolean> anomaly = MergePrompt.validateSpeedProfile(entry.getValue());
            if (anomaly != null && anomaly.size() > 0) {
                _anomalies.put(entry.getKey(), anomaly);
            }
            String rosterId = entry.getKey();
            if (Roster.getDefault().getEntryForId(rosterId) != null) {
                _mergeCandidates.put(rosterId, true);
            }
        }
        if (_mergeCandidates.isEmpty()) {
            return false;
        }
        return true;
    }

    private void makeMergeWindow() {
        new MergePrompt(Bundle.getMessage("MergeTitle"), _mergeCandidates, _anomalies);
    }

    private void merge() {
        _mergeCandidates.forEach((id, merge) -> {
            if (merge) {
                RosterEntry rosterEntry = Roster.getDefault().entryFromTitle(id);
                if (rosterEntry != null) {
                    rosterEntry.setSpeedProfile(_mergeProfiles.get(id));
                    log.debug("Write SpeedProfile to Roster. id= {}", id);
                } else {
                    log.debug("Unable to Write SpeedProfile to Roster. No RosterEntry for {}", id);
                }
            } else {
                log.debug("SpeedProfile not merged to Roster. id= {}", id);
            }
        });
        Roster.getDefault().writeRoster();
    }

    private static final Logger log = LoggerFactory.getLogger(WarrantShutdownTask.class);

}

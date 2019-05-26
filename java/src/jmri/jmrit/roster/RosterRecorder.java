package jmri.jmrit.roster;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches a Roster and writes it to file when a change is seen.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
public class RosterRecorder extends Thread {

    public RosterRecorder() {
        Roster roster = Roster.getDefault();  // forces roster to be loaded

        // listen for any new entries
        roster.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                changedRoster(e);
            }
        });

        // listen to each entry
        roster.getAllEntries().forEach((entry) -> {
            watchEntry(entry);
        });

    }

    void watchEntry(RosterEntry e) {
        log.debug("watchEntry");
        e.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                changedEntry(e);
            }
        });
    }

    /**
     * Added or removed RosterEntry, make sure we're listening appropriately.
     */
    void changedRoster(java.beans.PropertyChangeEvent e) {
        log.debug("changedRoster");
        if (e.getPropertyName().equals("add")) {
            // new entry, must listen
            watchEntry((RosterEntry) e.getSource());
            // write roster out
            forceWrite();
        } else if (e.getPropertyName().equals("remove")) {
            // removed entry, write roster out
            forceWrite();
            // in future, may also want to stop listening
            // to that entry, but for now don't have listener reference

        }
    }

    /**
     * Changed RosterEntry changed RosterEntry fires off store.
     */
    void changedEntry(java.beans.PropertyChangeEvent e) {
        log.debug("changedEntry");
        // change causes roster to write out
        forceWrite();
    }

    /**
     * Trigger the next roster write
     */
    void forceWrite() {
        if (queue.offer(Roster.getDefault())) {
            log.debug("forceWrite queued OK");
        } else {
            log.error("forceWrite failed to queue roster write");
        }
    }

    // the actual thread code starts here
    @Override
    public void run() {
        while (true) {  // loop until daemon thread ends
            // wait roster to write
            try {
                queue.take();  // just take, don't actually use result
            } catch (InterruptedException e) {
                log.debug("ending due to interrupt in main wait");
                return;
            }
            log.debug("run awake");

            // skip to last available
            while (queue.peek() != null) {
                log.debug("  skip one");
                try {
                    queue.take();  // just take, don't actually use result
                } catch (InterruptedException e) {
                    log.debug("ending due to interrupt in purge take");
                    return;
                }
            }

            // write final result
            log.debug("writeRosterFile start");
            Roster.getDefault().writeRoster();
            log.debug("writeRosterFile end");
        }
    }

    BlockingQueue<Roster> queue = new ArrayBlockingQueue<Roster>(25);

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(RosterRecorder.class);

}

package jmri.jmrit.dispatcher;

import java.util.List;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.InvokeOnLayoutThread;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.jmrit.roster.RosterEntry;

/**
 * Finds the record of the train an Active Train runs, so that every Active
 * Train for the same train refers to the same record, run after run.
 * <p>
 * A train with a roster entry is the train that loco is in: the record that
 * has the entry among its locos' roster entries. Otherwise a train is known
 * by its name. If no record is found, one is
 * made.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class ActiveTrainRecords {

    private ActiveTrainRecords() {
    }

    /**
     * Find or make the record for an Active Train, give it to the Active
     * Train, and, if the train's start Block is occupied, place the record
     * there unless it already has the train in that Block: Dispatcher has
     * been told the train is there.
     *
     * @param at the Active Train, with its roster entry, Train Detection,
     *           Max Train Length and start Block already set
     * @return the record
     */
    @Nonnull
    @InvokeOnLayoutThread
    static TrainRecord link(@Nonnull ActiveTrain at) {
        TrainRecord record = find(at);
        at.setTrainRecord(record);
        jmri.jmrit.trainrecord.swing.WarningDialog.start(); // show train tracking's warnings
        Block start = at.getStartBlock();
        if (start != null && start.getState() == Block.OCCUPIED && !record.getBlocks().contains(start)) {
            record.place(List.of(start), false, TrainRecord.Reason.Rule.PLACED_BY_DISPATCHER);
        }
        return record;
    }

    /**
     * Find or make the record for an Active Train.
     * <ul>
     * <li>With a roster entry: the record that has that roster entry among
     * its locos'; else the record of the train's name if it has no locos'
     * roster entries, which then gets this one as its lead loco's; else a new
     * record.</li>
     * <li>Without: the record of the train's name; else a new record.</li>
     * </ul>
     * A new record is named after the train. If a record of another loco
     * already has that name, the new one is named after its roster entry,
     * then after both.
     *
     * @param at the Active Train
     * @return the record
     */
    @Nonnull
    @InvokeOnLayoutThread
    static TrainRecord find(@Nonnull ActiveTrain at) {
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        String name = at.getTrainName();
        RosterEntry entry = at.getRosterEntry();
        if (entry == null) {
            TrainRecord record = manager.getByName(name);
            return record != null ? record : manager.newRecord(name);
        }
        TrainRecord record = manager.getByRosterEntry(entry);
        if (record != null) {
            return record;
        }
        record = manager.getByName(name);
        if (record != null && record.getRosterEntries().isEmpty()) {
            record.insertFirstRosterEntry(entry);
            return record;
        }
        String[] names = {name, entry.getId(), name + " " + entry.getId()};
        for (String n : names) {
            if (n != null && !n.trim().isEmpty() && manager.getByName(n) == null) {
                record = manager.newRecord(n);
                record.insertFirstRosterEntry(entry);
                return record;
            }
        }
        // every candidate name is taken by another loco's record: number it
        int i = 2;
        while (manager.getByName(name + " (" + i + ")") != null) {
            i++;
        }
        record = manager.newRecord(name + " (" + i + ")");
        record.insertFirstRosterEntry(entry);
        return record;
    }
}

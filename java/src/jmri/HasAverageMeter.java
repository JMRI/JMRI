package jmri;

/**
 * Interface for obtaining AverageMeters.
 *
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public interface HasAverageMeter extends Manager<Meter> {

    /**
     * Get an AverageMeter for the meter m.
     * @param sysName   the system name
     * @param userName  the user name
     * @param m         the meter to base the average on.
     * @return          an AverageMeter.
     */
    AverageMeter newAverageMeter(String sysName, String userName, Meter m);

}

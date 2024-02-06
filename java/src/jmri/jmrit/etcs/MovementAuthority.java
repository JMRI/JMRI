package jmri.jmrit.etcs;

import java.util.*;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Class to represent a Movement Authority which is passed to the DMI
 * to authorise a route.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class MovementAuthority {

    private final List<TrackSection> list;

    /**
     * Create a new MovementAuthority from a List of TrackSections.
     * @param sectionList the list of TrackSections.
     */
    public MovementAuthority(List<TrackSection> sectionList){
        list = sectionList;
    }

    /**
     * Get the list of TrackSections.
     * @return Unmodifiable list of TrackSections.
     */
    public List<TrackSection> getTrackSections() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Remove a TrackSection from the Movement Authority.
     * @param ts the TrackSection to remove.
     */
    private void removeTrackSection(TrackSection ts){
        list.remove(ts);
    }

    /**
     * Aggregates a list of DmiMovementAuthorities into a list of
     * DmiTrackSections, based on either speed changes or gradient changes.
     *
     * @param completeList The list of MovementAuthority objects to process.
     * @param isSpeed      True to aggregate based on speed changes,
     *                     False to aggregate based on gradient changes.
     * @return A list of aggregated DmiTrackSection77 objects.
     */
    @Nonnull
    public static List<TrackSection> getTrackSectionList(
        @Nonnull List<MovementAuthority> completeList, boolean isSpeed) {

        List<TrackSection> trackSectionList = new ArrayList<>();
        TrackSection lastTs = null;
        List<TrackSection> tempTrackList;
        for (MovementAuthority ma : completeList) {
            tempTrackList = ma.getTrackSections();
            for (TrackSection ts : tempTrackList) {
                if (lastTs == null || checkAddToList(lastTs, ts, isSpeed)) {
                    trackSectionList.add(new TrackSection(ts.getLength(), ts.getSpeed(), ts.getGradient()));
                    lastTs = trackSectionList.get(trackSectionList.size() - 1);
                } else {
                    lastTs.setLength(ts.getLength() + lastTs.getLength());
                }
            }
        }
        return trackSectionList;
    }

    private static boolean checkAddToList(
        @Nonnull TrackSection lastTs, @Nonnull TrackSection ts, boolean isSpeed ) {
        if (isSpeed) {
            return lastTs.getSpeed() != ts.getSpeed();
        } else {
            return lastTs.getGradient() != ts.getGradient();
        }
    }

    /**
     * Advance forward a List of Movement Authorities.
     * The length of the nearest TrackSection(s) are reduced by the distance.
     * Unused Track Sections are removed from the List.
     * Track Section announcements are also advanced.
     * @param list the list to advance.
     * @param distance the distance to advance.
     * @return the modified List of Movement Authorities.
     */
    @Nonnull
    public static List<MovementAuthority> advanceForward(
        @Nonnull List<MovementAuthority> list, int distance) {
    
        if (list.isEmpty()){
            return list;
        }

        MovementAuthority ma = list.get(0);
        if (ma.getTrackSections().isEmpty()) {
            list.remove(ma);
            return advanceForward(list, distance);
        }
        TrackSection ts = ma.getTrackSections().get(0);

        int newLength = ts.getLength() - distance;
        if ( newLength < 0 ) {
            ma.removeTrackSection(ts);
            return advanceForward(list, distance-ts.getLength());
        }
        ts.setLength(newLength);
        ts.advanceAnnouncements(distance);
        return list;    
    }
    
}

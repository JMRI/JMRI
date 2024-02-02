package jmri.jmrit.etcs;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;

/**
 * Class to represent DMI Track Points of Interest,
 * i.e. Announcements and Orders.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class TrackSection {

    /**
     * Create a new TrackSection.
     * @param sectionLength the Section Length.
     * @param sectionSpeed the Section Speed.
     * @param sectionGradient the Section Gradient.
     */
    public TrackSection(int sectionLength, int sectionSpeed, int sectionGradient){
        length = sectionLength;
        speed = sectionSpeed;
        gradient = sectionGradient;
        accouncements = new CopyOnWriteArrayList<>();
    }

    private final CopyOnWriteArrayList<TrackCondition> accouncements;
    private int length;
    private final int speed;
    private final int gradient;

    /**
     * Get the Section Length.
     * @return section length.
     */
    public int getLength(){
        return length;
    }

    protected void setLength(int newLength) {
        length = newLength;
    }

    /**
     * Get the maximum speed for Section.
     * @return maximum speed.
     */
    public int getSpeed(){
        return speed;
    }

    /**
     * Get the Section Gradient.
     * @return gradient percentage for the Track Section.
     */
    public int getGradient(){
        return gradient;
    }

    /**
     * Add an Announcement to the Track Section.
     * @param ac the Announcement to add.
     * @throws  IllegalArgumentException if the Announcement
     *          does not fit into the TrackSection.
     */
    public void addAnnouncement(@Nonnull TrackCondition ac ) {
        if ( ac.getDistanceFromStart() >= this.getLength() ){
            throw new IllegalArgumentException("Announcement " + ac.toString() +
                " does not fit into track section");
        }
        accouncements.add(ac);
    }

    /**
     * Get the List of Announcements for the Section.
     * @return List of Announcements related to the TrackSection.
     */
    public List<TrackCondition> getAnnouncements() {
        return new ArrayList<>(accouncements);
        // return Collections.unmodifiableList(accouncements);
        // return accouncements;
    }

    protected void advanceAnnouncements(int distance){
        for (TrackCondition ac : accouncements) {
            ac.setDistanceFromStart(ac.getDistanceFromStart()-distance);
            if ( ac.getDistanceFromStart() < 0 ) {
                accouncements.remove(ac);
            }
        }
    }

    @Override
    public String toString(){
        return "TrackSection Length:"+getLength()+" Speed:"+getSpeed()+" Gradient:"+getGradient();
    }

}

package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.jmrit.operations.rollingstock.cars.Car;

import javax.swing.*;
import java.time.LocalTime;

public class TagCarItem {
    private String tag;
    private String road;
    private String carNumber;
    private String location;
    private String track;
    private String train;
    private Integer trainPosition;

    public Car getCurrentCar() {
        return currentCar;
    }

    public void setCurrentCar(Car currentCar) {
        this.currentCar = currentCar;
    }

    private Car currentCar;

    private String destination;
    private LocalTime lastSeen;
    private JButton action1 = null;
    private JButton action2 = null;
    private LocalTime tagTime;
    private int repeatCount = 0;

    public TagCarItem() {
        tagTime = LocalTime.now();
    }

    public TagCarItem(String newTag) {
        this.tag = newTag;
        tagTime = LocalTime.now();
        this.lastSeen = tagTime;
    }

    public TagCarItem(String newTag, LocalTime tagTime) {
        this.tag = newTag;
        this.tagTime = tagTime;
        this.lastSeen = tagTime;
    }

    public Integer getTrainPosition() {
        return trainPosition;
    }

    public void setTrainPosition(Integer trainPosition) {
        this.trainPosition = trainPosition;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public JButton getAction1() {
        return action1;
    }

    public void setAction1(JButton action1) {
        this.action1 = action1;
    }

    public JButton getAction2() {
        return action2;
    }

    public void setAction2(JButton action2) {
        this.action2 = action2;
    }


    public void setLastSeen(LocalTime lastSeen) {
        this.lastSeen = lastSeen;
        repeatCount++;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public LocalTime getLastSeen() {
        return lastSeen;
    }

    public LocalTime getTagTime() { return tagTime; }

    public void setTagTime(LocalTime tagTime) { this.tagTime = tagTime; }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getTrain() {
        return train;
    }

    public void setTrain(String train) {
        this.train = train;
    }

}

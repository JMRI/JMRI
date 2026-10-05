package jmri.jmrit.roster;

/**
 * An object to store all decoder function informations in one place.
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson Copyright (C) 2026
 * 
 */
public class RosterFunctionEntry {

    private int fn;
    private String label; 
    private String soundLabels;
    private String image;
    private String selectedImage;
    private boolean lockable;
    private boolean visible;

    RosterFunctionEntry(int fn) {
        this.fn = fn;
        label = null;
        soundLabels = null;
        image = null;
        selectedImage = null;
        lockable = true;
        visible = true;
    }

    RosterFunctionEntry(RosterFunctionEntry pEntry) {
        fn = pEntry.fn;
        label = pEntry.label;
        soundLabels = pEntry.soundLabels;
        image = pEntry.image;
        selectedImage = pEntry.selectedImage;
        lockable = pEntry.lockable;
        visible = pEntry.visible;
    }

    public int getFunctionNumber() {
        return fn;
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSoundLabels() {
        return soundLabels; 
    }

    public void setSoundLabels(String soundLabels) {
        this.soundLabels = soundLabels;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

    public boolean isLockable() {
        return lockable;
    }

    public void setLockable(boolean lockable) {
        this.lockable = lockable;
    }

    public boolean isVisible() {
        return visible; 
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean equals(RosterFunctionEntry o) {
        return (
            (o!=null) &&
            (fn == o.fn) &&
            (label == null ? o.label == null : label.equals(o.label)) &&
            (soundLabels == null ? o.soundLabels == null : soundLabels.equals(o.soundLabels)) &&
            (image == null ? o.image == null : image.equals(o.image)) &&
            (selectedImage == null ? o.selectedImage == null : selectedImage.equals(o.selectedImage)) &&
            (lockable == o.lockable) &&
            (visible == o.visible)
        );
    }

}

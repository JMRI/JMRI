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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null || o.getClass()!= this.getClass())) {
            return false;
        }
        RosterFunctionEntry r = (RosterFunctionEntry) o;
        return (            
            (fn == r.fn) &&
            (label == null ? r.label == null : label.equals(r.label)) &&
            (soundLabels == null ? r.soundLabels == null : soundLabels.equals(r.soundLabels)) &&
            (image == null ? r.image == null : image.equals(r.image)) &&
            (selectedImage == null ? r.selectedImage == null : selectedImage.equals(r.selectedImage)) &&
            (lockable == r.lockable) &&
            (visible == r.visible)
        );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + fn;
        hash = 31 * hash + (label == null ? 0 : label.hashCode());
        hash = 31 * hash + (soundLabels == null ? 0 : soundLabels.hashCode());
        hash = 31 * hash + (image == null ? 0 : image.hashCode());
        hash = 31 * hash + (selectedImage == null ? 0 : selectedImage.hashCode());
        hash = 31 * hash + (lockable ? 1 : 0);
        hash = 31 * hash + (visible ? 1 : 0);
        return hash;
    }

}

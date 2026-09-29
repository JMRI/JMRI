package jmri.jmrit.roster;

public class RosterEntryFunction {

    int fn;
    String label; 
    String soundLabels;
    String image;
    String selectedImage;
    boolean lockable;
    boolean visible;

    RosterEntryFunction(int fn) {
        this.fn = fn;
        label = null;
        soundLabels = null;
        image = null;
        selectedImage = null;
        lockable = true;
        visible = true;
    }

    RosterEntryFunction(RosterEntryFunction pEntry) {
        fn = pEntry.fn;
        label = pEntry.label;
        soundLabels = pEntry.soundLabels;
        image = pEntry.image;
        selectedImage = pEntry.selectedImage;
        lockable = pEntry.lockable;
        visible = pEntry.visible;
    }

    public int getFn() {
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

}

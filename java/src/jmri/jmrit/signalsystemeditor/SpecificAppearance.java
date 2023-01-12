package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * A specific appearance
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class SpecificAppearance {

    private String _aspectName;
    private final List<ImageLink> _imageLinks = new ArrayList<>();

    public SpecificAppearance() {
        this._aspectName = "";
    }

    public SpecificAppearance(String aspectName, ImageLink imageLink) {
        this._aspectName = aspectName;
    }

    public void setAspectName(String name) {
        this._aspectName = name;
    }

    public String getAspectName() {
        return this._aspectName;
    }

    public List<ImageLink> getImageLinks() {
        return this._imageLinks;
    }

}

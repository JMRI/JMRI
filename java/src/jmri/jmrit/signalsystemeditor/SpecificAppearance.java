package jmri.jmrit.signalsystemeditor;

/**
 * A specific appearance
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class SpecificAppearance {

    private String _aspectName;
    private ImageLink _imageLink;

    public SpecificAppearance() {
        this._aspectName = "";
        this._imageLink = null;
    }

    public SpecificAppearance(String aspectName, ImageLink imageLink) {
        this._aspectName = aspectName;
        this._imageLink = imageLink;
    }

    public void setAspectName(String name) {
        this._aspectName = name;
    }

    public String getAspectName() {
        return this._aspectName;
    }

    public void setImageLink(ImageLink imageLink) {
        this._imageLink = imageLink;
    }

    public ImageLink getImageLink() {
        return this._imageLink;
    }

}

package jmri.jmrit.signalsystemeditor;

/**
 * An appearance
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Appearance {

    private String _aspectName;
    private String _show;
    private String _reference;
    private ImageLink _imageLink;

    public Appearance(String aspectName, String show, String reference, ImageLink imageLink) {
        this._aspectName = aspectName;
        this._show = show;
        this._reference = reference;
        this._imageLink = imageLink;
    }

    public void setAspectName(String name) {
        this._aspectName = name;
    }

    public String getAspectName() {
        return this._aspectName;
    }

    public void setShow(String show) {
        this._show = show;
    }

    public String getShow() {
        return this._show;
    }

    public void setReference(String reference) {
        this._reference = reference;
    }

    public String getReference() {
        return this._reference;
    }

    public void setImageLink(ImageLink imageLink) {
        this._imageLink = imageLink;
    }

    public ImageLink getImageLink() {
        return this._imageLink;
    }

}

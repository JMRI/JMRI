package jmri.jmrit.signalsystemeditor;

/**
 * A link to an image with its type.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ImageLink {

    private String _imageLink;
    private ImageType _type;

    public ImageLink(String imageLink, ImageType type) {
        this._imageLink = imageLink;
        this._type = type;
    }

    public void setImageLink(String comment) {
        this._imageLink = comment;
    }

    public String getImageLink() {
        return this._imageLink;
    }

    public void setType(ImageType type) {
        this._type = type;
    }

    public ImageType getType() {
        return this._type;
    }

}

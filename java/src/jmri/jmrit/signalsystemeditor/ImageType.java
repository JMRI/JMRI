package jmri.jmrit.signalsystemeditor;

/**
 * An image type
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class ImageType {

    private String _type;

    public ImageType(String type) {
        this._type = type;
    }

    public void setType(String type) {
        this._type = type;
    }

    public String getType() {
        return this._type;
    }

}

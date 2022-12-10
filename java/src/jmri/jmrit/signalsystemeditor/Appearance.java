package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * An appearance
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class Appearance {

    private String _aspectName;
    private final List<String> _showList = new ArrayList<>();
    private final List<String> _references = new ArrayList<>();
    private final List<String> _comments = new ArrayList<>();
    private String _delay;
    private final List<ImageLink> _imageLinks = new ArrayList<>();

    public Appearance(String aspectName) {
        this._aspectName = aspectName;
    }

    public void setAspectName(String name) {
        this._aspectName = name;
    }

    public String getAspectName() {
        return this._aspectName;
    }

    public List<String> getShowList() {
        return this._showList;
    }

    public List<String> getReferences() {
        return this._references;
    }

    public List<String> getComments() {
        return this._comments;
    }

    public void setDelay(String delay) {
        this._delay = delay;
    }

    public String getDelay() {
        return this._delay;
    }

    public List<ImageLink> getImageLinks() {
        return this._imageLinks;
    }

}

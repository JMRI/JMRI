package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * A signal system
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class SignalSystem {

    private final String _folderName;
    private String _name;
    private String _processingInstructionHRef;
    private String _processingInstructionType;
    private String _aspectSchema;
    private String _date;
    private final List<String> _references = new ArrayList<>();
    private final Copyright _copyright = new Copyright();
    private final List<Author> _authors = new ArrayList<>();
    private final List<Aspect> _aspects = new ArrayList<>();
    private final List<Revision> _revisions = new ArrayList<>();
    private final List<ImageType> _imageTypes = new ArrayList<>();
    private final List<SignalMastType> _signalMastTypes = new ArrayList<>();

    public SignalSystem(String folderName) {
        this._folderName = folderName;
    }

    public String getFolderName() {
        return this._folderName;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getName() {
        return this._name;
    }

    public void setProcessingInstructionHRef(String href) {
        this._processingInstructionHRef = href;
    }

    public String getProcessingInstructionHRef() {
        return _processingInstructionHRef;
    }

    public void setProcessingInstructionType(String type) {
        this._processingInstructionType = type;
    }

    public String getProcessingInstructionType() {
        return _processingInstructionType;
    }

    public void setAspectSchema(String schema) {
        this._aspectSchema = schema;
    }

    public String getAspectSchema() {
        return this._aspectSchema;
    }

    public void setDate(String date) {
        this._date = date;
    }

    public String getDate() {
        return this._date;
    }

    public List<String> getReferences() {
        return this._references;
    }

    public Copyright getCopyright() {
        return this._copyright;
    }

    public List<Author> getAuthors() {
        return this._authors;
    }

    public List<Revision> getRevisions() {
        return this._revisions;
    }

    public List<Aspect> getAspects() {
        return this._aspects;
    }

    public List<ImageType> getImageTypes() {
        return this._imageTypes;
    }

    public ImageType getImageType(String type) {
        for (ImageType imageType : _imageTypes) {
            if (type.equals(imageType.getType())) {
                return imageType;
            }
        }
        throw new IllegalArgumentException("Image type '"+type+"' does not exists");
    }

    public List<SignalMastType> getSignalMastTypes() {
        return this._signalMastTypes;
    }

}

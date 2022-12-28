package jmri.jmrit.signalsystemeditor;

import java.util.*;

/**
 * A signal mast type
 * @author Daniel Bergqvist (C) 2022
 */
public class SignalMastType {

    private final String _fileName;
    private String _name;
    private String _processingInstructionHRef;
    private String _processingInstructionType;
    private String _description;
    private String _aspectTable;
    private String _appearanceSchema;
    private final List<StringWithLinks> _references = new ArrayList<>();
    private final List<StringWithLinks> _descriptions = new ArrayList<>();
    private final List<Appearance> _appearances = new ArrayList<>();
    private final SpecificAppearance _appearanceDanger = new SpecificAppearance();
    private final SpecificAppearance _appearancePermissive = new SpecificAppearance();
    private final SpecificAppearance _appearanceHeld = new SpecificAppearance();
    private final SpecificAppearance _appearanceDark = new SpecificAppearance();
    private final List<AspectMapping> _aspectMappings = new ArrayList<>();
    private final List<Author> _authors = new ArrayList<>();
    private final List<Revision> _revisions = new ArrayList<>();
    private final Copyright _copyright = new Copyright();

    public SignalMastType(String fileName) {
        this._fileName = fileName;
    }

    public String getFileName() {
        return this._fileName;
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

    public Copyright getCopyright() {
        return this._copyright;
    }

    public List<Author> getAuthors() {
        return this._authors;
    }

    public List<Revision> getRevisions() {
        return this._revisions;
    }

    public List<StringWithLinks> getReferences() {
        return this._references;
    }

    public List<StringWithLinks> getDescriptions() {
        return this._descriptions;
    }

    public List<Appearance> getAppearances() {
        return this._appearances;
    }

    public SpecificAppearance getAppearanceDanger() {
        return this._appearanceDanger;
    }

    public SpecificAppearance getAppearancePermissive() {
        return this._appearancePermissive;
    }

    public SpecificAppearance getAppearanceHeld() {
        return this._appearanceHeld;
    }

    public SpecificAppearance getAppearanceDark() {
        return this._appearanceDark;
    }

    public List<AspectMapping> getAspectMappings() {
        return this._aspectMappings;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    public String getDescription() {
        return this._description;
    }

    public void setAspectTable(String aspectTable) {
        this._aspectTable = aspectTable;
    }

    public String getAspectTable() {
        return this._aspectTable;
    }

    public void setAppearanceSchema(String schema) {
        this._appearanceSchema = schema;
    }

    public String getAppearanceSchema() {
        return this._appearanceSchema;
    }

}

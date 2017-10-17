package jmri.web.servlet.roster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.InputStream;

/**
 * object type for storing uploaded file data based on examples at
 * http://hmkcode.com/java-servlet-jquery-file-upload/ 
 *
 */
@JsonIgnoreProperties({"content"}) //this prevents serializer error when mapping to send back
public class FileMeta {

    private String fileName;
    private String fileSize;
    private String fileType;
    private boolean fileReplace;
    private String rosterGroup;
    private InputStream fileContent;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean getFileReplace() {
        return fileReplace;
    }

    public void setFileReplace(boolean fileReplace) {
        this.fileReplace = fileReplace;
    }

    public InputStream getContent() {
        return this.fileContent;
    }

    public void setContent(InputStream content) {
        this.fileContent = content;
    }

    public String getRosterGroup() {
        return rosterGroup;
    }

    public void setRosterGroup(String rosterGroup) {
        this.rosterGroup = rosterGroup;
    }

    @Override
    public String toString() {
        return "FileMeta [fileName=" + fileName + ", fileSize=" + fileSize
                + ", fileType=" + fileType + "]";
    }

}

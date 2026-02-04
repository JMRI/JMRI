package jmri.jmrit.dispatcher;

public class TrainInfoFileSummary {

    private String filename = "";
    private String transitName = "";
    private String startBlockName = "";
    private String endBlockName = "";
    private String trainName = "";
    private String dccAddress = "";
    
    public TrainInfoFileSummary(String filename, String trainName, String transitName,
            String startBlockName, String endBlockName, String dccAddress) {
        this.filename = filename;
        this.trainName = trainName;
        this.transitName = transitName;
        this.startBlockName = startBlockName;
        this.endBlockName = endBlockName;
        this.dccAddress = dccAddress;
    }

    public TrainInfoFileSummary(String filename) {
        this.filename = filename;
        this.transitName = "Invalid File";
    }

    public String getFileName() {
        return filename;
    }
    public String getTransitName() {
        return transitName;
    }
    public String getStartBlockName() {
        return startBlockName;
    }
    public String getEndBlockName() {
        return endBlockName;
    }
    public String getTrainName() {
        return trainName;
    }
    public String getDccAddress() {
        return dccAddress ;
    }
    
    @Override
    public String toString() {
        return filename;
    }
}

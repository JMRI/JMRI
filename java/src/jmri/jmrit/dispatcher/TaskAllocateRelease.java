package jmri.jmrit.dispatcher;

public class TaskAllocateRelease {

    TaskAllocateRelease(TaskAction action) {
        act = action;
    }

    TaskAllocateRelease(TaskAction action, String trainName) {
        act = action;
        this.trainName = trainName;
    }

    TaskAllocateRelease(TaskAction action, AllocatedSection aSection, boolean termTrain) {
        act = action;
        allocatedSection = aSection;
        terminateingTrain = termTrain;
    }

    private TaskAction act;
    private String trainName = null;
    private AllocatedSection allocatedSection = null;
    private boolean terminateingTrain = false;

    public enum TaskAction {
        SCAN_REQUESTS,
        RELEASE_RESERVED,
        RELEASE_ONE,
        AUTO_RELEASE,
        ABORT;
    }

    public TaskAction getAction() {
        return act;
    }

    public String getTrainName() {
        return trainName;
    }

    public AllocatedSection getAllocatedSection() {
        return allocatedSection;
    }

    public boolean getTerminatingTrain() {
        return terminateingTrain;
    }
}

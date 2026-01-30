package rand;

public class CloseTurnout extends TurnoutOp {

    public CloseTurnout(String turnoutName) {
        super(turnoutName);
    }

    @Override
    void perform() {
        closeTurnout();
    }

}

package rand;

public class ThrowTurnout extends TurnoutOp {
    public ThrowTurnout(String turnoutName) {
        super(turnoutName);
    }

    @Override
    void perform() {
        throwTurnout();
    }

}

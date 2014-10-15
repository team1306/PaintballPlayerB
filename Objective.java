/**
 *
 * @author Finn
 */
public class Objective {
    public Objective(Point position, Action action) {
        this.position = position;
        this.action = action;
    }
    public Point getPosition() {
        return position;
    }
    public Action getAction() {
        return action;
    }

    @Override
    public String toString() {
        return action + " at " + position;
    }
    private final Point position;
    private final Action action;
}

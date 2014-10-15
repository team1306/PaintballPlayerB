import java.util.Objects;

/**
 * Class for generating the action commands.
 */
public class Action
{
    private final String fCommand;
    private final int ammoRequired;
    private final Point target;

    private Action( String command, int ballsRequired, Point target)
    {
        fCommand = command;
        this.ammoRequired = ballsRequired;
        this.target = target;
    }

    @Override
    public String toString()
    {
        return fCommand;
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof Action) {
            return other.toString().equals(fCommand);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.fCommand);
        return hash;
    }
    
    public int ammoRequired() {
        return ammoRequired;
    }
    public Point getTarget() {
        return target;
    }

    /**
     * Create an idle action.
     *
     * @return the idle action
     */
    public static Action makeIdleAction()
    {
        return new Action( "idle", 0, null);
    }

    /**
     * Create a move action.
     *
     * @param target the target point on the grid
     *
     * @return a move action to the target
     */
    public static Action makeMoveAction( Point target )
    {
        return new Action( String.format( "move %d %d", target.getX(), target.getY() ), 0, target);
    }


    /**
     * Creates an action to pickup the object at the given coordinates.
     *
     * @param point the target
     *
     * @return a pickup action
     */
    public static Action makePickupItemAction(Point point)
    {
        return new Action( String.format( "pickup %d %d", point.getX(), point.getY()), 0, point);
    }

    /**
     * Creates an action to launch a paintball at a target.
     *
     * @param target the coordinates of the target as a point
     * @param rapidFire if it is shooting with a rapid-fire launcher
     *
     * @return a launch action
     */
    public static Action makeLaunchAction( Point target, boolean rapidFire)
    {
        return new Action( String.format( "launch %d %d", target.getX(), target.getY() ), rapidFire ? 3 : 1, target);
    }

    /**
     * Creates an action to launch a paintball using rapid-fire mode.
     *
     * @param start the first target coordinate
     * @param end the last target coordinate
     *
     * @return a rapid-fire launch command
     */
    public static Action makeLaunchAction( Point start, Point end )
    {
        return new Action( String.format( "launch %d %d %d %d", start.getX(), start.getY(), end.getX(), end.getY() ), 3, start);
    }

    /**
     * Creates a defend action.
     *
     * @return a defend action
     */
    public static Action makeDefendAction()
    {
        return new Action( "defend", 0 , null);
    }

    /**
     * Creates an undefend action (exits defensive mode).
     *
     * @return an undefend action
     */
    public static Action makeUndefendAction()
    {
        return new Action( "undefend", 0, null);
    }

    /**
     * Creates a crouch action.
     *
     * @return a crouch action
     */
    public static Action makeCrouchAction()
    {
        return new Action( "crouch", 0, null);
    }

    /**
     * Creates a stand action.
     *
     * @return a stand action
     */
    public static Action makeStandAction()
    {
        return new Action( "stand", 0, null);
    }

    /**
     * Creates an action to plant a flag at a location.
     *
     * @param point the target
     * @return an action to plant a flag
     */
    public static Action makePlantAction(Point point)
    {
        return new Action( String.format( "plant %d %d", point.getX(), point.getY()), 25, point);
    }

    /**
     * Creates an action to drop some number of paintballs.
     *
     * @param dropTarget the coordinates where to drop
     * @param numPaintballsToDrop the number of paintballs to drop
     *
     * @return an action to drop paintballs
     */
    public static Action makeDropAction( Point dropTarget, int numPaintballsToDrop )
    {
        return new Action( String.format( "drop %d %d %d", dropTarget.getX(), dropTarget.getY(), numPaintballsToDrop ), numPaintballsToDrop, dropTarget);
    }
}
/**
 * This class holds useful global constants.
 */
public class Constants
{
    /**
     * The Red player.
     */
    public static final int RED = 0;

    /**
     * The Blue player.
     */
    public static final int BLUE = 1;

    /**
     * The number of children on a single team.
     */
    public static final int NUM_CHILDREN_PER_TEAM = 4;

    /**
     * The number of spaces on one of the dimensions of the square field.
     */
    public static final int FIELD_DIMENSION = 31;

    /**
     * Field encodings as integers.
     */
    public static final int EMPTY               = 0;
    public static final int TREE                = 1;
    public static final int LOW_WALL            = 2;
    public static final int HIGH_WALL           = 3;
    public static final int RED_FLAG            = 4;
    public static final int BLUE_FLAG           = 5;
    public static final int ADAPTER             = 6;
    public static final int SHIELD              = 7;
    public static final int BASIC_LAUNCHER      = 8;
    public static final int RAPID_FIRE_LAUNCHER = 9;
    public static final int UNKNOWN             = 10;

    /**
     * Number of actions
     */
    public static final int NUM_ACTIONS = 10;

    /**
     * The maximum euclidean distance a paintball can travel when launched.
     */
    public static final int MAX_LAUNCH_DISTANCE = 24;

    /**
     * Inventory encodings.
     */
    public static final int NOTHING                        = 0;
    public static final int ONE_BASIC_LAUNCHER             = 1;
    public static final int ONE_SHIELD                     = 2;
    public static final int SHIELD_AND_BASIC_LAUNCHER      = 3;
    public static final int ONE_RAPID_FIRE_LAUNCHER        = 4;
    public static final int SHIELD_AND_RAPID_FIRE_LAUNCHER = 5;
}

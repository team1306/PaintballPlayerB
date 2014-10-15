import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 *
 * @author Finn
 */
public class Field {

    private int[][] field;
    private int[][] storedField;
    private final Child[] children;
    private int turnNumber;

    public Field(int[][] field, Child[] children) {
        this.field = copyField(field);
        storedField = copyField(field);
        this.children = children;
        turnNumber = -1;
    }

    public void update(int[][] newField) {
//        log();
        field = copyField(newField);
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                if (field[i][j] == Constants.UNKNOWN && isWorthTracking(storedField[i][j])) {
                    field[i][j] = storedField[i][j];
                }
            }
        }
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                if (field[i][j] == Constants.UNKNOWN && isWorthReflecting(field[30 - i][30 - j])) {
                    if (storedField[i][j] == Constants.UNKNOWN) {
                        field[i][j] = field[30 - i][30 - j];
                    } else if (storedField[i][j] == Constants.EMPTY){
                        field[i][j] = Constants.EMPTY;
                    }
                }
            }
        }
        storedField = copyField(field);
        turnNumber++;
//        log();
    }
    
    public boolean isEmptyAt(Point point) {
        int x = point.getX();
        int y = point.getY();
        if (!point.exists()) {
            return false;
        }
        if (type(point) != Constants.EMPTY && type(point) != Constants.UNKNOWN) {
            return false;
        }
        for (Child child : children) {
            Point childPosition = child.getPosition();
            if (childPosition.equals(point) || child.getBlockedSpaces().contains(point)) {
                return false;
            }
        }
        return true;
    }
    
    public Set<Child> enemies() {
        return new HashSet<>(Arrays.asList(Arrays.copyOfRange(children, 4, children.length)));
    }
    
    public boolean isChildPickingUp(Point target) {
        for (Child child : children) {
            if (child.getCurrentAction() != null && child.getCurrentAction().toString().equals(Action.makePickupItemAction(target).toString())) {
                return true;
            }
        }
        return false;
    }

    public int type(Point point) {
        return field[point.getX()][point.getY()];
    }
    public int height(Point point) {
        for (Child child : children) {
            if (child.getPosition().equals(point) || child.getBlockedSpaces().contains(point)) {
                return child.isStanding() ? 9 : 3;
            }
        }
        switch(type(point)) {
            case Constants.TREE:
                return 20;
            case Constants.LOW_WALL:
                return 3;
            case Constants.HIGH_WALL:
                return 7;
            case Constants.RED_FLAG:
                return 7;
            case Constants.BLUE_FLAG:
                return 7;
            default:
                return 0;
        }
    }
    public int turnNumber() {
        return turnNumber;
    }
    public boolean scoutHasShield() {
        return children[0].isHoldingShield();
    }
    private boolean isWorthTracking(int type) {
        return type == Constants.ADAPTER
                || type == Constants.BLUE_FLAG
                || type == Constants.HIGH_WALL
                || type == Constants.LOW_WALL
                || type == Constants.SHIELD
                || type == Constants.TREE
                || type < 0;
    }
    private boolean isWorthReflecting(int type) {
        return type == Constants.ADAPTER
                || type == Constants.HIGH_WALL
                || type == Constants.LOW_WALL
                || type == Constants.SHIELD
                || type == Constants.TREE
                || type < 0;
    }
    public boolean canEnemySee(Point point) {
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                Point blueFlag = new Point(i, j);
                if (point.distance(blueFlag) < 8 && type(blueFlag) == Constants.BLUE_FLAG) {
                    return true;
                }
            }
        }
        for (Child enemy : enemies()) {
            if (!enemy.getPosition().equals(new Point()) && point.distance(enemy.getPosition()) < 8) {
                return true;
            }
        }
        return false;
    }
//    public void log() {
//        String fieldOutput = "\n";
//        for (int y = 30; y >= 0; y--) {
//            for(int x = 0; x < 31; x++) {
//                fieldOutput += (field[x][y] == 10 ? "*" : field[x][y]) + " ";
//            }
//            fieldOutput += "\n";
//        }
//        LOG.info(fieldOutput);
//    }
    public static int[][] copyField(int[][] field) {
        int[][] copy = new int[Constants.FIELD_DIMENSION][Constants.FIELD_DIMENSION];
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                copy[i][j] = field[i][j];
            }
        }
        return copy;
    }
//    private static final Logger LOG = Logger.getLogger("1306B");
    
}

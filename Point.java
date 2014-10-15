import java.util.HashSet;
import java.util.Set;

/**
 * A point on the grid.
 */
public class Point {

    // the X coordinate
    private final int fX;

    // the  coordinate
    private final int fY;

    private static Field field;

    /**
     * Construct a new point.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Point(int x, int y) {
        fX = x;
        fY = y;
    }

    /**
     * Default constructor makes a point off the field.
     */
    public Point() {
        this(-1, -1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point other = (Point) obj;
            return fX == other.getX() && fY == other.getY();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.fX;
        hash = 41 * hash + this.fY;
        return hash;
    }

    @Override
    public String toString() {
        return "(" + fX + ", " + fY + ")";
    }

    public static void updateField(Field updatedField) {
        field = updatedField;
    }

    /**
     * Gets the X coordinate value.
     *
     * @return the X coordinate value
     */
    public int getX() {
        return fX;
    }

    /**
     * Gets the Y coordinate value.
     *
     * @return the Y coordinate value
     */
    public int getY() {
        return fY;
    }

    public double distance(Point other) {
        int run = other.getX() - fX;
        int rise = other.getY() - fY;
        return Math.sqrt(run * run + rise * rise);
    }

    public boolean exists() {
        return fX >= 0 && fX < Constants.FIELD_DIMENSION && fY >= 0 && fY < Constants.FIELD_DIMENSION;
    }

    public Point add(Point other) {
        return new Point(fX + other.getX(), fY + other.getY());
    }

    public Set<Point> pointsWithin(double radius) {
        Set<Point> moves = new HashSet<>(4 * (int) (radius * radius));
        for (int i = -(int) radius; i <= radius; i++) {
            for (int j = -(int) radius; j <= radius; j++) {
                Point possibleMove = new Point(i, j);
                if ((new Point(0, 0)).distance(possibleMove) <= radius) {
                    moves.add(possibleMove);
                }
            }
        }
        Set<Point> locations = new HashSet<>(moves.size());
        for (Point move : moves) {
            Point location = add(move);
            if (location.exists() && !location.equals(this)) {
                locations.add(location);
            }
        }
        return locations;
    }
}

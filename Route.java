import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Finn
 */
public class Route {

    private static Field field = null;

    public static void setField(Field gameField) {
        field = gameField;
    }

    public static Point closestPointNextTo(Point origin, Point target) {
        Point closest = null;
        double shortestDistance = Double.MAX_VALUE;
        for (Point adjacentPosition : target.pointsWithin(1.5)) {
            if (origin.equals(adjacentPosition) || field.isEmptyAt(adjacentPosition) && origin.distance(adjacentPosition) < shortestDistance) {
                shortestDistance = origin.distance(adjacentPosition);
                closest = adjacentPosition;
            }
        }
        return closest;
    }

    public static List<Point> straightRoute(Point from, Point to) {
        int x1 = from.getX();
        int y1 = from.getY();
        int x2 = to.getX();
        int y2 = to.getY();
        int numberOfSteps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        List<Point> straightSteps = new ArrayList<>(numberOfSteps);
        for (int t = 0; t <= numberOfSteps; t++) {
            straightSteps.add(t, new Point(x1 + roundAwayFromZero(t * (x2 - x1) / ((double) numberOfSteps)), y1 + roundAwayFromZero(t * (y2 - y1) / ((double) numberOfSteps))));
        }
        return straightSteps;
    }

    public static Point shootAt(Point start, Point target) {
        double shootDistance = start.distance(target);
        for (int power = 24; power >= shootDistance; power--) {
            int x1 = start.getX();
            int y1 = start.getY();
            int x2 = target.getX();
            int y2 = target.getY();
            double slope = (y2 - y1) / ((double) (x2 - x1));
            double deltaX = (power / Math.sqrt(slope * slope + 1)) * (x2 > x1 ? 1 : -1);
            int aimX = x1 + roundAwayFromZero(deltaX);
            int aimY = y1 + roundAwayFromZero(slope * (aimX - x1));

            Point aimPoint = new Point(aimX, aimY);
            if (start.distance(aimPoint) <= Constants.MAX_LAUNCH_DISTANCE) {
                Point hitPoint = hitPoint(start, aimPoint);
//                LOG.log(Level.INFO, "Route: {0} to {1} Target: {2} Hitpoint: {3}", new Object[]{start, aimPoint, target, hitPoint});
                if (hitPoint != null && hitPoint.equals(target)) {
                    return aimPoint;
                }
            }
        }
        return null;
    }

    private static boolean hasObstacles(Collection<Point> points) {
        for (Point point : points) {
            if (!field.isEmptyAt(point)) {
                return true;
            }
        }
        return false;
    }

    private static List<Point> reconstructPath(Map<Point, Point> cameFrom, Point currentNode) {
        if (cameFrom.containsKey(currentNode)) {
            List<Point> p = reconstructPath(cameFrom, cameFrom.get(currentNode));
            p.add(currentNode);
            return p;
        } else {
            List<Point> p = new ArrayList<>(16);
            p.add(currentNode);
            return p;
        }
    }

    private static Set<Point> neighborNodes(Point node, int distance) {
        Set<Point> nodes = new HashSet<>(8);
        for (Point neighbor : node.pointsWithin(distance)) {
            if (openPath(node, neighbor)) {
                nodes.add(neighbor);
            }
        }
        return nodes;

    }

    private static boolean openPath(Point from, Point to) {
        List<Point> straightRoute = straightRoute(from, to);
        return !hasObstacles(straightRoute.subList(1, straightRoute.size()));
    }

    private static Point hitPoint(Point from, Point to) {
        List<Point> straightRoute = straightRoute(from, to);
        for (int i = 1; i < straightRoute.size(); i++) {
            Point point = straightRoute.get(i);
            if (!point.exists()) {
                return null;
            }
            double height;
            if (i > 0.5 * straightRoute.size()) {
                height = 18.0 * (1 - i / (double) straightRoute.size());
                if (height <= 0) {
                    return null;
                }
            } else {
                height = 9.0;
            }
            if (height <= field.height(point)) {
                return point;
            }
        }
        return null;
    }
    
    private static List<Point> routeTo(Point start, Point goal, int stepDistance) {
        Set<Point> closedSet = new HashSet<>(16);
        Set<Point> openSet = new HashSet<>(16);
        openSet.add(start);
        Map<Point, Point> cameFrom = new HashMap<>(16);

        Map<Point, Integer> gScore = new HashMap<>(16);
        gScore.put(start, 0);
        Map<Point, Integer> fScore = new HashMap<>(16);
        fScore.put(start, gScore.get(start) + (int) Math.ceil(start.distance(goal) / stepDistance));
        while (!openSet.isEmpty()) {
            int lowestFScore = Integer.MAX_VALUE;
            Point current = null;
            for (Point node : openSet) {
                int fScoreOfNode = fScore.get(node);
                if (fScoreOfNode < lowestFScore) {
                    lowestFScore = fScoreOfNode;
                    current = node;
                }
            }
            if (current.equals(goal)) {
                return reconstructPath(cameFrom, goal);
            }

            openSet.remove(current);
            closedSet.add(current);
            for (Point neighbor : neighborNodes(current, stepDistance)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                int tentativeGScore = gScore.get(current) + 1;

                if (!openSet.contains(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeGScore);
                    fScore.put(neighbor, gScore.get(neighbor) + (int) Math.ceil(neighbor.distance(goal) / 3));
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    public static Point firstMove(Point start, Point goal, int stepDistance) {

        List<Point> steps = routeTo(start, goal, stepDistance);

        if (steps == null) {
            return null;
        }

        return steps.get(1);
    }

    private static int roundAwayFromZero(double x) {
        if (x < 0) {
            return -(int) (Math.round(-x));
        } else {
            return (int) (Math.round(x));
        }
    }

    private static int floorTowardZero(double x) {
        if (x > 0) {
            return (int) Math.floor(x);
        } else {
            return -(int) (Math.floor(-x));
        }
    }

//    private static final Logger LOG = Logger.getLogger("1306B");

    private Route() {
    }
}

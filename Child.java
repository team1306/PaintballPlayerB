import java.util.ArrayList;
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
 * Class that holds the state of a child.
 */
public abstract class Child {

    // Location of the child.
    private Point fPos;

    private final Set<Point> blockedSpaces;
    private Action currentAction;

    // True if the child is standing.
    private boolean fIsStanding;

    // True if the child is defending
    private boolean fIsDefending;

    // Side the child is on.
    private final int fColor;

    // What the child is holding.
    private int fHolding;

    // Number of paintballs being held by the child
    private int fPaintballs;

    private static final Random RANDOM = new Random();

    protected final Field field;

    public Child(int color, Field field) {
        fColor = color;
        fPos = new Point();
        fIsStanding = true;
        fIsDefending = false;
        fHolding = 0;
        fPaintballs = 0;

        this.field = field;

        randomMove = null;

        blockedSpaces = new HashSet<>(4);
    }

    /**
     * Sets the position of this child.
     *
     * @param point the coordinates of the child
     */
    public void setPosition(Point point) {
        fPos = point;
        blockedSpaces.clear();
        currentAction = null;
    }

    public Point getPosition() {
        return fPos;
    }

    protected int stepDistance() {
        return fIsStanding ? fIsDefending ? 2 : 3 : 1;
    }

    public Set<Point> getBlockedSpaces() {
        return Collections.unmodifiableSet(blockedSpaces);
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    /**
     * Sets the posture of this child (either standing or crouching).
     *
     * @param isStanding true if the child is standing, false if the child is
     * crouching
     */
    public void setIsStanding(boolean isStanding) {
        fIsStanding = isStanding;
    }

    protected boolean isStanding() {
        return fIsStanding;
    }

    /**
     * Sets the defensive mode of this child (either defending or not
     * defending).
     *
     * @param isDefending true if the child is defending, false if the child is
     * not
     */
    public void setIsDefending(boolean isDefending) {
        fIsDefending = isDefending;
    }

    protected boolean isDefending() {
        return fIsDefending;
    }

    /**
     * Sets what the child is holding. This is an encoding of the possible
     * inventory combinations.
     *
     * @param holding the code for what the child is holding
     */
    public void setHolding(int holding) {
        fHolding = holding;
    }

    /**
     * Sets the number of paintballs this child is holding.
     *
     * @param numPaintballs the number of paintballs the child is holding
     */
    public void setPaintballCount(int numPaintballs) {
        fPaintballs = numPaintballs;
    }

    protected int getPaintballCount() {
        return fPaintballs;
    }

    private Objective randomMove;

    private Map<Objective, Double> priorityRankings() {
        Map<Objective, Double> rankings = new HashMap<>(8);

        Point closestShield = null;
        Point closestAdapter = null;
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                Point point = new Point(i, j);
                if (field.type(point) == Constants.SHIELD && (closestShield == null || getPosition().distance(point) < getPosition().distance(closestShield))) {
                    closestShield = point;
                } else if (field.type(point) == Constants.ADAPTER && (closestAdapter == null || getPosition().distance(point) < getPosition().distance(closestAdapter))) {
                    closestAdapter = point;
                } else if (field.type(point) < 0) {
                    Objective obj = pickup(point);
                    if (obj != null) {
                        rankings.put(obj, getAmmoImportance(obj.getPosition(), point));
                    }
                }
            }
        }
        if (closestShield != null) {
            Objective obj = pickup(closestShield);
            if (obj != null) {
                rankings.put(obj, getShieldImportance(obj.getPosition()));
            }
        }
        if (closestAdapter != null) {
            Objective obj = pickup(closestAdapter);
            if (obj != null) {
                rankings.put(obj, getAdapterImportance(obj.getPosition()));
            }
        }

        double shortestDistance = Double.MAX_VALUE;
        Point closestEnemy = null;
        Point closestReachableEnemy = null;
        for (Child enemy : field.enemies()) {
            Point enemyPosition = enemy.getPosition();
            double distance = getPosition().distance(enemyPosition);
            if (!enemyPosition.equals(new Point()) && distance < shortestDistance && !enemy.isDefending()) {
                if (enemy.isStanding()) {
                    shortestDistance = distance;
                    closestEnemy = enemyPosition;
                }
                if (Route.shootAt(getPosition(), enemyPosition) != null) {
                    closestReachableEnemy = enemyPosition;
                }
            }
        }
        if (closestReachableEnemy != null) {
            Point shootTarget = Route.shootAt(getPosition(), closestReachableEnemy);
            Objective obj = new Objective(getPosition(), Action.makeLaunchAction(shootTarget, isHoldingRapidFireLauncher()));
            rankings.put(obj, getShootImportance(closestReachableEnemy, obj.getAction().ammoRequired()));
        }
        if (closestEnemy != null) {
            Objective obj = new Objective(Route.closestPointNextTo(getPosition(), closestEnemy), Action.makeLaunchAction(closestEnemy, isHoldingRapidFireLauncher()));
            rankings.put(obj, getMoveToEnemyImportance(closestEnemy));
        }

        if (randomMove == null || field.type(randomMove.getPosition()) != Constants.UNKNOWN) {
            randomMove = randomMove();
        }
        rankings.put(randomMove, getRandomMoveImportance(randomMove.getPosition()));

        Point closestFlagPosition = null;
        for (Point position : FLAG_POSITIONS) {
            if (!hasFlag(position)) {
                if (closestFlagPosition == null || fPos.distance(position) < fPos.distance(closestFlagPosition)) {
                    closestFlagPosition = position;
                }
            }
        }
        if (closestFlagPosition != null) {
            if (field.isEmptyAt(closestFlagPosition)) {
                Objective obj = plantFlag(closestFlagPosition);
                rankings.put(obj, getFlagImportance(obj.getPosition()));
            }
        }

        return rankings;
    }

    private Objective chooseObjective() {
        Map<Objective, Double> rankings = priorityRankings();

        Objective bestObjective = null;
        Double mostPoints = 0.0;
//        LOG.log(Level.INFO, "Objectives: {0}", rankings.keySet());
        for (Objective objective : rankings.keySet()) {
            double ranking = rankings.get(objective);
//            LOG.log(Level.INFO, "Ranking of {0} at {1}: {2}", new Object[]{objective.getAction(), objective.getPosition(), ranking});
            if (ranking > mostPoints && enoughAmmoFor(objective.getAction())) {
                mostPoints = ranking;
                bestObjective = objective;
            }
        }
//        LOG.log(Level.INFO, "Best objective: {0}", bestObjective == null ? null : bestObjective.getAction());
//        if (bestObjective.getAction().equals(Action.makeIdleAction())) {
//            field.log();
//        }
//        if (bestObjective == null || bestObjective.getAction().equals(Action.makeIdleAction())) {
//            LOG.info("No objectives!");
//            field.log();
//        }
        String action = bestObjective.getAction().toString().split(" ", 2)[0];
        if (action.equals("launch")) {
            if (!fIsStanding) {
                return new Objective(fPos, Action.makeStandAction());
            } else if (fIsDefending) {
                return new Objective(fPos, Action.makeUndefendAction());
            }
        } else if (field.canEnemySee(fPos)) {
            if (!fIsDefending) {
                if (isHoldingShield()) {
                    return new Objective(fPos, Action.makeDefendAction());
                } else if (fIsStanding) {
                    if (!action.equals("plant")) {
                        return new Objective(fPos, Action.makeCrouchAction());
                    }
                } else if (action.equals("plant") && bestObjective.getPosition().equals(fPos)){
                    return new Objective(fPos, Action.makeStandAction());
                }
            } else if (!fIsStanding) {
                return new Objective(fPos, Action.makeStandAction());
            }
        } else {
            if (!fIsStanding) {
                return new Objective(fPos, Action.makeStandAction());
            } else if (fIsDefending) {
                return new Objective(fPos, Action.makeUndefendAction());
            }
        }
//        LOG.log(Level.INFO, "{0} Best Objective: {1}", new Object[]{field.turnNumber(), bestObjective});
        return bestObjective;
    }

    protected Objective crouch() {
        if (fIsStanding && !fIsDefending) {
            return new Objective(fPos, Action.makeCrouchAction());
        }
        return null;
    }

    protected Objective randomMove() {
        List<Point> randomPoints = new ArrayList<>(128);
        List<Point> unexploredPoints = new ArrayList<>(128);
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                Point point = new Point(i, j);
                if (field.type(point) == Constants.UNKNOWN) {
                    randomPoints.add(point);
                    if (!field.canEnemySee(point)) {
                        unexploredPoints.add(point);
                    }
                }
            }
        }
        Point randomPoint;
        if (!unexploredPoints.isEmpty()) {
            randomPoints = unexploredPoints;
        }
        if (randomPoints.isEmpty()) {
            do {
                randomPoint = new Point(RANDOM.nextInt(Constants.FIELD_DIMENSION), RANDOM.nextInt(Constants.FIELD_DIMENSION));
            } while (!field.isEmptyAt(randomPoint));
        } else {
            randomPoint = randomPoints.get(RANDOM.nextInt(randomPoints.size()));
        }
        return new Objective(randomPoint, Action.makeIdleAction());
    }

    private Objective plantFlag(Point location) {
        Point closestAdjacent = Route.closestPointNextTo(getPosition(), location);
        if (field.isEmptyAt(location)) {
            return new Objective(closestAdjacent, Action.makePlantAction(location));
        } else {
            return new Objective(Route.closestPointNextTo(getPosition(), closestAdjacent), Action.makePlantAction(closestAdjacent));
        }
    }

    public final Action chooseAction() {
        Objective currentObj = chooseObjective();
//        if (!enoughAmmoFor(currentObj.getAction())) {
//            Objective reload = reload();
//            Objective findItem = findItem();
//            Objective defend = defend();
//            Objective crouch = crouch();
//            Objective randomMove = randomMove();
//            if (reload != null) {
//                currentObj = reload;
//            } else if (findItem != null) {
//                currentObj = findItem;
//            } else if (defend != null) {
//                currentObj = defend;
//            } else if (crouch != null) {
//                currentObj = crouch;
//            } else {
//                currentObj = randomMove;
//            }
//        }
        Point position = currentObj.getPosition();
        if (position.equals(fPos)) {
            currentAction = currentObj.getAction();
        } else {
            Point move = Route.firstMove(fPos, position, stepDistance());
            blockedSpaces.addAll(Route.straightRoute(fPos, move));
            currentAction = Action.makeMoveAction(move);
        }
        return currentAction;
    }

    protected boolean enoughAmmoFor(Action action) {
        return action.ammoRequired() <= fPaintballs;
    }

    protected Objective reload() {
        Point closestAmmo = null;
        double shortestDistance = Double.MAX_VALUE;
        for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
            for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                Point point = new Point(i, j);
                if (field.type(point) < 0 && fPos.distance(point) < shortestDistance && !field.isChildPickingUp(point)) {
                    closestAmmo = point;
                    shortestDistance = fPos.distance(point);
                }
            }
        }
        Objective pickup = pickup(closestAmmo);
        if (pickup == null) {
            return null;
        }
        return pickup;
    }

    protected Objective pickup(Point target) {
        if (target == null || field.isChildPickingUp(target)) {
            return null;
        }
        Point goTo = Route.closestPointNextTo(fPos, target);
        if (goTo != null && !field.isChildPickingUp(target)) {
            return new Objective(goTo, Action.makePickupItemAction(target));
        }

        return null;
    }

    protected Objective goTo(Point target) {
        return new Objective(target, Action.makeIdleAction());
    }

    protected double getShieldImportance(Point standPosition) {
        if (isHoldingShield()) {
            return 0;
        }
        return getMoveImportance(standPosition);
    }

    protected double getAdapterImportance(Point standPosition) {
        if (isHoldingRapidFireLauncher()) {
            return 0;
        }
        return getMoveImportance(standPosition) * (fPaintballs + 1) / 25;
    }

    protected double getAmmoImportance(Point standPosition, Point ammoPosition) {
        int numberOfPaintballs = -field.type(ammoPosition);
        return getMoveImportance(standPosition) * numberOfPaintballs / fPaintballs;
    }

    private final double getMoveImportance(Point moveTo) {
        double moves = Math.ceil(fPos.distance(moveTo) / stepDistance());
        return 1.0 / moves;
    }

    protected double getRandomMoveImportance(Point moveTo) {
        return getMoveImportance(moveTo);
    }

    protected double getMoveToEnemyImportance(Point standPosition) {
        return getMoveImportance(standPosition) * fPaintballs / 25;
    }

    protected double getFlagImportance(Point standPosition) {
        return getMoveImportance(standPosition) * fPaintballs / 25 * (150 - field.turnNumber()) / 100;
    }

    protected double getShootImportance(Point target, int ammoRequired) {
        return 1.0 / getPosition().distance(target) * 3 * getPaintballCount() / ammoRequired;
    }

    /**
     * Determines if this child is holding a shield.
     *
     * @return true if the child is holding a shield, false otherwise.
     */
    protected boolean isHoldingShield() {
        return fHolding == Constants.ONE_SHIELD
                || fHolding == Constants.SHIELD_AND_BASIC_LAUNCHER
                || fHolding == Constants.SHIELD_AND_RAPID_FIRE_LAUNCHER;
    }

    /**
     * Determines if this child is holding a rapid fire launcher.
     *
     * @return true if the child is holding a rapid fire launcher, false
     * otherwise.
     */
    protected boolean isHoldingRapidFireLauncher() {
        return fHolding == Constants.ONE_RAPID_FIRE_LAUNCHER
                || fHolding == Constants.SHIELD_AND_RAPID_FIRE_LAUNCHER;
    }

    private static final Set<Point> FLAG_POSITIONS = new HashSet<>(5);

    static {
//        FLAG_POSITIONS.add(new Point(10, 5));
//        FLAG_POSITIONS.add(new Point(20, 5));
//        FLAG_POSITIONS.add(new Point(5, 15));
//        FLAG_POSITIONS.add(new Point(15, 15));
//        FLAG_POSITIONS.add(new Point(25, 15));
//        FLAG_POSITIONS.add(new Point(10, 25));
//        FLAG_POSITIONS.add(new Point(20, 25));
        FLAG_POSITIONS.add(new Point(5, 5));
        FLAG_POSITIONS.add(new Point(5, 25));
        FLAG_POSITIONS.add(new Point(15, 15));
        FLAG_POSITIONS.add(new Point(25, 5));
        FLAG_POSITIONS.add(new Point(25, 25));
    }

    private boolean hasFlag(Point point) {
        if (field.type(point) == Constants.RED_FLAG) {
            return true;
        }
        for (Point adjacentPoint : point.pointsWithin(1.5)) {
            if (field.type(adjacentPoint) == Constants.RED_FLAG) {
                return true;
            }
        }
        return false;
    }

//    private static final Logger LOG = Logger.getLogger("1306B");
}

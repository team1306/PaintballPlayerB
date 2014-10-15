import java.util.Random;

/**
 *
 * @author Finn
 */
public class Scout extends Child {

    private static final Random RANDOM = new Random();

    public Scout(int color, Field field) {
        super(color, field);
    }

//    @Override
//    public Objective chooseObjective() {
//
//        if (!isHoldingShield()) {
//            double shortestDistance = Double.MAX_VALUE;
//            Point shieldPosition = null;
//            for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
//                for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
//                    Point point = new Point(i, j);
//                    if (field.type(point) == Constants.SHIELD && getPosition().distance(point) < shortestDistance) {
//                        shortestDistance = getPosition().distance(point);
//                        shieldPosition = point;
//                    }
//                }
//            }
//            if (shieldPosition != null) {
//                return pickup(shieldPosition);
//            }
//        }
//        Point targetPosition = null;
//        double shortestDistance = Double.MAX_VALUE;
//        for (Point position : FLAG_POSITIONS) {
//            if (getPosition().distance(position) < shortestDistance && !hasFlag(position)) {
//                shortestDistance = getPosition().distance(position);
//                targetPosition = position;
//            }
//        }
//        if (targetPosition == null) {
//            return super.chooseObjective();
//        }
//
//        Objective defend = defend();
//        if (defend != null) {
//            return defend;
//        }
//        Point flagPosition;
//        if (field.isEmptyAt(targetPosition)) {
//            flagPosition = targetPosition;
//        } else {
//            flagPosition = Route.closestPointNextTo(getPosition(), targetPosition);
//        }
//        if (flagPosition == null) {
//            return super.chooseObjective();
//        }
//        Point standPosition = Route.closestPointNextTo(getPosition(), flagPosition);
//        if (standPosition == null) {
//            return super.chooseObjective();
//        }
//        return new Objective(standPosition, Action.makePlantAction(flagPosition));
//
//    }

    @Override
    protected double getShootImportance(Point target, int ammoRequired) {
        return 0.5 * super.getShootImportance(target, ammoRequired);
    }

    @Override
    protected double getShieldImportance(Point standPosition) {
        return 4 * super.getShieldImportance(standPosition);
    }

    @Override
    protected double getAmmoImportance(Point standPosition, Point ammoPosition) {
        return 2 * super.getAmmoImportance(standPosition, ammoPosition);
    }

    @Override
    protected double getRandomMoveImportance(Point moveTo) {
        return 0.05 * super.getRandomMoveImportance(moveTo);
    }

    @Override
    protected double getMoveToEnemyImportance(Point standPosition) {
        return 0.5 * super.getMoveToEnemyImportance(standPosition);
    }

    @Override
    protected double getFlagImportance(Point standPosition) {
        return 4 * super.getFlagImportance(standPosition);
    }

    @Override
    protected double getAdapterImportance(Point standPosition) {
        return 2 * super.getAdapterImportance(standPosition);
    }

}

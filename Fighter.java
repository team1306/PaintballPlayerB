
/**
 *
 * @author Finn
 */
public class Fighter extends Child{

    public Fighter(int color, Field field) {
        super(color, field);
    }
    
//    @Override
//    protected Objective chooseObjective() {
//        double shortestDistance = Double.MAX_VALUE;
//        Point closestEnemy = null;
//        Point closestReachableEnemy = null;
//        for (Child enemy : field.enemies()) {
//            Point enemyPosition = enemy.getPosition();
//            double distance = getPosition().distance(enemyPosition);
//            if (!enemyPosition.equals(new Point()) && distance < shortestDistance  && !enemy.isDefending()) {
//                shortestDistance = distance;
//                closestEnemy = enemyPosition;
//                if (Route.shootAt(getPosition(), enemyPosition) != null) {
//                    closestReachableEnemy = closestEnemy;
//                }
//            }
//        }
//        if (closestReachableEnemy == null) {
//            if (closestEnemy == null) {
//            return super.chooseObjective();
//            } else {
//                Objective possibleDefense = defend();
//                if (possibleDefense == null) {
//                    return new Objective(Route.closestPointNextTo(getPosition(), closestEnemy), Action.makeLaunchAction(closestEnemy, isHoldingRapidFireLauncher()));
//                } else {
//                    return possibleDefense;
//                }
//            }
//        }
//        boolean enoughAmmo = enoughAmmoFor(Action.makeLaunchAction(closestReachableEnemy, isHoldingRapidFireLauncher()));
//        if (!isStanding() && enoughAmmo) {
//            return new Objective(getPosition(), Action.makeUndefendAction());
//        }
//        if (isDefending() && enoughAmmo) {
//            return new Objective(getPosition(), Action.makeUndefendAction());
//        }
//        Point shootTarget = Route.shootAt(getPosition(), closestReachableEnemy);
//        if (shootTarget == null) {
//            return super.chooseObjective();
//        }
//        return new Objective(getPosition(), Action.makeLaunchAction(shootTarget, isHoldingRapidFireLauncher()));
//    }

    @Override
    protected double getShootImportance(Point target, int ammoRequired) {
        return 3 * super.getShootImportance(target, ammoRequired);
    }

    @Override
    protected double getShieldImportance(Point standPosition) {
        return 2 * super.getShieldImportance(standPosition);
    }

    @Override
    protected double getAmmoImportance(Point standPosition, Point ammoPosition) {
        return 1 * super.getAmmoImportance(standPosition, ammoPosition);
    }

    @Override
    protected double getRandomMoveImportance(Point moveTo) {
        return 0.02 * super.getRandomMoveImportance(moveTo);
    }

    @Override
    protected double getMoveToEnemyImportance(Point standPosition) {
        return 2 * super.getMoveToEnemyImportance(standPosition);
    }

    @Override
    protected double getFlagImportance(Point standPosition) {
        return 0.5 * super.getFlagImportance(standPosition);
    }

    @Override
    protected double getAdapterImportance(Point standPosition) {
        return 4 * super.getAdapterImportance(standPosition);
    }

}

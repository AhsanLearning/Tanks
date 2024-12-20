package ai;

import game.Ammo;
import game.GameObject;
import game.PowerUp;
import game.Target;
import game.Tank;
import game.TankAIBase;
import game.Vec2;

public class TankAI extends TankAIBase {

    public String getPlayerName() {
        return "Wania";  // <---- Put your first name here
    }
    public int getPlayerPeriod() {
        return 5;                // <---- Put your period here
    }
        
    // You are free to add member variables & methods to this class (and delete this comment).
    //  You should use the methods in its base class (TankAIBase) to query the world. 
    //  Note that you are not allowed to reach into game code directly or make any
    //  modifications to code in the game package. Use your judgement and ask your 
    //  teacher if you are not sure. If it feels like cheating, it probably is.

    // Member variables...
    private PowerUp currentPowerUp = null;

    public boolean updateAI() {

        // TODO: Your code goes here
        //PowerUp P=getPowerUp();
        //Vec2 moveVec= P.getPos().minus(getTankPos());
        //queueCmd("move", new Vec2( moveVec.x, 0));
        //queueCmd("move", new Vec2( 0,moveVec.y));
        //queueCmd("shoot", new Vec2( 5,0));

        // Check if there is a current power-up target and move towards it
        if (currentPowerUp != null) {
            boolean isAtCurrentPowerPosition = isAtPosition(currentPowerUp.getPos());

            //if (shouldPickUp(currentPowerUp) && isAtCurrentPowerPosition == false) {
            if (currentPowerUp != null && isAtCurrentPowerPosition == false) {
                moveTo(currentPowerUp);
            } else {
                shootAtClosestTargetOrTank();
            }
        }

        // If no current power-up, find a new power-up or engage enemies
        if (currentPowerUp == null) {
            currentPowerUp = findBestPowerUp();
            boolean isAtCurrentPowerPosition = isAtPosition(currentPowerUp.getPos());

            //if (currentPowerUp != null && shouldPickUp(currentPowerUp)) {
            if (currentPowerUp != null && isAtCurrentPowerPosition == false) {
                moveTo(currentPowerUp);
            }
        }

        return true;
    }

    private void shootAtClosestTargetOrTank() {
        // Find the closest target or tank and shoot at it
        GameObject closestEnemy = findClosestEnemy();
        
        if (closestEnemy != null) {
            shootAt(closestEnemy);
            currentPowerUp = null;  // Reset if no longer valid to pick up
        } else {
            // If no enemies are in range, find a range power-up and move towards it
            PowerUp rangePowerUp = findRangePowerUp();
            if (rangePowerUp != null) {
                currentPowerUp = rangePowerUp;
                moveTo(rangePowerUp);
            } else {
                // If no range power-up is available, move closer to the closest target to get in range
                Target closestTarget = findClosestTarget();
                if (closestTarget != null) {
                    moveToClosestTarget(closestTarget);
                } else {
                    moveRandomly();
                }
            }
        }
    }

    private GameObject findClosestEnemy() {
        GameObject closestEnemy = null;
        double minDistance = Double.MAX_VALUE;
        Vec2 currentPos = getTankPos(); // Get current position
        double tankShotRange = getTankShotRange(); // Get the current tank shot range

        // Find the closest target or tank
        Target[] targets = getTargets();
        Tank otherTank = getOther();

        for (Target target : targets) {
            double distance = currentPos.distance(target.getPos());
            if (distance < minDistance && distance <= tankShotRange) {
                minDistance = distance;
                closestEnemy = target;
            }
        }

        if (otherTank != null) {
            double distance = currentPos.distance(otherTank.getPos());
            if (distance < minDistance && distance <= tankShotRange) {
                closestEnemy = otherTank;
            }
        }

        return closestEnemy;
    }

    private PowerUp findBestPowerUp() {
        PowerUp[] powerUps = getPowerUps();
        PowerUp bestPowerUp = null;
        double minDistance = Double.MAX_VALUE;
        for (PowerUp powerUp : powerUps) {
            double distance = getTankPos().distance(powerUp.getPos());
            if (distance < minDistance) {
                minDistance = distance;
                bestPowerUp = powerUp;
            }
        }
        return bestPowerUp;
    }

    private void moveTo(PowerUp powerUp) {
        Vec2 position = powerUp.getPos();
        Vec2 moveVec = position.subtract(getTankPos());

        // Determine whether to move horizontally or vertically based on the shorter distance
        if (Math.abs(moveVec.x) > Math.abs(moveVec.y)) {
            // Move horizontally if the x distance is greater
            if (Math.abs(moveVec.x) > 0.1) {
                queueCmd("move", new Vec2(moveVec.x, 0));
            }
        } else {
            // Move vertically if the y distance is greater or equal
            if (Math.abs(moveVec.y) > 0.1) {
                queueCmd("move", new Vec2(0, moveVec.y));
            }
        }

        // Check if the tank has reached the power-up position
        if (isAtPosition(position)) {
            currentPowerUp = null; // Reset currentPowerUp once reached
        }
    }

    private void moveToClosestTarget(Target closestTarget) {
        Vec2 position = closestTarget.getPos();
        Vec2 moveVec = position.subtract(getTankPos());

        // Move horizontally first, then vertically, only if there's distance to cover
        if (Math.abs(moveVec.x) > 0.1) {
            queueCmd("move", new Vec2(moveVec.x, 0));
        } else if (Math.abs(moveVec.y) > 0.1) {
            queueCmd("move", new Vec2(0, moveVec.y));
        }
    }

    private PowerUp findRangePowerUp() {
        PowerUp[] powerUps = getPowerUps();
        for (PowerUp powerUp : powerUps) {
            if (powerUp.getType().equals("R")) {
                return powerUp;
            }
        }
        return null;
    }

    private void shootAt(GameObject target) {
        Vec2 position = target.getPos();
        Vec2 directionToTarget = position.subtract(getTankPos()).unit();
        queueCmd("turn", directionToTarget);
        queueCmd("shoot", directionToTarget);
    }

    private void moveRandomly() {
        Vec2 randomDirection = new Vec2(Math.random() * 2 - 1, Math.random() * 2 - 1);
        // Move horizontally or vertically based on random direction, avoiding zero distance
        if (Math.abs(randomDirection.x) > Math.abs(randomDirection.y)) {
            if (Math.abs(randomDirection.x) > 0.1) {
                queueCmd("move", new Vec2(randomDirection.x, 0));
            }
        } else {
            if (Math.abs(randomDirection.y) > 0.1) {
                queueCmd("move", new Vec2(0, randomDirection.y));
            }
        }
    }

    private boolean isAtPosition(Vec2 targetPos) {
        Vec2 currentPosition = getTankPos();
        return currentPosition.distance(targetPos) < 0.1;
    }

    private Target findClosestTarget() {
        Target[] targets = getTargets();
        Target closestTarget = null;
        double minDistance = Double.MAX_VALUE;
        Vec2 currentPos = getTankPos(); // Get current position
    
        for (Target target : targets) {
            double distance = currentPos.distance(target.getPos());
            if (distance < minDistance) {
                minDistance = distance;
                closestTarget = target;
            }
        }
        return closestTarget;
    }
}

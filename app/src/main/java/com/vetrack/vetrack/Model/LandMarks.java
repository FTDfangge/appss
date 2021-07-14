package com.vetrack.vetrack.Model;

/**
 * Vetrack
 * Create on 2019/5/30.
 */
public class LandMarks {

    private boolean bump;
    private double turn;//-1:turn right;1:turn left
    private boolean corner;
    private boolean stop;

    public LandMarks() {
        bump = false;
        turn = 0;
        corner = false;
    }

    public boolean isBump() {
        return bump;
    }

    public void setBump(boolean bump) {
        this.bump = bump;
    }

    public double getTurn() {
        return turn;
    }

    public void setTurn(double turn) {
        this.turn = turn;
    }

    public boolean isCorner() {
        return corner;
    }

    public void setCorner(boolean corner) {
        this.corner = corner;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}

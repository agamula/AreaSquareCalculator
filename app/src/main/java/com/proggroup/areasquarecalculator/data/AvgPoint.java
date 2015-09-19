package com.proggroup.areasquarecalculator.data;

import java.util.List;

public class AvgPoint {
    private final List<Float> squares;
    private Float cachedAvg;

    public AvgPoint(List<Float> squares) {
        this.squares = squares;
        cachedAvg = null;
    }

    public float avg() {
        if (cachedAvg == null) {
            float res = 0f;
            if (squares == null || squares.isEmpty()) {
                return res;
            }
            for (float square : squares) {
                res += square;
            }
            res /= squares.size();
            cachedAvg = res;
        }
        return cachedAvg;
    }
}

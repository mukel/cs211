package cs211.imageprocessing;


import processing.core.PVector;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CWComparator implements Comparator<PVector> {
    PVector center;

    public CWComparator(PVector center) {
        this.center = center;
    }

    @Override
    public int compare(PVector b, PVector d) {
        return Double.compare(Math.atan2(b.y - center.y, b.x - center.x), Math.atan2(d.y - center.y, d.x - center.x));
    }
}
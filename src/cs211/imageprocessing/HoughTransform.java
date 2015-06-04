package cs211.imageprocessing;

import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import processing.core.*;

class HoughComparator implements Comparator<Integer> {
    private int [] accumulator;
    public HoughComparator(int[] accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public int compare(Integer l1, Integer l2) {
        // correct implementation of compare
        int cmp = -Integer.compare(accumulator[l1], accumulator[l2]);
        return (cmp != 0) ? cmp : l1.compareTo(l2);
    }
}
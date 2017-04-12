package com.sample;

import java.util.ArrayList;

/**
 * Created by Julee on 11.04.2017.
 */
public class ClassData {
    private int outputValue;
    private ArrayList<Integer> indices;

    public int getOutputValue() {
        return outputValue;
    }

    public int getFingerprintId(int id) {
        return indices.get(id);
    }

    public void addFingerprintId(int fingerprintId) {
        indices.add(fingerprintId);
    }

    ClassData(int value, int fingerprintId) {
        this.outputValue = value;
        indices = new ArrayList<Integer>();
        indices.add(fingerprintId);
    }

    public int getCount() {
        return indices.size();
    }
}
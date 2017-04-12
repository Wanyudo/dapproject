package com.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julee on 11.04.2017.
 */
public class PublicMethods {

    public static void addItemToRepetitions(int item, HashMap<Integer, Integer> repetitions) {
        if (repetitions.containsKey(item)) {
            repetitions.put(item, repetitions.get(item) + 1);
        } else {
            repetitions.put(item, 1);
        }
    }

    public static ArrayList<outputProbability> calculateProbabilities(HashMap<Integer, Integer> repetitions, int itemsCount) {
        ArrayList<outputProbability> xValueProbabilities = new ArrayList<outputProbability>();
        for (Map.Entry<Integer, Integer> e : repetitions.entrySet()) {
            xValueProbabilities.add(new outputProbability(e.getKey(), (double) e.getValue() / itemsCount));
        }
        return xValueProbabilities;
    }
}

package com.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Julee on 11.04.2017.
 */
class PublicMethods {

    static void addIntegerToRepetitions(int item, HashMap<Integer, Integer> repetitions) {
        if (repetitions.containsKey(item)) {
            repetitions.put(item, repetitions.get(item) + 1);
        } else {
            repetitions.put(item, 1);
        }
    }

    static void addStringToRepetitions(String item, HashMap<String, Integer> repetitions) {
        if (repetitions.containsKey(item)) {
            repetitions.put(item, repetitions.get(item) + 1);
        } else {
            repetitions.put(item, 1);
        }
    }

    static void addIntegerAndRepetitionCountToRepetitionsWithWeighing(int item, HashMap<Integer, Double> repetitions, double weight) {
        if (repetitions.containsKey(item)) {
            repetitions.put(item, repetitions.get(item) + weight);
        } else {
            repetitions.put(item, weight);
        }
    }

    static void addStringAndRepetitionCountToRepetitionsWithWeighing(String item, HashMap<String, Double> repetitions, double weight) {
        if (repetitions.containsKey(item)) {
            repetitions.put(item, repetitions.get(item) + weight);
        } else {
            repetitions.put(item, weight);
        }
    }

    static String getMostlyRepeatedString(HashMap<String, Integer> repetitions) {
        int maxRepetitionsCount = 0;
        String mostlyRepeatedItem = "";
        for (Map.Entry<String, Integer> e : repetitions.entrySet()) {
            if (e.getValue() > maxRepetitionsCount) {
                maxRepetitionsCount = e.getValue();
                mostlyRepeatedItem = e.getKey();
            }
        }
        return mostlyRepeatedItem;
    }

    static String getMostlyRepeatedStringWeighting(HashMap<String, Double> repetitions) {
        double maxRepetitionsCount = 0;
        String mostlyRepeatedItem = "";
        for (Map.Entry<String, Double> e : repetitions.entrySet()) {
            if (e.getValue() > maxRepetitionsCount) {
                maxRepetitionsCount = e.getValue();
                mostlyRepeatedItem = e.getKey();
            }
        }
        return mostlyRepeatedItem;
    }

    static int getMostlyRepeatedInteger(HashMap<Integer, Integer> repetitions) {
        int maxRepetitionsCount = 0;
        int mostlyRepeatedItem = 0;
        for (Map.Entry<Integer, Integer> e : repetitions.entrySet()) {
            if (e.getValue() > maxRepetitionsCount) {
                maxRepetitionsCount = e.getValue();
                mostlyRepeatedItem = e.getKey();
            }
        }
        return mostlyRepeatedItem;
    }

    static int getMostlyRepeatedIntegerWithWeighing(HashMap<Integer, Double> repetitions) {
        double maxRepetitionsCount = 0;
        int mostlyRepeatedItem = 0;
        for (Map.Entry<Integer, Double> e : repetitions.entrySet()) {
            if (e.getValue() > maxRepetitionsCount) {
                maxRepetitionsCount = e.getValue();
                mostlyRepeatedItem = e.getKey();
            }
        }
        return mostlyRepeatedItem;
    }

    static ArrayList<OutputProbability> calculateProbabilities(HashMap<Integer, Integer> repetitions, int itemsCount) {
        ArrayList<OutputProbability> xValueProbabilities = new ArrayList<OutputProbability>();
        for (Map.Entry<Integer, Integer> e : repetitions.entrySet()) {
            xValueProbabilities.add(new OutputProbability(e.getKey(), (double) e.getValue() / itemsCount));
        }
        return xValueProbabilities;
    }
}

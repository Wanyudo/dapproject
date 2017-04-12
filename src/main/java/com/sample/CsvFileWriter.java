package com.sample;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static com.sample.GlobalData.WAPS_COUNT;

/**
 * @author ashraf
 *
 */
public class CsvFileWriter {

    //Delimiter used in CSV file
    public static final String COMMA_DELIMITER = ",";
    public static final String NEW_LINE_SEPARATOR = "\n";

    public static void writeCsvFile(String fileName, List<Fingerprint> dataSrc, String header) {

        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(fileName);

            //Write the CSV file header
            fileWriter.append(header + NEW_LINE_SEPARATOR);

            //Write fingerprint list to the CSV file
            for(Fingerprint fingerprint : dataSrc) {
                for (int i = 0; i < WAPS_COUNT; i++) {
                    fileWriter.append(String.valueOf(fingerprint.wapSignalIntensities[i])).append(COMMA_DELIMITER);
                }
                fileWriter.append(String.valueOf(fingerprint.locationData.getFloor())).append(NEW_LINE_SEPARATOR);
            }

            System.out.println("CSV file was created successfully !!!");

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
}

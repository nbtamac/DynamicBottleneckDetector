/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package system.level.event.detection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author nlztoo
 */
public class SystemLevelEventDetection {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws java.text.ParseException
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {

        // Change path
        String PATH = "/Downloads/DynamicBottleneckDetector-main/files/";
        // Change path
        String homeDir = System.getProperty("user.home");
        File dir = new File(homeDir + PATH);
        System.out.println(homeDir + PATH);
        
        for (File file : dir.listFiles()) {
            if (file.getName().contains("SEGMENT")) {
                BufferedWriter writer;
                writer = new BufferedWriter(new FileWriter(homeDir + PATH + "SYSTEM.csv"));
                
                System.out.println("file.getName() :" + file.getName());

                List segidList = new ArrayList();
                List inervalueList = new ArrayList();

                BlockageDetection checkBLAverage = new BlockageDetection();
                HighloadDetection checkHLPlot = new HighloadDetection();
                
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    String[] array2 = line.split(",");

                    String pid = array2[0];
                    String segmentid = array2[1];
                    String time_start = array2[2];
                    String duration = array2[3];

                    if (!(pid.equals("id"))) {
                        if ((!(pid == null))) {
                            if ((!(pid.contains(":")))) {
                                String InnerValue = segmentid.concat("#");
                                InnerValue = InnerValue.concat(duration);
                                InnerValue = InnerValue.concat(",");
                                InnerValue = InnerValue.concat(time_start);
                                // InnerValue is "a:b#duration,time_start"
                                inervalueList.add(InnerValue);
                                if (!(segidList.contains(segmentid))) {
                                    segidList.add(segmentid);
                                }
                            }
                        }
                    }
                }

                //for each segment check the blockage and high load
                for (Object st : segidList) {
                    String ss = (String) st;
                    // inervalueList is a list of entries like "a:b#duration,time_start"
                    // The ss is the segmentid, meaning a:b
                    checkBLAverage.check(inervalueList, ss, writer);
                    checkHLPlot.check(inervalueList, ss, writer);

                }
                writer.close();
            }
        }
    }
}

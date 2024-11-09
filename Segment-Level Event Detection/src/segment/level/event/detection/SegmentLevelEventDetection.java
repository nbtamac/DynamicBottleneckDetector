/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package segment.level.event.detection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author nlztoo
 */
public class SegmentLevelEventDetection {

    

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.text.ParseException
     */
    public static void main(String[] args) throws IOException, ParseException {
        String PATH = "/Downloads/DynamicBottleneckDetector-main/files/";
        // Change path
        String homeDir = System.getProperty("user.home");
        File dir = new File(homeDir + PATH);
        int c = 0;
        System.out.println("Directory: " + dir.listFiles());
        for (File file : dir.listFiles()) {
            System.out.println(file.getName().toString());
            if (file.getName().contains("TRACKING")) {
                System.out.println("in here" + file.getName().toString());  
                
                BufferedWriter writer;
                writer = new BufferedWriter(new FileWriter(homeDir + PATH + "SEGMENT.csv"));
                HashMap<String, List> BagsMap = new HashMap<String, List>();
                System.out.println(file.getName().toString());
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                System.out.println(lines.size());
                
                for (String line : lines) {
                    // The document is semicolon separated
                    String[] array2 = line.split(";");
                    // Each row has "case_id", "timestamp", "activity"
                    String PID = array2[0];
                    String time = array2[1];
                    // This is to remove the ""
                    time = time.substring(1, time.length() - 1);
                    String location = array2[2];
                    location = location.substring(1, location.length() - 1);

                    String LocationTime = location.concat("*");
                    LocationTime = LocationTime.concat(time);

                    // This first if is to get rid of the header if there is of hte file
                    if (!(PID.equals("id"))) {
                        if (PID != null & !(PID.contains("="))) {
                            // Seems that there is a problem with caseid 0 and we want to avoid it
                            if (!(PID.equals("0") || PID.equals(""))) {
                                //We are putting each activity with same caseid
                                if (BagsMap.containsKey(PID)) {
                                    List l = BagsMap.get(PID);
                                    l.add(LocationTime);
                                    BagsMap.put(PID, l);
                                } else {
                                    c++;
                                    List<String> l = new ArrayList<String>();
                                    l.add(LocationTime);
                                    BagsMap.put(PID, l);
                                }
                            }
                        }
                    }
                }

                // Now, compute duration time for bag in each segment (a:b)
                Set set1 = BagsMap.entrySet();
                Iterator itr1 = set1.iterator();
                // For each case_id/PID
                while (itr1.hasNext()) {
                    
                    Map.Entry entry = (Map.Entry) itr1.next();
                    String pid = (String) entry.getKey();
                    List lpid = (List) entry.getValue();
                    // Depend on our file we may need to change the pattern of the SimpleDateFormat
                    SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
                    Map< Long, Object> msorted = new TreeMap< Long, Object>();
                    
                    // For each LocationTime assigned to a case_id
                    for (int i = 0; i < lpid.size() - 1; i++) {
                        String locationtime1 = (lpid.get(i).toString());
                        String location1 = locationtime1.substring(0, locationtime1.indexOf("*"));
                        String time1 = locationtime1.substring(locationtime1.indexOf("*") + 1);
                        time1 = time1.substring(0, 23);
                        
                        Date d = f.parse(time1);
                        // with respect to January 1, 1970, 00:00:00 GMT, the standard representation of time in Java.
                        long millisecondss = d.getTime();
                        // we sort the activities depending on the time taken
                        msorted.put(millisecondss, location1);
                        // We are assuming that two activities will not happen at the exact same time
                    }

                    Set sett = msorted.entrySet();
                    Iterator itrt = sett.iterator();
                    String locationq = "";
                    String timeq = "";
                    // For each time-location in msorted
                    while (itrt.hasNext()) {

                        Map.Entry entryy = (Map.Entry) itrt.next();
                        String time = String.valueOf(entryy.getKey());
                        String location1 = (String) entryy.getValue();

                        if (!(locationq.equals("") & timeq.equals(""))) {

                            String segment = locationq.concat(":");
                            segment = segment.concat(location1);

                            long elapsed = Long.valueOf(time) - Long.valueOf(timeq);
                            String Duration = String.valueOf(elapsed);
                            // caseid, segment(a:b), time start, duration
                            String result = pid + "," + segment + "," + timeq + "," + Duration;
                            writer.write(result);
                            writer.newLine();

                            locationq = location1;
                            timeq = time;

                        } else {
                            locationq = location1;
                            timeq = time;
                        }
                    }
                }
                writer.close();
            }
        }
        System.out.println("number of bags = " + c);
    }
}

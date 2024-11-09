/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package system.level.event.detection;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 *
 * @author nlztoo
 */
public class BlockageDetection {

    public int check(List inervalueList, String segmentid, BufferedWriter writer) throws FileNotFoundException, ParseException, IOException {
        // inervalueList is  a list of entries like "a:b#duration,time_start"
        // segmentid is a:b, where a and b are activities in our BHS        
        
        Map zscoremap = new HashMap();
        Map segpidstarttimemap = new HashMap();

        int i;
        int threshold = 4;
        int countpid = 0;

        List<String> numList = new ArrayList<String>();
        List<String> madList = new ArrayList<String>(); 

        // Save in segpidstarttimemap the items that are in the same segmentid,
        // in numList it is stored their duration
        for (Object st : inervalueList) {
            String segiddurationtime = (String) st;
            String segid = segiddurationtime.substring(0, segiddurationtime.indexOf("#"));
            String duration = segiddurationtime.substring(segiddurationtime.indexOf("#") + 1, segiddurationtime.indexOf(","));
            if (segid.equals(segmentid)) {
                numList.add(duration);
                segpidstarttimemap.put(countpid, segiddurationtime);
                countpid++;
            }
        }

        int size = numList.size();
        Collections.sort(numList);
        
        int[] numArray = new int[size];
        double[] MADArray = new double[size];
        
        // Convert the durations to int
        int h = 0;
        for (String st : numList) {
            numArray[h] = Integer.valueOf(st);
            h++;
        }
        //Sort the order of duration
        Arrays.sort(numArray);

        double median;
        if (numArray.length % 2 == 0) {
            median = ((double) numArray[numArray.length / 2-1] + (double) numArray[numArray.length / 2 ]) / 2;
        } else {
            median = (double) numArray[numArray.length / 2];
        }

        //now i have median (^t) and can compute MAD
        /// median of ( diffrence of variable and median)
        for (i = 0; i < size; i++) {
            double diff = numArray[i] - median;
            if (diff < 0) {
                diff = diff * -1;
            }
            madList.add(String.valueOf(diff));
        }

        Collections.sort(madList);

        int k = 0;
        for (String st : madList) {
            MADArray[k] = Double.valueOf(st);
            k++;
        }
        
        Arrays.sort(MADArray);
        
        double MAD;
        if (MADArray.length % 2 == 0) {
            MAD = ((double) MADArray[MADArray.length / 2 - 1] + (double) MADArray[MADArray.length / 2]) / 2;
        } else {
            MAD = (double) MADArray[MADArray.length / 2];
        }

        //now i have mad and I can calcute z score
        if (MAD != 0.0) {
            //compute zscore averag for threshold in finding outliers
            //check if the duartion is an outlier or not
            Set set1 = segpidstarttimemap.entrySet();
            Iterator itr1 = set1.iterator();

            while (itr1.hasNext()) {
                //Converting to Map.Entry so that we can get key and value separately  
                Map.Entry entry = (Map.Entry) itr1.next();
                String segiddurationtime = (String) entry.getValue();
                String segid = segiddurationtime.substring(0, segiddurationtime.indexOf("#"));
                String duration = segiddurationtime.substring(segiddurationtime.indexOf("#") + 1, segiddurationtime.indexOf(","));
                
                if (segid.equals(segmentid)) {                    
                    int durationint = Integer.valueOf(duration);
                    double zscore = (0.6745 * (durationint - median)) / MAD;
                    if (zscore > threshold) {
                        zscoremap.put(entry.getKey(), 1);
                    } else {
                        zscoremap.put(entry.getKey(), 0);
                    }
                }
            }
        }

        //// now i have z score and a generated id in a map: have delay window!!
        // start window if see an outlier(value 1 in zscore map)
        // end window if wee an normal bag

        // for each blockage there is an windows that opens when see the first outlier in duration and
        // closes by seeing a non outlier duration
        Set setz = zscoremap.entrySet();
        Iterator itrz = setz.iterator();
        int start = 0;
        int end = 0;
        String previousendtime = "";
        String startwindow = "";
        String endwindow = "";
        int blockagesize = 0;
        while (itrz.hasNext()) {
            //Converting to Map.Entry so that we can get key and value separately  
            Map.Entry entry = (Map.Entry) itrz.next();
            int outlierornot = (int) entry.getValue();

            String segiddurationtime = (String) segpidstarttimemap.get(entry.getKey());
            // segiddurationtime "a:b#duration,time_start"
            String duration = segiddurationtime.substring(segiddurationtime.indexOf("#") + 1, segiddurationtime.indexOf(","));
            String starttime = segiddurationtime.substring(segiddurationtime.indexOf(",") + 1);
            long finishtime = Long.valueOf(starttime) + Long.valueOf(duration);
            if (outlierornot == 1) {
                ///save previus outlier for closing the window
                previousendtime = String.valueOf(finishtime);
                blockagesize++;
                if (start == 0) {
                    Date date = new Date(Long.valueOf(starttime));
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String dateFormatted = formatter.format(date);

                    startwindow = dateFormatted;
                    start = 1;
                }
            }
            if (outlierornot == 0) {
                if (end == 0 & start == 1) {
                    Date date = new Date(Long.valueOf(previousendtime));
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String dateFormatted = formatter.format(date);

                    endwindow = dateFormatted;
                    // Not isolated cases
                    if (blockagesize >= 1) {
                        String result = "BL," + segmentid + "," + startwindow + "," + endwindow;
                        writer.write(result);
                        writer.newLine();
                    }
                    start = 0;
                    end = 0;
                    blockagesize = 0;
                }
            }
        }
        return 0;
    }
}

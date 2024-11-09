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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author nlztoo
 */
public class HighloadDetection {

    public int check(List inervalueList, String segmentid, BufferedWriter writer) throws FileNotFoundException, ParseException, IOException {
        // inervalueList is  a list of entries like "a:b#duration,time_start"
        // segmentid is a:b, where a and b are activities in our BHS
        
        List<String> Segmenttimelist = new ArrayList<String>();
        List<Integer> OneminListForAverage = new ArrayList<Integer>();
        
        String startwin = "";
        String endwin = "";

        int freq = 0;
        double percentile = 0.75;
        int window_size = 60000;
        
        // Save in Segmenttimelist the starting times of the segments
        for (Object st1 : inervalueList) {
            String segiddurationtime1 = (String) st1;
            String segid1 = segiddurationtime1.substring(0, segiddurationtime1.indexOf("#"));
            String starttime1 = segiddurationtime1.substring(segiddurationtime1.indexOf(",") + 1);
            // We check whether the segment event happens in the same segment a:b that we are checking
            if (segid1.equals(segmentid)) {
                if (!(starttime1.equals(""))) {
                    Segmenttimelist.add(starttime1);
                }
            }
        }
        
        // These operations are working with timestamps represented in milliseconds since the Unix epoch (January 1, 1970)
        String time = (Segmenttimelist.get(0));
        // Set the start of the day as the time in our first entry
        long startofday = Long.parseLong(time);
        // 86400000 is 24 hours in miliseconds
        long endofday = startofday + 86400000;
        // 60000 is 1 minute in miliseconds, win means window
        long endwintimel = (window_size + (startofday));
        long startwintimel = startofday;

        ///start a 1 min window fors
        while (endwintimel < endofday) {
            for (int i = 0; i < Segmenttimelist.size() - 1; i++) {

                long timebag = Long.parseLong(Segmenttimelist.get(i));
                if (startwintimel <= timebag) {
                    if (timebag < endwintimel) {
                        freq++;
                    }
                }
            }
            OneminListForAverage.add(freq);
            freq = 0;
            startwintimel = endwintimel;
            endwintimel = (window_size + (startwintimel));
        }

        ///compute max and min freq in list
        int max = 0;
        int min = 0;
        for (int st : OneminListForAverage) {
            if (st > max) {
                max = st;
            }
            if (st < min) {
                min = st;
            }
        }
        
//      Threshold is 75% of max
        double threshold = max * percentile;

        // Find highloads
        String time2 = (Segmenttimelist.get(0));
        long startofday2 = Long.parseLong(time2);
        long endofday2 = startofday2 + 86400000;
        long endwintimel2 = (window_size + (startofday2));
        long startwintimel2 = startofday2;

        ///start a 1 min window fors
        while (endwintimel2 < endofday2) {
            for (int i = 0; i < Segmenttimelist.size() - 1; i++) {
                long timebag = Long.parseLong(Segmenttimelist.get(i));
                if (startwintimel2 <= timebag) {
                    if (timebag < endwintimel2) {
                        freq++;
                    }
                }
            }
            
            ////check high load here//
            if (freq >= threshold) {
                // Initially, startwin ="", as well as endwin
                if (startwin.equals("")) {
                    startwin = String.valueOf(startwintimel2);
                }
            } else {
                if (endwin.equals("") & (!(startwin.equals("")))) {
                    endwin = String.valueOf(startwintimel2);

                    Date sdate = new Date(Long.valueOf(startwin));
                    Date edate = new Date(Long.valueOf(endwin));
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String dateFormatteds = formatter.format(sdate);
                    String edateFormatted = formatter.format(edate);
                    String startwin1 = dateFormatteds;
                    String endwin1 = edateFormatted;

                    String result = "HL," + segmentid + "," + startwin1 + "," + endwin1;
                    writer.write(result);
                    writer.newLine();
                    startwin = "";
                    endwin = "";
                }
            }
            freq = 0;
            startwintimel2 = endwintimel2;
            endwintimel2 = (window_size + (startwintimel2));
        }
        return 0;
    }
}

/*
They are not using the IQR-method nor the 75th percentile. They are just taking the max frequency and multiply it by 0.75

When computing the frequency again I think it is inefficient and they could have stored the information of the bins for 
later use (maybe the file were so big that it was not possible)

When it comes to store the system-level HL, they do not meet the definition in the article,
they are using as the start time and end time of the HL the times of the windows, but in the paper 
they claimed to be the time start/end minimum/maximum of the segments in the bin.

--------------------------------------------------------------------------------
Things to try: Change the threshold of 0.75, change the window size of 1 min
*/
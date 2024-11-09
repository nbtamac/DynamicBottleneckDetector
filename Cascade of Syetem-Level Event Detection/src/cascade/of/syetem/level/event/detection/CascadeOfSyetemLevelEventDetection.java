/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cascade.of.syetem.level.event.detection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nlztoo
 */
public class CascadeOfSyetemLevelEventDetection {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        String PATH = "/Downloads/DynamicBottleneckDetector-main/files/";
        // Change path
        String homeDir = System.getProperty("user.home");
        File dir = new File(homeDir + PATH);
        
        
        /**
         * In writer file we will have each graph separated by the t value, then the nodes will be classified
         * by v followed by the label of the node in the graph and the next number is the id from the node in
         * the writeractivityid represented by segment and type. The edges represented by e followed by the 
         * two nodes label and a 1.
        */
        BufferedWriter writer = new BufferedWriter(new FileWriter(homeDir + PATH + "GRAPH.txt"));
        BufferedWriter writeractivityid = new BufferedWriter(new FileWriter(homeDir + PATH + "GRAPH_NAMES.txt"));

        HashMap<String, String> inputmap = new HashMap<String, String>();
        HashMap<String, Integer> nodeidandrealnamenmap = new HashMap<String, Integer>();
        ArrayList nodelist = new ArrayList();
        ArrayList edgelist = new ArrayList();

        int CaseID = 0;
        int k = 0;
        int t = 0;
        int blo = 0;
        int hlo = 0;

       
        for (File file : dir.listFiles()) {
            if (file.getName().contains("RELATION")) {
                HashMap<Integer, List> graphmap = new HashMap<Integer, List>();
                ArrayList blolist = new ArrayList();
                ArrayList hillist = new ArrayList();
                int allcascade = 0;
                int blcascade = 0;

                int inputcounter = 0;

                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    //Add all events to inputmap, used as id for each relation of relations
                    inputcounter++;

                    String input = line;
                    // Structure of the lines are supposed to be 
                    // mainsegid + "," + mstart + "," + mfinish + "," + maineventtype + "*" + othersegid + "," + ostart + "," + ofinish + "," + othereventtype

                    if (input.contains("*")) {

                        inputmap.put(String.valueOf(inputcounter), input);
                        String firstnode = input.substring(0, input.indexOf("*"));
                        String secondnode = input.substring(input.indexOf("*") + 1);
                        if (!(nodelist.contains(firstnode))) {
                            nodelist.add(firstnode);
                        }
                        if (!(nodelist.contains(secondnode))) {
                            nodelist.add(secondnode);
                        }
                        if (firstnode.contains("BL")) {
                            blo++;
                            if (!(blolist.contains(firstnode))) {
                                blolist.add(firstnode);
                            }
                        }
                        if (secondnode.contains("BL")) {
                            blo++;
                            if (!(blolist.contains(secondnode))) {
                                blolist.add(secondnode);
                            }
                        }
                        if (firstnode.contains("HL")) {
                            hlo++;
                            if (!(hillist.contains(firstnode))) {
                                hillist.add(firstnode);
                            }
                        }
                        if (secondnode.contains("HL")) {
                            hlo++;
                            if (!(hillist.contains(secondnode))) {
                                hillist.add(secondnode);
                            }
                        }
                        if (!(edgelist.contains(input))) {
                            edgelist.add(input);
                        }
                    }
                }

                System.out.println("bl relations  : " + blo);
                System.out.println("bllist size node  : " + blolist.size());
                System.out.println("hl relations  : " + hlo);
                System.out.println("hillist size node  : " + hillist.size());
                
                // For each node in nodelist (systems that are relationed in some way)
                for (int i = 0; i < nodelist.size(); i++) {
                    String node = (String) nodelist.get(i);
                    ArrayList graphlist = new ArrayList();
                    nodelist.remove(node);

                    ///find all related nodes to "node" and remove it from node list
                    ArrayList relatededgelist = FindRelatedNode(edgelist, node);

                    if (!relatededgelist.isEmpty()) {

                        int j = 0;
                        while (!relatededgelist.isEmpty()) {

                            String edge = (String) relatededgelist.get(j);
                            // Each edge can be node*newnode or newnode*node
                            graphlist.add(edge);
                            
                            String firstnode = edge.substring(0, edge.indexOf("*"));
                            String secondnode = edge.substring(edge.indexOf("*") + 1);
                            
                            // remove the edge to not get it again
                            // remove the nodes because they will be already in the graph
                            relatededgelist.remove(edge);
                            nodelist.remove(secondnode);
                            nodelist.remove(firstnode);
                            edgelist.remove(edge);

                            if (firstnode.equals(node)) {
                                ArrayList newedgerelatedlist = FindRelatedNode(edgelist, secondnode);
                                // Get the list of edges connecting the other node
                                for (int w = 0; w < newedgerelatedlist.size(); w++) {

                                    String reledge = (String) newedgerelatedlist.get(w);
                                    String ftnode = reledge.substring(0, reledge.indexOf("*"));
                                    String snode = reledge.substring(reledge.indexOf("*") + 1);
                                    if (!(relatededgelist.contains(reledge))) {
                                        //Add new edges to be considered in the while loop
                                        relatededgelist.add(reledge);
                                    }
                                    if ((edgelist.contains(reledge))) {
                                        edgelist.remove(reledge);
                                    }
                                    if ((nodelist.contains(snode))) {
                                        nodelist.remove(snode);
                                    }
                                    if ((nodelist.contains(ftnode))) {
                                        nodelist.remove(ftnode);
                                    }
                                }
                                node = secondnode;
                            }
                            if (secondnode.equals(node)) {
                                ArrayList newedgerelatedlist = FindRelatedNode(edgelist, firstnode);
                                for (int w = 0; w < newedgerelatedlist.size(); w++) {

                                    String reledge = (String) newedgerelatedlist.get(w);
                                    String ftnode = reledge.substring(0, reledge.indexOf("*"));
                                    String snode = reledge.substring(reledge.indexOf("*") + 1);
                                    if (!(relatededgelist.contains(reledge))) {
                                        relatededgelist.add(reledge);
                                    }
                                    if ((edgelist.contains(reledge))) {
                                        edgelist.remove(reledge);
                                    }
                                    if ((nodelist.contains(snode))) {
                                        nodelist.remove(snode);
                                    }
                                    if ((nodelist.contains(ftnode))) {
                                        nodelist.remove(ftnode);
                                    }
                                }
                                node = firstnode;
                            }
                        }
                    }
                    // Store the graph information (graphlist) in the graphmap
                    if (graphlist.size() > 1) {
                        int bl = 0;
                        allcascade++;
                        for (int ii = 0; ii < graphlist.size(); ii++) {
                            String ed = (String) graphlist.get(ii);
                            // Only interested in cascades with at least one blockage node
                            if (ed.contains("BL")) {
                                bl = 1;
                            }
                        }
                        if (bl == 1) {
                            blcascade++;
                            graphmap.put(CaseID, graphlist);
                            CaseID++;
                        }
                    }
                }
                System.out.println("___________________________________");
                System.out.println("all cascades : " + allcascade);
                System.out.println("blockage cascades : " + blcascade);
                System.out.println("___________________________________");

    //   TKG? Start the suggraph mining problem?      
                // The graphmap has as key an id and value a graphlist fed with edges
                for (Map.Entry<Integer, List> entry : graphmap.entrySet()) {
                    ArrayList graphnodelist = new ArrayList();
                    ArrayList graphedgelist = new ArrayList();
                    HashMap<String, Integer> nodeidandlabelforedgesmap = new HashMap<String, Integer>();
                    // t is a number starting from 0
                    writer.write("t # " + t);
                    writer.newLine();
                    t++;
                    List listgraph = entry.getValue();
                    for (int i = 0; i < listgraph.size(); i++) {
                        Integer firstnodeid;
                        Integer secondnodeid;
                        String firstouttype;
                        String secouttype;
                        int firstnodeidnotlabel;
                        int secondnodeidnotlabel;
                        String edge = (String) listgraph.get(i);
                        String ftnode = edge.substring(0, edge.indexOf("*"));
                        String firstnodesegment = ftnode.substring(0, ftnode.indexOf(","));
                        if (ftnode.contains("BL")) {
                            firstouttype = "BL";
                        } else {
                            firstouttype = "HL";
                        }
                        String firstnodesegmenttype = firstnodesegment.concat("*");
                        firstnodesegmenttype = firstnodesegmenttype.concat(firstouttype);

                        ///add node to realnameidmap, nodeidandrealnamenmap empty at the start
                        if (nodeidandrealnamenmap.containsKey(firstnodesegmenttype)) {
                            firstnodeid = nodeidandrealnamenmap.get(firstnodesegmenttype);
                        } else {
                            // Nodes with their ID
                            firstnodeid = nodeidandrealnamenmap.size() + 1;
                            nodeidandrealnamenmap.put(firstnodesegmenttype, firstnodeid);
                            writeractivityid.write(firstnodesegmenttype + "," + firstnodeid);
                            writeractivityid.newLine();
                        }

                        if (nodeidandlabelforedgesmap.containsKey(String.valueOf(firstnodeid))) {
                            firstnodeidnotlabel = nodeidandlabelforedgesmap.get(String.valueOf(firstnodeid));
                        } else {
                            nodeidandlabelforedgesmap.put(String.valueOf(firstnodeid), k);
                            firstnodeidnotlabel = k;
                            k++;
                        }

                        String firstnode = "v ";
                        firstnode = firstnode.concat(String.valueOf(firstnodeidnotlabel));
                        firstnode = firstnode.concat(" ");
                        firstnode = firstnode.concat(String.valueOf(firstnodeid));

                        String snode = edge.substring(edge.indexOf("*") + 1);
                        String secondnodesegment = snode.substring(0, snode.indexOf(","));
                        if (snode.contains("BL")) {
                            secouttype = "BL";
                        } else {
                            secouttype = "HL";
                        }
                        String secondnodesegmenttype = secondnodesegment.concat("*");
                        secondnodesegmenttype = secondnodesegmenttype.concat(secouttype);

                        ///add node to realnameidmap
                        if (nodeidandrealnamenmap.containsKey(secondnodesegmenttype)) {
                            secondnodeid = nodeidandrealnamenmap.get(secondnodesegmenttype);
                        } else {
                            secondnodeid = nodeidandrealnamenmap.size() + 1;
                            nodeidandrealnamenmap.put(secondnodesegmenttype, secondnodeid);
                            writeractivityid.write(secondnodesegmenttype + "," + secondnodeid);
                            writeractivityid.newLine();
                        }

                        if (nodeidandlabelforedgesmap.containsKey(String.valueOf(secondnodeid))) {
                            secondnodeidnotlabel = nodeidandlabelforedgesmap.get(String.valueOf(secondnodeid));
                        } else {
                            nodeidandlabelforedgesmap.put(String.valueOf(secondnodeid), k);
                            secondnodeidnotlabel = k;
                            k++;
                        }

                        String secondnode = "v ";
                        secondnode = secondnode.concat(String.valueOf(secondnodeidnotlabel));
                        secondnode = secondnode.concat(" ");
                        secondnode = secondnode.concat(String.valueOf(secondnodeid));

                        String graphedge = "e ";
                        graphedge = graphedge.concat(String.valueOf(firstnodeidnotlabel));
                        graphedge = graphedge.concat(" ");
                        graphedge = graphedge.concat(String.valueOf(secondnodeidnotlabel));
                        graphedge = graphedge.concat(" 1");
                        
                        if (!(graphnodelist.contains(firstnode))) {
                            graphnodelist.add(firstnode);
                        }
                        if (!(graphnodelist.contains(secondnode))) {
                            graphnodelist.add(secondnode);
                        }
                        if (!(graphedgelist.contains(graphedge))) {
                            graphedgelist.add(graphedge);
                        }
                    }
                    for (int i = 0; i < graphnodelist.size(); i++) {
                        writer.write((String.valueOf(graphnodelist.get(i))));
                        writer.newLine();
                    }

                    for (int i = 0; i < graphedgelist.size(); i++) {
                        writer.write((String.valueOf(graphedgelist.get(i))));
                        writer.newLine();
                    }
                }
            }
        }
        writer.close();
        writeractivityid.close();
    }

    public static ArrayList FindRelatedNode(List edgelist, String node) {
        ArrayList relatedlist = new ArrayList();
        for (int i = 0; i < edgelist.size(); i++) {
            String reledge = (String) edgelist.get(i);

            String firstnode = reledge.substring(0, reledge.indexOf("*"));
            String secondnode = reledge.substring(reledge.indexOf("*") + 1);

            if ((secondnode.equals(node)) || ((firstnode.equals(node)))) {
                //if one of the nodes in edge is the node we are looking for related for that

                if (!(relatedlist.contains(reledge))) {
                    relatedlist.add(reledge);
                }                  
            }
        }
        return relatedlist;
    }

}

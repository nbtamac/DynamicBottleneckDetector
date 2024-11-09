# cascades

project description

## Table of contents
* [Overview](#Overview)
* [Systemrequirment](#system_requirment)
* [Guidtorun](#Guid_to_run)
* [CodeExamples](#Code_Examples)
* [Contact](#contact)

## Overview
overview

## Guid_to_run
For detecting systematic behavior leading to dynamic bottlenecks we should follow 5 steps. First step is detecting segemnt-level events from event log. We detect system-level events form detected segment-level events. Third step is relation detection between detected system-level events. When we detetc related system-level events, we apply cascade detection method to detect cascades of correlated system-level events. Final step is applying subgraph mining method.

###### Segment-level events detection


You can use the available data set x. Where Each row indicates one case-level event and contains case id, activity name which is the location of the case in a system, and time stamp which is the time that cases started the activity. Case-level events are sorted by time. By applying source code Segment-Level Event Detection on the event log you can detect the segment-level events.

###### System-level events detection

By applying source code System-Level Event Detection on the detected segment-level events (events should be sorted by time) you can detect the system-level events. We detect two types of system-level events and we call them Blockage and High load. 

###### Relation detection

We detect the related system-level events by considering temporal and spatial dimensions conditions. By applying source code System-Level Events Relation Detection on the detected system-level events you can detect the related system-level events.

You can change the temporal and spatial conditions to the condition you want.

###### Cascade detection

Detected related system-level events are the input for this step of word and source code ....

###### Subgraph mining

For detecting frequent subgraphs we use TKG, which is a subgraph mining algorithm implemented by SPMF.

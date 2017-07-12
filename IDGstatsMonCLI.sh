#!/bin/bash
#
# Module: IDGstatsMonCLI
#
# The script is provided as it is and was modified from the one found @
# http://www-01.ibm.com/support/docview.wss?uid=swg21377610
#
# Version 1.0
#
# Description: This is intended to be used in either
#   temporary or testing situations where you want to run
#   the data gathering for a specific number of times or
#   for a short period of time.
#
#   If you plan to impliment this as a long term solution
#   I would recommend that you use IDGstatsMonCLI-cron from
#   an automation tool such as cron on linux or a similar
#   utility on other OS platforms.
#
#   Date:   2016-04-02
#
#####################################################!/bin/bash

##=============================================
##  Edit next 5 lines according to your needs
##=============================================
## Hostname or ip address of the DataPower device 
DPHOST=172.16.184.30

## The INFILE is used each time the SSH connection is made
## A sample INFILE is provided, they are the CLI commands that
## need to be executed with out any echo or blank lines.
INFILE=IDGstatsMonCLI-cmds

## The filename prefix these will have a date and time stamp added
OUTFILE=IDGv720-A

## Run Count - How many times you want the script to run.
RUNCOUNT=60

## Sleep Time between runs - in seconds
SLEEPTIME=60

##==========================================================================================
## Count and Time Quick Notes ....
## COUNT   TIME   Result
## 60      60sec  1 hour at 1 minute intervals     COUNT = (1 times per minute x 60min)
## 1440    60sec  24 hours at 1 minute intervals   COUNT = (1 times per minute x 60min x 24hrs)
## 2880    30sec  24 hours at 30 second intervals  COUNT = (2 times per minute x 60min x 24hrs)
## 8640    10sec  24 hours at 10 second intervals  COUNT = (6 times per minute x 60min x 24hrs)
## 40320   15sec  1 week at 15 second intervals    COUNT = (4 times per minute x 60min x 24hrs x7days)
##==========================================================================================

##=============================================
## End of the script that will run each time.
##=============================================

##=============================================
## Begin the run.
##=============================================
COUNT=$RUNCOUNT
STARTDATE=`date`
while [[ $COUNT -gt 0 ]]
do
	TIMESTAMP=$(date +%Y%m%d-%H%M%S)
	echo $TIMESTAMP >> $OUTFILE
	echo "Data collection run started at $STARTDATE" >> $OUTFILE
	echo "===== Number = $COUNT of $RUNCOUNT @ $TIMESTAMP =====" >> $OUTFILE
	ssh  -T $DPHOST < $INFILE  >> $OUTFILE
	# You should change the ./_data/ part to the location where you what the output stored for the Java app to read it from.
	mv $OUTFILE ./_data/$OUTFILE-$TIMESTAMP.out
	echo "Created file: " $OUTFILE-$TIMESTAMP.out
	sleep $SLEEPTIME
	(( COUNT -= 1 ))
done

echo "Run Complete"
##=============================================
## All Done.
##=============================================

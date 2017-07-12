#!/bin/bash
#
# Module: IDGstatsMonCLI-cron
#
# Version 1.0
#
# Description: This is just a tester shortcut intended
#   to get a single run of the CLI file to use as input
#   for the testing of changes to the IDGstatsMon.java
#   application.
#
#   It is also more suitable for insertion into the cron
#   table than the included script IDGstatsMonCLI.sh
#   since that script contains looping and timing logic
#   that would not be needed in cron.
#
#   Date:   2016-04-02
#
####################################################

##=============================================
##  Edit next 4 lines according to your needs
##=============================================
## Hostname or ip address of the DataPower device 
DPHOST=172.16.184.30

## The INFILE is used each time the SSH connection is made
## A sample INFILE is provided, they are the CLI commands that
## need to be executed with out any echo or blank lines.
INFILE=IDGstatsMonCLI-cmds

## The PREFIX is the beginning of the filename that will
## be used to create the output files and the result csv file.
## I would recommend something like the machine name or a
## combination of machine name and environment.
PREFIX=IDGv720-A

## The output directory where the Java application will pick up
## the files to extract the data for the csv file
OUTDIR=./_result/

##=============================================
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
OUTFILE = $PREFIX-$TIMESTAMP.out
echo  $TIMESTAMP>> $OUTFILE
ssh  -T $DPHOST < $INFILE  >> $OUTFILE
mv $OUTFILE $OUTDIR

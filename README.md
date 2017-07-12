# DataPower-Gateway-Inventory
This is intended to gather data from an IBM DataPower Gateway and create a cvs file that can be imported into excel and graphed to see when your peak usage/tps times are.

## Synopsis
This tool relies on the core Java application and a few scripts to gather some performance metrics on a DataPower appliance and writes them to a csv file.

## Requirements
This was developed on 64bit Java v1.8 but should work with anything back to 1.6, I have not back tested but I’m not doing anything exotic either.

## Configuration
```
 * The *.sh files are linux bash scripts and should be made executable.
 * Those files also contain some required edits.
   - The IP Address or hostname of the DataPower appliance
   - The name of the INFILE: Which contains the actual DataPower CLI
     commands that need to be run to create the output data files
   - The OUTFILE and/or PREFIX: This is the name that the java application
     will look for to get the data and also to use as the result filename.
   - RUNCOUNT: if available is the number of times the script will be run
     to gather data.
   - SLEEPTIME: is the interval at which the CLI commands will be run.
 * The IDGstatsMonCLI-cmds file: This is a sample file of CLI commands that
   will be run.
   - You need to add the an admin id or better yet a privileged user only used for this
   - You need to add a password for the user (I know, password in plain text, I don't like it either)
```

## Code Example
There are lots of comments in the .java file, I believe in heavy commenting.
The basic strategy here is to use the IDGstatsMonCLI-cron.sh file or put it's entries in a cron job.
That will run the IDGstatsMonCLI.sh script file.

## Motivation
A past customer asked me if there was a way to do this, so I created a sample for them to use as a base for their own use. This is the sample that they started with. I hope is useful to others as well.

## Installation
This is a simple command line Java program, it does require Java v1.6+
This was developed on Linux it is intended to be extracted into a base directory
and run from there. With that said, it’s easy to move things around and flexible
enough that it will live with other folder structures.

## Usage
```
===============================================================================
Usage: java IDGstatsMon runInterval dataDir procDir resultDir filename
    Where:
          runInterval = Number of seconds between checking the folder for new data files, must be > 0
          dataDir     = Relative or Absolute path to the directory for data files
          procDir     = Relative or Absolute path to the directory for processed data files
          resultDir   = Relative or Absolute path to the directory for the output .csv file
          filename    = The base name of the data files to be collected

Example > java IDGstatsMon 30 /Users/uid/MonitorResult/_data/ /Users/uid/MonitorResult/_processed/ /Users/uid/MonitorResult/ IDGv720-1
Example > java IDGstatsMon 30 ./_data/ ./_processed/ ./_result/ IDGv720-1
Example > java IDGstatsMon 30 _data/ _processed/ _result/ IDGv720-1
===============================================================================
```
## Troubleshooting
Make sure that the wait time is a positive integer
Make sure that all of the required directories exist and are case specific
Make sure that the filename prefix is all valid characters, it should not end with a “-“

## License
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

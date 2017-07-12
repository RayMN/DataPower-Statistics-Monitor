/**
*   Module: IDGstatsMon.java
*
*   Written by Ray Wilson
*
*   Description: Parse CLI output files and put the data into a csv file. This file
*                can then be imported into your favorite spreadsheet and graphed.
*
*                A very small performance increase in this application can be achieved by
*                commenting out all of the "if(DEBUG)" statements.
*
*   CSV format:
*   date-time, Memory%, cpu10s, cpu1m, cpu10m, cpu1h, cpu1d, tps10s, tps1m, tps10m, tps1h, tps1d
*
*   Notes: date-time - is taken from the timestamp in the source data file.
*          Memory - is read once for each file processed. (because that is how the CLI script works)
*          CPU data - is read once for each file processed.
*          TPS data - is the SUM of all gateways in all domains that are reported in the CLI script
*
*	Version 1.0
*
*   History:
*	2016-04-02 v1.0 Created.
*
*   Copyright (C) 2016  Paul Ray Wilson
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
*/

import java.io.*;
import java.util.*;
import java.io.File;

public class IDGstatsMon {
	public static boolean DEBUG = false;
	public static int runInterval = 10;
	public static String dataDir = "";
	public static String procDir = "";
	public static String resultDir = "";
	public static String baseName = "";

	/** main()
	*
	* This is the "main" method that checks for command line arguments and
	* for starting the doWork method below where all the actual work will happen
	*
	*/
	public static void main(String arg[]) throws Exception {

		boolean argsValid = checkArgs(arg); // Check command line arugments

		if (!argsValid) {
    	  	System.out.println("\n===============================================================================");
    	  	System.out.println(" ");
    	  	System.out.println("Usage: java IDGstatsMon runInterval dataDir procDir resultDir filename");
    	  	System.out.println("    Where:");
    	  	System.out.println("          runInterval = Number of seconds between checking the folder for new data files, must be > 0");
    	  	System.out.println("          dataDir     = Relative or Absolute path to the directory for data files");
    	  	System.out.println("          procDir     = Relative or Absolute path to the directory for processed data files");
    	  	System.out.println("          resultDir   = Relative or Absolute path to the directory for the output .csv file");
    	  	System.out.println("          filename    = The base name of the data files to be collected");
    	  	System.out.println(" ");
    	  	System.out.println("Example > java IDGstatsMon 30 /Users/uid/MonitorResult/_data/ /Users/uid/MonitorResult/_processed/ /Users/uid/MonitorResult/ IDGv720-1");
    	  	System.out.println("Example > java IDGstatsMon 30 ./_data/ ./_processed/ ./_result/ IDGv720-1");
    	  	System.out.println("Example > java IDGstatsMon 30 _data/ _processed/ _result/ IDGv720-1");
    	  	System.out.println(" ");
    	  	System.out.println("===============================================================================\n");
			System.exit(0);
		} else {
			try {

				// Validate the command line arguments.
				runInterval = Integer.parseInt(arg[0]);
				dataDir = arg[1];
				procDir = arg[2];
				resultDir = arg[3];
				baseName = arg[4];

				String result = doWork();
				System.out.println(result);

			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(0);
			}
		}
	}

	/** doWork()
	*
	* This method scan the folder for matching files and get the data our of them,
	* then update a data file and move the file to the processed folder. It will
	* then wait for some period of time before checking the folder again.
	* assumptions:
	*       - 1) the out put files are name "something"+time-stamp+".out"
	*       - 2) they are all in one folder, porinted to with myPath.
	*       - 3) there are sub-folders in this folder called "processed" and "result_data"
	*             - processed is for the files once the data has been extracted
	*               the extension ".done" is added to the file and it is moved ot this folder
	*             - result_data is where the .csv file is stored and updated.
	*
	* return - String - The results of the echo opperation.
	*/
	public static String doWork () {
		if(DEBUG) {System.out.println("DEBUG :: Entering doWork");}
		boolean runMe = true;
		String outputFilename = resultDir+baseName+".csv";
		File fout = new File(outputFilename);
		String processedFolder = procDir;
		try {
			while (runMe) {

				// Create the output file if it does not exist.
				if(!fout.exists()){
					fout.createNewFile();
					if(DEBUG){System.out.println("DEBUG :: Creating output file - " + outputFilename);}
				}
				//
				// Output file handle
				//
				BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename,true));
				out.write("Date-Time,Mem %,CPU-10s,CPU-1m,CPU-10m,CPU-1h,CPU-1d,TPS-10s,TPS-1m,TPS-10m,TPS-1h,TPS-1d\n");

				//
				// Create a list of files in the folder pointed to by the path on the command line.
				//
			    String thisfile;
			    File folderToScan = new File(dataDir);
			    File[] listOfFiles = folderToScan.listFiles();
				if(DEBUG) {System.out.println("DEBUG :: Found "+listOfFiles.length+" Files");}
				if(DEBUG){
					System.out.println("DEBUG :: Processing File List:");
					for(int j=0;j<listOfFiles.length;j++){
						System.out.println("DEBUG :: File ["+j+"] = "+listOfFiles[j]);
					}
				}

			    //
			    // Scan through the list of files looking for the ones that match our filename from
			    // the command line (as well as an extension that I added that is hard coded)
			    //
			    for (int i = 0; i < listOfFiles.length; i++) {
			        if (listOfFiles[i].isFile()) {
			            thisfile = listOfFiles[i].getName();

			            //
			            // File selection - the starts with comes from the filename you enter on the command
			            // line, and the ends with is just what I used for a filename. Everything in the middle
			            // is assumed to be some sort of time stamp or something.
			            //
			            if (thisfile.startsWith(baseName) && thisfile.endsWith(".out")) {
			                File fin = new File(dataDir+thisfile);
			                String dateTimeStamp = "";
							if(DEBUG) {System.out.println("DEBUG :: Processing file "+listOfFiles[i]);}

			                try{
								String csvString = "";
			                	int memPct = 0;
								int[] cpuStats = {0,0,0,0,0};
								int[] tpsStats = {0,0,0,0,0};

			                	//
			                	// Input file handle
			                	//
								BufferedReader in = new BufferedReader(new FileReader(fin));

								//
								// start processesing the file, read a line, for our use the first line is a
								// date-time stamp. After that we scan down the file looking the lines with lots
								// of dashed, the data follows that line. We process the data then start looking
								// for more dashes, if we find some we process that as well, otherwise it will
								// process until EOF.

								String thisLine = in.readLine();	///
								dateTimeStamp = thisLine;			/// These lines are specific to the files I'm using
								thisLine = in.readLine();			///

								//
								// Process the rest of the file
								//
								while (thisLine != null) {

									if (thisLine.contains("Memory") && thisLine.contains("Usage")) {
										memPct = getMemStats(thisLine);
										if(DEBUG) {System.out.println("Memory Data gathered");}
									}
									else if (thisLine.contains("cpu") && thisLine.contains("usage")) {
										cpuStats = getCpuStats(thisLine, cpuStats);
										if(DEBUG) {System.out.println("CPU Data gathered");}
									}
									else if (thisLine.contains("---")) { // Line with all the --- before the stats we want
										thisLine = in.readLine();
										while (thisLine.trim().length() > 0) { // Blank line after the stats we want
											tpsStats = getTpsStats(thisLine, tpsStats);
											thisLine = in.readLine();
										}
										if(DEBUG) {System.out.println("TPS Data gathered");}
									}
									thisLine = in.readLine();
								}
								//
								// Build the string for the csv output file
								//
								String cpuString = cpuStats[0]+", "+cpuStats[1]+", "+
									cpuStats[2]+", "+cpuStats[3]+", "+cpuStats[4];

								String tpsString = tpsStats[0]+", "+tpsStats[1]+", "+
									tpsStats[2]+", "+tpsStats[3]+", "+tpsStats[4];

								csvString = dateTimeStamp+", "+
											memPct+", "+
											cpuString+", "+
											tpsString;
								//
								// Update the results .csv file
								//
								out.write(csvString + "\n");
								in.close();
								//
								// Move and rename the datafile to the processed folder with the
								// extension of .done
								//
								fin.renameTo(new File(processedFolder+thisfile+".done"));
			                } catch (Exception e) {
			                	System.out.println("\nA Error Occured with file processing");
								e.printStackTrace();
								runMe=false;
								out.close();
			                }
			            }
			        }
			    }
			    //
				// Wait timer for running continuously.
				// The timer is set at the top of the class with the runInterval argument.
				//
				try {
					Thread.sleep(runInterval*1000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				//
				// Run once! This is mostly for DEBUGging, when the statement does not execute this will
				// become more of a deamon like process in it's behavior. Rescanning the folder every
				// "runInterval" seconds until it is killed.
				//
				if(DEBUG) { runMe=false; }
				out.close();
			}

		} catch (Exception e) {
			System.out.println("\nA Error Occured");
			e.printStackTrace();
		}
		return("Done");
	}

	/** checkArgs()
	*
	* This is method validates the command line arguments as best as we can.
	*
	* param - arg[]
	*
	* return - boolean - true of no errors detected else false.
	*/
	public static boolean checkArgs(String[] arg){
		boolean valid = true;
		if(DEBUG){ for(int i=0;i<arg.length;i++){ System.out.println("DEBUG :: arg "+i+" -"+arg[i]+"-"); }}

		// Check Interval time
		if(Integer.parseInt(arg[0]) < 1) { valid = false; System.out.println("EEROR :: runInterval -"+arg[0]+"- is not valid"); }

		// Check for the data directory
		File dDir = new File(arg[1]);
		if(!dDir.exists()){ valid = false; System.out.println("EEROR :: dataDir -"+arg[1]+"- is not valid"); }

		// Check for the processed directory
		File pDir = new File(arg[2]);
		if(!pDir.exists()){ valid = false; System.out.println("EEROR :: procDir -"+arg[2]+"- is not valid"); }

		// Check for the result directory
		File rDir = new File(arg[3]);
		if(!rDir.exists()){ valid = false; System.out.println("EEROR :: resultDir -"+arg[3]+"- is not valid"); }

		// Check for a basename
		if(!(arg[4].length() > 0)) { valid = false; System.out.println("EEROR :: basename -"+arg[4]+"- is not valid"); }

		return valid;
	}

	/** getMemStats()
	*
	* This is method that gets the Memory stats from the string of data
	*
	* param - String containing a record to parse.
	*
	* return - int - value for the % of memory being used.
	*/
	public static int getMemStats(String record){
		String[] toks = record.split("\\s+");
		if(DEBUG) { System.out.print("DEBUG :: "); for(int i=0;i<toks.length;i++){ System.out.print(" -"+toks[i]+"-"); } System.out.print("\n");}
		return Integer.parseInt(toks[3]);
	}

	/** getCpuStats()
	*
	* This is method that gets the CPU stats from the string of data
	*
	* param - String containing a record to parse.
	*       - int array for storing the values of the CPU data
	*
	* return - int[] - with the CPU data for this record.
	*/
	public static int[] getCpuStats(String record, int[] cpuStats){
		String[] toks = record.split("\\s+");
		if(DEBUG) { System.out.print("DEBUG :: "); for(int i=0;i<toks.length;i++){ System.out.print(" -"+toks[i]+"-"); } System.out.print("\n");}
		cpuStats[0] = Integer.parseInt(toks[3]);
		cpuStats[1] = Integer.parseInt(toks[4]);
		cpuStats[2] = Integer.parseInt(toks[5]);
		cpuStats[3] = Integer.parseInt(toks[6]);
		cpuStats[4] = Integer.parseInt(toks[7]);
		return cpuStats;
	}

	/** getTpsStats()
	*
	* This is method that gets the TPS stats from the string of data
	*
	* param - String containing a record to parse.
	*       - int array for storing the values of the TPS data
	*
	* return - int[] - with the TPS data for this record.
	*/
	public static int[] getTpsStats(String record, int[] tpsStats){
		String[] toks = record.split("\\s+");
		if(DEBUG) { System.out.print("DEBUG :: "); for(int i=0;i<toks.length;i++){ System.out.print(" -"+toks[i]+"-"); } System.out.print("\n");}		tpsStats[0] = tpsStats[0]+Integer.parseInt(toks[3]);
		tpsStats[1] = tpsStats[1]+Integer.parseInt(toks[4]);
		tpsStats[2] = tpsStats[2]+Integer.parseInt(toks[5]);
		tpsStats[3] = tpsStats[3]+Integer.parseInt(toks[6]);
		tpsStats[4] = tpsStats[4]+Integer.parseInt(toks[7]);
		return tpsStats;
	}

}

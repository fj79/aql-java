/* 
 * Copyright 2012-2015 Aerospike, Inc.
 *
 * Portions may be licensed to Aerospike, Inc. under one or more contributor
 * license agreements.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aerospike.aql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import com.aerospike.aql.AQLGenerator.Language;
import com.aerospike.aql.IResultReporter.ViewFormat;
import com.aerospike.client.AerospikeClient;
/**
 * AQL is a runnable utility that can either compile AQL to Java, C or C#
 * or it will execute the commands directly on a cluster.
 * 
 * To run: java -jar AQL.jar <options> 
 * The options are:
 * -c,--compile <arg>    AQL file name to compile
 * -h,--host <arg>       Server hostname (default: localhost)
 * -l,--language <arg>   Target language, supported languages: C, CSHARP and JAVA. Only valid with -c
 *                       option
 * -n,--namespace <arg>  Namespace (default: test)
 * -o,--output <arg>     Output file name. Only valid with -c option
 * -p,--port <arg>       Server port (default: 3000)
 * -s,--set <arg>        Set name. Use 'empty' for empty set (default: demoset)
 * -u,--usage            Print usage.
 * 
 * The file names can be a series of files and directories. 
 * If directories are supplied, files with the extension ".csv" will be processed. 
 * Note that the extension is case sensitive.
 * 
 * 
 * @author peter
 *
 */

public class AQLrun {
	private static Logger log = Logger.getLogger(AQLrun.class);
	public static void main(String[] args) {
		try {
			Options options = new Options();
			options.addOption("h", "host", true, "Server hostname (default: localhost)");
			options.addOption("p", "port", true, "Server port (default: 3000)");
			options.addOption("n", "namespace", true, "Namespace (default: test)");
			options.addOption("s", "set", true, "Set name. Use 'empty' for empty set (default: demoset)");
			options.addOption("c", "compile", true, "AQL file name to compile, cannot be used with -f");
			options.addOption("f", "file", true, "AQL file name to execute, cannot be used with -c");
			options.addOption("l", "language", true, "Target language, supported languages: JAVA (future: C and CSHARP). Only valid with -c option");
			options.addOption("O", "output", true, "Output file name. Only valid with -c option");
			options.addOption("o", "of", true, "Output format. TABLE or JSON");
			options.addOption("u", "help", false, "Print usage.");

			CommandLineParser parser = new PosixParser();
			CommandLine cl = parser.parse(options, args, false);

			if (cl.hasOption("u")) {
				logUsage(options);
				return;
			}

			String host = cl.getOptionValue("h", "127.0.0.1");
			String portString = cl.getOptionValue("p", "3000");
			int port = Integer.parseInt(portString);
			String namespace = cl.getOptionValue("n","test");
			String set = cl.getOptionValue("s", "test");
			
			
			String format = cl.getOptionValue("o", "TABLE");
			ViewFormat viewFormat = null;
			
			if (format.equalsIgnoreCase("JSON"))
				viewFormat = ViewFormat.JSON;
			else 
				viewFormat = ViewFormat.TABLE;

			
			if (set.equals("empty")) {
				set = "";
			}

			log.debug("Host: " + host);
			log.debug("Port: " + port);
			log.debug("Name space: " + namespace);
			log.debug("Set: " + set);

			if (cl.hasOption("c")){
				// run as a compiler
				String inputFileName = cl.getOptionValue("c");
				log.info("Compiling AQL file: " + inputFileName);
				File inputFile = new File(inputFileName);
				if (!inputFile.exists()){
					log.error("Input file does not exist: " + inputFileName);
					System.exit(-1);
				}
				if (!(inputFileName.endsWith(".aql") || inputFileName.endsWith(".AQL"))){
					log.error("Input file is not .aql: " + inputFileName);
					System.exit(-1);
				}

				String languageString = cl.getOptionValue("l", "JAVA");
				Language language = Language.valueOf(languageString);


				AQL aql = new AQL();
				String outputFileName = "output.java";

				File outputFile = null;
				if (cl.hasOption("o")){
					outputFileName = cl.getOptionValue("o");
					outputFile = new File(outputFileName);
				}

				log.debug("Output: " + outputFileName);
				log.debug("Language: " + language);
				aql.generate(inputFile, outputFile, language);
				log.info("Completed compile of " + inputFileName);
			} else {
				// run as an interpreter
				AerospikeClient client = new AerospikeClient(host, port);
				AQL aql = new AQL(client, 500, viewFormat);

				if (cl.hasOption("f")){
					String inputFileName = cl.getOptionValue("f");
					File inputFile = new File(inputFileName);
					log.info("AQL executing file: " + inputFileName);
					aql.execute(inputFile, null);
				} else {
					log.debug("AQL Interactive");
					aql.interpret();
				}
			}

		} catch (Exception e) {
			log.error("Critical error", e);
		}
	}
	/**
	 * Write usage to console.
	 * @throws IOException 
	 */
	public static void logUsage(Options options)  {
		HelpFormatter formatter = new HelpFormatter();
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		String syntax = AQL.class.getName() + " [<options>]";
		formatter.printHelp(pw, 100, syntax, "options:", options, 0, 2, null);
		log.info(sw.toString());
		printHelp();
	}
	
	public static void printHelp(){
		URL helpUrl = AQLrun.class.getResource("commands.txt");
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(helpUrl.openStream()));
			try {
				String line = br.readLine();

				while (line != null) {
					log.info(line);
					line = br.readLine();
				}
				
			} finally {
				br.close();
			}
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			log.debug("Detailed error:", e);
		} catch (IOException e){
			log.error(e.getMessage());
			log.debug("Detailed error:", e);
		}
	}

}

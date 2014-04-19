package info.financialecology.googlebooksextractor;
/*
 * Copyright (c) 2014 Gilbert Peffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */


import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volume.VolumeInfo.IndustryIdentifiers;
import com.google.api.services.books.model.Volumes;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jargs.gnu.CmdLineParser;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;


/**
 * This application lets you specify a combination of search terms to search for entries in the 
 * Google Books collection. The search function uses the Google Books API http://goo.gl/H9Onl5.
 * <p>
 * For the application to work, you need to obtain an API key and either store it in the 
 * ClientCredentials.java file (if you are a developer) or provide it as a command line argument
 * if you use the executable jar. Get your API key from:
 *      https://developers.google.com/books/docs/v1/using#APIKey
 * <p>
 * There are basically two types of searches possible: simple search and term cluster search.
 * <p>
 *    1. Simple search
 *    ----------------
 *    You provide an arbitrary number of search term combinations and the application queries 
 *    the Google Books API for each of the combinations. The results are then merged and 
 *    duplicates are removed. Search term combinations are whatever can be handled by the Google
 *    Books API. Examples are:
 *      - systemic risk
 *      - "systemic risk" (TODO currently quoted terms cannot be mixed with unquoted terms)
 *      - intitle:uncertainty
 *      - intitle:risk uncertainty intitle:probability subject:economics
 *    
 *    2. Term clusters - Binomial combinations
 *    ----------------------------------------
 *    You can define several clusters containing an arbitrary number of search term combinations.
 *    The application will create binomial combinations of new search terms based on the terms in 
 *    the different clusters. An example of two clusters containing two and three search terms 
 *    respectively:
 *    
 *      - Cluster A
 *          - risk
 *          - intitle:uncertainty
 *          - "accident theory"
 *      - Cluster B
 *          - finance
 *          - financial
 *      
 *      - The application builds queries for the following term combinations:
 *          - {risk, finance}, {risk, financial}, {intitle:uncertainty, finance}, 
 *            {intitle:uncertainty, financial}, {"accident theory", finance}, 
 *            {"accident theory", financial}
 *        and searches the Google Books API for each of them. The results are then merged and duplicates
 *        removed.   
 *    
 * The search terms together with other parameters are provided in .xml files, which can be 
 * edited using standard text editors. The parameters are described in below. See the example files
 * in the folder ./in/examples for further help.
 *    
 *   - The parameter files have to start and end with a descriptor containing the package and 
 *     class name
 *       <info.financialecology.googlebooksextractor.GoogleBooksExtractorParams>
 *       ...
 *       </info.financialecology.googlebooksextractor.GoogleBooksExtractorParams>
 *          
 *   - Maximum pagination for the queries. Each query returns a maximum of 40 results, and pagination 
 *     can be used to get more results.
 *       <paginations>8</paginations> - this queries Google Books a maximum of 8 times for a given 
 *                                      search term
 *                                         
 *   - Term clusters, represented internally as a HashMap containing array lists of search terms. 
 *     Each list of search terms is separated in the xml file by the <entity> tag. The first <string>
 *     after the <entity> tag is the key of the term list in the HashMap, but is not used in the 
 *     application. It needs to be unique though. The search terms are provided as strings inside 
 *     the <list> tag. An example is 
 *        
 *       <clusters>
 *           <entry>
 *               <string>risk</string>
 *               <list>
 *                   <string>intitle:risk uncertainty</string>
 *                   <string>nuclear</string>
 *               </list>
 *           <entry>
 *           <entry>
 *               <string>system</string>
 *               <list>
 *                   <string>complex intitle:chaos</string>
 *                   <string>Luhmann</string>
 *                   <string>Varela</string>
 *               </list>
 *           <entry>
 *       </clusters>
 *          
 *   - Filters based on regex expressions to further specify what books are stored. The regex 
 *     expression is applied to lower case versions of both the title and the description 
 *     (if provided). An example which stores only those books where the title or description
 *     contains the word 'handbook' is
 *           <string>(.*)handbook(.*)</string>
 * <p>
 * The application can be run from the command line with the parameters:
 *    -p                name of parameter file, including path
 *    -v (optional)     verbose output
 *    -o (optional)     name of the output file
 *    -a (optional)     api key provided externally, as an alternative to hard coding in 
 *                      {@code ClientCredentials}
 * <p>
 * To run the application in Eclipse, you need to set up the command line parameters as follows
 * <ul>
 * <li> Right-click on GoogleBooksExtractor.java in the Package Explorer view
 * <li> Select Run As -> Run Configurations...
 * <li> If GoogleBooksExtractor is not in the Java Application list, select the 'Java Application' 
 *      entry and then click on the 'New launch configuration' button at the top left. A new entry 
 *      GoogleBooksExtractor will appear.
 * <li> Click on the GoogleBooksExtractor entry
 * <li> Click on the tab '(x) = Arguments' tab in the right-hand pane
 * <li> Add the following parameter line: -p "${file_prompt}" -v -o "./out/" -a "ENTER YOUR API KEY HERE"
 * <li> Copy your API key to between the double quotes
 * <li> Click Apply or Run
 * </ul>
 * <p> 
 * 
 * Limitations
 *  - The API seems to have a limit of 1000 search results it can return via pagination. This 
 *    limitation is not documented.
 *    
 * Some Google Books basics:
 *    - Volume: A volume in Google Books speak represents the data that Google Books hosts about 
 *      a book or magazine.
 *    - You can perform searches directly via HTTPS, which comes in handy for testing purposes.
 *      For example:
 *          https://www.googleapis.com/books/v1/volumes?q=systemic+intitle:risk
 *    
 *    - You can find the list of allowable query parameters (e.g. 'intitle:') here:
 *          https://developers.google.com/books/docs/v1/using#st_params
 *    - You can review the Google Books API documentation at:
 *          https://developers.google.com/books/docs/v1/getting_started
 *    - Javadoc for Books API v1: http://goo.gl/rilXX
 *    - Google API Console: http://goo.gl/vmZf9
 * 
 */
public class GoogleBooksExtractor {

    private static final Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    
	/**
	 * The name of the application. If the application name is {@code null} or blank, the Google 
	 * Books API will log a warning.
	 */
	private static final String APPLICATION_NAME = "GilbertPeffer-GoogleBookParser/1.0";

	// The command line arguments and parameters extracted from the parameter file
	private static class CmdArgs {
		static GoogleBooksExtractorParams params; // parameters from the parameter file 
		static Boolean verbose;                   // command line argument 
		static String output;                     // output file name (relative path)
		static String apikey = null;              // api key, so that jar can be executed without need for changes in java code
	}
	
	// Book identifiers = a unique ID stored in the 'id' field of the volumes data structure
	private static HashSet<String> bookIds = new HashSet<String>();
	
	// Details of all identified book volumes
	private static ArrayList<ArrayList<String>> volList = new ArrayList<ArrayList<String>>();
	
	// Logged search information
	private static ArrayList<ArrayList<String>> logList = new ArrayList<ArrayList<String>>(); 
		
	// Misc parameters
	private static int maxResults = 40;			// maximum number of results returned by the Google Books API (40 is the maximum allowed)
	private static int startIndex = 0;			// volume index at which to start in the Google Books search
	private static int startVolList = 0;		// start index for a given set of volumes in the volList ArrayList
	private static int totalVolCounter = 0;		// Total number of volumes returned by Google Books for a particular query
	private static int retainedVolCounter = 0;	// Total number of volumes retained for a given set returned by Google Books (excludes duplicates)
	
	/**      
	 * Main routine
	 *     - Read arguments from the command line and parameters from the parameter file
	 *     - Determine whether simple search or a term cluster search
	 *     - For each term or term combination
	 *         - build the query string
	 *         - get the volumes (books) using the query, up to the maximum number of paginations
	 *         - remove duplicates and store the volumes
	 *     - Output information on results
	 *      
	 * Command line arguments:
	 *    -p                name of parameter file, including path
	 *    -v (optional)     verbose output
	 *    -o (optional)     name of the output file
	 *    -a (optional)     api key provided externally, as an alternative to hard coding in 
	 *                      {@code ClientCredentials}
	 */
	public static void main(String[] args) {
	    root.setLevel(Level.DEBUG);
        Logger logger = (Logger)LoggerFactory.getLogger("main");
        
        System.out.println("\nGoogle Books Extractor");
        System.out.println("======================");
        System.out.println("[add brief description]\n");

		JsonFactory jsonFactory = new JacksonFactory();
		
//        String outputDir = "./out/";	// TODO add folder name equal to parameter file name

		logger.debug("\n\nSTART PROCESSING GOOGLE BOOKS METADATA\n");

		try {
	        logger.trace("Reading command line arguments and parameter file...");

	        String fileName = processCmdLine(args);		// process command line arguments

	        logger.trace("Reading parameter values from file: {}...", fileName);
	        GoogleBooksExtractorParams params = CmdArgs.params;
	        
	        String outputDir = CmdArgs.output;
	        
	        // Validate parameters
	        GoogleBooksExtractorParams.validate(params);
	        
	        ArrayList<String> tmpNameList;
	        	        
	        // Extract file name from path
	        if (fileName.indexOf("/") >= 0)  // path name with forward slashes (when running from MS-DOS)
                tmpNameList = new ArrayList<String>(Arrays.asList(fileName.split("/")));
	        else
    	        tmpNameList = new ArrayList<String>(Arrays.asList(fileName.split("\\\\")));
	        
            String fileNameStripped = (tmpNameList.get(tmpNameList.size() - 1).split("\\."))[0];              
	        
	        // Extract book volumes from Google Books for all inter-cluster term combinations
	        Set<String> clusterNames = params.clusters.keySet();
	        ArrayList<String> clusterNameList = new ArrayList<String>(clusterNames);
	        	        
	        logger.trace("Term clusters: {}", params.clusters);
	        
	        int totalTermCombinations = 0;
	        int numClusters = clusterNameList.size();
	        if (numClusters == 1) clusterNameList.add("null_cluster");	// get it to work with just one cluster
	        
	        /*
	         * Loop over all pairwise cluster combinations and create pairs of terms
	         */
	        for (int i = 0; i < clusterNameList.size(); i++) {
		        for (int j = i + 1; j < clusterNameList.size(); j++) {
        			logger.trace("Cluster combination: [{}, {}]", clusterNameList.get(i), clusterNameList.get(j));

		        	// Get the terms for the clusters 'i' and 'j'
		        	ArrayList<String> terms_1 = params.clusters.get(clusterNameList.get(i));
		        	ArrayList<String> terms_2;
		        	
		        	// Handle special case where just one cluster of terms is given
		        	if (clusterNameList.get(j).compareTo("null_cluster") != 0)
		        		terms_2 = params.clusters.get(clusterNameList.get(j));
		        	else {
		        		terms_2 = new ArrayList<String>();
		        		terms_2.add("null_term");
		        	}
		        	
		        	/*
		        	 * Loop over all term pairs of {cluster 'i', cluster 'j'}
		        	 */
		        	for (String term_1 : terms_1) {			// loop over all terms in the first cluster
		        		for (String term_2 : terms_2) {		// loop over all terms in the second cluster
		        		    
		        		    if (terms_2.get(0).compareTo("null_term") == 0)
	                            logger.trace("\n\nNEW TERM: [{}]\n", term_1);
		        		    else
		        		        logger.trace("\n\nNEW TERM COMBINATION: [{}, {}]\n", term_1, term_2);
		        			
		        			retainedVolCounter = 0;
		        			startIndex = 0;
		        			int queryPage = 0;
		        			
		        			// Construct full query term
                            String query = term_1;

                            if (terms_2.get(0).compareTo("null_term") != 0)
		        			    query += "+" + term_2;
                            
                            // Surround '+' with spaces, to prevent strange (unexplicable) behaviour of the Google Books query
                            query = query.replaceAll("\\+", " + ");
		        					        			
   		        			/*
		        			 * Apply pagination to extract all (available) book volumes - there seems 
		        			 * to be a limit of approx. 1000 volumes returned, imposed (probably) by 
		        			 * Google Books API.
		        			 */
		        			Volumes volumes = null;
		        			int numPaginations = 0;
		        			logger.trace("\n\nQUERY PAGE {}\n", queryPage + 1);
		        			
		        			do {		        			    
	                            try {
	                                volumes = queryGoogleBooks(jsonFactory, query, startIndex, maxResults);
	                            } catch (IOException e) {
	                                System.err.println(e.getMessage());
	                            }
	                            
	                            if (volumes != null) {
                                    storeBooks(params.filters, volumes, query);
                                    totalVolCounter += volumes.getItems().size();
                                    startIndex += maxResults;
                                    
                                    logger.trace("\n\nQUERY PAGE {} (startIndex = {})\n", ++queryPage + 1, startIndex);
	                            }
	                            
	                            numPaginations++;
	                            
		        			} while ((volumes != null) && (numPaginations < params.paginations));		        			

		        			// Volume information
		        			ArrayList<String> logInfo = new ArrayList<String>();
		        			logInfo.add(query);
		        			logInfo.add(Integer.toString(totalVolCounter));
		        			logInfo.add(Integer.toString(retainedVolCounter));
		        			logInfo.add(Integer.toString(startVolList));
		        			logList.add(logInfo);

		        			logger.trace("\n\nEND QUERY PAGES\n");
		        			logger.debug("Logged query: [query, totalVol, retainedVol, start index in volList] {}", logInfo);
		        			
		        			startVolList += retainedVolCounter;
		        			totalTermCombinations++;
		        		}
		        	}
		        			        	
                    if (terms_2.get(0).compareTo("null_term") == 0) // only one cluster was provided
                        logger.debug("\n\nPROCESSED {} CLUSTER TERMS\n", terms_1.size());
		        }
	        }
	        
            if (clusterNameList.get(1).compareTo("null_cluster") != 0)  // only one cluster was provided
                logger.debug("\n\nPROCESSED {} TERM COMBINATIONS FROM {} CLUSTERS\n", totalTermCombinations, clusterNameList.size());


        	/*
        	 *  Write volList results to CSV file
        	 */
            if ((outputDir != null) && (volList.size() != 0)) {
    	        logger.debug("\n\nWriting results to CSV file: {}\n", outputDir + fileNameStripped + ".csv");
    	        ResultWriterFactory.newCsvWriter(outputDir + fileNameStripped + ".csv").write(volList);
            }
            
	        logger.debug("\n\nFINISHED PROCESSING GOOGLE BOOKS METADATA\n");
            logger.debug("Total number of volumes returned by Google Books API: {}", totalVolCounter);
        	logger.debug("Total number of volumes stored: {}", volList.size() - 1);
        	
            if (volList.size() == 0) {
                logger.debug("\n\n### IMPORTANT ### Google Books has returned no results, which can mean "
                        + "that your API key has not been set\n");
            }
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	
	/**
	 * Execute the query on Google Books to obtain the list of volumes. The maximum number of volumes
	 * returned is 40.
	 * 
	 * @param jsonFactory
	 * @param query
	 * @param startIndex
	 * @param maxResults
	 * @return volumes
	 * @throws Exception
	 */
    private static Volumes queryGoogleBooks(JsonFactory jsonFactory, String query, int startIndex, int maxResults) throws Exception {
        Logger logger = (Logger)LoggerFactory.getLogger("queryGoogleBooks");
        
        ClientCredentials.errorIfNotSpecified(CmdArgs.apikey);
        
        String apiKey = ClientCredentials.API_KEY;
        
        if (CmdArgs.apikey != null) apiKey = CmdArgs.apikey;

        // Create books client (provides the query service)
        final Books books = new Books.Builder(new NetHttpTransport(), jsonFactory, null)
                                     .setApplicationName(APPLICATION_NAME)
                                     .setGoogleClientRequestInitializer(new BooksRequestInitializer(apiKey))
                                     .build();
        
        logger.trace("Query: [{}]\n", query);
        
        List volumesList = books.volumes().list(query);
        volumesList.setStartIndex((long) startIndex);
        volumesList.setMaxResults((long) maxResults);

        Volumes volumes = volumesList.execute();    // Execute the query
        
        // No volumes found
        if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
            logger.trace("==============================");
            logger.trace("No (further) matches found for query");
            logger.trace("==============================\n");
            return null;
        }

        return volumes;
    }
    
    
    /**
     * Storing the volumes (books) in the global table {@code volList}. The table is represented 
     * internally as an arraylist of arraylists of strings (the volume information).
     * 
     * Duplicates and books that have no title are not stored. If filters are provided in the parameter
     * file, they are applied here and only those books are stored whose title or description (if 
     * provided) are matched by the regex filter(s). 
     * 
     * @param filters
     * @param volumes
     * @param query
     */
	private static void storeBooks(ArrayList<String> filters, Volumes volumes, String query) {
		
		Logger logger = (Logger)LoggerFactory.getLogger("storeBooks");

		// Store each book (volume) in the array list volList if it isn't there already
		for (Volume volume : volumes.getItems()) {
			Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();
			java.util.List<IndustryIdentifiers> ids = volumeInfo.getIndustryIdentifiers();
            String title = volumeInfo.getTitle();
            String bookId = "";
			
	        if (title != null) // skip volumes where a title is not provided
	               if (title.isEmpty()) continue;
			
			/*
			 * We are using the industry identifiers such as ISBN to avoid duplicate entries. In case
			 * that the identifier is not available, we use the 20 first characters of the title.
			 */
			if (ids != null && !ids.isEmpty()) {
				bookId = ids.get(0).getIdentifier();
				
				if (bookIds.contains(bookId)) {
					logger.trace("DUPLICATE: Book not added, because industry identifier [{}] already registered. [TITLE: {}]", bookId, title);
					continue;
				}
				else
					bookIds.add(bookId);
			}
			else {
				// TODO If no industry identifier is given, create a hash key using the first N terms of the title
				bookId = volumeInfo.getTitle();
				
				if (bookId != null && !bookId.isEmpty()) {
					int len = bookId.length();
					if (len > 20) len = 20;
					bookId = bookId.substring(0, len-1);
					
					if (bookIds.contains(bookId)) {
						logger.trace("DUPLICATE: Book not added, because title substring [{}] already registered. [TITLE: {}]", bookId, title);
						continue;
					}
					else {
						bookIds.add(bookId);
						logger.trace("Book added, with title substring [{}] registered in HashSet", bookId);
					}
				} else {
					logger.error("Book not added, because of both the industry identifier and the title are missing");
					continue;					
				}
			}
			
			/*
			 *  Apply regex filters to titles and descriptions. Filters are OR'ed, meaning
			 *  that if several are provided, one match is sufficient to store the volume.
			 */
			Boolean match = false;
	        Boolean storeVolume = true;

			String description = volumeInfo.getDescription();
			
			if (description == null) description = "";
			
            if (filters != null && !filters.isEmpty()) {
				for (String filter : filters) { // no need to apply multiple filters since this can be handled by regex
				    if ((description != null) && (!description.isEmpty())) {
    					if (description.toLowerCase().matches(filter.toLowerCase()) || title.toLowerCase().matches(filter.toLowerCase())) {
    						match = true;
    						break;
    					}
				    }
				    else {  // if no description is provided, do not apply filter to it
                        if (title.toLowerCase().matches(filter.toLowerCase())) {
                            match = true;
                            break;
                        }				        
				    }
				}
				
                if (!match)     // don't store the book if there is no regex match for the given title or description
                    storeVolume = false;
			}
            
            /*
             * Create a list of lists containing the book information (in string form) 
             */
			if (storeVolume) {
				ArrayList<String> volInfo = new ArrayList<String>();
				
				// Add a header with the entry labels
				if (volList.isEmpty()) {					
					ArrayList<String> volHeader = new ArrayList<String>();
					volHeader.add("title");
					volHeader.add("query");
					volHeader.add("authors");
					volHeader.add("categories");
					volHeader.add("description");
					volHeader.add("published");
					volHeader.add("previewLink");
					volHeader.add("industryId");
					volList.add(volHeader);
				}
				
				if (volumeInfo.getSubtitle() != null)
					title += ". " + volumeInfo.getSubtitle();
				
				volInfo.add(title);
				
				volInfo.add(query);
				
				if (volumeInfo.getAuthors() == null)
					volInfo.add(null);
				else
					volInfo.add(volumeInfo.getAuthors().toString());
				
				if (volumeInfo.getCategories() == null)
					volInfo.add(null);
				else
					volInfo.add(volumeInfo.getCategories().toString());
				
				volInfo.add(volumeInfo.getDescription());
				volInfo.add(volumeInfo.getPublishedDate());		// TODO extract year, using regex
				volInfo.add(volumeInfo.getPreviewLink());
				volInfo.add(bookId);
				volList.add(volInfo);
				
				logger.trace("Stored title: {}", title.substring(0, Math.min(title.length(), 100)));
	
				retainedVolCounter++;   // counter for the number of stored volumes (books)
			}
			else
				logger.trace("NOT STORED: {}", title.substring(0, Math.min(title.length(), 100)));

		}
	}

	    
	/**
	 * Print the help string for command line usage 
	 */
	private static void printUsage() {
		System.err.println(getUsage());
	}

	
    /**
     * The help string for command line usage 
     */
	private static String getUsage() {
		return "Usage: GoogleBooksExtractor [{-v,--verbose}] {-p,--params} parameter file [{-o,--output} output file] [{-a,--apikey} api key]";
	}

	
	/**
	 * Processing the command line arguments
	 * 
	 * @param args
	 * @return
	 */
	private static String processCmdLine(String[] args) {
		Logger logger = (Logger)LoggerFactory.getLogger("processCmdLine");

		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option verbose = parser.addBooleanOption('v', "verbose");
        CmdLineParser.Option inputFileName = parser.addStringOption('p', "params");
        CmdLineParser.Option outputFileName = parser.addStringOption('o', "output");
        CmdLineParser.Option apiKey = parser.addStringOption('a', "apikey");

		try {
			parser.parse(args);
		}
		catch ( CmdLineParser.OptionException e ) {
			System.err.println(e.getMessage());
			printUsage();
			System.exit(2);
		}

        String fileNameValue = (String)parser.getOptionValue(inputFileName);
        Assertion.assertStrict(fileNameValue != null, Assertion.Level.ERR, "Parameter file name argument is missing\n\n" + getUsage() + "\n");

        CmdArgs.output = (String)parser.getOptionValue(outputFileName);
        CmdArgs.apikey = (String)parser.getOptionValue(apiKey);

		CmdArgs.verbose = (Boolean)parser.getOptionValue(verbose, Boolean.FALSE);

		if (CmdArgs.verbose)
			root.setLevel(Level.TRACE);
		else
			root.setLevel(Level.DEBUG);

		GoogleBooksExtractorParams params = null;

		try {
			params = GoogleBooksExtractorParams.readParameters(fileNameValue);            
		} catch (Throwable e) {
			logger.error(e.getMessage());
			System.exit(0);
		}

		CmdArgs.params = params;

		return fileNameValue;
	}

}
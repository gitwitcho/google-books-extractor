/*
 * Copyright (c) 2013 Gilbert Peffer
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
package info.financialecology.googlebooksextractor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple parameter class that uses xstream to read and write parameters for the
 * GoogleBooksExtractor class from and to files.
 * 
 * To create a sample parameter file from this class, use writeParameterDefinition.
 *      
 * See also:
 *      Xstream: http://tinyurl.com/66o9od
 *      "Use XStream to serialize Java objects into XML": http://tinyurl.com/6ah27g
 * 
 * @author Gilbert Peffer
 *
 */
public class GoogleBooksExtractorParams extends XmlParameters {
        
    /**
     * Parameter declarations.
     * 
     * See {@code GoogleBooksExtractor} for a description of the paramters
     */    
    public HashMap<String, ArrayList<String>> clusters = new HashMap<String, ArrayList<String>>();  // the clusters or sets of term combinations
    public ArrayList<String> filters = new ArrayList<String>(); // the regex filters for the volume title or description 
    public int paginations; // the maximum number of paginations for any given search term

    //    GoogleBooksExtractorParams() {	// Uncomment to write a test XML file with writeParamDefinition(...) 
//    	L1 = new Sweep_1();
//    	ArrayList<String> al = new ArrayList<String>();
//    	ArrayList<String> al2 = new ArrayList<String>();
//    	
//    	al.add("Term 1");
//    	al.add("Term 2");
//    	L1.clusters.put("Cluster 1", al);
//    	
//    	al2.add("Term 10");
//    	al2.add("Term 1");
//    	al2.add("Term 3");
//    	al2.add("Term 2");
//    	L1.clusters.put("Cluster 2", al2);
//    	
//    	L1.filters.add("Filter 1");
//    	L1.filters.add("Filter 2");
//    	L1.filters.add("Filter 3");
//      paginations = 5;
//    }
    
    GoogleBooksExtractorParams() { }
    
    /**
     * Parameter validation
     */
    public static void validate(GoogleBooksExtractorParams params) {
        
        Assertion.assertStrict(params.paginations > 0, info.financialecology.googlebooksextractor.Assertion.Level.ERR,
                "Number of paginations either not set or set to less than 1 in parameter file");
    }
    
    /**
     * Creates an xml file that holds the fields of this object
     * 
     * @param file
     * @throws FileNotFoundException 
     */
    public static void writeParamDefinition(String file) throws FileNotFoundException {
        writeParamsDefinition(file, new GoogleBooksExtractorParams());
    }

    /**
     * Reads values from an xml file and initialises the fields of the newly created parameter object
     * 
     * @param file
     * @return
     * @throws FileNotFoundException 
     */
    public static GoogleBooksExtractorParams readParameters(String file) throws FileNotFoundException {
        return (GoogleBooksExtractorParams) readParams(file, new GoogleBooksExtractorParams());
    }

}

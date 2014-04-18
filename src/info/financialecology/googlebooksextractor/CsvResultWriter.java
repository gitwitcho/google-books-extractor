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

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * A class to write string values provided in an array list of array lists of strings to
 * a CSV file.  
 * 
 * @author Gilbert Peffer
 */
public class CsvResultWriter implements ResultWriter {
    private static final char SEPARATOR = ',';
    private CSVWriter w;
    
    /**
     * Constructor
     * 
     * @param fileName
     */
    public CsvResultWriter(String fileName) {
        try {
            w = new CSVWriter(new FileWriter(fileName), SEPARATOR);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor
     * 
     * @param fileName
     * @param separator
     */
    public CsvResultWriter(String fileName, char separator) {
        try {
            w = new CSVWriter(new FileWriter(fileName), separator);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Write results to the file. Currently only results in the form of an ArrayList
     * of ArrayLists can be processed.
     * 
     * @param result
     */
    @SuppressWarnings({ "unchecked" })
    public void write(Object result) {
      Class<? extends Object> c = result.getClass();
      
      if (c.equals(ArrayList.class))
          writeTableOfStrings((ArrayList<ArrayList<String>>) result);
    }

    /**
     * Write the table (ArrayList of ArrayLists) data to the file in CSV format. The first 
     * inner ArrayList needs to contain the header labels for the CSV file.
     * 
     * @param table the ArrayList of ArayLists containing the strings
     */
    private void writeTableOfStrings(ArrayList<ArrayList<String>> table) {
        
        // TODO validate argument

        ArrayList<String> row = table.get(0);			// header labels
        int numRows       = table.size();				// number of rows in the table
        int numCols	      = row.size();				    // number of columns in the table
        String csvItems[] = new String[numCols + 1];    // array holding the items for a single row in the CSV file
        
        // Write table rows to CSV file, including the header row
        for (int i = 0; i < numRows; i++) {
        	row = table.get(i);
        	
        	for (int j = 0; j < numCols; j++)
        		csvItems[j] = row.get(j);
            
        	w.writeNext(csvItems);
        }
        
        try {
            w.flush();  // TODO the file stream needs to be closed properly, but where?
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}

# Google Books Extractor

Obtain information about books via the Google Books API using a variety of search query combinations and filters.

## What it does

The application uses the Google Books API to extract book details such as title, authors, and description, and write these into a CSV file for further processing, for instance by text mining tools. Single, multiple, and clusters of search queries are specified in an XML file that can be edited using any standard editor. You can also define regex-type filters and set the maximum number of paginations in the XML file.

**NOTE**: The Google Books API has a quota for the maximum number of requests that can be done per day for free. The quota is 1000 requests per day, at the time of writing. 

There are two ways in which you can run the application: from Eclipse or from the command line (.bat files are provided for Windows).

## Running the extractor from Eclipse

1. Get a copy of the files from this GitHub repository, for instance using the Eclipse SVN plug-in
2. In Eclipse, add the following program arguments via 'Run Configurations...': ``` -p "${file_prompt}" -v -o "./out/" -a "ENTER YOUR API KEY HERE" ```
3. Obtain an API key for the Google Books API as described in the section [Authentication and Authorisation](https://developers.google.com/books/docs/v1/using#auth) on the API help pages. Add the key to either of
  * the program arguments add under point 2. above
  * the class ```ClientCredentials.java```
4. Run the application in Eclipse via the main method in ```GoogleBooksExtractor.java```

## Running the extractor from the command line

This includes .bat files and has been tested on Windows.

1. Download the ```google-books-extractor.rar``` file from the [release page](https://github.com/gitwitcho/google-books-extractor/releases)
2. Unzip into some directory
3. On Windows, you can do either of two things to run the application
  * Run the executable jar in the command line using an MS-DOS window (cmd.exe). The command line options are
    * -p name of parameter file, including path
    * -v (optional) verbose output
    * -o (optional) name of the output file
    * -a (optional) api key provided externally, as an alternative to hard coding it in ```ClientCredentials.java```
    * Example: ```java -jar google-books-extractor.jar -p ./params/google_books_extractor_VE_1_0.xml -o ./output/ -a "ENTER YOUR API KEY HERE"```
  * Or use one of the three .bat files provided to run the application
    * ```run-dist.bat``` - 
    * ```run_to_txt-dist.bat``` - 
    * ```run_to_csv-dist.bat``` - 

## Single, multiple, and clusters of search queries

There are three different ways in which search queries can be specified:
* **Single search query** - Uses a single query of one or several search terms, with the format defined in the Google Books API [query parameter description](https://developers.google.com/books/docs/v1/using#api_params). Examples of single search queries are:
```
    risk+intitle:bank
    intitle:probability+"systemic risk"+accident+intitle:"accident theory"
    intitle:chaos+subject:economics
```
* **Multiple search queries** - You can provide more than one search query in the XML file. In that case, the application runs the queries separately and merges the results into a single table. Duplicates are removed in the process. An example of how multiple search queries are defined in the XML file is:
```
    <string>complexity</string>
    <string>"bank run"+incentives</string>
    <string>intitle:"behaviour change"+subject:economics</string>
```
* **Multiple clusters of search queries** - You can define an arbitrary number of clusters of search queries, which are then combined pairwise into new search queries. If you define two clusters with 2 and 3 search queries respectively, the application will construct 6 new search queries by combining each search query from the first cluster with each serach query from the second cluster. The new queries are then run separately and the results merged into a single table, as in the case of multiple search queries. An example of how clusters are defined in the XML file is:
```
    <clusters>
        <entry>
            <string>cluster 1</string>
            <list>
                <string>sociology+intitle:finance</string>
                <string>intitle:"social psychology"+crisis</string>
            </list>
        </entry>
        <entry>
            <string>cluster 2</string>
            <list>
                <string>"complex system"</string>
                <string>intitle:multiagent+simulation</string>
                <string>"data mining"</string>
            </list>
        </entry>
     </clusters>
```

# Google Books Extractor

Obtain information about books via the Google Books API using a variety of search query combinations and filters.

## What it does

The application uses the Google Books API to extract book details such as title, authors, and description, and write these into a CSV file for further processing, for instance by text mining tools.

Search query combinations and regex-type filters are specified in an XML file that can be edited using any standard editor. There are three different ways in which search queries can be specified:
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
* **Binomial combinations of search terms** - 

The application can be run on Eclipse or using the executable jar and helper files provided as a zipped archive in the release section.

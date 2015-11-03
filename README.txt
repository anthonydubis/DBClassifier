GROUP INFORMATION
-----------------

Juliana Louback (jl4354) and Anthony Dubis (ajd2194)

FILES
-----

- Interaction.java - handles interactions with the user, such as getting input
- Classifier.java - implementation for Part 1
- Summarizer.java - implementation for Part 2
- Utils.java - contains supportive methods, such as those that query Bing
- TopK.java - object for holding top document urls and webtotals for a query
- getWordsLynx.java - calls the provided Java script for processing a webpage

RUNNING THE PROGRAM
-------------------



PROGRAM DESIGN
--------------

Part 1:

Part 1 is primarily contained in Classifer.java and works as follows. When the
class is constructed, the classification hierarchy is created in the form of a
tree (a simple Node class is represented within Classifer.java). This tree
makes traversing the nodes simple and clearly represents the hierarchy of
categories, their children subcategories, and parent category. The nodes contain
information such as the category name, name of the query file associated with
it, and the coverage associated with it.

From classifyDB(), the only public method, the classify() method is called. The
classify() method is a close representation of Figure 4 in the provided paper.
To handle the set of results, which is added to as new classificaitons are made,
we use an ArrayList of Nodes. This ArrayList contains the most specific node for
any classification. For example, if one of the classifications were
Root/Sports/Soccer, the ArrayList would only contain the Soccer node as we can
work our way up the nodes' parent pointers to create the hierarchy. This
ArrayList can contain multiple nodes, such as Soccer, Programming, and Health.
Only nodes that pass the coverage and specificity thresholds are put in this
list, and we call them "qualifying nodes."

Specificity and coverage are calculated as detailed in Definitions 3.2 and 3.3
in the provided paper and printed to standard output per category processed. 

As mentioned before, parent pointers can be used to create the classification.
Once we have the qualifying nodes, we do just this to create our array of
strings where each entry is a classification for the database that passes the
thresholds provided.

Part 2:

Part 2 is primarily contained in Summarizer.java. A Summarizer object has the
following attributes: the host to be summarized, Bing account key, a set to
contain the urls of the document sample, a map structure for term frequencies,
a map structure for term matches and a map structure to reference the respective
text file for a given category.

From buildSummaries(), the only public method, an instance of the Classifier
oject is created and Classifier.classifyDB() is run with the given host, key,
specificty threshold and coverage threshold resulting in an array of
classifications (i.e. {"Root/Health", "Root/Sports/Soccer"}). For each of the
classifications, sampleAndSummarize() is run for the categories in ascending
order ignoring leaf categories (i.e. For "Root/Sports/Soccer" the first run is
"Sports" then "Root"). Doing so avoids re-processing the subcategory when 
processing the category; the 'frequencies' and 'matches' maps are already filled
with the subcategory data and can be incremented with category values. Note that
for each classification, the restart() method is called that (re)initializes
empty maps for a new run.

The method sampleAndSummarize() contains the lion's share of the logic.
According to the category received, the corresponding query probe file is
traversed. For each query probe, a Utils function returns a TopK object
containing the up to 4 top results returned by Bing (at times there are less)
and the total number of results available (WebTotal). For these up to 4
documents, if a document is not previously contained in the set 'sample',
the method addFrequenciesAndMatches() runs Graviano's getWordsLynx script
to extract the set of words for a given url. For each of these words, the
'frequencies' map has it's value incremented or a new entry with the word
as key and value 1 is added. The 'matches' map either maintains its value
(in the case of the word being a query probe with a non-default matches
value) or a new entry with the word as key and a default value -1 is added.
After processing the [up to] 4 documents, the query probe itself is added
to the 'frequencies' and 'matches' maps with up to 4 as frequency and the
WebTotal value as matches.

Note for each query probe, the following is printed to standard output:
The round, the query probe, the (up to 4) number of top documents retrieved
from Bing, and the new documents to be included in the sample. As in, a
given query may have retrieved 4 urls, but if 3 of these have been added to
the sample in previous rounds, only the remaining new url will be printed.

Finally, writeSummaries() is run, traversing the 'frequencies' and 'matches'
maps and writing to a file named according to the project specifications.
The line structure is <term>#<frequency>#<matches>. Note the numerical
values are in integer format (not float).

MULTI-WORD QUERY PROBE HANDLING
-------------------------------

As explained in PROGRAM DESIGN, Part 2, we opted to attribute up to 4 points
to a query probe's 'frequency' value. This is done both for single and for
multi-word query probes. The logic is that results returned by Bing for a
multi-word query probe are likely to include all words; as such for a top-4
request we attribute the number of results returned (at times less than 4) to
the respective query probe. However, the frequency is only added if the term
is not present in the 'frequencies' map. Since the query probe is processed 
for entry after the document sample, it is highly likely that single word probes
will not have the associated frequency value incremented by up to 4 and only the
matches value is modified. Even so, the overestimation will be slight and
comparitively insignificant. For multi-word probes, these will result in a new
entry with a minimum document frequency estimate (up to 4) and the Webtotal
value as matches.

BING ACCOUNT KEY
----------------



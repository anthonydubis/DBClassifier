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

Part 1 is primary contained in Classifer.java and works as follows. When the
class if constructed, the classification hierarchy is created in the form of a
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
in the provided paper. 

As mentioned before, parent pointers can be used to create the classification.
Once we have the qualifying nodes, we do just this to create our array of strings
where each entry is a classification for the database that passes the thresholds
provided.

BING ACCOUNT KEY
----------------



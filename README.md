# Quick search in a large corpus of text with constant memory complexity

Using a lazy hashing datastructure this code can search for occurances of a word in a large text corpus with constant memory complexity. 

It will output all occurances of the word, including its context, with 30 characters before and after.

## Usage:

- Generate an index of your text file using the script <strong>createindex.sh</strong>

    - This will use the tokenizer script to generate the index file and sort it

```
sh createindex.sh big.txt
```

* Update the java file variable <strong>korpus</strong> to the location of your textfile.

* Compile and search for occurances:

```
javac Konkordans.java

java Konkordans summarize
```

Output:

```
There exists 3 occurances of the word
cy of regulation in 1767.  7. Summarize the events connected with Ame
f Marbury _vs._ Madison.  15. Summarize Marshall's views on: (_a_) st
eral suffrage amendment.  14. Summarize the history of the suffrage i

```


Remember to delete the lazyhash file if changing text

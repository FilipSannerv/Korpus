#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: $0 <textfile>"
    exit 1
fi

textfile=$1

if [ ! -f "$textfile" ]; then
    echo "File not found"
    exit 1
fi

# Run tokenizer script on textfile to create index file
echo "Generating index..."
./tokenizer < "$textfile" > rawindex.txt

# Create a temporary file to store the sorted output
temp_file=$(mktemp)

# Sort the file by the word alphabetically, and then by the number (offset)
sort -k1,1 -k2,2n rawindex.txt > "$temp_file"

# Overwrite the original file with the sorted content
mv "$temp_file" rawindex.txt
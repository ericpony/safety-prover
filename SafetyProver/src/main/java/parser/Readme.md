### How to modify the parser for a new input format

1. Replace file `sample-input.txt` with an input file in the new format
2. Update `grammar.cf` to accept the new format
3. Run `./build`

That's it.
The script will rebuild the parser and test it against the sample file.
If the sample file is successfully parsed, the script will replace
the parser of the safety checker with the new parser.

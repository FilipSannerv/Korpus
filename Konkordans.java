import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/*
Konkordans written by @sannerv
 */

public class Konkordans {
    private static File korpus;
    private static File rawIndex;
    private static File lazyHashes;
    private static String alphabet = " abcdefghijklmnopqrstuvwxyzåäö";
    private static long startTime;
    private static long endTime;
    private static boolean benchmark = false;

    public static void main(String[] args) throws IOException {
        // Replace korpus with your textfile
        korpus = new File("big.txt");
        rawIndex = new File("rawindex.txt");
        lazyHashes = new File("lazyhash");

        String input = "";
        try {
            if (args.length == 1 && args[0].matches("[A-Öa-ö]*")) {
                input = args[0];
            } else {
                System.out.println("You need to enter only one word");
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("Caught error" + e);
        }

        input = input.toLowerCase();
        // Create new hash file if it does not exist
        // Remember to remove if text changes
        if (!lazyHashes.exists()) {
            startTime = System.nanoTime();
            createHashFile();
            endTime = System.nanoTime();

            if (benchmark) {
                long hashTime = (long) ((endTime - startTime) / 1e6);
                System.out.println();
                System.out.println("******** BENCHMARKING ********");
                System.out.println("Hashning took " + (hashTime / 1000) + " seconds");
            }
        }

        startTime = System.nanoTime();
        int[] hashArray = createHashArray(lazyHashes);
        ArrayList<String> matches = searchWord(input, hashArray);
        endTime = System.nanoTime();

        if (benchmark) {
            long searchTime = (long) ((endTime - startTime) / 1e6);
            System.out.println("All " + matches.size() + " occurances was found in " + (searchTime) + " ms.");
            System.out.println("*******************************");
            System.out.println();
        }

        if (matches.size() == 0) {
            System.out.println("The word you searched for was not found");
        } else if (matches.size() == 1) {
            System.out.println("There exists " + matches.size() + " occurance of the word");
            printLines(matches, input);
        } else if (matches.size() <= 25) {
            System.out.println("There exists " + matches.size() + " occurances of the word");
            printLines(matches, input);
        } else if (matches.size() > 25) {
            System.out.println("There exists " + matches.size() + " occurances of the word");
            System.out.println("Do you want to output all occurances? (y/n)");
            try {
                Scanner scanner = new Scanner(System.in);
                String userInput = scanner.nextLine();
                if (userInput.equalsIgnoreCase("y")) {
                    printLines(matches, input);
                } else if (userInput.equalsIgnoreCase("n")) {
                    System.exit(0);
                }
            } catch (Exception e) {
                System.out.println("Caught error" + e);
            }
        }
    }

    // Takes 30 bytes before and after the words in korpus using their byte offsets from raw index, then prints the characters.
    public static void printLines(ArrayList<String> matches, String input) throws IOException {
        RandomAccessFile in = new RandomAccessFile(korpus, "r");

        for (int i = 0; i < matches.size(); i++) {

            in.seek(0);
            int offset = Integer.parseInt(matches.get(i)) - 30;
            in.skipBytes(offset);

            byte[] line = new byte[60 + input.length()];

            while (true)  {
                try {
                    for (int j = 0; j < (60 + input.length()); j++) {
                        line[j] = in.readByte();
                        if (line[j] == '\n') {
                            line[j] = ' ';
                        }
                    }
                    break;
                } catch (EOFException e) {
                    break;
                }
            }

            String word = new String(line, StandardCharsets.ISO_8859_1);
            System.out.println(word);
        }
    }

    // Gets index of character in alphabet
    public static int charToNum(char x) {
        return alphabet.indexOf(x);
    }

    /*
    Return the lazyHash of a word
    Assumes substring(0, 3) on word has already been called before using method
     */
    public static int wordToHash(String word) {
        if (!(word.length() > 3)) {
            int hashValue = 0;
            int a = 0, b = 0, c = 0;

            if (word.length() == 3) {
                a = charToNum(word.charAt(0));
                b = charToNum(word.charAt(1));
                c = charToNum(word.charAt(2));
            } else if (word.length() == 2) {
                a = charToNum(word.charAt(0));
                b = charToNum(word.charAt(1));
                c = 0;
            } else if (word.length() == 1) {
                a = charToNum(word.charAt(0));
                b = 0;
                c = 0;
            }
            hashValue = (a * 900 + b * 30 + c);
            return hashValue;
        }
        return 0;
    }

    // Generates hash file with the lazy hashes of the words and the position (in byte offset) of the first occurrence.
    public static void createHashFile() throws IOException {
        System.out.println("Generating lazy hashes...");

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(rawIndex), StandardCharsets.ISO_8859_1));

        RandomAccessFile out = new RandomAccessFile(lazyHashes, "rw");
        long length = (30 * 30 * 30 + 1) * 8;
        out.setLength(length);

        String line;
        int byteOffset = 0;
        String previousLine = "";
        String currentLine = "";

        int hashValue;

        while ((line = in.readLine()) != null) {
            currentLine = line.split(" ")[0];
            if (currentLine.length() > 3)
                currentLine = currentLine.substring(0, 3);

            if (!currentLine.equals(previousLine)) {
                hashValue = wordToHash(currentLine);
                out.writeBytes(hashValue + " " + byteOffset + "\n");
            }
            // Byte offset of raw index (character bytes + 1 byte for newline) connecting hashes with raw index
            byteOffset += line.length() + 1;
            previousLine = currentLine;
        }
        in.close();
        out.close();
    }

    // Creates an int[] where the index is the lazyHash and the corresponding value is the position in raw index.
    public static int[] createHashArray(File lazyHashes) throws IOException {
        int[] hashArray = new int[(900 * 30 * 1)];
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(lazyHashes), StandardCharsets.ISO_8859_1));

        String line;
        int lazyHash, position;

        while (((line = in.readLine()) != null) && line.split(" ").length == 2) {
            lazyHash = Integer.parseInt(line.split(" ")[0]);
            position = Integer.parseInt(line.split(" ")[1]);
            hashArray[lazyHash] = position;
        }
        in.close();
        return hashArray;
    }

    // Returns the position of the next lazy hash
    public static int nextHashPosition(int currentHash, int[] hashArray) {
        for (int i = 1; i < (hashArray.length - currentHash); i++) {
            if (hashArray[currentHash + i] > 0) {
                return hashArray[currentHash + i];
            }
        }
        return hashArray[2699];
    }

    /*
    If string at rawindex position has fewer characters than search word set start = middle
    Else if string at rawindex position has more characters than search word, set end = middle
    (Divide and conquer) source for code skeleton of binary search: https://www.geeksforgeeks.org/variants-of-binary-search/
    */
    public static int binarySearch(int start, int end, String word) throws IOException {
        RandomAccessFile in = new RandomAccessFile(rawIndex, "r");

        String[] line = new String[2];

        int middle;
        while (start <= end) {
            middle = start + (end - start) / 2;
            in.seek(middle);
            //Skip first readLine
            if (in.readLine() != null) {
                line = in.readLine().split(" ");
            }
            if (line[0].compareTo(word) < 0) {
                start = middle + 1;
            } else if (line[0].compareTo(word) > 0) {
                end = middle - 1;
            } else if (line[0].compareTo(word) == 0) {
                return start;
            }
        }
        return start;
    }

    // Searches for the word and adds the position of the word from raw index of each match into an ArrayList.
    public static ArrayList<String> searchWord(String word, int[] hashArray) throws IOException {
        ArrayList<String> matches = new ArrayList<>();

        //Hash input word
        String subWord;
        if (word.length() > 3) {
            subWord = word.substring(0, 3);
        } else {
            subWord = word;
        }
        int hashedWord = wordToHash(subWord);

        int start = hashArray[hashedWord];
        int end = nextHashPosition(hashedWord, hashArray);

        // Binary search returns estimated position of where the first occurrence of the word is located in rawIndex
        int estimatedPosition = binarySearch(start, end, word);

        RandomAccessFile in = new RandomAccessFile(rawIndex, "r");
        in.skipBytes(estimatedPosition);

        String[] line;
        String nullCheck;
        String temp;
        while (true) {
            if ((nullCheck = in.readLine()) != null) {
                line = nullCheck.split(" ");
                if (line[0].equals(word)) {
                    while (line[0].equals(word)) {
                        matches.add(line[1]);
                        if ((temp = in.readLine()) != null) {
                            line = temp.split(" ");
                        } else {
                            break;
                        }
                    }
                    break;
                } else if (line[0].compareTo(word) == 1) {
                    break;
                }
            } else {
                break;
            }
        }
        return matches;
    }
}

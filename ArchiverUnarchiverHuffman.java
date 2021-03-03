import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class ArchiverUnarchiverHuffman {

    /**
     * Packer & UnPacker
     *
     * @param args user input
     * @throws IOException ioError
     */
    public static void main(String[] args) throws IOException {

        String packInputFile = "test.txt";
        String packOutputFile = "test.txt.par";
        String unPackOutputFile = "test.uar";

        // Check if no arguments were passed in
        if (args.length == 0) {
            System.out.println("Sorry, no Program arguments added, trying to compress \"test.txt\" to archive \"test.txt.par\"");
            packIt(packInputFile, packOutputFile);
            System.exit(0);
        }

        // first arg is -a
        if (args[0].equals("-a")) {
            aDecision(args, packOutputFile);
        }
        // first arg is -u
        else if (args[0].equals("-u")) {
            uDecision(args, unPackOutputFile);
        }
        // first arg is filename
        else {
            // first arg is source
            if (!extFromFullName(args[0]).equals("par")) {
                packDecision(args, packOutputFile);
            }
            // first arg is archive
            else {
                unPackDecision(args, unPackOutputFile);
            }
        }
    }

    /**
     * -u flag decision three
     *
     * @param args             file names
     * @param unPackOutputFile unPackOutputFile
     */
    private static void uDecision(String[] args, String unPackOutputFile) {
        if (args.length == 1 || args.length > 3) {
            System.out.println("Arguments is: " + Arrays.toString(args));
            System.out.println("Something wrong with them. Compression is impossible.");
            System.exit(1);
        }
        String unPackInputFile = args[1];
        if (args.length == 3) {
            unPackOutputFile = args[2];
        }
        if (args.length == 2) {
            unPackOutputFile = nameFromFullName(args[1]) + ".uar";
        }

        unPackIt(unPackInputFile, unPackOutputFile);
    }

    /**
     * -a flag decision three
     *
     * @param args           file names
     * @param packOutputFile packOutputFile
     * @throws IOException error
     */
    private static void aDecision(String[] args, String packOutputFile) throws IOException {
        if (args.length == 1 || args.length > 3) {
            System.out.println("Arguments is: " + Arrays.toString(args));
            System.out.println("Something wrong with them. Compression is impossible.");
            System.exit(1);
        }
        String packInputFile = args[1];
        if (args.length == 3) {
            packOutputFile = args[2];
        }
        if (args.length == 2) {
            packOutputFile = args[1] + ".par";
        }

        packIt(packInputFile, packOutputFile);
    }

    /**
     * unpack decision three
     *
     * @param args             file names
     * @param unPackOutputFile unPackOutputFile
     */
    private static void unPackDecision(String[] args, String unPackOutputFile) {
        String unPackInputFile = args[0];
        if (args.length == 2) {
            unPackOutputFile = args[1];
        }
        if (args.length == 1) {
            unPackOutputFile = nameFromFullName(args[0]) + ".uar";
        }
        if (args.length > 2) {
            System.out.println("Arguments is: " + Arrays.toString(args));
            System.out.println("Something wrong with them. Compression is impossible.");
            System.exit(1);
        }
        unPackIt(unPackInputFile, unPackOutputFile);
    }

    /**
     * pack decision three
     *
     * @param args           file names
     * @param packOutputFile packOutputFile
     * @throws IOException error
     */
    private static void packDecision(String[] args, String packOutputFile) throws IOException {
        String packInputFile = args[0];
        if (args.length == 2) {
            packOutputFile = args[1];
        }
        if (args.length == 1) {
            packOutputFile = args[0] + ".par";
        }
        if (args.length > 2) {
            System.out.println("Arguments is: " + Arrays.toString(args));
            System.out.println("Something wrong with them. Compression is impossible.");
            System.exit(1);
        }
        packIt(packInputFile, packOutputFile);
    }

    /**
     * Packer
     *
     * @param packInputFile  read from
     * @param packOutputFile write to
     * @throws IOException ioErrors
     */
    private static void packIt(String packInputFile, String packOutputFile) throws IOException {

        long startTime = System.nanoTime();

        byte[] buffer = new byte[0];

        long source = 1;
        try (FileInputStream fin = new FileInputStream(packInputFile)) {

            source = fin.available();
            System.out.printf("SOURCE File size: %d bytes \n", source);


            buffer = new byte[fin.available()];
            // read from file to buffer
            if (fin.read(buffer, 0, buffer.length) == -1) {
                System.out.println("File \"" + packInputFile + "\" read error. Compression is impossible.");
                System.exit(1);
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.out.println("File \"" + packInputFile + "\" read error. Compression is impossible.");
            System.exit(1);
        }

        Map<Byte, Integer> uniques = new HashMap<>();


        for (byte currentByte : buffer) {
            if (uniques.containsKey(currentByte)) uniques.put(currentByte, uniques.get(currentByte) + 1);
            else uniques.put(currentByte, 1);
        }

        if (uniques.size() < 2) {
            System.out.println("Unique bytes total: " + uniques.size());
            System.out.println("Unique bytes total is less then 2. Compression is impossible.");
            System.exit(1);
        }

        // TABLE array to add to file
        byte[] tableBytes = new byte[uniques.size()];

        int[] byteInts = new int[uniques.size()];


        // arraylist of leaves
        ArrayList<HuffTree> huffUnits = new ArrayList<>();
        // every unique byte cycle
        int k = 0;
        for (Byte b : uniques.keySet()) {
            // fill arraylist using constructor
            huffUnits.add(new HuffTree(b, uniques.get(b), null, null));
            tableBytes[k] = b;
            byteInts[k] = uniques.get(b);
            k++;
        }

        // TABLE INTS array to add to file

        byte[][] tableInts = new byte[uniques.size()][];
        for (int i = 0; i < byteInts.length; i++) {
            ByteBuffer reserveFourBytes = ByteBuffer.allocate(4);
            reserveFourBytes.putInt(byteInts[i]);
            tableInts[i] = reserveFourBytes.array();
        }

        // in debugger you can see "currentTree" as tree with children
        HuffTree currentTree = treeBuilder(huffUnits);


        // code table by recursive tree search for every unique byte
        Map<Byte, String> mainTable = new HashMap<>();
        // every unique byte cycle

        for (Byte currentByte : uniques.keySet()) {
            String initialString = "";
            // put taken code as string of 0 and 1
            mainTable.put(currentByte, currentTree.getCodeForByte(currentByte, initialString));

        }

        System.out.println("Code table: " + mainTable.toString());


        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < buffer.length; i++) {
            byte b = buffer[i];
            String s = mainTable.get(b);
            outStr.append(s);
            if (i % 10000 == 0) System.out.println("... track pack " + (double) i / buffer.length * 100 + "%");
        }

        System.out.println("Now compressing the file: " + packInputFile);

        // TABLE SIZE array to add to file
        ByteBuffer reserveFourBytes = ByteBuffer.allocate(4);
        reserveFourBytes.putInt(mainTable.size() * 5);
        byte[] tableSize = reserveFourBytes.array();


        // DATA SIZE array to add to file
        int dataLength = buffer.length;
        ByteBuffer reserve4Bytes = ByteBuffer.allocate(4);
        reserve4Bytes.putInt(dataLength);
        byte[] dataSize = reserve4Bytes.array();


        while (outStr.length() % 8 != 0) outStr.append("0");

        byte[] bufferOut = new byte[(outStr.length() / 8)];

        for (int i = 0; i < outStr.length(); i += 8) {
            String s = outStr.substring(i, i + 8);
            int intByte = Integer.parseInt(s, 2);
            bufferOut[i / 8] = (byte) intByte;
            if (i % 10000 == 0)
                System.out.println("... track write packed " + (double) i / outStr.length() * 100 + "%");
        }

        // combine final byte array
        ByteArrayOutputStream temporaryStream = new ByteArrayOutputStream();
        temporaryStream.write(tableSize);
        temporaryStream.write(dataSize);
        temporaryStream.write(tableBytes);
        for (byte[] tableInt : tableInts) {
            temporaryStream.write(tableInt);
        }
        temporaryStream.write(bufferOut);
        byte[] bufferFinal = temporaryStream.toByteArray();

        try (FileOutputStream fos = new FileOutputStream(packOutputFile)) {

            // write from buffer to file
            fos.write(bufferFinal, 0, bufferFinal.length);
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }

        long arch = 1;
        try (FileInputStream ft = new FileInputStream(packOutputFile)) {

            arch = ft.available();
            System.out.printf("PACKED File size: %d bytes \n", arch);

        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }


        System.out.println("Compressed to source ratio is: " + 100 * arch / source + " %");
        System.out.println("Elapsed time for pack " + (System.nanoTime() - startTime) / 1000000000.0 + " seconds.");

    }


    /**
     * our main tree builder
     */
    private static HuffTree treeBuilder(ArrayList<HuffTree> huffUnits) {
        // repeat till only root lasts left
        while (huffUnits.size() > 1) {
            // sort to found smallest
            Collections.sort(huffUnits);
            // smallest copy and remove
            HuffTree child0 = huffUnits.remove(huffUnits.size() - 1);
            // smallest copy and remove
            HuffTree child1 = huffUnits.remove(huffUnits.size() - 1);
            // make father from two children
            HuffTree father = new HuffTree(null, child1.repeats + child0.repeats, child0, child1);
            // record father
            huffUnits.add(father);
        }
        // only root lasts, so return
        return huffUnits.get(0);
    }


    private static byte[] sourceFinder(int dataLength, String inString, HuffTree currentTree) {
        byte currentByte;
        // byte accumulator
        byte[] buffer = new byte[dataLength];
        // current object
        HuffTree currentObject = currentTree;
        // inString counter
        int i = 0;
        // byte counter
        int j = 0;
        while (i < inString.length()) {
            // child0
            if (inString.charAt(i) == '0') currentObject = currentObject.child0;
                // child1
            else currentObject = currentObject.child1;
            // leaf found
            if (currentObject.filling != null) {
                currentByte = currentObject.filling;
                // byte to out array
                try {
                    buffer[j] = currentByte;
                } catch (Exception e) {
                    return buffer;
                }
                // return to root
                currentObject = currentTree;
                j++;
            }
            i++;
        }
        return buffer;
    }


    /**
     * UnPacker
     *
     * @param unPackInputFile  read from
     * @param unPackOutputFile write to
     */
    private static void unPackIt(String unPackInputFile, String unPackOutputFile) {

        long startTime = System.nanoTime();

        byte[] bufferIn = new byte[0];


        long arch = 1;

        try (FileInputStream fin = new FileInputStream(unPackInputFile)) {

            arch = fin.available();

            System.out.printf("PACKED File size: %d bytes \n", arch);

            bufferIn = new byte[fin.available()];
            // read from file to buffer
            if (fin.read(bufferIn, 0, bufferIn.length) == -1) {
                System.out.println("File \"" + unPackInputFile + "\" read error. Decompression is impossible.");
                System.exit(1);
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.out.println("File \"" + unPackInputFile + "\" read error. Decompression is impossible.");
            System.exit(1);
        }

        // table size found
        byte[] unTableSize = Arrays.copyOfRange(bufferIn, 0, 8);
        ByteBuffer arrayAsStream = ByteBuffer.wrap(unTableSize);
        int uniqueBytesSize = arrayAsStream.getInt(0);

        // data size found
        int dataSize = arrayAsStream.getInt(4);
        System.out.println("Unpacked data size in bits: " + dataSize);


        // table1 found
        byte[] unTable = Arrays.copyOfRange(bufferIn, 8, 8 + uniqueBytesSize / 5);
        // table to arraylist
        ArrayList<Byte> unUniqueBytes = new ArrayList<>();
        for (byte b : unTable) unUniqueBytes.add(b);


        // table2 found
        byte[] unTable1 = Arrays.copyOfRange(bufferIn, 8 + uniqueBytesSize / 5, 8 + uniqueBytesSize);

        int len = uniqueBytesSize / 5;
        int[] table2 = new int[len];
        for (int i = 0; i < len; i++) {

            ByteBuffer arrayAsStream2 = ByteBuffer.wrap(unTable1);
            table2[i] = arrayAsStream2.getInt(i * 4);

        }

        // data found
        byte[] unBufferIn = Arrays.copyOfRange(bufferIn, 8 + uniqueBytesSize, bufferIn.length);

        // StringBuilder added by IntelliJ advice and increased speed HERE at ~50%
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < unBufferIn.length; i++) {
            byte b = unBufferIn[i];
            String s = Integer.toBinaryString((b + 256) % 256);
            String string8 = String.format("%8s", s).replaceAll(" ", "0");
            // StringBuilder added by IntelliJ advice and increased speed HERE at ~50%
            in.append(string8);
            if (i % 10000 == 0)
                System.out.println("... track read packed " + (double) i / unBufferIn.length * 100 + "%");
        }


        Map<Byte, Integer> uniques = new HashMap<>();


        int l = 0;
        for (byte currentByte : unUniqueBytes) {
            uniques.put(currentByte, table2[l]);
            l++;
        }


        // arraylist of leaves
        ArrayList<HuffTree> huffUnits = new ArrayList<>();
        // every unique byte cycle

        for (Byte b : uniques.keySet()) {
            // fill arraylist using constructor
            huffUnits.add(new HuffTree(b, uniques.get(b), null, null));

        }

        // in debugger you can see "currentTree" as tree with children
        HuffTree currentTree = treeBuilder(huffUnits);

        // code table by recursive tree search for every unique byte
        Map<Byte, String> mainTable = new HashMap<>();
        // every unique byte cycle

        for (Byte currentByte : uniques.keySet()) {
            String initialString = "";
            // put taken code as string of 0 and 1
            mainTable.put(currentByte, currentTree.getCodeForByte(currentByte, initialString));

        }

        System.out.println("Code table: " + mainTable.toString());


        byte[] sourceBuffer = sourceFinder(dataSize, in.toString(), currentTree);

        try (FileOutputStream fos = new FileOutputStream(unPackOutputFile)) {

            // write from buffer to file
            fos.write(sourceBuffer, 0, sourceBuffer.length);


        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }

        long source = 1;
        try (FileInputStream ft = new FileInputStream(unPackOutputFile)) {

            source = ft.available();
            System.out.printf("SOURCE File size: %d bytes \n", source);

        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }


        System.out.println("Compressed to source ratio is: " + 100 * arch / source + " %");
        System.out.println("Elapsed time for unpack " + (System.nanoTime() - startTime) / 1000000000.0 + " seconds");


    }

    /**
     * ext getter
     */
    private static String extFromFullName(String fullFile) {


        if (fullFile.lastIndexOf(".") > 0)

            return fullFile.substring(fullFile.lastIndexOf(".") + 1);

        else return "";
    }

    /**
     * name getter
     */
    private static String nameFromFullName(String fullFile) {


        if (fullFile.lastIndexOf(".") > 0)

            return fullFile.substring(0, fullFile.lastIndexOf("."));

        else return fullFile;
    }
}

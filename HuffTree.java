package com.shpp.p2p.cs.vpasechnyk.assignment15;

import java.util.Objects;

/**
 * Huffman tree as class
 */
class HuffTree implements Comparable<HuffTree> {

    // element filling
    Byte filling;
    // element repeats
    int repeats;
    // zero child
    HuffTree child0;
    // child 1
    HuffTree child1;

    /**
     * constructor for tree fathers and leaves
     */
    public HuffTree(Byte filling, int repeats, HuffTree child0, HuffTree child1) {
        // father filling
        this.filling = filling;
        // father repeats
        this.repeats = repeats;
        // zero child
        this.child0 = child0;
        // child 1
        this.child1 = child1;
    }

    /**
     * finding difference between our tree's items
     */
    @Override
    public int compareTo(HuffTree currentByte) {
        return currentByte.repeats - repeats;
    }


    /**
     * take byte code as a string by recursive three search in depth
     */
    public String getCodeForByte(Byte currentByte, String wayToFather) {
        // there is 4 cases:
        if (!Objects.equals(filling, currentByte)) {
            // case 1 - zero child found
            if (child0 != null) {
                // recursive code add for zero child
                String currentWay = child0.getCodeForByte(currentByte, wayToFather + "0");
                // return temporary string
                if (currentWay != null) return currentWay;
            }
            // case 2 - child 1 found. recursive code add for child 1. return temporary string
            if (child1 != null) return child1.getCodeForByte(currentByte, wayToFather + "1");
        }
        // case 3 - correct leaf found. return correct code
        if (Objects.equals(filling, currentByte)) return wayToFather;
        // case 4 - wrong leaf found. return null
        return null;
    }
}

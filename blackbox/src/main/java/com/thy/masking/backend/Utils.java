package com.thy.masking.backend;


public class Utils {




    public static int getByteNo(byte[] word, int bitNo) {
        //return  word.length -  ((bitNo) / 8) - 1;
        return  (bitNo) / 8;
    }
    public static boolean getBitValue(byte[] word, int bitNo) {
        int byteNo = getByteNo(word, bitNo);
        return  ((word[byteNo] >> (bitNo %8)) & 1 ) > 0;
    }

    public static void setBit(byte[] word, int bitNo, boolean value) {
        int byteNo = getByteNo(word, bitNo);

        byte myByte = word[byteNo] ;

        if (value) {
            myByte |= 1 << (bitNo %8);
        } else {
            myByte &= ~(1 << (bitNo %8));
        }
        word[byteNo] = myByte;

    }



}

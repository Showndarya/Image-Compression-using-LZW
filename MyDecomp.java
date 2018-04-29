import java.io.*;
import java.util.*;

public class MyDecomp {

    static public String[] arrayOfChar;
    static public String filename="",extension="";
    static public int dictSize = 256, currentword, previousword;
    static public byte[] buffer = new byte[3];
    static public boolean isLeft = true;
    
    public static void main(String[] args) {
        System.out.println("*****LZW Image Decompression*****");
        System.out.println("Enter lzw file name to decompress with extension: ");
        Scanner sc = new Scanner(System.in);
        filename = sc.next();
        System.out.println("Enter extension of output image required (Ex: for .bmp enter bmp)\nNote- Extension must be same as the original image: ");
        extension = sc.next();
        try {
            File file = new File(filename);
            decompress();
            System.out.println("Decompression complete! Check file output_image."+extension);      
        }
        catch(IOException ie) {
            System.out.println("File "+filename+" not found!");
        }
    }
    
    public static void decompress() throws IOException {
        arrayOfChar = new String[4096];
        int i;
        
        for (i=0;i<256;i++) arrayOfChar[i] = Character.toString((char)i);
        
        // Read input file and output file
        RandomAccessFile inputFile = new RandomAccessFile(filename,"r");
        RandomAccessFile outputFile = new RandomAccessFile("output_image.".concat(extension),"rw");
        
        try {
            buffer[0] = inputFile.readByte();
            buffer[1] = inputFile.readByte();
            previousword = getIntValue(buffer[0], buffer[1], isLeft);
            isLeft = !isLeft;
            outputFile.writeBytes(arrayOfChar[previousword]);

            // Reads three bytes and generates corresponding characters
            while (true) {
            
                if (isLeft) {
                    buffer[0] = inputFile.readByte();
                    buffer[1] = inputFile.readByte();
                    currentword = getIntValue(buffer[0], buffer[1], isLeft);
                } 
                else {
                    buffer[2] = inputFile.readByte();
                    currentword = getIntValue(buffer[1], buffer[2], isLeft);
                }
                isLeft = !isLeft;

                /*
                 currentword not in dictionary, we just add the previousword in the entry.
                */
                
                if (currentword >= dictSize) {
                    if (dictSize < 4096) {
                        arrayOfChar[dictSize] = arrayOfChar[previousword] + arrayOfChar[previousword].charAt(0);
                    }
                    dictSize++;
                    outputFile.writeBytes(arrayOfChar[previousword] + arrayOfChar[previousword].charAt(0));
                } 
                /*
                 If word is present, we form a word with the previousword and the first character of the 
                 currentword and add it in a new entry
                */
                
                else {
                    if (dictSize < 4096) {
                        arrayOfChar[dictSize] = arrayOfChar[previousword] + arrayOfChar[currentword].charAt(0);
                    }
                    dictSize++;
                    outputFile.writeBytes(arrayOfChar[currentword]);
                }
                previousword = currentword;
            }
        } 
        catch (EOFException e) {
            inputFile.close();
            outputFile.close();
        }
    }
    
    /*
        Converting 2 bytes to 12-bit code.
        Converting 12-bit code to integer value.
    */ 
    public static int getIntValue(byte b1, byte b2, boolean isLeft) {
        String t1 = Integer.toBinaryString(b1);
        String t2 = Integer.toBinaryString(b2);

        while (t1.length() < 8) t1 = "0" + t1;
        if (t1.length() == 32) t1 = t1.substring(24, 32);
        
        while (t2.length() < 8) t2 = "0" + t2;
        if (t2.length() == 32) t2 = t2.substring(24, 32);

        if (isLeft) return Integer.parseInt(t1 + t2.substring(0, 4), 2);
        else return Integer.parseInt(t1.substring(4, 8) + t2, 2);
        
    }
  
}


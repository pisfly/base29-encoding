package com.pisfly.util;

import com.pisfly.encoding.Base29Encoding;

import java.io.*;
import java.util.Scanner;

/**
 * Created by fatihdonmez on 16/08/15
 */
public class StreamProcessor implements Closeable {

    //TODO inject it so it can be extendable
    private Base29Encoding encodingUtil = new Base29Encoding();

    private Scanner scanner;
    private PrintWriter outputStream;

    private boolean isAscii = false;
    private boolean isEncode = false;

    public StreamProcessor(InputStream inputStream, PrintWriter outputStream, boolean ascii, boolean isEncode) {
        this.scanner = new Scanner(inputStream).useDelimiter("\n");
        this.outputStream = outputStream;
        this.isAscii = ascii;
        this.isEncode = isEncode;
    }

    /**
     * TODO refactor it
     * This process method more memory efficient.
     * It only loads 100 byte of stream
     */
    /*
    public void process() {

        int bufferSize = 100;
        int numberOfBytes = 0;
        boolean newLine = false;

        try {

            byte[] byteBuffer = new byte[bufferSize];

            do {

                StringBuilder data = new StringBuilder();
                numberOfBytes = inputStream.read(byteBuffer);

                for (int i = 0; i < numberOfBytes; i++) {

                    if(byteBuffer[i] == '\n') {
                        newLine = true;
                        break;
                    }

                    if(isEncode) {

                        //Hex hack
                        if(!isAscii)
                            data.append((char)byteBuffer[i]);
                        else {

                            String str = Integer.toHexString((int) byteBuffer[i]);
                            data.append(str);
                        }


                    } else {
                        data.append((char)byteBuffer[i]);
                    }
                }

                String temp = "";

                if(isEncode) {
                    temp = encodingUtil.encode(data.toString());
                } else {
                    temp = encodingUtil.hex2Ascii(encodingUtil.decode(data.toString()));
                }

                outputStream.write(temp);

                Arrays.fill(byteBuffer,(byte) 0);

            } while (numberOfBytes >= 0 && !newLine);

        } catch (IOException e) {
            System.err.println("Error happened during stream processing " + e.getMessage());
        }
    } */

    /**
     * This process method is more pragmatic and readable
     */
    public void process2() {

        if(!isAscii) {
            if (isEncode) {
                scanner.useRadix(16);
                String plain = scanner.hasNext() ? scanner.next() : "";
                String encoded = encodingUtil.encode(plain);
                outputStream.print(encoded);
            } else {
                String encoded = scanner.hasNext() ? scanner.next() : "";
                outputStream.print(encodingUtil.decode(encoded));
            }
        } else {

            if (isEncode) {
                String plain = scanner.hasNext() ? scanner.next() : "";
                char[] byteArr = plain.toCharArray();

                StringBuffer hex = new StringBuffer();
                for (int i = 0; i < byteArr.length ; i++) {
                    hex.append(Integer.toHexString((int) byteArr[i]));
                }

                String encoded = encodingUtil.encode(hex.toString());
                outputStream.print(encoded);
            } else {
                String encoded = scanner.hasNext() ? scanner.next() : "";
                outputStream.print(Base29Encoding.hex2Ascii(encodingUtil.decode(encoded)));
            }
        }
    }

    public void close() {
        if(scanner != null)
            scanner.close();
    }

}

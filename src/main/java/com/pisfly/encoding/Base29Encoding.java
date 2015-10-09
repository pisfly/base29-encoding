package com.pisfly.encoding;

import java.math.BigInteger;

/**
 * Created by fatihdonmez on 16/08/15
 */
public class Base29Encoding implements Encoding {

    private String encodeMap = "abcdefghijklmnopqrstuvwxyz123";
    private final int CHUNK_SIZE = 5*2;
    private final int ENCODED_CHUNK_SIZE = 9;

    private String LSByte(String hex) {

        StringBuilder lsByte = new StringBuilder();
        for (int i = hex.length() - 1; i >= 0 ; i = i - 2) {

            if((i - 1) >= 0) {
                lsByte.append(hex.charAt(i-1)).append(hex.charAt(i));
            }
        }

        return lsByte.toString();
    }

    private String mod29(BigInteger plainValue) {

        if(plainValue.compareTo(BigInteger.valueOf(29)) == -1) {
            return encodeMap.charAt(plainValue.intValue())+ "" + encodeMap.charAt(plainValue.divide(BigInteger.valueOf(29)).intValue());
        }
        else {
            BigInteger mod = plainValue.mod(BigInteger.valueOf(29));
            return encodeMap.charAt(mod.intValue()) + mod29(plainValue.divide(BigInteger.valueOf(29)));
        }
    }

    private BigInteger mod29ToDecimal(String encoded) {

        BigInteger decimal = BigInteger.valueOf(0);

        for (int i = 0; i < encoded.length(); i++) {

            BigInteger index = BigInteger.valueOf(encodeMap.indexOf(encoded.charAt(i)));
            BigInteger pow = BigInteger.valueOf(29).pow(i);
            decimal = decimal.add(index.multiply(pow));
        }

        return decimal;
    }

    public String encode(String plainValue) {

        int numberOfChunks =  plainValue.length() / CHUNK_SIZE;
        int remainder = plainValue.length() % CHUNK_SIZE;

        StringBuilder encoded = new StringBuilder();

        for(int i = 0; i < numberOfChunks; i++) {

            String byte5 = plainValue.substring(i*CHUNK_SIZE, (i+1)*CHUNK_SIZE);
            String lsByte = LSByte(byte5);

            BigInteger decimal = new BigInteger(lsByte,16);
            encoded.append(mod29(decimal));
        }

        if(remainder > 0) {

            String byte5 = plainValue.substring(numberOfChunks * CHUNK_SIZE);

            for(int i = 0; i < (CHUNK_SIZE - remainder); i++) {
                byte5 += "0";
            }

            String lsByte = LSByte(byte5);

            BigInteger decimal = new BigInteger(lsByte,16);
            encoded.append(mod29(decimal));
        }

        return encoded.toString();
    }

    public String decode(String encodedValue) {

        int numberOfChunks =  encodedValue.length() / ENCODED_CHUNK_SIZE;
        int remainder = encodedValue.length() % ENCODED_CHUNK_SIZE;

        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < numberOfChunks ; i++) {
            String sub = encodedValue.substring(i * ENCODED_CHUNK_SIZE, (i + 1) * ENCODED_CHUNK_SIZE);
            BigInteger dec = mod29ToDecimal(sub);
            String lsByte = LSByte(dec.toString(16));
            decoded.append(lsByte);

        }

        if(remainder > 0) {
            String sub = encodedValue.substring(numberOfChunks * ENCODED_CHUNK_SIZE);
            BigInteger dec = mod29ToDecimal(sub);
            String lsByte = LSByte(dec.toString(16));
            decoded.append(lsByte);
        }

        return decoded.toString();
    }

    /**
     * Util function for conversion of hex to Ascii
     * @param hex
     * @return
     */
    public static String hex2Ascii(String hex) {

        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < hex.length() ; i = i + 2) {

            String sub = hex.substring(i,i+2);
            output.append((char) Integer.parseInt(sub, 16));
        }

        return output.toString();
    }
}

package com.pisfly.encoding;

/**
 * Created by fatihdonmez on 16/08/15
 */
public interface Encoding {

    public enum Mode {
        ASCII,HEX
    }

    /**
     * Encode plain input
     *
     * @param plainValue hex string to encode
     * @return encoded string equivalent of input
     */
    String encode(String plainValue);

    /**
     * Decode string to hex
     *
     * @param encodedValue encoded text
     * @return decoded equivalent of input
     */
    String decode(String encodedValue);
}

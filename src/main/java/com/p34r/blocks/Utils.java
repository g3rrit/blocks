package com.p34r.blocks;

import java.io.IOException;
import java.io.InputStream;

public class Utils {
    public static String readFile(String res) {
        try (InputStream is = Utils.class.getResourceAsStream(res)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file [" + res + "]", e);
        }
    }
}

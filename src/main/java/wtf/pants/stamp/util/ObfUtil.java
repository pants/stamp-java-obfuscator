package wtf.pants.stamp.util;

import java.util.Random;

/**
 * @author Spacks
 */
public class ObfUtil {

    private static final String[] obfuscationChars = {"I", "l", "i", "!", "1", "|"};
    private static Random random = new Random();

    public static String getRandomObfString() {
        int length = 12;
        int charSize = obfuscationChars.length;

        StringBuilder randString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            randString.append(obfuscationChars[random.nextInt(charSize)]);
        }

        return randString.toString();
    }

}

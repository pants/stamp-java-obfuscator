package wtf.pants.stamp.util;

import java.util.Random;

/**
 * @author Pants
 */
public class ObfUtil {

    private static final String[] obfuscationChars = {"I", "l", "i", "!", "1", "|"};
    private static Random random = new Random();

    /**
     * Generates a random string to use for obfuscated names
     * @return Returns 12 character string looking similar to: 'I|1I|Li!il||'
     */
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

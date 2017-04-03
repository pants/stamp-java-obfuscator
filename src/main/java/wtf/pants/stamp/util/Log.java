package wtf.pants.stamp.util;

/**
 * @author Pants
 */
public class Log {

    public static boolean DEBUG = false;

    public static void info(String msg, Object... o) {
        print("INFO", msg, o);
    }

    public static void log(String msg, Object... o) {
        print("LOG", msg, o);
    }

    public static void error(String msg, Object... o) {
        print("ERROR", msg, o);
    }

    public static void warning(String msg, Object... o) {
        print("WARN", msg, o);
    }

    public static void debug(String msg, Object... o) {
        if (DEBUG) {
            print("DEBUG", msg, o);
        }
    }

    public static void success(String msg, Object... o) {
        print("SUCCESS", msg, o);
    }

    public static void print(String flag, String msg, Object... o) {
        print(flag, false, msg, o);
    }

    public static void print(String flag, boolean err, String msg, Object... o) {
        if (!err)
            System.out.println("[" + flag.toUpperCase() + "] " + String.format(msg, o));
        else
            System.err.println("[" + flag.toUpperCase() + "] " + String.format(msg, o));
    }

}


package lab;

import java.util.Date;

public class MyLog {
    public static void log(String msg) {
        System.out.println(new Date() + ": " + msg);
    }

    public static void log(String tag, String msg) {
        System.out.println(new Date() + "["+ tag + "]" + ": " + msg);
    }
}

package lab.lab1;

import lab.MyLog;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Firewall {
    static final int ONLY_USER_IN_ALLOW_LIST_CAN_ACCESS = 0;
    static final int ONLY_USER_IN_BAN_LIST_CAN_NOT_ACCESS = 1;
    static final int ONLY_URL_IN_ALLOW_LIST_CAN_BE_VISITED = 2;
    static final int ONLY_URL_IN_BAN_LIST_CAN_NOT_BE_VISITED = 3;


    private static List<String> banList = new ArrayList<>();
    private static Set<String> allowList = new HashSet<>();
    private static Mode m = new Mode();

    static {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("source/lab1/url_ban.txt")));
            String url;
            while ((url = reader.readLine()) != null) {
                banList.add(url);
            }
        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                MyLog.log(e.getLocalizedMessage());
            }
        }
    }

    static void addToBanList(String url) {
        banList.add(url);
    }

    static void removeFromBanList(String url) {
        banList.remove(url);
    }

    static void addToAllowList(String url) {
        allowList.add(url);
    }

    static void removeFromAllowList(String url) {
        allowList.remove(url);
    }

    static void setMode(int mode) {
        m.mode = mode;
    }

    static void redirectTo(String url) {

    }

    static boolean isAllowed(String url) {
        boolean flag = true;
        if (m.mode == ONLY_URL_IN_BAN_LIST_CAN_NOT_BE_VISITED) {
            for (String s : banList) {
                if (url.contains(s)) {
                    flag = false;
                }
            }
        } else if (m.mode == ONLY_URL_IN_ALLOW_LIST_CAN_BE_VISITED) {
            flag = false;
            for (String s : allowList) {
                if (url.contains(s)) flag = true;
            }
        }
        return flag;
    }

    static class Mode {
        int mode = ONLY_URL_IN_BAN_LIST_CAN_NOT_BE_VISITED;
    }

    static void print(String name) {
        switch (name) {
            case "banList":
                System.out.println("in ban list:");
                for (String s : banList) System.out.println("->" + s);
                break;
            case "allowList":
                System.out.println("in allow list:");
                for (String s : allowList) System.out.println("->" + s);
                break;
        }
    }

    static void saveConfigToFile() {
        BufferedWriter banWriter = null;
        BufferedWriter allowWriter = null;
        try {
             banWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("source/lab1/url_ban.txt")));
             allowWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("source/lab1/url_allow.txt")));
             for (String s : banList) banWriter.write(s + "\n");
             for (String s : allowList) allowWriter.write(s + "\n");
        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        } finally {
            try {
                if (banWriter != null) banWriter.close();
                if (allowWriter != null) allowWriter.close();
            } catch (Exception e) {
                MyLog.log(e.getLocalizedMessage());
            }
        }

    }
}

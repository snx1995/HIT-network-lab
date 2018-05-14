package lab.lab1;

import lab.MyLog;

import javax.xml.crypto.dsig.TransformService;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class ProxyServer extends Thread{

    public static void main(String[] args) {
        new ProxyServer().start();
        new CMDThread().start();
    }

    @Override
    public void run() {
        ServerSocket server = null;

        try {
            server = new ServerSocket(54321);
            MyLog.log("Server started..");
            while (true) {
                new ClientConnectThread(server.accept()).start();
            }
        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        } finally {
            try {
                if (server != null) server.close();
            } catch (Exception e) {
                MyLog.log(e.getLocalizedMessage());
            }
        }
    }
}

class ClientConnectThread extends Thread {
    private Socket client;
    private InputStream cis;
    private OutputStream cos;

    public ClientConnectThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            cis = client.getInputStream();
            cos = client.getOutputStream();
            int c;
            StringBuilder reqLine = new StringBuilder();
            while ((c = cis.read()) != -1) {
                if (c == '\r' || c == '\n') break;
                reqLine.append((char) c);
            }
            String firstLine = reqLine.toString();
            String url = extractUrl(firstLine);
//            MyLog.log("URL", url);
            Socket desServer;
            if (url != null && Firewall.isAllowed(url)) {
                URL netUrl = new URL(url);
//                MyLog.log("firstLine", firstLine);
                try {
                    int port = netUrl.getPort() < 0 ? 80 : netUrl.getPort();
                    desServer = new Socket(netUrl.getHost(), port);
                    InputStream sis = desServer.getInputStream();
                    OutputStream sos = desServer.getOutputStream();
                    sos.write(firstLine.getBytes());
                    establishConnection(cis, sis, cos, sos);
                } catch (Exception e) {
                    MyLog.log(e.getLocalizedMessage());
                }
            } else {
                String notFound = "HTTP/1.1 404 NOT FOUND\r\n\r\n";
                cos.write(notFound.getBytes());
                cos.flush();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                cis.close();
                cos.close();
                client.close();
            } catch (Exception e) {
                MyLog.log(e.getLocalizedMessage());
            }
        }
    }

    private String extractUrl(String reqLine) {
        String url = reqLine.split("\\s")[1];
        if (url.startsWith("http://")) return url;
        // MyLog.log("Unsupported request(maybe https) found->" + url);
        return null;
    }

    private void establishConnection(InputStream cis, InputStream sis, OutputStream cos, OutputStream sos) {
        TransDataThread cToS = new TransDataThread(cis, sos);
        TransDataThread sToC = new TransDataThread(sis, cos);
        cToS.start();
        sToC.start();
        try {
            cToS.join();
            sToC.join();
        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        }
    }
}

class TransDataThread extends Thread {
    private InputStream is;
    private OutputStream os;

    public TransDataThread(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        int length;
        byte[] bytes = new byte[1024];
        try {
            while ((length = is.read(bytes)) > 0) {
                os.write(bytes, 0, length);
                os.flush();
            }
        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        }
    }
}

class CMDThread extends Thread {
    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command;
        try {
            while (!(command = reader.readLine()).equalsIgnoreCase("stop")) {
                String[] options = command.split("\\s");
                if (options.length >= 2) {
                    switch (options[0]) {
                        case "ban":
                            if (options[1].matches("\\w+(\\.\\w+)+")) {
                                Firewall.addToBanList(options[1]);
                                Firewall.removeFromAllowList(options[1]);
                                System.out.println(options[1] + " in ban list now");
                            } else System.out.println("invalid url!");
                            break;
                        case "redirectTo":
                            if (options[1].matches("\\w+(\\.\\w+)+")) {
                                Firewall.redirectTo(options[1]);
                                System.out.println("All request will be redirected to " + options[1]);
                            } else System.out.println("invalid url!");
                            break;
                        case "allow":
                            if (options[1].matches("\\w+(\\.\\w+)+")) {
                                Firewall.addToAllowList(options[1]);
                                Firewall.removeFromBanList(options[1]);
                                System.out.println(options[1] + " in allow list now");
                            } else System.out.println("invalid url");
                            break;
                        case "mode":
                            if (options[1].equalsIgnoreCase("allow")) {
                                Firewall.setMode(Firewall.ONLY_URL_IN_ALLOW_LIST_CAN_BE_VISITED);
                                System.out.println("change mode to allow");
                            }
                            else if (options[1].equalsIgnoreCase("ban")) {
                                Firewall.setMode(Firewall.ONLY_URL_IN_BAN_LIST_CAN_NOT_BE_VISITED);
                                System.out.println("change mode to ban");
                            }
                            else System.out.println("Invalid option "+ options[1] + " for command mode");
                        case "print":
                            if (options[1].equalsIgnoreCase("banList")) Firewall.print("banList");
                            else if (options[1].equalsIgnoreCase("allowList")) Firewall.print("allowList");
                            break;
                    }
                } else if (options.length == 1 && options[0].equalsIgnoreCase("help")) {
                    System.out.println("input:\n" +
                            "ban [url] :to add an url to ban list\n" +
                            "allow [url] :to add an url to allow list\n" +
                            "redirectTo [url] :to redirect all request to the url\n" +
                            "mode [ban/allow] :to switch mode between 'only url in ban list was banned/only url in allow list was allowed'\n" +
                            "stop :to stop this proxy server.");
                } else System.out.println("Invalid options! input help to get help.");
            }
        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        } finally {
            try {
                reader.close();
                Firewall.saveConfigToFile();
            } catch (Exception e) {
                MyLog.log(e.getLocalizedMessage());
            }
        }
    }
}

package lab.lab2;

import lab.MyLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GBNServer {
    private DatagramSocket server;
    private boolean run = true;
    private int base = 0;
    private int next = 0;
    private int size = 5;
    private InetAddress clientAddress;
    private int clientPort;

    public static void main(String[] args) {
        new GBNServer().start();
    }

    /**
     *    ACK 报文格式：
     *    ACK index\r\n
     *
     *    数据报文格式：
     *    SEQ index\r\n
     *    data
      */

    private void start() {


        byte[] data = new byte[1024];
        try {
            server = new DatagramSocket(54321);
            DatagramPacket packet = new DatagramPacket(data, data.length);
            MyLog.log("server started..");

            MyLog.log("wait client's connection..");
            server.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());
            MyLog.log("receive client request " + msg);
            clientAddress = packet.getAddress();
            clientPort = packet.getPort();

            MyLog.log("ready to send data");
            sendMessage();
            server.setSoTimeout(5000);
            while (true) {
                try {
                    while (true) {
                        server.receive(packet);
                        msg = new String(packet.getData(), 0, packet.getLength());
                        MyLog.log("receive msg:" + msg);
                        String[] tmp = msg.split("\\s");
                        int rIndex = Integer.valueOf(tmp[1]);
                        if (rIndex > base) {
                            base = rIndex + 1;
                            sendMessage();
                        }
                        else if (rIndex == base){
                            base++;
                            sendMessage();
                        } else MyLog.log("error ACK lower than base");
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    MyLog.log("timeout happened with seq " + base);
                next = base;
                sendMessage();
            }
            }

        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        } finally {
            server.close();
        }
    }

    private void sendMessage() {
        byte[] data;
        String msg;
        DatagramPacket packet;
        try {
            while (next < base + size) {
                msg = "SEQ " + next + "\r\n" + "this is the No. " + next + " msg.";
                data = msg.getBytes();
                packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
                server.send(packet);
                MyLog.log("send data No. " + next);
                this.next++;
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



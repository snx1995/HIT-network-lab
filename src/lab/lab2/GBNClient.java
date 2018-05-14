package lab.lab2;

import lab.MyLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class GBNClient {
    private DatagramSocket client;
    private int expect = 0;

    public static void main(String[] args) {
        new GBNClient().start();
    }

    private void start() {
        try {
            byte[] data;

            client = new DatagramSocket();
            MyLog.log("client started..");

            InetAddress address = InetAddress.getLocalHost();
            int port = 54321;

            data = ("SEQ 0\r\nneed data.").getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            client.send(packet);

            data = new byte[1024];
            DatagramPacket sp = new DatagramPacket(data, data.length);

            int i = 0;
            while (true) {
                client.receive(sp);
                String msg = new String(sp.getData(), 0, sp.getLength());
                i++;
                String[] tmp = msg.split("\\s");
                int index = Integer.valueOf(tmp[1]);
                MyLog.log("received server data No. " + index);
                byte[] ack;
                if (index == expect) {
                    msg = "ACK " + expect + "\r\n";
                    ack = msg.getBytes();
                    expect++;
                } else {
                    msg = "ACK " + (expect - 1) + "\r\n";
                    ack = msg.getBytes();
                }
                if (i % 6 == 0) {
                    MyLog.log("send " + msg);
                    packet = new DatagramPacket(ack, ack.length, address, port);
                    client.send(packet);
                }
            }

        } catch (Exception e) {
            MyLog.log(e.getLocalizedMessage());
        } finally {
            client.close();

        }
    }
}
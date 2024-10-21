package br.yuri.coordinator.servers;

import br.yuri.coordinator.handlers.UDPHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer implements Server{

    final private Integer port;

    public UDPServer(Integer port) {
        this.port = port;
    }

    public void run() {

        try  {
            DatagramSocket socket = new DatagramSocket(port);
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);

                Thread.ofVirtual().start(new UDPHandler(datagramPacket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

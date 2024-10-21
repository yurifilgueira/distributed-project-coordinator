package br.yuri.coordinator.clients;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UDPClient implements Client {

    private DatagramSocket socket;
    private DatagramPacket sendPacket;

    public UDPClient(DatagramPacket sendPacket) throws Exception {
        this.socket = new DatagramSocket();
        this.sendPacket = sendPacket;
    }

    @Override
    public String sendAndReceive() throws IOException {

        try {


            socket.setSoTimeout(5000);

            socket.send(sendPacket);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);
            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Resposta recebida: " + response);

            return response;
        }catch (SocketTimeoutException e) {
            throw new SocketTimeoutException();
        }
    }

    public String run() {
        try {
            return sendAndReceive();
        }
        catch (Exception e) {
            return "TIME_OUT";
        }
        finally {
            socket.close();
        }
    }
}

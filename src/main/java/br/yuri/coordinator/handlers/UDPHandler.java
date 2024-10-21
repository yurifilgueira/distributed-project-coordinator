package br.yuri.coordinator.handlers;

import br.yuri.coordinator.requests.RequestHandler;

import java.net.DatagramPacket;

public class UDPHandler implements Runnable {

    private final DatagramPacket packet;

    public UDPHandler(DatagramPacket packet) {
        this.packet = packet;
    }

    @Override
    public void run() {
        try {
            new RequestHandler(packet).run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

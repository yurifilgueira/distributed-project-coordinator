package br.yuri.coordinator.requests;

import br.yuri.coordinator.clients.UDPClient;
import br.yuri.coordinator.entities.Member;
import br.yuri.coordinator.membeship.ActiveMembers;
import br.yuri.coordinator.partitions.PartitionTable;
import br.yuri.coordinator.partitions.utils.PartitionUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RequestHandler {

    DatagramSocket socket;
    private ActiveMembers activeMembers = ActiveMembers.getInstance();
    private DatagramPacket packet;
    private PartitionTable partitionTable;
    private PartitionUtils partitionUtils;

    public RequestHandler(DatagramPacket packet) throws SocketException {
        this.socket = new DatagramSocket();
        this.packet = packet;
        this.partitionTable = PartitionTable.getInstance();
        this.partitionUtils = new PartitionUtils();
    }

    public void run() throws Exception {

        String request = new String(packet.getData(), 0, packet.getLength());

        String[] tokens = tokenizer(request);

        switch (tokens[0]) {
            case "get":
                //TODO
                break;
            case "post":
                makeAction(tokens);
                break;
        }

    }

    private void makeAction(String[] tokens) throws Exception {

        if (tokens.length == 2) {

            if (tokens[1].equals("register-server")) {
                Member member = new Member(packet.getAddress(), packet.getPort());

                if (!partitionTable.isPartitionTableCreated()) {
                    activeMembers.put(member, System.currentTimeMillis());

                    var message = "registered".getBytes();
                    var sendPacket = new DatagramPacket(message, message.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);

                }
                else if (!partitionTable.memberExists(member) && partitionTable.isPartitionTableCreated()) {
                    activeMembers.put(member, System.currentTimeMillis());
                    var message = "registered".getBytes();
                    new UDPClient(new DatagramPacket(message, message.length, packet.getAddress(), packet.getPort())).run();

                    if (activeMembers.size() >= 4) {
                        partitionUtils.migratePartitions(activeMembers.keySet());
                    }
                }
            }

        }
    }

    private String[] tokenizer(String request) {
        return request.split(";");
    }
}

package br.yuri.coordinator.heartBeat;

import br.yuri.coordinator.entities.Member;
import br.yuri.coordinator.membeship.ActiveMembers;
import br.yuri.coordinator.partitions.PartitionTable;
import br.yuri.coordinator.partitions.utils.PartitionUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartBeatStarter {

    private Integer port;
    private ActiveMembers activeMembers = ActiveMembers.getInstance();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private PartitionTable partitionTable = PartitionTable.getInstance();
    private Boolean isPartitionCreated = false;
    private Boolean movePartitions = false;
    private PartitionUtils partitionUtils = new PartitionUtils();

    public HeartBeatStarter(Integer port) {
        this.port = port;
    }

    public void run() {
        receiveHeartBeat();
        startMonitoring();
    }

    public void receiveHeartBeat() {
        Thread.ofVirtual().start(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {

                while (true) {

                    if (activeMembers.size() == 3 && !isPartitionCreated) {
                        createTablePartitions();
                    }

                    if (movePartitions) {
                        partitionUtils.migratePartitions(activeMembers.keySet());
                        partitionTable.printPartitions();
                        movePartitions = false;
                    }

                    byte[] buffer = new byte[1024];

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    var member = new Member(packet.getAddress(), packet.getPort());

                    if (!partitionTable.memberExists(member) && isPartitionCreated) {
                        activeMembers.put(member, System.currentTimeMillis());

                        if (activeMembers.size() >= 4) {
                            movePartitions = true;
                        }
                    }
                    else {
                        activeMembers.put(member, System.currentTimeMillis());
                        updatePartitionStatus(member);
                    }

                    System.out.println("Received heartbeat from: " + packet.getPort());

                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updatePartitionStatus(Member member) {
        partitionTable.resurrectPartition(member);
        partitionTable.printPartitions();
    }

    public void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (Member member : activeMembers.keySet()) {
                long lastHeartbeat = activeMembers.get(member);

                if (currentTime - lastHeartbeat > 5000L) {
                    System.out.println("Server " + member + " is down!");

                    if (isPartitionCreated) {
                        partitionTable.disablePartitions(member);
                        partitionTable.printPartitions();
                    }

                    activeMembers.remove(member);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void createTablePartitions() {
        partitionTable.arrangePartitions(9, activeMembers.keySet().stream().toList());
        isPartitionCreated = true;
    }
}
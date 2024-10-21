package br.yuri.coordinator.partitions.utils;

import br.yuri.coordinator.clients.UDPClient;
import br.yuri.coordinator.entities.Member;
import br.yuri.coordinator.partitions.Partition;
import br.yuri.coordinator.partitions.PartitionMigration;
import br.yuri.coordinator.partitions.PartitionTable;
import br.yuri.coordinator.partitions.status.PartitionStatus;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.util.*;

public class PartitionUtils {

    private PartitionTable partitionTable = PartitionTable.getInstance();

    public int getAveragePartitionsPerNode(Set<Member> members) {
        int totalMembers = members.size();

        return totalMembers == 0 ? 0 : partitionTable.getPartitions().size() / totalMembers;
    }

    public List<Member> getOverloadedMembers(Set<Member> members) {
        List<Member> overloadedMembers = new ArrayList<>();
        int averagePartitions = getAveragePartitionsPerNode(members);

        for (Member member : members) {
            long partitionCount = partitionTable.getPartitions().values().stream()
                    .filter(partition -> partition.getAssignedMember().equals(member))
                    .count();

            if (partitionCount > averagePartitions) {
                overloadedMembers.add(member);
            }
        }

        overloadedMembers.sort((m1, m2) -> {
            long size1 = partitionTable.getPartitions().values().stream()
                    .filter(partition -> partition.getAssignedMember().equals(m1)).count();
            long size2 = partitionTable.getPartitions().values().stream()
                    .filter(partition -> partition.getAssignedMember().equals(m2)).count();
            return Long.compare(size2, size1);
        });

        return overloadedMembers;
    }

    public List<Member> getUnderloadedMembers(Set<Member> members) {
        List<Member> underloadedMembers = new ArrayList<>();
        int averagePartitions = getAveragePartitionsPerNode(members);

        for (Member member : members) {
            long partitionCount = partitionTable.getPartitions().values().stream()
                    .filter(partition -> partition.getAssignedMember().equals(member))
                    .count();

            if (partitionCount < averagePartitions) {
                underloadedMembers.add(member);
            }
        }

        underloadedMembers.sort(Comparator.comparingLong(member -> partitionTable.getPartitions().values().stream()
                .filter(partition -> partition.getAssignedMember().equals(member))
                .count()));

        return underloadedMembers;
    }

    public List<PartitionMigration> tryMovingPartitionsToUnderLoadedMembers(Set<Member> members) {
        List<Member> overloadedMembers = getOverloadedMembers(members);
        List<Member> underloadedMembers = getUnderloadedMembers(members);
        List<PartitionMigration> migrations = new ArrayList<>();

        int averagePartitions = getAveragePartitionsPerNode(members);
        for (Member overloadedMember : overloadedMembers) {
            List<Partition> overloadedPartitions = new ArrayList<>(partitionTable.getPartitions().values());
            overloadedPartitions.removeIf(partition -> !partition.getAssignedMember().equals(overloadedMember));

            int excessPartitions = overloadedPartitions.size() - averagePartitions;

            if (excessPartitions > 0) {
                Queue<Partition> moveQueue = new ArrayDeque<>(overloadedPartitions.subList(averagePartitions, overloadedPartitions.size()));

                for (Member underloadedMember : new ArrayList<>(underloadedMembers)) {
                    List<Partition> underloadedPartitions = new ArrayList<>(partitionTable.getPartitions().values());
                    underloadedPartitions.removeIf(partition -> !partition.getAssignedMember().equals(underloadedMember));

                    while (!moveQueue.isEmpty() && underloadedPartitions.size() < averagePartitions) {
                        Partition partitionToMove = moveQueue.poll();
                        if (partitionToMove != null) {
                            partitionToMove.setStatus(PartitionStatus.MIGRATING);
                            migrations.add(new PartitionMigration(partitionToMove, overloadedMember, underloadedMember));
                            partitionToMove.setAssignedMember(underloadedMember);
                            underloadedPartitions.add(partitionToMove);
                            overloadedPartitions.remove(partitionToMove);
                        }
                    }

                    if (underloadedPartitions.size() >= averagePartitions) {
                        underloadedMembers.remove(underloadedMember);
                    }
                }

                if (!moveQueue.isEmpty()) {
                    overloadedPartitions.subList(averagePartitions, overloadedPartitions.size()).clear();
                    overloadedPartitions.addAll(moveQueue);
                }
            }
        }

        return migrations;
    }

    public void migratePartitions(Set<Member> members) throws Exception {
        var migrations = tryMovingPartitionsToUnderLoadedMembers(members);

        for (PartitionMigration migration : migrations) {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(migration);
            objectStream.flush();
            byte[] serializedData = byteStream.toByteArray();

            var sourceMember = migration.getSourceMember();

            System.out.println(sourceMember);

            DatagramPacket sendPacket = new DatagramPacket(serializedData, serializedData.length, sourceMember.getAddress(), sourceMember.getPort());
            System.out.println("Moving...");
            Thread.ofVirtual().start(() -> {
                try {
                    new UDPClient(sendPacket).run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}

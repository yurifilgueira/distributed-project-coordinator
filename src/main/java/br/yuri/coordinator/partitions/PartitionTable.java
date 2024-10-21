package br.yuri.coordinator.partitions;

import br.yuri.coordinator.entities.Member;
import br.yuri.coordinator.partitions.status.PartitionStatus;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionTable {

    private ConcurrentHashMap<String, Partition> partitions = new ConcurrentHashMap<>();

    private static PartitionTable instance;
    private boolean isPartitionTableCreated = false;

    private PartitionTable() {}

    public static synchronized PartitionTable getInstance() {
        if (instance == null) {
            instance = new PartitionTable();
        }
        return instance;
    }

    public void addPartition(String partitionId, Partition partition) {
        partitions.put(partitionId, partition);
    }

    public void removePartition(String partitionId) {
        partitions.remove(partitionId);
    }

    public Partition getPartition(String partitionId) {
        return partitions.get(partitionId);
    }

    public ConcurrentHashMap<String, Partition> getPartitions() {
        return partitions;
    }

    public boolean isPartitionPresent(String partitionId) {
        return partitions.containsKey(partitionId);
    }

    public void arrangePartitions(int noOfPartitions, List<Member> liveMembers) {

        for (int partitionId = 1; partitionId <= noOfPartitions; partitionId++) {
            int index = partitionId % liveMembers.size();
            Member member = liveMembers.get(index);

            partitions.put(String.valueOf(partitionId),
                    new Partition(partitionId, PartitionStatus.ASSIGNED, member));
        }

        isPartitionTableCreated = true;
    }

    public void printPartitions() {
        System.out.println("Partition Table:");
        for (var entry : partitions.entrySet()) {
            String partitionId = entry.getKey();
            Partition partition = entry.getValue();
            System.out.println("Partition ID: " + partitionId +
                    ", Status: " + partition.getStatus() +
                    ", Assigned to: " + partition.getAssignedMember());
        }
    }

    public void disablePartitions(Member member) {
        partitions.forEach((partitionId, partition) -> {
            if (partition.getAssignedMember().equals(member)) {
                partition.setStatus(PartitionStatus.INACTIVE);
            }
        });
    }

    public boolean memberExists(Member member) {
        return partitions
                .values()
                .stream()
                .anyMatch(partition -> partition.getAssignedMember().equals(member));
    }

    public void resurrectPartition(Member member) {

        partitions
                .values()
                .stream()
                .filter(partition -> partition.getAssignedMember().equals(member) && partition.getStatus() == PartitionStatus.INACTIVE)
                .forEach(partition -> partition.setStatus(PartitionStatus.ASSIGNED));
    }

    public boolean isPartitionTableCreated() {
        return isPartitionTableCreated;
    }

    public void setPartitionTableCreated(boolean partitionTableCreated) {
        isPartitionTableCreated = partitionTableCreated;
    }
}

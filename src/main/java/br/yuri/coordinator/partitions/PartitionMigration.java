package br.yuri.coordinator.partitions;

import br.yuri.coordinator.entities.Member;

import java.io.Serial;
import java.io.Serializable;

public class PartitionMigration implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Partition partition;
    private Member sourceMember;
    private Member destinationMember;

    public PartitionMigration(Partition partition, Member sourceMember, Member destinationMember) {
        this.partition = partition;
        this.sourceMember = sourceMember;
        this.destinationMember = destinationMember;
    }

    public Partition getPartition() {
        return partition;
    }

    public Member getSourceMember() {
        return sourceMember;
    }

    public Member getDestinationMember() {
        return destinationMember;
    }

    @Override
    public String toString() {
        return "PartitionMigration{" +
                "partition=" + partition +
                ", sourceMember=" + sourceMember +
                ", destinationMember=" + destinationMember +
                '}';
    }
}

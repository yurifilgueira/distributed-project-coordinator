package br.yuri.coordinator.partitions;

import br.yuri.coordinator.entities.Member;
import br.yuri.coordinator.partitions.status.PartitionStatus;

import java.io.Serial;
import java.io.Serializable;

public class Partition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private PartitionStatus status;
    private Member assignedMember;

    public Partition(Integer id, PartitionStatus status, Member assignedMember) {
        this.id = id;
        this.status = status;
        this.assignedMember = assignedMember;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PartitionStatus getStatus() {
        return status;
    }

    public void setStatus(PartitionStatus status) {
        this.status = status;
    }

    public Member getAssignedMember() {
        return assignedMember;
    }

    public void setAssignedMember(Member assignedMember) {
        this.assignedMember = assignedMember;
    }

}

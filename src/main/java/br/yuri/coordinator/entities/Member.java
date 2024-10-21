package br.yuri.coordinator.entities;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public class Member implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private InetAddress address;
    private Integer port;

    public Member() {
    }

    public Member(InetAddress address, Integer port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(address, member.address) && Objects.equals(port, member.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    @Override
    public String toString() {
        return "Member{" +
                "address='" + address + '\'' +
                ", port=" + port +
                '}';
    }
}

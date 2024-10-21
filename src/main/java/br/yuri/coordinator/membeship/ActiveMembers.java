package br.yuri.coordinator.membeship;

import br.yuri.coordinator.entities.Member;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveMembers {

    private ConcurrentHashMap<Member, Long> activeServers = new ConcurrentHashMap<>();

    private static ActiveMembers instance;

    private ActiveMembers() {}

    public static synchronized ActiveMembers getInstance() {
        if (instance == null) {
            instance = new ActiveMembers();
        }
        return instance;
    }

    public void put(Member member, Long timestamp) {
        activeServers.put(member, timestamp);
    }

    public void remove(Member member) {
        activeServers.remove(member);
    }

    public Long get(Member member) {
        return activeServers.get(member);
    }

    public ConcurrentHashMap<Member, Long> getActiveServers() {
        return activeServers;
    }

    public boolean isMemberActive(Member member) {
        return activeServers.containsKey(member);
    }

    public Set<Member> keySet() {
        return activeServers.keySet();
    }

    public int size() {
        return activeServers.size();
    }
}

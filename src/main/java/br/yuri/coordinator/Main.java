package br.yuri.coordinator;

import br.yuri.coordinator.heartBeat.HeartBeatStarter;
import br.yuri.coordinator.serverTypes.ServerType;
import br.yuri.coordinator.servers.ServerFactory;

import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws SocketException {

        new HeartBeatStarter(Integer.parseInt(args[2])).run();
        var serverType = ServerType.valueOf(args[0].toUpperCase());
        ServerFactory.createServer(serverType, Integer.parseInt(args[1])).run();
    }
}
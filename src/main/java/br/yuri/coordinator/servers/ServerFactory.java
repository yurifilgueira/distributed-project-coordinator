package br.yuri.coordinator.servers;

import br.yuri.coordinator.serverTypes.ServerType;

public class ServerFactory {

    public static Server createServer(ServerType type, Integer port) {

        switch (type) {
            case ServerType.UDP:
                return new UDPServer(port);
            default:
                throw new IllegalArgumentException("Unsupported server type");
        }

    }

}

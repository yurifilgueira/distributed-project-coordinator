package br.yuri.coordinator.clients;

import java.io.IOException;

public interface Client {
    String sendAndReceive() throws IOException;
    String run();
}

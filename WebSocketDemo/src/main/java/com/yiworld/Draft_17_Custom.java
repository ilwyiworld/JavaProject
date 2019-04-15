package com.yiworld;


import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ClientHandshakeBuilder;

public class Draft_17_Custom extends Draft_17 {

    public Draft_17_Custom() {

    }

    @Override
    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) {
        super.postProcessHandshakeRequestAsClient(request);
        request.put("Sec-WebSocket-Version", "13");
        request.put("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NTUxNDY4MDcsInVzZXJuYW1lIjoic3lzdGVtIn0.n486lew7qq2qL4JCh5OxM4TLLAAIYTBxc7FZ0p9Lnr8");
        return request;
    }

    public Draft copyInstance() {
        return new Draft_17_Custom();
    }

}

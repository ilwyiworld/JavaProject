package com.yiworld.route.kit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetAddressIsReachable {

    /**
     * check ip and port
     *
     * @param address
     * @param port
     * @param timeout
     * @return True if connection successful
     */
    public static boolean checkAddressReachable(String address, int port, int timeout) {
        Socket socket = new Socket() ;
        try {
            socket.connect(new InetSocketAddress(address, port), timeout);
            return true;
        } catch (IOException exception) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                return false ;
            }
        }
    }
}

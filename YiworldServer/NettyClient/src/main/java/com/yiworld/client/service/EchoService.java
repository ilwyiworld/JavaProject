package com.yiworld.client.service;

public interface EchoService {
    /**
     * echo msg to terminal
     * @param msg message
     * @param replace
     */
    void echo(String msg, Object... replace) ;
}

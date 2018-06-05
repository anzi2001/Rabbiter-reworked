package com.example.kocja.rabbiter_reworked;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketIOManager {
    private static Socket socket;
    public static void initSocket(){
        try {
            socket = IO.socket("http://192.168.0.130");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public static Socket getSocket(){
        return socket;
    }
}

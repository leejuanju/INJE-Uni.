package com.example.scrollingshooter_3;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static final int PORT = 8888;

    // 서버 관련 변수
    private ServerSocket serverSocket;
    private boolean isServer = false;

    // 클라이언트 관련 변수
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isConnected = false;

    // 서버 시작
    public void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    isServer = true;
                    Log.d(TAG, "서버 시작: " + PORT);

                    // 클라이언트 연결 대기
                    clientSocket = serverSocket.accept();
                    setupStreams();

                } catch (IOException e) {
                    Log.e(TAG, "서버 시작 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 클라이언트로 연결
    public void connectToServer(final String serverIP) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(serverIP);
                    clientSocket = new Socket(serverAddr, PORT);
                    setupStreams();

                } catch (UnknownHostException e) {
                    Log.e(TAG, "알 수 없는 호스트: " + e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, "서버 연결 오류: " + e.getMessage());
                }
            }
        }).start();
    }

    // 입출력 스트림 설정
    private void setupStreams() {
        try {
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            isConnected = true;

            Log.d(TAG, "네트워크 스트림 설정 완료");
        } catch (IOException e) {
            Log.e(TAG, "스트림 설정 오류: " + e.getMessage());
        }
    }

    // 게임 상태 전송
    public void sendGameState(final GameState state) {
        if (!isConnected) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.writeObject(state);
                    outputStream.flush();
                } catch (IOException e) {
                    Log.e(TAG, "데이터 전송 오류: " + e.getMessage());
                }
            }
        }).start();
    }

    // 게임 상태 수신
    public GameState receiveGameState() {
        if (!isConnected) return null;

        try {
            return (GameState) inputStream.readObject();
        } catch (Exception e) {
            Log.e(TAG, "데이터 수신 오류: " + e.getMessage());
            return null;
        }
    }

    // 연결 종료
    public void disconnect() {
        isConnected = false;

        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "연결 종료 오류: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isServer() {
        return isServer;
    }
}

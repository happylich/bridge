package com.happylich.bridge.game.wlan.wifihotspot.transmitdata;

import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.happylich.bridge.game.main.Game;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by lich on 2018/6/3.
 */

public class GameClientReceiveDataThread extends Thread {
    private boolean running = false;
    private InputStream mInputStream;
    private InputStreamReader mInputStreamReader;
    private BufferedReader mBufferedReader;
    private String response;
    private Game game;

    public GameClientReceiveDataThread() {
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void run() {
        this.game.mHandler.sendEmptyMessage(220);
        while (running) {
//            this.game.mHandler.sendEmptyMessage(221);
            if (game.socket != null && game.socket.isConnected()) {
                try {
                    mInputStream = game.socket.getInputStream();
                    mInputStreamReader = new InputStreamReader(mInputStream);
                    mBufferedReader = new BufferedReader(mInputStreamReader);
                    response = mBufferedReader.readLine();
                } catch (Exception e){
                    Message message1 = new Message();
                    message1.what = 0;
                    message1.obj = e.toString();
                    game.mHandler.sendMessage(message1);
                    this.game.mHandler.sendEmptyMessage(223);
                }
                this.game.mHandler.sendEmptyMessage(222);


                // 客户端可能受到0或者2两种消息
                // 先将1>解析出来
                // 1要交给handler处理
                Log.v(this.getClass().getName(), response);
                String[] message = response.split(">");
                Message message1 = new Message();
                message1.what = Integer.parseInt(message[0]);
                message1.obj = message[1];
                game.mHandler.sendMessage(message1);
            }
        }
    }
}

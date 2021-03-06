package com.happylich.bridge.game.wlan.wifihotspot.autofind;

import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.happylich.bridge.engine.game.Game;
import com.happylich.bridge.game.player.ProxyPlayer;
import com.happylich.bridge.game.utils.RoomAdapter;
import com.happylich.bridge.game.utils.RoomBean;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by lich on 2018/5/17.
 */

/**
 * 这个类监听局域网中发送"IP + 状态"UDP包
 * 将这些IP和状态显示在列表中
 *
 * 点击列表后，向服务器发送连接请求
 * 客户端所有的请求都发送给服务器å
 */
public class WifiBroadcastReceiverThread extends Thread {

    private WifiInfo mWifiInfo;
    private WifiManager.MulticastLock mMulticastLock;

    private String ip;
    private CopyOnWriteArrayList<RoomBean> mRoomList;
    private RoomAdapter mRoomAdapter;
    private static int BROADCAST_PORT = 8003;
    private static String BROADCAST_IP = "255.255.255.255";

    InetAddress     mInetAddress = null;
    MulticastSocket mMulticastSocket = null;
    DatagramPacket  mDatagramPacket = null;

    Handler mHandler;

    // 游戏线程运行开关
    private boolean running = false;
    private final static int DELAY_TIME = 5000;
    private boolean isPaused = false;

    byte[] data = new byte[1024];
    /**
     * 线程构造函数
     */
    public WifiBroadcastReceiverThread() {
    }
    public WifiBroadcastReceiverThread(WifiManager mWifiManager) {
        // 自动获得本机IP地址
        if (mWifiManager.isWifiEnabled()) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            ip = getIpString(mWifiInfo.getIpAddress());
        }
    }

    public void setRoomList(CopyOnWriteArrayList<RoomBean> mRoomList) {
        this.mRoomList = mRoomList;
    }

    public CopyOnWriteArrayList<RoomBean> getmRoomList() {
        return this.mRoomList;
    }

    public void setRoomAdapter(RoomAdapter mRoomAdapter) {
        this.mRoomAdapter = mRoomAdapter;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    /**
     * IP转换函数
     * @param i
     * @return
     */
    public String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    /**
     * 线程运行标志设置函数（设置为false后停止线程）
     * @param state
     */
    public void setRunning (boolean state) {
        this.running = state;
    }
    public void setPause (boolean state) {
        this.isPaused = state;
    }

    public void setMulticastLock (WifiManager.MulticastLock mMulticastLock) {
        this.mMulticastLock = mMulticastLock;
    }

    /**
     * 线程更新函数
     */
    public synchronized void run() {

        // 这里是广播IP
        // 视情况的不同，这个类还可能广播
        // 1. 房间的情况
        //   1. 未满员：可以点击加入
        //   2. 满员：不可以加入了
        //   3. 游戏中：游戏已经开始
        // 2. 心跳检测
        //   1. 依次向连接的客户端发起连接请求
        try {
            InetAddress groupAddress = InetAddress.getByName(BROADCAST_IP);
            mMulticastSocket = new MulticastSocket(BROADCAST_PORT);
//            mMulticastSocket.joinGroup(groupAddress);
            mDatagramPacket = new DatagramPacket(data, data.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        byte[] data = ip.getBytes();
//        dataPacket = new DatagramPacket(data, data.length, inetAddress, BROADCAST_PORT);
        while(running) {
            if(!isPaused) {
                mMulticastLock.acquire();

                RoomBean roomBean = new RoomBean();
                try {
                    if (mMulticastSocket != null) {
                        mMulticastSocket.setSoTimeout(500);
                        mMulticastSocket.receive(mDatagramPacket);
                    }

                    String message = new String(mDatagramPacket.getData(), "utf-8");
                    String[] messageList = message.split(" ");

                    roomBean.setIP(messageList[0].trim());
                    roomBean.setState(messageList[1].trim());
                    // 检查是否重复

                    // 清除没有时间的选项

                    // 获得发送的地址和数据
                    // 怎样将这个线程和Activity连接起来
                }  catch (SocketTimeoutException e) {
                } catch (Exception e) {
                } finally {
                    int flag = 1;
                    for(RoomBean tmp:mRoomList) {
                        if (tmp != null && tmp.getIP() != null) {
                            if (tmp.getIP().equals(roomBean.getIP())) {
                                flag = 0;
                                tmp.setTime(mRoomList.size() * 2 + 3);
                            }
                        }
                        tmp.refreshTime();
                    }
                    if (flag == 1) {
                        if (roomBean.getIP() != null && !roomBean.getIP().equals(ip) && !roomBean.getIP().equals("null") && !roomBean.getIP().equals("")) {
                            roomBean.setTime(mRoomList.size() * 2 + 3);
                            mRoomList.add(roomBean);
                        }
                    }
                    // 如果不是新增的
                    // 给监听到的增加时间
                    // 这个似乎无效？
                    for (RoomBean tmp : mRoomList) {
                        if (tmp != null) {
                            if (tmp.getTime() <= 0) {
                                mRoomList.remove(tmp);
                            }
                        } else {
                            mRoomList.remove(tmp);
                        }
                    }

                    mHandler.sendEmptyMessage(0);
                }
                mMulticastLock.release();
            } else {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }
        Log.v(this.getClass().getName(), "线程结束了");
    }
}

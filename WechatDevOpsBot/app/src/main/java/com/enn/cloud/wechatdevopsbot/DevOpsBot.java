package com.enn.cloud.wechatdevopsbot;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.enn.cloud.socket.SocketTransceiver;
import com.enn.cloud.socket.TcpServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangjiahao on 11/10/2016.
 */

public class DevOpsBot extends AccessibilityService {

    private AccessibilityNodeInfo rootNodeInfo;

    private final static int SERVER_PORT = 7344;

    private final static int MAX_SIZE = 50;
    private final static List<AccessibilityNodeInfo> chatHistory = new ArrayList<AccessibilityNodeInfo>(MAX_SIZE);
    private Handler handler = new Handler();

    private TcpServer server = new TcpServer(SERVER_PORT) {
        @Override
        public void onConnect(SocketTransceiver client) {
            android.util.Log.d("maptrix", "Client " + client.getInetAddress().getHostAddress() + "   Connect");
        }

        @Override
        public void onConnectFailed() {
            android.util.Log.d("maptrix", "Client Connect Failed");
        }

        @Override
        public void onReceive(SocketTransceiver client,final String s) {
            android.util.Log.d("maptrix", "Client " + client.getInetAddress().getHostAddress() + "   Send data" + s);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(fill(s)) {
                        send();
                    }
                }
            }, 1000);
        }

        @Override
        public void onDisconnect(SocketTransceiver client) {
            android.util.Log.d("maptrix", "Client " + client.getInetAddress().getHostAddress() + "   Disconnect");
        }

        @Override
        public void onServerStop() {
            android.util.Log.d("maptrix", "---------------Server Stopped-----------------");
        }
    };


    @Override
    protected void onServiceConnected() {
        android.util.Log.d("maptrix", "----------------Server Started-----------------");
        server.start();
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {

        if(event == null || event.getSource() == null) {
            return ;
        }

        try {
            android.util.Log.d("maptrix", "get event = " + event);

            rootNodeInfo = event.getSource();

            android.util.Log.d("maptrix", "get event type = " + event.getEventType());

            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

                if (rootNodeInfo != null && rootNodeInfo.findAccessibilityNodeInfosByText(">") != null) {

                    List<AccessibilityNodeInfo> accessibilityNodeInfos = rootNodeInfo.findAccessibilityNodeInfosByText(">");

                    if (accessibilityNodeInfos != null
                            && accessibilityNodeInfos.size() > 0
                            && accessibilityNodeInfos.get(accessibilityNodeInfos.size() - 1) != null
                            && accessibilityNodeInfos.get(accessibilityNodeInfos.size() - 1).getText() != null) {

                        AccessibilityNodeInfo node = accessibilityNodeInfos.get(accessibilityNodeInfos.size() - 1);

                        android.util.Log.d("maptrix", "get the text = " + node.getText());
                        if (!chatHistory.contains(node) && server.getClients().size() > 0) {

                            if (chatHistory.size() >= MAX_SIZE) {
                                chatHistory.remove(MAX_SIZE - 1);
                            }
                            chatHistory.add(node);

                            String content = node.getText().toString();
                            String command = content.substring(content.indexOf(">") + 1, content.length() - 1).trim();
                            server.getClients().get(0).send(command);
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.d("maptrix", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void send() {

        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("发送");

            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if(n.getClassName().equals("android.widget.Button") && n.isEnabled()){
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }

            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo.findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        if(n.getClassName().equals("android.widget.Button") && n.isEnabled()){
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }

        }
    }


    @SuppressLint("NewApi")
    private boolean fill(String s) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findEditText(rootNode, s);
        }
        return false;
    }


    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {

        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + "," + rootNode.getText() + "," + rootNode.getChildCount());

        for (int i = 0; i < rootNode.getChildCount(); i++) {

            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);

            if (nodeInfo == null) {
                android.util.Log.d("maptrix", "nodeinfo = null");
                continue;
            }

            android.util.Log.d("maptrix", "class=" + nodeInfo.getClassName());
            android.util.Log.e("maptrix", "ds=" + nodeInfo.getContentDescription());

            if(nodeInfo.getContentDescription() != null){
                rootNodeInfo = nodeInfo;
                android.util.Log.i("maptrix", "find node info");
            }

            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                android.util.Log.i("maptrix", "==================");

                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT, AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN, true);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY, arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }

            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {

    }
}
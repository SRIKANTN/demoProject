package com.hiibox.houseshelter.listener;

import android.os.Handler;
import android.os.Message;

import com.hiibox.houseshelter.net.Frame;
import com.hiibox.houseshelter.net.TCPServiceClientV2.CommandListener;

public class HandlerCommandListener implements CommandListener {

    Handler handler;
    public HandlerCommandListener(Handler h) {
        handler = h;
    }
    
    @Override
    public int onReceive(Frame src, Frame f) {
        Message msg = handler.obtainMessage();
        msg.obj = new Frame[]{src,f};
        msg.what  = 0;
                                                                                                        
        handler.sendMessage(msg);
        return 0;
    }

    @Override
    public void onTimeout(Frame src, Frame f) {
        Message msg = handler.obtainMessage();
        msg.obj = new Frame[]{src,f};
        msg.what  = -1;
        handler.sendMessage(msg);
    }

}

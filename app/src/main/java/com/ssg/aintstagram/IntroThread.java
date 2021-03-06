package com.ssg.aintstagram;

import android.os.Message;
import android.os.Handler;
import com.kakao.auth.Session;

public class IntroThread extends Thread {
    private Handler handler;

    public IntroThread(Handler handler){
        this.handler = handler;
    }

    @Override
    public void run(){
        Message msg = new Message();

        try{
            Thread.sleep(2000);

            if(Session.getCurrentSession().isOpened()){
                msg.what = 1;
            }
            else{
                msg.what = 2;
            }
            handler.sendEmptyMessage(msg.what);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

package com.hiibox.houseshelter.activity;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.annotation.view.ViewInject;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hiibox.houseshelter.ShaerlocActivity;
import com.hiibox.houseshelter.MyApplication;
import com.hiibox.houseshelter.R;
import com.hiibox.houseshelter.adapter.InvadeAdapter;
import com.hiibox.houseshelter.core.MianActivity;
import com.hiibox.houseshelter.core.GlobalUtil;
import com.hiibox.houseshelter.listener.HandlerCommandListener;
import com.hiibox.houseshelter.net.Frame;
import com.hiibox.houseshelter.net.InvadePhotoResult;
import com.hiibox.houseshelter.net.InvadePhotoResult.PhotoInfo;
import com.hiibox.houseshelter.net.SpliteUtil;
import com.hiibox.houseshelter.net.TCPServiceClientV2.CommandListener;
import com.hiibox.houseshelter.service.PushMessageService;
import com.hiibox.houseshelter.util.DateUtil;
import com.hiibox.houseshelter.util.ImageOperation;
import com.hiibox.houseshelter.util.LocationUtil;
import com.hiibox.houseshelter.util.MessageUtil;
import com.hiibox.houseshelter.util.PreferenceUtil;
import com.hiibox.houseshelter.util.StringUtil;
import com.hiibox.houseshelter.view.PullToRefreshView;
import com.hiibox.houseshelter.view.PullToRefreshView.OnFooterRefreshListener;
import com.hiibox.houseshelter.view.PullToRefreshView.OnHeaderRefreshListener;

    
  
  
  
  
  
  
  
public class InvadeActivity extends ShaerlocActivity implements OnFooterRefreshListener, OnHeaderRefreshListener {

    @ViewInject(id = R.id.root_layout) RelativeLayout rootLayout;
    @ViewInject(id = R.id.back_iv, click = "onClick") ImageView backIV;
    @ViewInject(id = R.id.phone_iv, click = "onClick") ImageView phoneIV;
    @ViewInject(id = R.id.buzzer_switch_iv, click = "onClick") ImageView buzzerIV;
    @ViewInject(id = R.id.pull_to_refresh_view) PullToRefreshView refreshView;
    @ViewInject(id = R.id.invade_lv) ListView invadeLV;
    @ViewInject(id = R.id.progress_bar) ProgressBar progressBar;
    
    private List<PhotoInfo> list = null;
    private InvadeAdapter adapter = null;
    private boolean buzzerSwitch = false;
    private ProgressDialog dialog = null;
    private String emergencyContact = null;
                                                           
    private InvadePhotoResult albumResult = null;
    private HandlerCommandListener commandListener = null;
    private String filedId = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buzzerSwitch = PreferenceUtil.getInstance(mContext).getBoolean("buzzerSwitch", false);
        filedId = getIntent().getStringExtra("filedId");
        setContentView(R.layout.activity_invade_layout);
        
        if (buzzerSwitch) {
            buzzerIV.setBackgroundResource(R.drawable.buzzer_on);
        } else {
            buzzerIV.setBackgroundResource(R.drawable.buzzer_off);
        }
        
        refreshView.setHeadRefresh(false);
        refreshView.setFooterRefresh(false);
        refreshView.setOnFooterRefreshListener(this);
        refreshView.setOnHeaderRefreshListener(this);
        
        list = new ArrayList<InvadePhotoResult.PhotoInfo>();
        commandListener = new HandlerCommandListener(picHandler);
        albumResult = new InvadePhotoResult();
                                                        
        queryPhotos();
        
        adapter = new InvadeAdapter(mActivity, mContext, finalBitmap);
        
        dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        getContact();
    }
    
    private void queryPhotos() {
        if (LocationUtil.checkNetWork(mContext).endsWith(GlobalUtil.NETWORK_NONE)) {
            MessageUtil.alertMessage(mContext, R.string.sys_network_error);
            startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
            return;
        }
        String phone = PreferenceUtil.getInstance(getApplicationContext()).getString("phone", null);
        if (StringUtil.isEmpty(phone)) {
            MessageUtil.alertMessage(mContext, R.string.please_login);
            if (null != MyApplication.mainClient) {
                MyApplication.mainClient.stop();
                MyApplication.mainClient = null;
            }
            if (null != MyApplication.fileClient) {
                MyApplication.fileClient.stop();
                MyApplication.fileClient = null;
            }
            stopService(new Intent(mContext, PushMessageService.class));
            startActivity(new Intent(mContext, LoginActivity.class));
            MianActivity.getScreenManager().exitAllActivityExceptOne();
            return;
        }
        if (null == MyApplication.fileClient || !MyApplication.fileClient.isConnected()) {
            MyApplication.initTcpManager();
            MyApplication.fileClient = MyApplication.tcpManager.getFileClient(phone);
        }
        MyApplication.fileClient.queryInvadePhotos(MyApplication.phone, filedId, commandListener);
    }
    
    @SuppressLint("HandlerLeak")
    private Handler picHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null != msg) {
                Frame[] f = (Frame[]) msg.obj;
                if (null == f) {
                    return;
                }
                if (null == f[1]) {
                    return;
                }
                if (f[1].subCmd == 39) {
                    int rc = albumResult.prasePhotoAlbum(f[1]);
                    Log.i("InvadeActivity", "handleMessage()  rc = "+rc);
                    if (rc == 0) {
                        list = albumResult.getList();
                        Log.i("InvadeActivity", "handleMessage()  list = "+list);
                        if (list.size() > 0) {
                            adapter.setList(list);
                            invadeLV.setAdapter(adapter);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    };
    
    private void getContact() {
                                                               
        emergencyContact = PreferenceUtil.getInstance(mContext).getString("emergencyContact", null);
        if (StringUtil.isEmpty(emergencyContact)) {
            if (LocationUtil.checkNetWork(mContext).endsWith(GlobalUtil.NETWORK_NONE)) {
                MessageUtil.alertMessage(mContext, R.string.sys_network_error);
                startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
                return;
            }
            String phone = PreferenceUtil.getInstance(getApplicationContext()).getString("phone", null);
            if (StringUtil.isEmpty(phone)) {
                MessageUtil.alertMessage(mContext, R.string.please_login);
                if (null != MyApplication.mainClient) {
                    MyApplication.mainClient.stop();
                    MyApplication.mainClient = null;
                }
                if (null != MyApplication.fileClient) {
                    MyApplication.fileClient.stop();
                    MyApplication.fileClient = null;
                }
                stopService(new Intent(mContext, PushMessageService.class));
                startActivity(new Intent(mContext, LoginActivity.class));
                MianActivity.getScreenManager().exitAllActivityExceptOne();
                return;
            }
            if (null == MyApplication.mainClient || !MyApplication.mainClient.isConnected()) {
                MyApplication.initTcpManager();
                MyApplication.mainClient = MyApplication.tcpManager.getMainClient(phone, null, "1111111111111111", "66666666");
            }
            MyApplication.mainClient.queryEmergencyTelephone(phone, new CommandListener() {
                
                @Override
                public void onTimeout(Frame src, Frame f) {
                    
                }
                
                @Override
                public int onReceive(Frame src, Frame f) {
                    if (null != f) {
                        if (f.subCmd == 79) {
                            if (f.strData.startsWith("1")) {
                                return 0;
                            }
                            emergencyContact = SpliteUtil.getResult(f.strData);
                            PreferenceUtil.getInstance(mContext).saveString("emergencyContact", emergencyContact.trim());
                                                                                   
                        }
                    }
                    return 0;
                }
            });
        }
    }
    
    @SuppressWarnings("static-access")
    public void onClick(View v) {
        if (v == backIV) {
            MianActivity.getScreenManager().exitActivity(mActivity);
        } else if (v == phoneIV) {
            Intent intent = new Intent();
            intent.setAction(intent.ACTION_DIAL);
            if (StringUtil.isNotEmpty(emergencyContact)) {
                intent.setData(Uri.parse("tel:"+emergencyContact));
            } else {
                intent.setData(Uri.parse("tel:"+110));
            }
            startActivity(intent);
        } else if (v == buzzerIV) {
        	Log.e("InvadeActivity", "onClick()   buzzerSwitch = "+buzzerSwitch);
            sendOrder();
        }
    }
    
    private void sendOrder() {
    	if (LocationUtil.checkNetWork(mContext).endsWith(GlobalUtil.NETWORK_NONE)) {
        	MessageUtil.alertMessage(mContext, R.string.sys_network_error);
        	startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
        	return;
        }
		String phone = PreferenceUtil.getInstance(getApplicationContext()).getString("phone", null);
		if (StringUtil.isEmpty(phone)) {
			MessageUtil.alertMessage(mContext, R.string.please_login);
			if (null != MyApplication.mainClient) {
            	MyApplication.mainClient.stop();
            	MyApplication.mainClient = null;
            }
            if (null != MyApplication.fileClient) {
            	MyApplication.fileClient.stop();
                MyApplication.fileClient = null;
            }
            stopService(new Intent(mContext, PushMessageService.class));
			startActivity(new Intent(mContext, LoginActivity.class));
			MianActivity.getScreenManager().exitAllActivityExceptOne();
			return;
		}
		if (null == MyApplication.mainClient || !MyApplication.mainClient.isConnected()) {
    		MyApplication.initTcpManager();
            MyApplication.mainClient = MyApplication.tcpManager.getMainClient(phone, null, "1111111111111111", "66666666");
    	}
		if (buzzerSwitch) {
            buzzerOff();
        } else {
            buzzerOn();
        }
    }
    
    private void buzzerOn() {
    	dialog.setMessage(getString(R.string.buzzer_opening));
    	dialog.show();
        MyApplication.mainClient.beepOn(MyApplication.phone, DateUtil.getcurrentDay(), new CommandListener() {
            @Override
            public void onTimeout(Frame src, Frame f) {
            	Log.d("InvadeActivity", "buzzerOn()   time out... ");
                handler.sendEmptyMessage(3);
            }
            
            @SuppressWarnings("unused")
			@Override
            public int onReceive(Frame src, Frame f) {
                Log.d("InvadeActivity", "buzzerOn() data = "+f.strData);
                if (null != f) {
                    if (f.strData.equals("0")) {
                    	handler.sendEmptyMessage(0);
                    } else {
                        handler.sendEmptyMessage(1);
                    }
                } else {
                    handler.sendEmptyMessage(2);
                }
                Log.e("InvadeActivity", "buzzerOn() before  return..............");
                return 0;
            }
        });
    }
    
    private void buzzerOff() {
    	dialog.setMessage(getString(R.string.buzzer_closing));
    	dialog.show();
        MyApplication.mainClient.beepOff(MyApplication.phone, DateUtil.getcurrentDay(), new CommandListener() {
            @Override
            public void onTimeout(Frame src, Frame f) {
            	Log.d("InvadeActivity", "buzzerOff()   time out... ");
            	handler.sendEmptyMessage(3);
            }
            
            @SuppressWarnings("unused")
			@Override
            public int onReceive(Frame src, Frame f) {
                Log.d("InvadeActivity", "buzzerOff() data = "+f.strData);
                if (null != f) {
                    if (f.strData.equals("0")) {
                    	handler.sendEmptyMessage(4);
                    } else {
                        handler.sendEmptyMessage(1);
                    }
                } else {
                	handler.sendEmptyMessage(2);
                }
                Log.e("InvadeActivity", "buzzerOff()  befrore return..............");
                return 0;
            }
        });
    }
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			dialog.dismiss();
			switch (what) {
				case 0:         
					buzzerSwitch = true;
	                PreferenceUtil.getInstance(mContext).saveBoolean("buzzerSwitch", true);
					buzzerIV.setBackgroundResource(R.drawable.buzzer_on);
					MessageUtil.alertMessage(mContext, R.string.buzzer_on);
					break;
				case 1:         
					MessageUtil.alertMessage(mContext, R.string.operate_failed);
					break;
				case 2:          
					MessageUtil.alertMessage(mContext, R.string.network_not_response);
					break;
				case 3:       
					MessageUtil.alertMessage(mContext, R.string.network_timeout);
					break;
				case 4:         
					buzzerSwitch = false;
	                PreferenceUtil.getInstance(mContext).saveBoolean("buzzerSwitch", false);
	                buzzerIV.setBackgroundResource(R.drawable.buzzer_off);
	                MessageUtil.alertMessage(mContext, R.string.buzzer_off);
					break;
				default:
					break;
			}
		}
    };
    
    @Override
    public void onFooterRefresh(PullToRefreshView view) {
        adapter.notifyDataSetChanged();
        refreshView.onFooterRefreshComplete();
    }

    @Override
    public void onHeaderRefresh(PullToRefreshView view) {
        refreshView.onHeaderRefreshComplete();   
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(
            ImageOperation.readBitMap(mContext, R.drawable.bg_app));
        rootLayout.setBackgroundDrawable(bitmapDrawable);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        rootLayout.setBackgroundDrawable(null);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	handler.removeCallbacks(null);
    	picHandler.removeCallbacks(null);
    }
    
}

package com.hiibox.houseshelter.activity;

import java.io.IOException;

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
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiibox.houseshelter.ShaerlocActivity;
import com.hiibox.houseshelter.MyApplication;
import com.hiibox.houseshelter.R;
import com.hiibox.houseshelter.adapter.AdsAdapter;
import com.hiibox.houseshelter.core.MianActivity;
import com.hiibox.houseshelter.core.GlobalUtil;
import com.hiibox.houseshelter.util.BackToExitUtil;
import com.hiibox.houseshelter.util.ImageOperation;
import com.hiibox.houseshelter.util.LocationUtil;
import com.hiibox.houseshelter.util.MessageUtil;
import com.hiibox.houseshelter.util.PreferenceUtil;
import com.hiibox.houseshelter.util.ScreenUtil;
import com.hiibox.houseshelter.util.StringUtil;
import com.hiibox.houseshelter.view.AdsDialog;

    
  
  
  
  
  
  
  
public class LoginActivity extends ShaerlocActivity {

    @ViewInject(id = R.id.root_layout) RelativeLayout rootLayout;
    @ViewInject(id = R.id.login_iv, click = "onClick") ImageView loginIV;
    @ViewInject(id = R.id.register_iv, click = "onClick") ImageView registerIV;
    @ViewInject(id = R.id.dial_to_find_password_tv, click = "onClick") TextView dialIV;
    @ViewInject(id = R.id.network_setting_tv, click = "onClick") TextView settingsIV;
    @ViewInject(id = R.id.phone_number_et) EditText phoneET;
    @ViewInject(id = R.id.password_et) EditText passwordET;
    @ViewInject(id = R.id.login_register_layout) LinearLayout loginRegisterLayout;
    @ViewInject(id = R.id.phone_layout) LinearLayout phoneLayout;
    @ViewInject(id = R.id.password_layout) LinearLayout passwordLayout;
    
    private AdsDialog adsDialog = null;
    private View splashView = null;
    private ViewPager pager = null;
                                      
    private BackToExitUtil exitPrompt = null;
    private String phone = "";
    private String password = "";
    private ProgressDialog loginDialog = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!PreferenceUtil.getInstance(mContext).getBoolean("showedAds", false)) {
            loadAdsDialog();
        }
        setContentView(R.layout.activity_login_layout);
        exitPrompt = new BackToExitUtil();
        loginDialog = new ProgressDialog(this);
        loginDialog.setCancelable(true);
        loginDialog.setCanceledOnTouchOutside(false);
    }
    
	@Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setLargeScreenParams();
                                                              
                                                                         
                                                            
    }
    
    @Override
    protected void onStop() {
        super.onStop();
                                                  
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(null);
    }
    
    private void loadAdsDialog() {
        AdsAdapter adapter = new AdsAdapter(this);
        splashView = getLayoutInflater().inflate(R.layout.advertisement_layout, null);
        pager = (ViewPager) splashView.findViewById(R.id.activity_poster_vp);
                                                                     
        adsDialog = new AdsDialog(this, pager, splashView);
        adsDialog.show();
        pager.setAdapter(adapter);
                                                            
                        
                                            
                                                                                                     
                                         
                
              
        PreferenceUtil.getInstance(mContext).saveBoolean("showedAds", true);
    }
    
    private void setLargeScreenParams() {
        int screenHeight = ScreenUtil.getScreenHeight(mActivity);
        if (screenHeight > 854 && screenHeight <= 1280) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
            params.topMargin = 50;
            loginRegisterLayout.setLayoutParams(params);
            
            LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
            phoneLayout.setLayoutParams(phoneParams);
            
            LinearLayout.LayoutParams passwordParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 90);
            passwordParams.topMargin = 30;
            passwordLayout.setLayoutParams(passwordParams);
        }
    }
    
    @SuppressWarnings("static-access")
    public void onClick(View v) {
        int vid = v.getId();
        Intent intent = new Intent();
        switch (vid) {
            case R.id.login_iv:
            	if (LocationUtil.checkNetWork(mContext).endsWith(GlobalUtil.NETWORK_NONE)) {
                	MessageUtil.alertMessage(mContext, R.string.sys_network_error);
                	startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
                	return;
                }
                phone = phoneET.getText().toString();
                if (StringUtil.isEmpty(phone)) {
                    setFocusable(phoneET, R.string.prompt_input_phone);
                    return;
                }
                password = passwordET.getText().toString();
                if (StringUtil.isEmpty(password)) {
                    setFocusable(passwordET, R.string.hint_input_password);
                    return;
                }
                
                Intent loginIntent = new Intent(mContext, LoginDialogActivity.class);
                loginIntent.putExtra("phone", phone);
                loginIntent.putExtra("password", password);
                startActivityForResult(loginIntent, 0x303);
                
                                                                        
                                   
                                                                    
                        
                                                     
                                                                                        
                                             
                                            
                    
                return;
            case R.id.register_iv:
                intent.setClass(this, RegisterActivity.class);
                break;
            case R.id.dial_to_find_password_tv:
                intent.setAction(intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:400-1080-580"));
                break;
            case R.id.network_setting_tv:
                intent.setClass(this, NetworkSettingsActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            pressAgainExit(); 
            return true; 
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @SuppressLint("ShowToast")
    private void pressAgainExit() {
        if (exitPrompt.isExit()) {
            MyApplication.showedAds = false;
            if (null != MyApplication.mainClient) {
            	MyApplication.mainClient.stop();
            	MyApplication.mainClient = null;
            }
            if (null != MyApplication.fileClient) {
            	MyApplication.fileClient.stop();
                MyApplication.fileClient = null;
            }
            MyApplication.tcpManager = null;
            MyApplication.phone = null;
            MyApplication.password = null;
            PreferenceUtil.getInstance(mContext).saveBoolean("showedAds", false);
            MianActivity.getScreenManager().exitAllActivityExceptOne();
        } else {
            Toast.makeText(this, getString(R.string.back_to_exit_app), Toast.LENGTH_SHORT).show();
            exitPrompt.doExitInOneSecond();
        }
    }
    
    public void setFocusable(EditText editText, int msg) {
        editText.setText("");
        editText.setFocusableInTouchMode(true);
        editText.setFocusable(true);
        editText.requestFocus();
        Editable editable = editText.getText();
        Selection.setSelection(editable, 0);
        MessageUtil.alertMessage(mContext, msg);
    }
    
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
	            case 0:
	            	MyApplication.phone = phone;
	            	MyApplication.password = password;
	                PreferenceUtil.getInstance(mContext).saveString("phone", phone);
	                PreferenceUtil.getInstance(mContext).saveString("password", password);
	            	break;
	            case 1:
	            	if (null != loginDialog && loginDialog.isShowing()) {
						loginDialog.dismiss();
					}
					MessageUtil.alertMessage(mContext, getString(R.string.login_failed)+msg.obj);
	            	break;
				case 2:
					loginDialog.setMessage(getString(R.string.user_auth));
					MyApplication.phone = phone;
	            	MyApplication.password = password;
	                PreferenceUtil.getInstance(mContext).saveString("phone", phone);
	                PreferenceUtil.getInstance(mContext).saveString("password", password);
					MyApplication.initTcpManager();
	                MyApplication.mainClient = MyApplication.tcpManager.getMainClient(phone, password, "1111111111111111", "66666666");
	                MyApplication.fileClient = MyApplication.tcpManager.getFileClient(phone);
	                startActivity(new Intent(LoginActivity.this, HomepageActivity.class));
	                if (null != loginDialog && loginDialog.isShowing()) {
                        loginDialog.dismiss();
                    }
	                MianActivity.getScreenManager().exitActivity(mActivity);
					break;
				case 3:
					if (null != loginDialog && loginDialog.isShowing()) {
						loginDialog.dismiss();
					}
					MessageUtil.alertMessage(mContext, R.string.receive_server_info_failed);
					break;
				case 4:
					if (null != loginDialog && loginDialog.isShowing()) {
						loginDialog.dismiss();
					}
					MessageUtil.alertMessage(mContext, R.string.network_error);
					break;
				case 5:
					if (null != loginDialog && loginDialog.isShowing()) {
						loginDialog.dismiss();
					}
					MessageUtil.alertMessage(mContext, R.string.network_not_response);
					break;
				case 6:
					if (null != loginDialog && loginDialog.isShowing()) {
						loginDialog.dismiss();
					}
					MessageUtil.alertMessage(mContext, R.string.network_timeout);
					break;
				default:
					break;
			}
        }
    };
    
}

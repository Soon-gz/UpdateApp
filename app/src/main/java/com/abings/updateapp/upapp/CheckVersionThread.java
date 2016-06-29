package com.abings.updateapp.upapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;

import com.abings.updateapp.AppUtils;
import com.abings.updateapp.NetworkUtil;
import com.abings.updateapp.R;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 检查版本
 *
 * @author zwj
 * @Date 2016-05-24
 */
public class CheckVersionThread extends Thread {
    /**
     * 读取服务器的版本号
     */
    public final String kVersion = "version";
    /**
     * 下载连接
     */
    public final String kUrl = "url";
    /**
     * 描述
     */
    public final String kDesc = "desc";

    private Context bContext;
    private Handler mHandler;
    /**
     * 升级
     */
    public final static int NEEDUPDATE = 1;
    /**
     * 已经最新
     */
    public final static int Newest = 2;
    private String bFilePath;//文件目录
    private String updateUrl;//升级地址

    private boolean isManual = false;
    private ProgressDialog mProgressDialog = null;

    @SuppressWarnings("unused")
    private CheckVersionThread() {
    }

    /**
     * @param bContext
     * @param bFilePath
     * @param updateUrl
     */
    public CheckVersionThread(Context bContext, String bFilePath, String updateUrl) {
        this.bContext = bContext;
        this.bFilePath = bFilePath;
        this.updateUrl = updateUrl;
        mHandler = new MHandler();
    }

    /**
     * @param bContext
     * @param bFilePath
     * @param updateUrl
     * @param isManual  是否手动申请查看最新版
     */
    public CheckVersionThread(Context bContext, String bFilePath, String updateUrl, boolean isManual) {
        this(bContext, bFilePath, updateUrl);
        this.isManual = isManual;
        if (isManual) {
            mProgressDialog = showProgressDialog(mProgressDialog, "提示", "正在获取最新版本信息", bContext);
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            String nowVersion = AppUtils.getVersionName(bContext);
            // 不需要在这里做自动升级判断
            if (!NetworkUtil.isNetworkConnected(bContext)) {
                // 不自动升级，无网络
                //sendHandler(BaseHandler.netNo);
                return;
            }

            Map<String, String> updateInfo = getUpdateInfo(updateUrl);
            if (updateInfo != null && updateInfo.isEmpty()) {
                /* 获取服务器上的最新版本信息失败 */
            } else {
                if (nowVersion.equals(updateInfo.get(kVersion))) {
                    // 不用显示更新的对话框
                    /* 没有升级信息，进入主界面 */
                    sendHandler(Newest, "已经是最新版本");
                } else {
                    // 显示更新的对话框
                    sendHandler(NEEDUPDATE, updateInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 获取服务器上的最新版本信息失败
//            sendHandler(BaseHandler.netUnkownErr);
        } finally {
            dismissProgressDialog(mProgressDialog);
        }
    }

    /**
     * 获取服务上的最新的版本信息
     *
     * @param path 路径
     * @return
     * @throws Exception
     */
    private Map<String, String> getUpdateInfo(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            return parserUpdateInfo(is);
        }
        return new HashMap<String, String>();
    }

    /**
     * 解析服务的流信息为UpdateInfo对象
     *
     * @param is
     * @return
     * @throws Exception
     */
    private Map<String, String> parserUpdateInfo(InputStream is) throws Exception {
        Map<String, String> map = null;
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("updateinfo".equals(parser.getName())) {
                        map = new HashMap<String, String>();
                    } else if (kVersion.equals(parser.getName())) {
                        String version = parser.nextText();
                        map.put(kVersion, version);
                    } else if (kUrl.equals(parser.getName())) {
                        String url = parser.nextText();
                        map.put(kUrl, url);
                    } else if (kDesc.equals(parser.getName())) {
                        String description = parser.nextText();
                        map.put(kDesc, description);
                    }
                    break;

                default:
                    break;
            }
            eventType = parser.next();
        }
        return (map != null && !map.isEmpty()) ? map : new HashMap<String, String>();
    }


    private void sendHandler(int iWhat, Object obj) {
        Message msg = Message.obtain();
        msg.what = iWhat;
        msg.obj = obj;
        this.mHandler.sendMessage(msg);
    }

    private void sendHandler(int iWhat) {
        Message msg = Message.obtain();
        msg.what = iWhat;
        this.mHandler.sendMessage(msg);
    }

    private class MHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Newest:
                    if (isManual) {
//                        DialogUtils.ShowMsgDialog(mContext, msg.obj.toString());
                    }
                    break;
                case NEEDUPDATE:
                    final Map<String, String> map = (Map<String, String>) msg.obj;

                    setNotification(map.get(kDesc), map.get(kUrl));
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }


    private void setNotification(String desc, String url) {
        setNotificationUpdate(bContext, desc, url);
    }

    /**
     * 升级的提醒
     *
     * @param mContext
     */
    public static void setNotificationUpdate(Context mContext, String desc, String url) {

        int NOTIFICATION_FLAG = 1;
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(mContext, UpAppDialogActivity.class);
        intent.putExtra("desc", desc);
        intent.putExtra("url", url);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        Notification notify = new Notification.Builder(mContext)
                .setSmallIcon(R.drawable.logo)
                .setTicker("新版本来了,请尽快升级哦！")
                .setContentTitle("升级提醒")
                .setContentText("新版本来了,请尽快升级哦！")
                .setContentIntent(pendingIntent).setNumber(1).getNotification();
        notify.defaults = Notification.DEFAULT_ALL;
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notify.flags |= Notification.FLAG_SHOW_LIGHTS;
        manager.notify(NOTIFICATION_FLAG, notify);
    }

    /**
     * @param mProgressDialog
     * @param title
     * @param msg
     * @param context
     * @return
     * @author zwj
     */
    private ProgressDialog showProgressDialog(ProgressDialog mProgressDialog, String title, String msg, Context context) {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mProgressDialog = null;
        }
        mProgressDialog = new ProgressDialog(context,
                ProgressDialog.THEME_HOLO_LIGHT);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle(title);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
        return mProgressDialog;
    }

    /**
     * 取消等待条
     *
     * @param mProgressDialog
     * @date 2014-6-19上午10:18:24
     */
    private void dismissProgressDialog(ProgressDialog mProgressDialog) {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}

package xwang.cordova.umeng.push;

import android.util.Log;

import com.umeng.message.ALIAS_TYPE;
import com.umeng.message.PushAgent;
import com.umeng.message.tag.TagManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class UmengPush extends CordovaPlugin {
    public static final int ALIAS_TYPE_SINA = 0;
    public static final int ALIAS_TYPE_TENCENT = 1;
    public static final int ALIAS_TYPE_QQ = 2;
    public static final int ALIAS_TYPE_WEIXIN = 3;
    public static final int ALIAS_TYPE_BAIDU = 4;
    public static final int ALIAS_TYPE_RENREN = 5;
    public static final int ALIAS_TYPE_KAIXIN = 6;
    public static final int ALIAS_TYPE_DOUBAN = 7;
    public static final int ALIAS_TYPE_FACEBOOK = 8;
    public static final int ALIAS_TYPE_TWITTER = 9;

    public static final String ERROR_INVALID_PARAMETERS = "参数错误";

    // public static final String TAG = "Cordova.Plugin.Push";
    // public static final String UMENGPUSHAPPID = "umengpushappid";
    // public static final String UMENGMESSAGESECRET = "umengmessagesecret";

    protected String appId;
    protected String appMessageSecret;
    protected PushAgent mPushAgent;

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        // this.appId = preferences.getString(UMENGPUSHAPPID, "");
        // this.appMessageSecret = preferences.getString(UMENGMESSAGESECRET, "");
        // Log.d(TAG, "appId: " + appId + "; appSecret: " + appMessageSecret);
        this.mPushAgent = PushAgent.getInstance(webView.getContext());
        this.mPushAgent.enable();
        this.mPushAgent.onAppStart();
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("addTag")) {
            return addTag(args, callbackContext);
        }
        else if (action.equals("removeTag")) {
            return removeTag(args, callbackContext);
        }
        else if (action.equals("getTags")) {
            return getTags(callbackContext);
        }
        else if (action.equals("removeAllTags")) {
            return removeAllTags(callbackContext);
        }
        else if (action.equals("addAlias")) {
            return addAlias(args, callbackContext);
        }
        else if (action.equals("setAlias")) {
            return setAlias(args, callbackContext);
        }
        else if (action.equals("removeAlias")) {
            return removeAlias(args, callbackContext);
        }
        return false;
    }

    protected boolean addTag(CordovaArgs args, final CallbackContext callbackContext) {
        final String tags;
        try {
            tags = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TagManager.Result result = mPushAgent.getTagManager().add(tags);
                    if (result.status == "success") {
                        callbackContext.success("添加成功");
                    }
                    else {
                        callbackContext.success(result.toString());
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    protected boolean removeTag(CordovaArgs args, final CallbackContext callbackContext) {
        final String tags;
        try {
            tags = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TagManager.Result result = mPushAgent.getTagManager().delete(tags);
                    if (result.status == "success") {
                        callbackContext.success("移除成功");
                    }
                    else {
                        callbackContext.success(result.toString());
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    protected boolean removeAllTags(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mPushAgent.getTagManager().reset();
                    callbackContext.success("重置成功");
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    protected boolean getTags(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    java.util.List<String> result = mPushAgent.getTagManager().list();
                    callbackContext.success(new JSONArray(result.toString()));
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    protected boolean addAlias(CordovaArgs args, final CallbackContext callbackContext) {
        final String alias;
        try {
            alias = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        Object obj;
        try {
            obj = args.get(1);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final String aliasType;
        if (obj instanceof String) {
            aliasType = (String) obj;
        } else if (obj instanceof Integer){
            aliasType = mapUMessageAliasType((Integer) obj);
        } else {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = mPushAgent.addAlias(alias, aliasType);
                    if (result) {
                        callbackContext.success("添加成功");
                    }
                    else {
                        callbackContext.success("添加失败");
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    protected boolean setAlias(CordovaArgs args, final CallbackContext callbackContext) {
        final String alias;
        try {
            alias = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        Object obj;
        try {
            obj = args.get(1);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final String aliasType;
        if (obj instanceof String) {
            aliasType = (String) obj;
        } else if (obj instanceof Integer){
            aliasType = mapUMessageAliasType((Integer) obj);
        } else {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = mPushAgent.addAlias(alias, aliasType);
                    if (result) {
                        callbackContext.success("添加成功");
                    }
                    else {
                        callbackContext.success("添加失败");
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    protected boolean removeAlias(CordovaArgs args, final CallbackContext callbackContext) {
        final String alias;
        try {
            alias = args.getString(0);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        Object obj;
        try {
            obj = args.get(1);
        } catch (JSONException e) {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        final String aliasType;
        if (obj instanceof String) {
            aliasType = (String) obj;
        } else if (obj instanceof Integer){
            aliasType = mapUMessageAliasType((Integer) obj);
        } else {
            callbackContext.error(ERROR_INVALID_PARAMETERS);
            return true;
        }

        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = mPushAgent.removeAlias(alias, aliasType);
                    if (result) {
                        callbackContext.success("移除成功");
                    }
                    else {
                        callbackContext.success("移除失败");
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });

        sendNoResultPluginResult(callbackContext);
        return true;
    }

    private String mapUMessageAliasType(int intType) {
        switch (intType) {
            case ALIAS_TYPE_SINA:
                return "SINA_WEIBO";
            case ALIAS_TYPE_TENCENT:
                return "TENCENT_WEIBO";
            case ALIAS_TYPE_QQ:
                return "QQ";
            case ALIAS_TYPE_WEIXIN:
                return "WEIXIN";
            case ALIAS_TYPE_BAIDU:
                return "BAIDU";
            case ALIAS_TYPE_RENREN:
                return "RENREN";
            case ALIAS_TYPE_KAIXIN:
                return "KAIXIN";
            case ALIAS_TYPE_DOUBAN:
                return "DOUBAN";
            case ALIAS_TYPE_FACEBOOK:
                return "FACEBOOK";
            case ALIAS_TYPE_TWITTER:
                return "TWITTER";
            default:
                return "UNKOWN";
        }
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }
}
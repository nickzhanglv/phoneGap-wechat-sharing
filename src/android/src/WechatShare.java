package com.share.wechatShare;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.travelers.together.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WechatShare extends CordovaPlugin {
  public CallbackContext callbackContext;

  private String title;
  private String summary;
  private String target_url;
  private String image_url;

  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    try {
      this.callbackContext = callbackContext;

      JSONObject jsonObject = args.getJSONObject(0);

      title = jsonObject.getString("title");
      summary = jsonObject.getString("summary");
      target_url = jsonObject.getString("target_url");
      image_url = jsonObject.getString("image_url");

      JSONObject result = new JSONObject();
      result.put("result", true);

      if (action.equals("moment")) {
        share(SendMessageToWX.Req.WXSceneTimeline, title, summary, target_url, image_url);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        return true;
      } else if (action.equals("session")) {
        share(SendMessageToWX.Req.WXSceneSession, title, summary, target_url, image_url);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        return true;
      } else if (action.equals("favorite")) {
        share(SendMessageToWX.Req.WXSceneFavorite, title, summary, target_url, image_url);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        return true;
      } else {
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
        return false;
      }
    } catch (JSONException e) {
      e.printStackTrace();
      Log.e("Protonet", "JSON Exception Plugin... :(");
    }
    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
    return false;
  }

  public void share(final int scene, final String title, final String summary, final String target_url, final String image_url) {
    final Context context = this.cordova.getActivity().getApplicationContext();
    IWXAPI api;
    String APP_ID = "wxb8587d398599a602";
    api = WXAPIFactory.createWXAPI(context, APP_ID, false);
    api.registerApp(APP_ID);
    WXWebpageObject webpage = new WXWebpageObject();
    webpage.webpageUrl = target_url;
    WXMediaMessage msg = new WXMediaMessage(webpage);
    msg.title = title;
    msg.description = summary;
    if (image_url == null||image_url.equals("")) {
      Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
      msg.thumbData = Util.bmpToByteArray(thumb, true);
    } else {
      msg.thumbData = Util.getHtmlByteArray(image_url);
    }
    SendMessageToWX.Req req = new SendMessageToWX.Req();
    req.transaction = buildTransaction("webpage");
    req.message = msg;
    req.scene = scene;
    api.sendReq(req);
  }

  private String buildTransaction(final String type) {
    return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
  }

}

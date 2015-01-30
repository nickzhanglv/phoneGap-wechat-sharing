package com.share.wechatShare;

import java.io.IOException;
import java.net.URL;

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
import android.widget.Toast;

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
  private static int scene = 0;

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      this.callbackContext = callbackContext;

      JSONObject jsonObject = args.getJSONObject(0);

      title = jsonObject.getString("title");
      summary = jsonObject.getString("summary");
      target_url = jsonObject.getString("target_url");
      image_url = jsonObject.getString("image_url");

      final JSONObject result = new JSONObject();
      result.put("result", true);
      final Context context = this.cordova.getActivity().getApplicationContext();
      Runnable runable = new Runnable() {
        @Override
        public void run() {
          IWXAPI api;
          String APP_ID = "wxb8587d398599a602";
          api = WXAPIFactory.createWXAPI(context, APP_ID, false);
          if (!api.isWXAppInstalled()) {

            cordova.getActivity().runOnUiThread(new Runnable() {
              public void run() {
                Toast.makeText(context, "您还未安装微信客户端", Toast.LENGTH_SHORT).show();
              }
            });
            return;
          }
          api.registerApp(APP_ID);
          WXWebpageObject webpage = new WXWebpageObject();
          webpage.webpageUrl = target_url;
          WXMediaMessage msg = new WXMediaMessage(webpage);
          msg.title = title;
          msg.description = summary;
          Bitmap thumb = null;
          if (image_url == null || image_url.equals("")) {
            thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
          } else {
            try {
              URL url = new URL(image_url);
              thumb = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          thumb = centerSquareScaleBitmap(thumb, 200);
          msg.setThumbImage(thumb);
          SendMessageToWX.Req req = new SendMessageToWX.Req();
          req.transaction = buildTransaction("webpage");
          req.message = msg;
          req.scene = scene;
          api.sendReq(req);
        }
      };

      if (action.equals("moment")) {
        scene = SendMessageToWX.Req.WXSceneTimeline;
        cordova.getThreadPool().execute(runable);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        return true;
      } else if (action.equals("session")) {
        scene = SendMessageToWX.Req.WXSceneSession;
        cordova.getThreadPool().execute(runable);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
        return true;
      } else if (action.equals("favorite")) {
        scene = SendMessageToWX.Req.WXSceneFavorite;
        cordova.getThreadPool().execute(runable);
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

  /**
   * 
   * @param bitmap
   *            原图
   * @param edgeLength
   *            希望得到的正方形部分的边长
   * @return 缩放截取正中部分后的位图。
   */
  public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
    if (null == bitmap || edgeLength <= 0) {
      return null;
    }

    Bitmap result = bitmap;
    int widthOrg = bitmap.getWidth();
    int heightOrg = bitmap.getHeight();

    if (widthOrg > edgeLength && heightOrg > edgeLength) {
      // 压缩到一个最小长度是edgeLength的bitmap
      int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
      int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
      int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
      Bitmap scaledBitmap;

      try {
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
      } catch (Exception e) {
        return null;
      }

      // 从图中截取正中间的正方形部分。
      int xTopLeft = (scaledWidth - edgeLength) / 2;
      int yTopLeft = (scaledHeight - edgeLength) / 2;

      try {
        result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
        scaledBitmap.recycle();
      } catch (Exception e) {
        return null;
      }
    }

    return result;
  }

  private String buildTransaction(final String type) {
    return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
  }

}

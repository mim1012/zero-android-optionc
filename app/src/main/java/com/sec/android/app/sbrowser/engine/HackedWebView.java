package com.sec.android.app.sbrowser.engine;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;

import com.sec.android.app.sbrowser.ActivityMCloud;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// 미사용. 참고용.
public class HackedWebView extends WebView {
    private static String mAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36",
            mTypes = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
            mLangs = "tr-TR,en-US;q=0.8";
    private static List<String[]> mParams = null;
    private static List<String> history = new ArrayList<String>();

    public HackedWebView(Context c){
        super(c);
//        setWebViewClient(new WebClient());
//        setWebChromeClient(new ChromeClient());
//        WebSettings s = getSettings();
//        s.setUserAgentString(mAgent);
//        accessToMethod(s,boolean.class,"setJavaScriptEnabled",true);
//        Class z = getFromName("android.webkit.WebSettings$ZoomDensity");
//        if(z != null) accessToMethod(s,z,"setDefaultZoom",getDeclaredField(z,"FAR"));
//        accessToMethod(s,boolean.class,"setBuiltInZoomControls",true);
//        accessToMethod(s,boolean.class,"setSupportZoom",true);
//        accessToMethod(s,boolean.class,"setDisplayZoomControls",false);
//        z = getFromName("android.webkit.WebSettings$RenderPriority");
//        if(z != null) accessToMethod(s,z,"setRenderPriority",getDeclaredField(z,"HIGH"));
//        accessToMethod(s,boolean.class,"setAllowFileAccess",true);
//        accessToMethod(s,boolean.class,"setDomStorageEnabled",true);
//        accessToMethod(s,boolean.class,"setDatabaseEnabled",true);
//        accessToMethod(s,String.class,"setDatabasePath",c.getCacheDir().toString().replace("cache","databases"));
//        accessToMethod(s,long.class,"setAppCacheMaxSize",1024*1024*8);
//        accessToMethod(s,String.class,"setAppCachePath",c.getCacheDir());
//        accessToMethod(s,boolean.class,"setAppCacheEnabled",true);
//        accessToMethod(s,boolean.class,"setUseWideViewPort",true);
    }

    public void setUserAgentString(String userAgent){
        mAgent = userAgent;
        getSettings().setUserAgentString(userAgent);
    }

    public void setAcceptTypes(String types){
        mTypes = types;
    }

    public void setAcceptLangs(String langs){
        mLangs = langs;
    }

    public void setCustomRequestParameters(List<String[]> params){
        mParams = params;
    }

    @Override
    public void loadUrl(String s){
        try {
            new WebkitHelper().execute(s);
        } catch (Exception | Error e){}
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders){
        loadUrl(url);
    }

    private void loadData(WebViewHolder h){
        if(h != null){
            stopLoading();
            h.p = createNavigatorInjector() + h.p;
            loadDataWithBaseURL(h.b,h.p,h.t,h.e,history.size() > 0 ? history.get(history.size() - 1) : "");
            history.add(h.f);
        }
    }

    private class WebkitHelper extends AsyncTask<String,String,WebViewHolder> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            ((Activity)getContext()).setTitle("Buffering...");
        }

        @Override
        protected WebViewHolder doInBackground(String[] p1){
            try {
                if(check(p1[0],true)){
                    if(p1[0].startsWith("error<>")){
                        String[] x = p1[0].split("<>");
                        return getWebError(x[1],x[2]);
                    } else {
                        while(true){
                            WebViewHolder wh = setConnection(p1[0]);
                            if(wh != null){
                                return wh;
                            } else if(rc >= mr){
                                rc = 0;
                                return getWebError(p1[0],"net::ERR_TOO_MANY_REDIRECTS");
                            }
                        }
                    }
                    //return getWebError(p1[0],"net::EER_GOOGLE_SHIT");
                }

            } catch(Exception | Error e){}
            return null;
        }

        @Override
        public void onPostExecute(WebViewHolder result){
            loadData(result);
        }

        int rc = 0,mr = 30;

        private WebViewHolder getWebError(String url, String err){
            WebViewHolder h = new WebViewHolder();
            h.b = "about:blank";
            h.t = "text/html";
            h.e = "UTF-8";
			/*String s = "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"/>";
			s += "<title>Webpage not available</title>\n<style type=\"text/css\">";
			s += "body { margin-top: 0px; padding-top: 0px; }\nh2 { margin-top: 5px; padding-top: 0px; }</style>\n<body>\n";
            s += "<img src=\"file:///android_asset/webkit/android-weberror.png\" align=\"top\" />";
            s += "<h2>Webpage not available</h2>";
            s += "<p>The webpage at <a href=\"%s\">%s</a> could not be loaded because:</p>".replaceAll("%s",url);
            s += "<p>%e</p></body></head></html>".replaceAll("%e",err);*/
            String s = "<html><head><style>";
            s += "html { background-color: lightgrey; padding: 16px; } body { padding: 24px; border-radius: 16px; box-shadow: 0px 0px 16px red; background-color: white; }";
            s += " h3, h5 { word-wrap: break-word; text-align: center; font-family: \"Lucida Console\", Monaco, monospace; }";
            s += " h5 { font-weight: 300; } ";
            s += " img { width: 100%; height: 15%; } </style>";
            s += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"/>";
            s += "<title>%t</title></head><body>".replace("%t","Hoop!");
            s += "<img src=\"data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIj8+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3J";
            s += "nLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgaGVpZ2h0PSI1MTJweCIgdmlld0JveD0iMCAw";
            s += "IDQ5NiA0OTYiIHdpZHRoPSI1MTJweCI+PGxpbmVhckdyYWRpZW50IGlkPSJhIiBncmFkaWVudFVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgeDE9I";
            s += "jI0OCIgeDI9IjI0OCIgeTE9IjQ5NiIgeTI9IjAiPjxzdG9wIG9mZnNldD0iMCIgc3RvcC1jb2xvcj0iIzlmMmZmZiIvPjxzdG9wIG9mZnNldD";
            s += "0iMSIgc3RvcC1jb2xvcj0iIzBiYjFkMyIvPjwvbGluZWFyR3JhZGllbnQ+PHBhdGggZD0ibTI0OCAyNTZjMTMuMjMwNDY5IDAgMjQtMTAuNzY";
            s += "5NTMxIDI0LTI0cy0xMC43Njk1MzEtMjQtMjQtMjQtMjQgMTAuNzY5NTMxLTI0IDI0IDEwLjc2OTUzMSAyNCAyNCAyNHptMC0zMmM0LjQwNjI1";
            s += "IDAgOCAzLjU4NTkzOCA4IDhzLTMuNTkzNzUgOC04IDgtOC0zLjU4NTkzOC04LTggMy41OTM3NS04IDgtOHptLTE2LjU1MDc4MS00Ny40MDYyN";
            s += "WMuNjEzMjgxIDguNjM2NzE5IDcuODg2NzE5IDE1LjQwNjI1IDE2LjU1MDc4MSAxNS40MDYyNXMxNS45Mzc1LTYuNzY5NTMxIDE2LjU1MDc4MS";
            s += "0xNS40MDYyNWw3LjM4NjcxOS0xMDMuNDU3MDMxYy4wNDY4NzUtLjU2MjUuMDYyNS0xLjEyODkwNy4wNjI1LTEuNjg3NSAwLTEyLjkzNzUtMTA";
            s += "uNTE5NTMxLTIzLjQ0OTIxOS0yMy40NDkyMTktMjMuNDQ5MjE5aC0xLjEwMTU2MmMtMTIuOTI5Njg4IDAtMjMuNDQ5MjE5IDEwLjUxMTcxOS0y";
            s += "My40NDkyMTkgMjMuNDQ5MjE5IDAgLjU1ODU5My4wMTU2MjUgMS4xMzY3MTkuMDU0Njg4IDEuNjcxODc1em0xNi0xMTIuNTkzNzVoMS4xMDE1N";
            s += "jJjNC4xMTMyODEgMCA3LjQ0OTIxOSAzLjM0Mzc1IDcuNDMzNTk0IDcuOTc2NTYybC03LjM5MDYyNSAxMDMuNDgwNDY5Yy0uMDQyOTY5LjYwNT";
            s += "Q2OS0xLjE0NDUzMS42MDU0NjktMS4xNzk2ODggMGwtNy40MTQwNjItMTA0LjAwNzgxMmMwLTQuMTA1NDY5IDMuMzM1OTM4LTcuNDQ5MjE5IDc";
            s += "uNDQ5MjE5LTcuNDQ5MjE5em0yMjQuNTUwNzgxIDE1MmgtODcuMDU0Njg4bC05NS4wNTg1OTMtMTkwLjExMzI4MWMtNy45ODQzNzUtMTUuOTY0";
            s += "ODQ0LTI0LjAzOTA2My0yNS44ODY3MTktNDEuODg2NzE5LTI1Ljg4NjcxOXMtMzMuOTAyMzQ0IDkuOTIxODc1LTQxLjg4NjcxOSAyNS44ODY3M";
            s += "TlsLTk1LjA1ODU5MyAxOTAuMTEzMjgxaC04Ny4wNTQ2ODhjLTEzLjIzMDQ2OSAwLTI0IDEwLjc2OTUzMS0yNCAyNHYxMTJjMCAxMy4yMzA0Nj";
            s += "kgMTAuNzY5NTMxIDI0IDI0IDI0aDI0LjUxOTUzMWw2LjkzNzUgMTA0aC0yMy40NTcwMzF2MTZoNDMydi0xNmgtMjMuNDQ5MjE5bDYuOTM3NS0";
            s += "xMDRoMjQuNTExNzE5YzEzLjIzMDQ2OSAwIDI0LTEwLjc2OTUzMSAyNC0yNHYtMTEyYzAtMTMuMjMwNDY5LTEwLjc2OTUzMS0yNC0yNC0yNHpt";
            s += "OCAyNHY0MC4zOTg0MzhsLTQ2LjA5NzY1Ni00OC4zOTg0MzhoMzguMDk3NjU2YzQuNDA2MjUgMCA4IDMuNTg1OTM4IDggOHptLTI1OS41NzQyM";
            s += "TktMjA2Ljk1MzEyNWM1LjI1MzkwNy0xMC41MTk1MzEgMTUuODIwMzEzLTE3LjA0Njg3NSAyNy41NzQyMTktMTcuMDQ2ODc1czIyLjMyMDMxMi";
            s += "A2LjUyNzM0NCAyNy41NzQyMTkgMTcuMDQ2ODc1bDk3LjE2Nzk2OSAxOTQuMzI4MTI1YzIuMTI4OTA2IDQuMjU3ODEyIDMuMjU3ODEyIDkuMDM";
            s += "xMjUgMy4yNTc4MTIgMTMuNzkyOTY5IDAgMTctMTMuODMyMDMxIDMwLjgzMjAzMS0zMC44MzIwMzEgMzAuODMyMDMxaC0xOTQuMzM1OTM4Yy0x";
            s += "NyAwLTMwLjgzMjAzMS0xMy44MzIwMzEtMzAuODMyMDMxLTMwLjgzMjAzMSAwLTQuNzYxNzE5IDEuMTI4OTA2LTkuNTI3MzQ0IDMuMjU3ODEyL";
            s += "TEzLjc5Mjk2OXptMTQ2LjQyOTY4OCAzMjYuOTUzMTI1LTY4LjU3NDIxOS03Mmg0Ni44ODY3MTljNy40ODA0NjkgMCAxNC41MjczNDMtMS44MD";
            s += "g1OTQgMjAuODAwNzgxLTQuOTM3NWw3My4yNjk1MzEgNzYuOTM3NXptMS42NjQwNjIgMTYgNi45Mzc1IDEwNGgtMjU0LjkwNjI1bDYuOTM3NS0";
            s += "xMDR6bS0yMTcuNjg3NS04OGgzMC44ODY3MTlsNjguNTcwMzEyIDcyaC03Mi4zODY3MThsLTEyMS45MDIzNDQtMTI4aDQ4Ljk4NDM3NWMtLjYw";
            s += "OTM3NSAzLjAyMzQzOC0uOTg0Mzc1IDYuMDg5ODQ0LS45ODQzNzUgOS4xNjc5NjkgMCAyNS44MjQyMTkgMjEuMDA3ODEyIDQ2LjgzMjAzMSA0N";
            s += "i44MzIwMzEgNDYuODMyMDMxem0xMjUuMzU5Mzc1IDAgNjguNTc4MTI1IDcyaC03Mi4zODY3MTlsLTY4LjU2NjQwNi03MnptLTI1Mi4xOTE0MD";
            s += "YtNTZoOS45MDIzNDRsMTIxLjkwNjI1IDEyOGgtNzIuMzgyODEzbC02Ny40MjU3ODEtNzAuODAwNzgxdi00OS4xOTkyMTljMC00LjQxNDA2MiA";
            s += "zLjU5Mzc1LTggOC04em0tOCAxMjB2LTM5LjYwMTU2Mmw0NS4zMzU5MzggNDcuNjAxNTYyaC0zNy4zMzU5MzhjLTQuNDA2MjUgMC04LTMuNTg1";
            s += "OTM4LTgtOHptNDguNTUwNzgxIDI0aDQ2LjkwNjI1bC02LjkzNzUgMTA0aC0zMy4wMzkwNjJ6bTM1OS45Njg3NSAxMDRoLTMzLjAzMTI1bC02L";
            s += "jkzNzUtMTA0aDQ2LjkwNjI1em00Ny40ODA0NjktMTIwaC0xMC42NjQwNjJsLTgyLjM3NS04Ni40ODgyODFjOC4wNTQ2ODctOC40MTQwNjMgMT";
            s += "MuMDM5MDYyLTE5LjgwMDc4MSAxMy4wMzkwNjItMzIuMzQzNzUgMC0zLjA3ODEyNS0uMzc1LTYuMTQ0NTMxLS45NzY1NjItOS4xNjc5NjloMjA";
            s += "uNzkyOTY4bDY4LjE4MzU5NCA3MS42MDE1NjJ2NDguMzk4NDM4YzAgNC40MTQwNjItMy41OTM3NSA4LTggOHptOCAxMjBoMTZ2MTZoLTE2em0t";
            s += "NDgwIDBoMTZ2MTZoLTE2em0wIDAiIGZpbGw9InVybCgjYSkiLz48L3N2Zz4K\" />\n";
            s += "<h3>%t</h3>\n<h5><a href=\"%d\">%d</a> %u</h5></body></html>".
                    replace("%t","Web sayfası yüklenemedi").
                    replace("%d",url).
                    replace("%u","adresine giriş <b>%e</b> yüzünden başarısız oldu").
                    replace("%e",err);
            h.p = s;
            return h;
        }

        private WebViewHolder setConnection(String url) throws IOException {
            URL u = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
            uc.setDoInput(true);
            uc.setRequestProperty("User-Agent",mAgent);
            uc.setRequestProperty("Accept",mTypes);
            uc.setRequestProperty("Accept-Language",mLangs);
            //uc.setRequestProperty("X-Frame-Options","");
            uc.setConnectTimeout(Integer.MAX_VALUE);
            uc.setReadTimeout(Integer.MAX_VALUE);
            if(mParams != null){
                for(String[] p : mParams){
                    uc.setRequestProperty(p[0],p[1]);
                }
            }
            uc.setFollowRedirects(true);
            uc.connect();
            switch(uc.getResponseCode()){
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    if(rc < mr){
                        uc.disconnect();
                        u = new URL(u, URLDecoder.decode(uc.getHeaderField("Location")));
                        rc++;
                        return setConnection(u.toExternalForm());
                    } else {
                        return null;
                    }
                default:
                    Scanner sc = new Scanner(uc.getInputStream());
                    String s = "";
                    while(sc.hasNext()){
                        s += sc.nextLine()+"\n";
                    }
                    WebViewHolder h = new WebViewHolder();
                    h.e = uc.getContentEncoding();
                    h.t = uc.getContentType();
                    h.b = u.getProtocol()+"://"+u.getHost();
                    h.p = s;
                    h.f = url;
                    rc = 0;
                    return h;
            }
        }
    }

    private class WebViewHolder {
        String e,t,p,b,f;
    }

    private static Object getDeclaredField(Object o, String field){
        try {
            Field f = null;
            f = o.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(o);
        } catch(Exception | Error e){
            Log.e(ActivityMCloud.class.getName(),e.getMessage());
            return null;
        }
    }

    private static void accessToMethod(Object o, Class arg, String name, Object v){
        try {
            Method m = null;
            if(arg != null) m = o.getClass().getMethod(name, arg);
            else m = o.getClass().getMethod(name);
            m.setAccessible(true);
            if(arg != null) m.invoke(o,v);
            else m.invoke(o);
        } catch(Exception | Error e){
            Log.e(ActivityMCloud.class.getName(),e.getMessage());
        }
    }

    private static Class getFromName(String name){
        try {
            return Class.forName(name);
        } catch(Exception | Error e){
            Log.e(ActivityMCloud.class.getName(),e.getMessage());
            return null;
        }
    }

    private class WebClient extends WebViewClient {

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
            if(failingUrl == history.get(history.size()-1)){
                loadUrl("error<>"+failingUrl+"<>"+description);
            }
            //goBack();
			/*if(check(failingUrl,true)){
				loadUrl("error::"+failingUrl+"::"+description);
			} else {*/
            //super.onReceivedError(view,errorCode,description,failingUrl);
            //}
        }

//        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
//            onReceivedError(view,error.getErrorCode(),error.getDescription().toString(),request.getUrl().toString());
//        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            if(check(url,false)){
                super.onPageStarted(view,url,favicon);
            } else {
                stopLoading();
            }
        }

        @Override
        public void onLoadResource(WebView view, String url){
            if(check(url,true)){
                super.onLoadResource(view,url);
                Log.d("LoadRes: ACCEPTED - "+history.get(history.size()-1),url);
            } else {
                Log.d("LoadRes: REJECTED - "+history.get(history.size()-1),url);
            }
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url){
            if(check(url,false)){
                loadUrl(url);
            }
            return true;
        }

        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
            return shouldOverrideUrlLoading(view,request.getUrl().toString());
        }

    }


    String[] bannedRes = {"1e100.net","doubleclick.net","accounts.google.com/",
            "www.googleadservices.com/","googlesyndication.com/",
            "/adservice.google.","/id.google.","fonts.googleapis.com/",
            "/pagead/","gstatic.com/","ggpht.com/","google-analytics.com/",
            "l.google.com/","el=adunit","video_masthead","adformat=",
            "ad_block=1","yt_ad=1","key=yt","embed-player.js","adnohost",
            "player-webp","remote.js","log_event?","player-sprite-mode",
            "player_ias","player_remote_ux","widgetapi.js","browse_ajax",
            "service_ajax","endscreen.js","annotations_module.js",
            "JOYx3W5PzoQFRZAOzhrT8YIZJDIx1URDMPi7CeVLUwM.js","videomasthead",
            "/stats/?qoe","clients1.google.com","clients2.google.com",
            "clients3.google.com"};

    public boolean checkRes(String url){
        for(String s : bannedRes){
            if(url.contains(s)){
                return false;
            }
        }
        return true;
    }

    public boolean check(String url,boolean res){
        if(checkRes(url)){
            return true;
        }
        if(!res){
            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setTitle("Hoop! Ufak bir sorun var");
            b.setMessage("Bazı Google sitelerine girmeyi deneseniz bile bu modifiye edilmiş webkitle başarısız olacaksınız. Bu yüzden bu webkitle "+url+" adresine bağlanmayı aklınızdan bile geçirmeyin. Bu uyarıyı görmek istemiyorsanız ve çöplerinizle mutluysanız lütfen bu webkiti kullanmak yerine standart webkiti kullanın.");
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface p1, int p2){
                    p1.cancel();
                }
            });
            b.create().show();
        }
        return false;
    }

    private String createNavigatorInjector(){
        String s = "<script>\n";
        s += defineGetter("vendor", "Unknown Company");
        s += defineGetter("maxTouchPoints",-1);
        s += defineGetter("hardwareConcurrency",-1);
        s += defineGetter("appVersion",mAgent.substring(8,mAgent.length()));
        s += defineGetter("platform","Linux x86_64");
        s += defineGetter("userAgent",mAgent);
        s += defineGetter("language",mLangs.split(",")[0]);
        s += defineGetter("languages",mLangs.split(";")[0]);
        s += defineGetter("onLine",true);
        s += defineGetter("doNotTrack",true);
        s += "</script>";
        return s;
    }

    private String defineGetter(String key, Object value){
        return "navigator.__defineGetter__('"+key+"', function(){ return '"+value+"' });\n";
    }

    private class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress){
            super.onProgressChanged(view, newProgress);
            Activity a = (Activity) getContext();
            PackageManager p = a.getPackageManager();
            if(newProgress > 98){
                try {
                    a.setTitle(p.getApplicationInfo(a.getPackageName(), 0).loadLabel(p));
                } catch(PackageManager.NameNotFoundException e){}
            } else {
                a.setTitle("Progress: "+newProgress);
            }
        }
		/*@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceID){
			Log.e(getClass().getName(),message+" on "+lineNumber+" "+sourceID);
			Toast.makeText(getContext(),message+" on "+lineNumber+" "+sourceID,Toast.LENGTH_LONG).show();
			super.onConsoleMessage(message, lineNumber, sourceID);
		}*/
    }

    @Override
    public boolean canGoBack(){
        return history.size() > 1;
    }

    @Override
    public void goBack(){
        loadUrl(history.get(history.size()-2));
        history.remove(history.size()-1);
    }

    public boolean handleBackButton(){
        if(canGoBack()){
            goBack();
            return true;
        }
        return false;
    }
}

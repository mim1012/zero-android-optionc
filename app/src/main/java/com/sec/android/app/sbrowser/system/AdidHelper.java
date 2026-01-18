package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

public class AdidHelper {

    private Context _context = null;
    private Handler.Callback _callbalk = null;

//    public void getAdid(Context context, Handler.Callback callback) {
//        new AdidTask().execute();
//    }
//
//    private class AdidTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected String doInBackground(Void... voids) {
//            AdvertisingIdClient.Info adInfo = null;
//            String advertId = null;
//
//            try {
//                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(_context);
//            } catch (GooglePlayServicesNotAvailableException e) {
//                e.printStackTrace();
//            } catch (GooglePlayServicesRepairableException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            try {
//                advertId = adInfo.getId();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return advertId;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            sendUpdateAd(s);
//        }
//    }

}

package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.net.ProxyInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class WifiProxyController {

    private static final String TAG = WifiProxyController.class.getSimpleName();

    public static void setWifiProxy(Context context, String host, int port) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setWifiProxySettings(context, host, port);
        } else {
            setWifiProxySettings5(context, host, port);
        }
    }

    public static void unsetWifiProxy(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            unsetWifiProxySettings(context);
        } else {
            unsetWifiProxySettings5(context);
        }
    }

    public static String getWifiProxy(Context context) {
        String proxyAddress = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            String ip = android.net.Proxy.getHost(context);

            if (ip != null && ip.length() > 0) {
                proxyAddress = ip + ":" + android.net.Proxy.getPort(context);
            }
        } else {
            String ip = System.getProperty("http.proxyHost");

            if (ip != null && ip.length() > 0) {
                proxyAddress = ip + ":" + System.getProperty("http.proxyPort");
            }
        }

        return proxyAddress;
    }

    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    private static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    private static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    private static void setProxySettings(String assign , WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "proxySettings");
    }


    private static WifiConfiguration GetCurrentWifiConfiguration(WifiManager manager) {
        if (!manager.isWifiEnabled())
            return null;

        List<WifiConfiguration> configurationList = manager.getConfiguredNetworks();
        WifiConfiguration configuration = null;
        int cur = manager.getConnectionInfo().getNetworkId();
        for (int i = 0; i < configurationList.size(); ++i) {
            WifiConfiguration wifiConfiguration = configurationList.get(i);
            if (wifiConfiguration.networkId == cur)
                configuration = wifiConfiguration;
        }

        return configuration;
    }

    private static void setWifiProxySettings(Context context, String host, int port) {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            //get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //get ProxyProperties constructor
            Class[] proxyPropertiesCtorParamTypes = new Class[3];
            proxyPropertiesCtorParamTypes[0] = String.class;
            proxyPropertiesCtorParamTypes[1] = int.class;
            proxyPropertiesCtorParamTypes[2] = String.class;

            Constructor proxyPropertiesCtor = proxyPropertiesClass.getConstructor(proxyPropertiesCtorParamTypes);

            //create the parameters for the constructor
            Object[] proxyPropertiesCtorParams = new Object[3];
            proxyPropertiesCtorParams[0] = host;
            proxyPropertiesCtorParams[1] = port;
            proxyPropertiesCtorParams[2] = null;

            //create a new object using the params
            Object proxySettings = proxyPropertiesCtor.newInstance(proxyPropertiesCtorParams);

            //pass the new object to setHttpProxy
            Object[] params = new Object[1];
            params[0] = proxySettings;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("STATIC", config);

            //save the settings
            updateWifiConfig(context, manager, config);
            Log.d(TAG, "setWifiProxySettings success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unsetWifiProxySettings(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            //get the link properties from the wifi configuration
            Object linkProperties = getField(config, "linkProperties");
            if (null == linkProperties)
                return;

            //get the setHttpProxy method for LinkProperties
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyPropertiesClass;
            Class lpClass = Class.forName("android.net.LinkProperties");
            Method setHttpProxy = lpClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //pass null as the proxy
            Object[] params = new Object[1];
            params[0] = null;
            setHttpProxy.invoke(linkProperties, params);

            setProxySettings("NONE", config);

            //save the config
            updateWifiConfig(context, manager, config);
            Log.d(TAG, "unsetWifiProxySettings success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setWifiProxySettings5(Context context, String host, int port)
    {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            //linkProperties is no longer in WifiConfiguration
            Class proxyInfoClass = Class.forName("android.net.ProxyInfo");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyInfoClass;
            Class wifiConfigClass = Class.forName("android.net.wifi.WifiConfiguration");
            Method setHttpProxy = wifiConfigClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //Method 1 to get the ENUM ProxySettings in IpConfiguration
            Class ipConfigClass = Class.forName("android.net.IpConfiguration");
            Field f = ipConfigClass.getField("proxySettings");
            Class proxySettingsClass = f.getType();

            //Method 2 to get the ENUM ProxySettings in IpConfiguration
            //Note the $ between the class and ENUM
            //Class proxySettingsClass = Class.forName("android.net.IpConfiguration$ProxySettings");

            Class[] setProxySettingsParams = new Class[1];
            setProxySettingsParams[0] = proxySettingsClass;
            Method setProxySettings = wifiConfigClass.getDeclaredMethod("setProxySettings", setProxySettingsParams);
            setProxySettings.setAccessible(true);


            ProxyInfo pi = ProxyInfo.buildDirectProxy(host, port);
            //Android 5 supports a PAC file
            //ENUM value is "PAC"
            //ProxyInfo pacInfo = ProxyInfo.buildPacProxy(Uri.parse("http://localhost/pac"));

            //pass the new object to setHttpProxy
            Object[] params_SetHttpProxy = new Object[1];
            params_SetHttpProxy[0] = pi;
            setHttpProxy.invoke(config, params_SetHttpProxy);

            //pass the enum to setProxySettings
            Object[] params_setProxySettings = new Object[1];
            params_setProxySettings[0] = Enum.valueOf((Class<Enum>) proxySettingsClass, "STATIC");
            setProxySettings.invoke(config, params_setProxySettings);

            //save the settings
            updateWifiConfig(context, manager, config);
            Log.d(TAG, "setWifiProxySettings5 success!");
        } catch (Exception e) {
            Log.v("wifiProxy", e.toString());
            e.printStackTrace();
        }
    }

    private static void unsetWifiProxySettings5(Context context)
    {
        //get the current wifi configuration
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration config = GetCurrentWifiConfiguration(manager);
        if (null == config)
            return;

        try {
            //linkProperties is no longer in WifiConfiguration
            Class proxyInfoClass = Class.forName("android.net.ProxyInfo");
            Class[] setHttpProxyParams = new Class[1];
            setHttpProxyParams[0] = proxyInfoClass;
            Class wifiConfigClass = Class.forName("android.net.wifi.WifiConfiguration");
            Method setHttpProxy = wifiConfigClass.getDeclaredMethod("setHttpProxy", setHttpProxyParams);
            setHttpProxy.setAccessible(true);

            //Method 1 to get the ENUM ProxySettings in IpConfiguration
            Class ipConfigClass = Class.forName("android.net.IpConfiguration");
            Field f = ipConfigClass.getField("proxySettings");
            Class proxySettingsClass = f.getType();

            //Method 2 to get the ENUM ProxySettings in IpConfiguration
            //Note the $ between the class and ENUM
            //Class proxySettingsClass = Class.forName("android.net.IpConfiguration$ProxySettings");

            Class[] setProxySettingsParams = new Class[1];
            setProxySettingsParams[0] = proxySettingsClass;
            Method setProxySettings = wifiConfigClass.getDeclaredMethod("setProxySettings", setProxySettingsParams);
            setProxySettings.setAccessible(true);


            //pass the new object to setHttpProxy
            Object[] params_SetHttpProxy = new Object[1];
            params_SetHttpProxy[0] = null;
            setHttpProxy.invoke(config, params_SetHttpProxy);

            //pass the enum to setProxySettings
            Object[] params_setProxySettings = new Object[1];
            params_setProxySettings[0] = Enum.valueOf((Class<Enum>) proxySettingsClass, "NONE");
            setProxySettings.invoke(config, params_setProxySettings);

            //save the settings
            updateWifiConfig(context, manager, config);
            Log.d(TAG, "unsetWifiProxySettings5 success!");
        } catch (Exception e) {
            Log.v("wifiProxy", e.toString());
            e.printStackTrace();
        }
    }

    private static void updateWifiConfig(Context context, WifiManager manager, WifiConfiguration config) {
        manager.updateNetwork(config);
        // 연결이 완벽하게 끊어지지 않을때가 잦으므로 wifi on/off 방식으로 변경.
//        manager.disconnect();
        manager.setWifiEnabled(false);
//        manager.setWifiEnabled(true);
//        manager.reconnect();
    }
}

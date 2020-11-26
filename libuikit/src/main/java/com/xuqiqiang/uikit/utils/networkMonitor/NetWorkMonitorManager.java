package com.xuqiqiang.uikit.utils.networkMonitor;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import com.xuqiqiang.uikit.utils.DeviceUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NetWorkMonitorManager {
    public static final String TAG = "NetWorkMonitor >>> : ";
    private static final String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static NetWorkMonitorManager ourInstance;
    /**
     * 存储接受网络状态变化消息的方法的map
     */
    private Map<Object, NetWorkStateReceiverMethod> netWorkStateChangedMethodMap = new HashMap<>();
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ANDROID_NET_CHANGE_ACTION)) {
                //网络发生变化 没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
                int netType = DeviceUtils.getAPNType(context);
                NetWorkState netWorkState = NetWorkState.NONE;
                switch (netType) {
                    case 0://None
                        netWorkState = NetWorkState.NONE;
                        break;
                    case 1://Wifi
                        netWorkState = NetWorkState.WIFI;
                        break;
                    default://GPRS
                        netWorkState = NetWorkState.GPRS;
                        break;
                }
                postNetState(netWorkState);
            }
        }
    };
    private Application application;

    private NetWorkMonitorManager() {
    }

    public static NetWorkMonitorManager getInstance() {
        synchronized (NetWorkMonitorManager.class) {
            if (ourInstance == null) {
                ourInstance = new NetWorkMonitorManager();
            }
        }
        return ourInstance;
    }

    /**
     * 初始化 传入application
     *
     * @param application
     */
    public void init(Application application) {
        if (application == null) {
            throw new NullPointerException("application can not be null");
        }
        this.application = application;
        initMonitor();
    }

    /**
     * 初始化网络监听 根据不同版本做不同的处理
     */
    private void initMonitor() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.application.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//API 大于26时
            connectivityManager.registerDefaultNetworkCallback(initNetworkCallback());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//API 大于21时
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            NetworkRequest request = builder.build();
            connectivityManager.registerNetworkCallback(request, initNetworkCallback());
        } else {//低版本
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ANDROID_NET_CHANGE_ACTION);
            this.application.registerReceiver(receiver, intentFilter);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ConnectivityManager.NetworkCallback initNetworkCallback() {
        return new ConnectivityManager.NetworkCallback() {
            /**
             * 网络可用的回调连接成功
             */
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                int netType = DeviceUtils.getAPNType(NetWorkMonitorManager.this.application);
                NetWorkState netWorkState = NetWorkState.NONE;
                switch (netType) {
                    case 0://None
                        netWorkState = NetWorkState.NONE;
                        break;
                    case 1://Wifi
                        netWorkState = NetWorkState.WIFI;
                        break;
                    default://GPRS
                        netWorkState = NetWorkState.GPRS;
                        break;
                }
                postNetState(netWorkState);
            }

            /**
             * 网络不可用时调用和onAvailable成对出现
             */
            @Override
            public void onLost(Network network) {
                super.onLost(network);
                postNetState(NetWorkState.NONE);
            }

            /**
             * 在网络连接正常的情况下，丢失数据会有回调 即将断开时
             */
            @Override
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
            }

            /**
             * 网络功能更改 满足需求时调用
             * @param network
             * @param networkCapabilities
             */
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
            }

            /**
             * 网络连接属性修改时调用
             * @param network
             * @param linkProperties
             */
            @Override
            public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties);
            }

            /**
             * 网络缺失network时调用
             */
            @Override
            public void onUnavailable() {
                super.onUnavailable();
            }
        };
    }

    /**
     * 反注册广播
     */
    private void onDestroy() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.application.unregisterReceiver(receiver);
        }
    }

    /**
     * 注入
     *
     * @param object
     */
    public void register(Object object) {
        if (this.application == null) {
            throw new NullPointerException("application can not be null,please call the method init(Application application) to add the Application");
        }
        if (object != null) {
            NetWorkStateReceiverMethod netWorkStateReceiverMethod = findMethod(object);
            if (netWorkStateReceiverMethod != null) {
                netWorkStateChangedMethodMap.put(object, netWorkStateReceiverMethod);
            }
        }
    }

    /**
     * 删除
     *
     * @param object
     */
    public void unregister(Object object) {
        if (object != null && netWorkStateChangedMethodMap != null) {
            netWorkStateChangedMethodMap.remove(object);
        }
    }

    /**
     * 网络状态发生变化，需要去通知更改
     *
     * @param netWorkState
     */
    private void postNetState(NetWorkState netWorkState) {
        Set<Object> set = netWorkStateChangedMethodMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            NetWorkStateReceiverMethod netWorkStateReceiverMethod = netWorkStateChangedMethodMap.get(object);
            invokeMethod(netWorkStateReceiverMethod, netWorkState);

        }
    }

    /**
     * 具体执行方法
     *
     * @param netWorkStateReceiverMethod
     * @param netWorkState
     */
    private void invokeMethod(NetWorkStateReceiverMethod netWorkStateReceiverMethod, NetWorkState netWorkState) {
        if (netWorkStateReceiverMethod != null) {
            try {
                NetWorkState[] netWorkStates = netWorkStateReceiverMethod.getNetWorkState();
                for (NetWorkState myState : netWorkStates) {
                    if (myState == netWorkState) {
                        netWorkStateReceiverMethod.getMethod().invoke(netWorkStateReceiverMethod.getObject(), netWorkState);
                        return;
                    }
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 找到对应的方法
     *
     * @param object
     * @return
     */
    private NetWorkStateReceiverMethod findMethod(Object object) {
        NetWorkStateReceiverMethod targetMethod = null;
        if (object != null) {
            Class myClass = object.getClass();
            //获取所有的方法
            Method[] methods = myClass.getDeclaredMethods();
            for (Method method : methods) {
                //如果参数个数不是1个 直接忽略
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (method.getParameterCount() != 1) {
                        continue;
                    }
                }
                //获取方法参数
                Class[] parameters = method.getParameterTypes();
                if (parameters == null || parameters.length != 1) {
                    continue;
                }
                //参数的类型需要时NetWorkState类型
                if (parameters[0].getName().equals(NetWorkState.class.getName())) {
                    //是NetWorkState类型的参数
                    NetWorkMonitor netWorkMonitor = method.getAnnotation(NetWorkMonitor.class);
                    targetMethod = new NetWorkStateReceiverMethod();
                    //如果没有添加注解，默认就是所有网络状态变化都通知
                    if (netWorkMonitor != null) {
                        NetWorkState[] netWorkStates = netWorkMonitor.monitorFilter();
                        targetMethod.setNetWorkState(netWorkStates);
                    }
                    targetMethod.setMethod(method);
                    targetMethod.setObject(object);
                    //只添加第一个符合的方法
                    return targetMethod;
                }
            }
        }
        return targetMethod;
    }

}
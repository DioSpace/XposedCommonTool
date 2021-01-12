package com.my.xposedbasedemo;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Arrow implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 不是需要 Hook 的包直接返回
        if (!loadPackageParam.packageName.equals("com.my.judge_in_proxy")) //com.my.judge_in_proxy
            return;

        XposedBridge.log("app包名：" + loadPackageParam.packageName);

        //自己设置的代理地址
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.2", 8888));

        //OkHttp 强制走代理
        XposedHelpers.findAndHookMethod("okhttp3.OkHttpClient$Builder", // 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "proxy", // 被Hook函数的名称proxy
                java.net.Proxy.class, // 被Hook函数的第一个参数Proxy
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ================== okhttp3.OkHttpClient ===============");
                        XposedBridge.log("OkHttpClient$Builder  proxy : " + param.args[0]);
                        //将传入的参数  改成自己的代理
                        param.args[0] = proxy;
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.afterHookedMethod(param);
                    }
                });

        /*URL url = new URL(urlPath);
        connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);//发起网络请求,并且不走代理的写法*/
        //URL 强制走代理
        XposedHelpers.findAndHookMethod("java.net.URL",// 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "openConnection",// 被Hook函数的名称 openConnection
                java.net.Proxy.class, // 被Hook函数的第一个参数Proxy
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ============= java.net.URL ============");
                        XposedBridge.log("java.net.URL proxy : " + param.args[0]);
                        param.args[0] = proxy;
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        // TODO Auto-generated method stub
                        super.afterHookedMethod(param);
                    }
                });

        //绕过代理检测
        XposedHelpers.findAndHookMethod("java.lang.System",// 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "getProperty",// 被Hook函数的名称
                String.class, // 被Hook函数的第一个参数
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ============= java.lang.System  getProperty ============");
                        XposedBridge.log("java.lang.System  proxy  :  " + param.args[0]);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        // TODO Auto-generated method stub
                        super.afterHookedMethod(param);
                        // 修改方法的返回值
                        if (param.args[0].equals("http.proxyHost") || param.args[0].equals("https.proxyHost")
                                || param.args[0].equals("http.proxyPort") || param.args[0].equals("https.proxyPort")) {
                            param.setResult(null);
                        }
                    }
                });


        // 获取到待hook 工程内的类
        HookUtil.TargetClass = XposedHelpers.findClass("com.qiyi.Protect", loadPackageParam.classLoader);
        // 可以在合适的地方启动自己的方法,方法内部设定了定时循环程序可以一直运行(只要待hook的工程进程没死掉)
        myFunction();
        XposedHelpers.findAndHookMethod("com.qiyi.Protect", // 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "getQdsc",
                Object.class,
                String.class,
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.afterHookedMethod(param);
                        if (HookUtil.first_start) {
                            HookUtil.first_start = false;
                            HookUtil.hook_param = param;
                            // 启动附加程序
                            myFunction();
                        }

                        // 获取方法的返回值
                        Object result = param.getResult();
                        XposedBridge.log("返回值：" + result.toString());

                        /*
                         * 获取自己 在myFunction() 中调用 这个方法获取的返回值。设置HookUtil.isOn 的目的是为了避免待hook工程内部调用这个方法时的返回值。
                         * 这个方法在一定程序上是可以避开干扰结果，但由于是在自己调用这个方法前把isOn设成true的，在这段时间内有其他方法也调用了当前方
                         * 法也是不能避开的返回的干扰结果的。所以这个方法还严谨。
                         * */
                        if (HookUtil.isOn) {
                            HookUtil.isOn = false;
                            HookUtil.retValue = result;
                        }
                    }
                });
    }

    public void myFunction() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                XposedBridge.log("num = " + HookUtil.num++);
                if (HookUtil.hook_param != null) {
                    String str = "QC005=8BDCE78F4DD01D01F0C1005CA92DB85B1610424528962&agenttype=163&appVersion=8.10.0&app_lm=cn&app_version=8.10.0&area_code=86&checkExist=1&device_id=865166028633454&device_name=NX629J&device_type=NX629J&dfp=14e38190da38a244ad825a5c00f36c5722bf7b79595bced599a4a5099022098290&email=17683927317&envinfo=eyJhYSI6IjgwMDgxMDAwMCIsImJnIjoiODY1MTY2MDI4NjMzNDU0IiwiYmIiOiJhc3VzXC9hbmRyb2lkX3g4NlwveDg2OjUuMS4xXC9MWVoyOE5cL045NTAwWkhTMUJSQzI6dXNlclwvcmVsZWFzZS1rZXlzIiwiYWsiOiIiLCJxZiI6IjYuMCIsImZ1IjoiY29tLnFpeWkudmlkZW8uY2hpbGQiLCJrbCI6IjUwYmU1NDZiLWE3MjAtNGJhZS04ZmNhLWZhZDU4OWE3NzYxYiIsImdvIjoiOC4xMC4wIiwib2wiOiIiLCJhYiI6Ik9MRElGdStTVXAzUzdxMDlNQ3FcL3NrRENIMnNZbG5iemNaWTJIOFRXdHhIaXVUYk1pcnNJQVwvU0doMmcrUTArT0tkU09RV0U3R3lmQmtla3pMVWJrZWJSMzJ5dGw3SXo2Y3hcL2hDYlJXRHR4RmZsSHdJK0dwQVNEZUxzTW1QMndIcVNNbEpHZktFK0NvR1RGbHoySWpyUW9xYU81UTd3WHFoVnB0SXFDXC9mZ0pnQTQydlk1aGV5UnB5NmtVQnV6YVFOeDZvQzdOaTRqYjZFZWplSERISFZPbTVURk9FTDgwTDVkOFZhU1psK3Y1bGtya3BRUDdcL1gxa09UNTV4cExNWUE0RFdYdWVSVHNPUHNoWmxqeVVnMzJNUkZQdFN2aW1Bemh5a0FHZU1hc0RUV3MzSzllYk1CWVllWWtIbE5FTWV1NTVOUllENWttZmxhYVhNRGtLVlRQeEFEYlVMaTZvK1MwcERZUGJKb2hBNUczNlg3a3h6V2NROFFJbzl6ZUlyUEdUVlUwbWFcLzY1aHZjdU4xdXNxNytWVGZFWTZCck5LUUJRQ3dqaDM3YXhDTHJlVXhPTHhVWFJxTnI5bndTRFBsbHVsOUYwaWIrWUQwTWVYUHpYZ011UXNmMHJKXC9tZ1IzZkd1MlJDS05PVXN3cDI3bG1kZ3NDa05KM2h2T3UzQ0RUcDVIbDVmTGRLRTlhbHV3VmhcL285d3FpWFlqQUo0eEhDWTdOQ2MwN1hVWnRpckhRV3pPN3l2XC93MjkxYzRLb0oyQjdsQmxWZVIrV0xOVzJGaHl5ZEJWXC9oRUVEMkxrYmo3bDBycEhJZnpKVVwvU1wvUFlYbStEb0NwUUNZMmF4eGhqYTRDMCs0bTQ1eCtJRW5uT0ZqRnVwNFAxMHZoa3dRb1NzK1wvRCtsaFhFa0N3cDNGV0tNRm5XbTBoK3BNNmtuRVo3WXdtS0tqQjVTSHJcL010R1hZalBQeW45SEJRNWlyblFXUUFWckhmUEhyZzFLMWNFa0gxbTV5UjRlaUF4WEFsMGpmSzJJSXM0Q2ZxMW1YWnBXOWpiYk5tR0NrV2ZcL3lmUDYrWFhkb0RyaElIYU5ZXC9LSzFyT1I4UVEyM0ZiS2d3Yks1amtLV2k5SkEwcHF1TzRZSFpXYlwvNHpHd2FwQktQWlhucU5qY3pOY1hEdzUxZm5wMnQyXC9YZVg3UEN5WkduK0xnVmw5b1NvYzl4XC9IK2U5S1wvUURJZEUzbU9zZGpjZDZBc0NMNEgzMkc3ZVdvQTE1SmdveXJUOVNcL3gxZjNpSkxpWUEzZDFweGd1dTZHdmRaeEJVWTlaYkJSZ0l6Q2JOTzR1cXhnbHQwbVlGNkdTNktCTGxESDlPc29NeGY4YlR1b0FQOCs0Q2hhQmJ3MFNEMjFSeHZtd25Oc0thNmVlQjFrb284RXVqandIalFVSmkzU0R1MXhnNk45aFRxSnJ6Vk9UaHkrbFNqSnBjWklHSG9vbUV2cDA0d1lCM013NkZMS0JPNDZGZExHaWFISlpGaVlLUXZnSDY5MFVlRTdlREpDbldUNDRXTkk5ekl2TEVxb0tDVkh4VTZybzVyYW02RXlXYmtBMzdhOXd5QVwvd3U0MFl5cHFLM2VRelJWVjZ5U1g2Q1FQWUV5OVdreGNSUExiNEhDK2ZjYzJJV1NRZXdzV1JYK2VaZkZ6QUhmVFNcL29RQUI2ZnowaWUwVkQ3VndveE5vclVET1o5bkVYWXdrN2F6MHZBQjJRZEtqOFp1RW51YzRCdFl4RUtnVnRJMnArcjFKTFpEXC9mVkNHbEM0ZWRXbGRrdkJnV1lUdW5xNFwveUxcL3BpcHV0cGI1NmxxbXFqc0pIQmllQUtkRXhram9OV2MrazFPY3pycDhxbGhtSmZ3dEJcL2Q4bVUyXC9lRVJlZjc5VkJrdXFJZldHTW16eDdhOWNvcDhhTm5CSXB2NlF0Ymkxck9BVFdcLzNtUHFsRVwvT01UYXY3dzU1TFlGTDM5R1lodTh3U3lLWHdEdGNkQkZhTzBLeGlSSUEwQ1pvRGR4NU1IMTlvM2JFOFZ1RXRGdTJNcnI4UmNkdm52WUYrZXk2VEZGMVdrOTNCdHprU3AwXC9mMUZxSGdpUmpPM0sxazV4UGJYT2JhT2VKZ0JRUTluS1FJdU5iV0ViblV4TVRiMytjK2wxTGRcL1hVTVhFWGZWTnEyS3RER05FQVpuOWhMYWJMVVR3dkRIOGk3Q0pJaVgifQ==&fields=userinfo,qiyi_vip,qiyi_tennis_vip,fun_vip,sport_vip&fromSDK=21&hfvc=95&imei=865166028633454&lang=zh_CN&lat=39.922705&lon=116.416637&mac=8838f2d3125b387f011a2c66860f5f53&passwd=Keh12wcgX5kJ0qdTkT2xqpcP++TZrHtM5pay1kxBQrmidB4VTbsVVF0FCr9eAbSv3+B3ewsp2UhxG9N05Ez6ew==&ptid=02023031010000000000&qd_sg=865166028633454-8.10.0-1610424576674-&qyidv2=8BDCE78F4DD01D01F0C1005CA92DB85B&s2=other&s3=unknow&s4=unknow&slide=&slidetoken=&v=1&vcode=Txdb&verifyPhone=1&wsc_cc=&wsc_iip=&wsc_imei=865166028633454&wsc_isc=460001640623566&wsc_istr=865166028633454&wsc_ldt=NX629J&wsc_lgt=&wsc_ltt=&wsc_osl=zh&wsc_ost=14&wsc_sid=32305&wsc_sm=00-81-62-C6-02-2D&wsc_sp=&wsc_st=8.10.0&wsc_tt=02";
                    HookUtil.isOn = true;
                    // 调用类里的 静态方法
                    XposedHelpers.callStaticMethod(HookUtil.TargetClass, "getQdsc", HookUtil.hook_param.args[0], str);
                    // 获取对象 后调用对象里的方法
                    XposedHelpers.callMethod(HookUtil.hook_param.thisObject, "getQdsc", HookUtil.hook_param.args[0], HookUtil.hook_param.args[1]);
                }
            }
        }, 15 * 1000, 5 * 1000);
    }

}

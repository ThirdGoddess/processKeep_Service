<h1>Android进程保活·设置前台Service，提升App进程优先级</h1>
<h2>Android进程</h2>
<br><br><br>
首先你要知道Android中的进程以及它的优先级，下面来说明它进程<br>
<ol>
	<li><font color="red">前台进程 (Foreground process)</font></li>
	<li><font color="red">可见进程 (Visible process)<font color="red"></li>
	<li><font color="red">服务进程 (Service process)<font color="red"></li>
	<li><font color="red">后台进程 (Background process)<font color="red"></li>
	<li><font color="red">空进程 (Empty process)<font color="red"></li>
</ol>

下面进行解释：<br><br><br>
<h5><font color="red">前台进程(Foreground process):</font></h5>
用户当前操作所必需的进程。如果一个进程满足以下任一条件，即视为前台进程：

<ul>
	<li>托管用户正在交互的 Activity（已调用 Activity 的 onResume() 方法）</li>
	<li>托管某个 Service，后者绑定到用户正在交互的 Activity</li>
	<li>托管正在“前台”运行的 Service（服务已调用 startForeground()）</li>
	<li>托管正执行一个生命周期回调的 Service（onCreate()、onStart() 或 onDestroy()）</li>
	<li>托管正执行其 onReceive() 方法的 BroadcastReceiver</li>
</ul>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;通常，在任意给定时间前台进程都为数不多。只有在内存不足以支持它们同时继续运行这一万不得已的情况下，系统才会终止它们。 此时，设备往往已达到内存分页状态，因此需要终止一些前台进程来确保用户界面正常响应。<br><br><br>

<h5><font color="red">可见进程 (Visible process):</font></h5>
没有任何前台组件、但仍会影响用户在屏幕上所见内容的进程。 如果一个进程满足以下任一条件，即视为可见进程：
<ul>
	<li>托管不在前台、但仍对用户可见的 Activity（已调用其 onPause() 方法）。例如，如果前台 Activity 启动了一个对话框，允许在其后显示上一 Activity，则有可能会发生这种情况。</li>
	<li>托管绑定到可见（或前台）Activity 的 Service。</li>
</ul>

可见进程被视为是极其重要的进程，除非为了维持所有前台进程同时运行而必须终止，否则系统不会终止这些进程。<br><br><br>

<h5><font color="red">服务进程 (Service process):</font></h5>
正在运行已使用 startService() 方法启动的服务且不属于上述两个更高类别进程的进程。尽管服务进程与用户所见内容没有直接关联，但是它们通常在执行一些用户关心的操作（例如，在后台播放音乐或从网络下载数据）。因此，除非内存不足以维持所有前台进程和可见进程同时运行，否则系统会让服务进程保持运行状态。<br><br><br>

<h5><font color="red">后台进程 (Service process):</font></h5>
包含目前对用户不可见的 Activity 的进程（已调用 Activity 的 onStop() 方法）。这些进程对用户体验没有直接影响，系统可能随时终止它们，以回收内存供前台进程、可见进程或服务进程使用。 通常会有很多后台进程在运行，因此它们会保存在 LRU （最近最少使用）列表中，以确保包含用户最近查看的 Activity 的进程最后一个被终止。如果某个 Activity 正确实现了生命周期方法，并保存了其当前状态，则终止其进程不会对用户体验产生明显影响，因为当用户导航回该 Activity 时，Activity 会恢复其所有可见状态。<br><br><br>

<h5><font color="red">空进程 (Empty process):</font></h5>
不含任何活动应用组件的进程。保留这种进程的的唯一目的是用作缓存，以缩短下次在其中运行组件所需的启动时间。 为使总体系统资源在进程缓存和底层内核缓存之间保持平衡，系统往往会终止这些进程。<br><br><br>

<h5><font color="red">进程优先级:</font></h5>
首先空进程是最先被回收的，其次便是后台进程，依次往上，前台进程是最后才会被结束。<br><br><br>

<h2>Android进程保活</h2>
有很多种方法可以实现Android的进程保活，比如通过&nbsp;<font color="blue">1像素且透明Activity提升App进程优先级</font>、<font color="blue">通过设置前台Service提升App进程优先级</font>、<font color="blue">Java层的双进程拉活</font>、<font color="skyblue">JobScheduler实现</font>、<font color="skyblue">NDK双进程守护</font>、<font color="skyblue">使用账户同步拉活</font>、<font color="skyblue">workmanager实现</font>。<br><br>
下面这幅图，说明的是：
<ul>
	<li>红色部分是容易被回收的进程，属于android进程</li>
	<li>绿色部分是较难被回收的进程，属于android进程</li>
	<li>其他部分则不是android进程，也不会被系统回收，一般是ROM自带的app和服务才能拥有</li>
</ul>

![在asdf这里插入图片描述](https://img-blog.csdnimg.cn/2018122010382369.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQwODgxNjgw,size_16,color_FFFFFF,t_70)

本篇文章介绍的是进程第二种方式:
<ul>
	<li><font color="red">设置前台Service，提升App进程优先级</font></li>
</ul>

<h5><font color="red">设置前台Service，提升App进程优先级:</font></h5>

<br>首先创建ForegroundService.java继承自Service(android.app.Service):↓

这里要注意，不同的Android版本，所用的方式也就不同，并且不能显示通知栏，这里需要在onStartCommand中判断Android版本，选择不同的操作
```java
public class ForegroundService extends Service {

    private static final int SERVICE_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ForegroundServiceNew", "开启ForegroundService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ForegroundServiceNew", "销毁ForegroundService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //判断版本
        if (Build.VERSION.SDK_INT < 18) {//Android4.3以下版本

            //将Service设置为前台服务，可以取消通知栏消息
            startForeground(SERVICE_ID, new Notification());

        } else if (Build.VERSION.SDK_INT < 24) {//Android4.3 - 7.0之间
            //将Service设置为前台服务，可以取消通知栏消息
            startForeground(SERVICE_ID, new Notification());
            startService(new Intent(this, InnerService.class));

        } else {//Android 8.0以上
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel("channel","name",NotificationManager.IMPORTANCE_NONE);
                manager.createNotificationChannel(channel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"channel");

                //将Service设置为前台服务,Android 8.0 App启动不会弹出通知栏消息，退出后台会弹出通知消息
                //Android9.0启动时候会立刻弹出通知栏消息
                startForeground(SERVICE_ID,new Notification());
            }
        }

        return START_STICKY;
    }

    public static class InnerService extends Service {

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(SERVICE_ID, new Notification());
            stopForeground(true);//移除通知栏消息
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }

}

```
<br>
加入权限

```html
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```


<br>
最后在MainActivity启动服务就行:↓

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置前台Service，提升App进程优先级
        startService(new Intent(this,ForegroundService.class));
    }
}

```

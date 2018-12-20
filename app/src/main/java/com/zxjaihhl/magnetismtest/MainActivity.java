package com.zxjaihhl.magnetismtest;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.zxjaihhl.magnetismtest.widget.CustomCurveChart;
import com.zxjaihhl.magnetismtest.widget.LineTo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //指南针
    private LinearLayout customCurveChart1, curveChart1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private CompassManager mCompassManager;
    private String mOrientaionText[] = new String[]{"北", "东北", "东", "东南", "南", "西南", "西", "西北"};
    private TextView mAngleTextview;
    private TextView mOrientaionTextview;
    private CompassManager.CompassLister mCompassLister = new CompassManager.CompassLister() {
        @Override
        public void onOrientationChange(float orientation) {
            Log.e(TAG, "onOrientationChange: orientation " + orientation);
            mOrientaionTextview.setText(mOrientaionText[((int) (orientation + 22.5f) % 360) / 45]);
            int orientation_data = (int) orientation;
            int orientation_data_end = 0;
            if (orientation_data > 180 & orientation_data < 360) {
                orientation_data_end = orientation_data - 360;

            } else if (orientation_data <= 180 & orientation_data >= 0) {
                orientation_data_end = orientation_data;

            } else if (orientation_data == 360) {
                orientation_data_end = 0;
            }
            mAngleTextview.setText(orientation_data_end + "");
        }
    };
    private TextView tx_data_x, tx_data_y, tx_data_z, tx_max_x, tx_max_y, tx_max_z;
    private ImageView imageView_point;
    private String str1;
    private RelativeLayout lineTo;
    private LinearLayout linearLayout_data;
    private int width, height, data_x, data_y, data_z, count_max = 0, data_start, data_check, data_end, count_map = 0;
    private int data[] = new int[9];
    private int data_max_x[] = new int[10];
    private int data_max_y[] = new int[10];
    private int data_max_z[] = new int[10];
    private int data_map_x[] = new int[100];
    private int data_map_y[] = new int[100];
    private int data_map_z[] = new int[100];
    private ServerSocket_thread serversocket_thread;
    ServerSocket serverSocket;//创建ServerSocket对象
    Socket clicksSocket;//连接通道，创建Socket对象
    InputStream inputstream;//创建输入数据流

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        //读一下手机wifi状态下的ip地址
        Toast.makeText(MainActivity.this, getLocalIpAddress(), Toast.LENGTH_LONG).show();
        //获取屏幕的宽高
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        intiView();

        serversocket_thread = new ServerSocket_thread();
        serversocket_thread.start();

        //方向角
        mCompassManager = CompassManager.getInstance();
        mCompassManager.init(this);
        mCompassManager.addCompassLister(mCompassLister);


    }

    private void intiView() {
        lineTo = (RelativeLayout) findViewById(R.id.lineTo);
        mAngleTextview = (TextView) findViewById(R.id.angle_value_textview);
        mOrientaionTextview = (TextView) findViewById(R.id.orientation_value_textview);
        tx_max_x = (TextView) findViewById(R.id.tx_max_x);
        tx_max_y = (TextView) findViewById(R.id.tx_max_y);
        tx_max_z = (TextView) findViewById(R.id.tx_max_z);
        tx_data_x = (TextView) findViewById(R.id.tx_data_x);
        tx_data_y = (TextView) findViewById(R.id.tx_data_y);
        tx_data_z = (TextView) findViewById(R.id.tx_data_z);
        imageView_point = (ImageView) findViewById(R.id.point_target);
        customCurveChart1 = (LinearLayout) findViewById(R.id.customCurveChart1);
        //曲线高度布局
        curveChart1 = (LinearLayout) findViewById(R.id.curveChart1);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) curveChart1.getLayoutParams();
        params.height = height / 2 - 40;
        curveChart1.setLayoutParams(params);
        //滚动数据居中显示
        linearLayout_data= (LinearLayout) findViewById(R.id.linearLayout_data);
        RelativeLayout.LayoutParams params_linearLayout_data = (RelativeLayout.LayoutParams) linearLayout_data.getLayoutParams();
        params_linearLayout_data.width=width/2;
        params_linearLayout_data.height=height/2;
        linearLayout_data.setLayoutParams(params_linearLayout_data);
    }
    /**
     * 改变目标的坐标
     */
    private void intiPoint() {
        imageView_point.setPadding(((int) data_yy(data_y) - 75) * 2, 0, 0, ((int) data_xx(data_x) - 75) * 2);
        if ((int) data_yy(data_y) - 75 <= 0) {
            LineTo lineTo1 = new LineTo(this, width / 2, height / 2 - 15, width / 2, height / 2 - 25 - ((int) data_xx(data_x) - 75) + 10);

            lineTo.removeAllViews();

            lineTo.addView(lineTo1);
        } else if ((int) data_xx(data_x) - 75 <= 0) {
            LineTo lineTo1 = new LineTo(this, width / 2, height / 2 - 15, width / 2 + (int) data_yy(data_y) - 75, height / 2 - 25);

            lineTo.removeAllViews();

            lineTo.addView(lineTo1);
        } else {
            LineTo lineTo1 = new LineTo(this, width / 2, height / 2 - 15, width / 2 + (int) data_yy(data_y) - 75, height / 2 - 25 - ((int) data_xx(data_x) - 75) + 10);
            LineTo lineTo2 = new LineTo(this, width / 2, height / 2 - 15, width / 2 + (int) data_yy(data_y) - 75, height / 2 - 25 - ((int) data_xx(data_x) - 75) + 60);
            LineTo lineTo3 = new LineTo(this, width / 2 + (int) data_yy(data_y) - 75, height / 2 - 25 - ((int) data_xx(data_x) - 75) + 10, width / 2 +(int) data_yy(data_y) - 75, height / 2 - 25 - ((int) data_xx(data_x) - 75) + 60);
            lineTo.removeAllViews();
            lineTo.addView(lineTo1);
            lineTo.addView(lineTo2);
            lineTo.addView(lineTo3);
        }

        //曲线数据
        if (count_map >= 0 && count_map <= 99) {
            data_map_x[count_map] = data_x;
            data_map_y[count_map] = data_y;
            data_map_z[count_map] = data_z;
        } else {
            for (int i = 0; i < 99; i++) {
                data_map_x[i] = data_map_x[i + 1];
                data_map_y[i] = data_map_y[i + 1];
                data_map_z[i] = data_map_z[i + 1];
            }
            data_map_x[99] = data_x;
            data_map_y[99] = data_y;
            data_map_z[99] = data_z;
        }
        count_map++;
        if (count_map >= 100) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customCurveChart1.removeAllViews();
                    initCurveChart1();
                }
            });
        }
        if (count_max < 10 && count_max >= 0) {
            data_max_x[count_max] = data_x;
            data_max_y[count_max] = data_y;
            data_max_z[count_max] = data_z;
        }
        if (count_max > 1 && count_max < 10) {
            for (int i = 0; i < count_max; i++) {
                if (data_max_x[i + 1] < data_max_x[i]) {
                    data_max_x[i + 1] = data_max_x[i];
                }
            }
            for (int i = 0; i < count_max; i++) {
                if (data_max_y[i + 1] < data_max_y[i]) {
                    data_max_y[i + 1] = data_max_y[i];
                }
            }
            for (int i = 0; i < count_max; i++) {
                if (data_max_z[i + 1] < data_max_z[i]) {
                    data_max_z[i + 1] = data_max_z[i];
                }
            }
            tx_max_x.setText(Integer.toString(data_max_x[9]));
            tx_max_y.setText(Integer.toString(data_max_y[9]));
            tx_max_z.setText(Integer.toString(data_max_z[9]));
        }
        count_max++;
        if (count_max >= 10) {
            if (data_x > Integer.parseInt(tx_max_x.getText().toString())) {
                tx_data_x.setText(Integer.toString(data_x));
            } else {
                tx_data_x.setText("0");
            }
            if (data_y > Integer.parseInt(tx_max_y.getText().toString())) {
                tx_data_y.setText(Integer.toString(data_y));
            } else {
                tx_data_y.setText("0");
            }
            if (data_z > Integer.parseInt(tx_max_z.getText().toString())) {
                tx_data_z.setText(Integer.toString(data_z));
            } else {
                tx_data_z.setText("0");
            }
        }
    }
    /**
     * 服务器监听线程
     */
    class ServerSocket_thread extends Thread {
        public void run()//重写Thread的run方法
        {
            try {
                serverSocket = new ServerSocket(1314);//监听port端口，这个程序的通信端口就是port了
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            while (true) {
                try {
                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                    clicksSocket = serverSocket.accept();
                    inputstream = clicksSocket.getInputStream();
                    //启动接收线程
                    Receive_Thread receive_Thread = new Receive_Thread();
                    receive_Thread.start();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 接收线程
     */
    class Receive_Thread extends Thread//继承Thread
    {
        public void run()//重写run方法
        {
            while (true) {
                try {
                    for (int i = 0; i < 9; i++) {
                        data[i] = inputstream.read();
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            for (int j = 0; j < 9; j++) {
                                str1 = Integer.toString(data[j]);
                                System.out.println(str1);
                            }
                            if (data[0] == 250 && data[8] == 170) {
                                //整组数据检验
                                //x,y,z方向的值
                                data_x = data[2] * 256 + data[1];
                                data_y = data[4] * 256 + data[3];
                                data_z = data[6] * 256 + data[5];
                                if (data_x >= 0 && data_y >= 0 && data_z >= 0) {
                                    saveFile(Integer.toString(count_max) + " " + Integer.toString(data_x) + " " + Integer.toString(data_y) + " " + Integer.toString(data_z));
                                }
                                intiPoint();
                            }

                        }
                    });
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取WIFI下ip地址
     */
    private String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        //返回整型地址转换成“*.*.*.*”地址
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    /**
     * 数据处理
     */
    public float data_xx(int data_x) {
        String currentLat2 = String.valueOf(height / 2);
        BigDecimal b_x = new BigDecimal(currentLat2);
        b_x = b_x.divide(new BigDecimal(4096), 3, BigDecimal.ROUND_HALF_UP);
        float num_x = Float.parseFloat(b_x.toString());
        float x = height / 2 - num_x * data_x;
        return x;
    }

    public double data_yy(int data_y) {
        String currentLat2 = String.valueOf(width / 2);
        BigDecimal b_y = new BigDecimal(currentLat2);
        b_y = b_y.divide(new BigDecimal(4096), 3, BigDecimal.ROUND_HALF_UP);
        float num_y = Float.parseFloat(b_y.toString());
        double y = width / 2 - num_y * data_y;
        return y;
    }

    /**
     * *写入并保存x、z、y向量
     */
    public void saveFile(String str) {
        FileOutputStream fos = null;
        String state = Environment.getExternalStorageState();
        //获取SD卡状态
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            //判断SD卡是否就绪
            Toast.makeText(getBaseContext(), "请检查SD卡", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = Environment.getExternalStorageDirectory();
        //取得SD卡根目录
        try {
            Log.d("===SD卡根目录：", file.getCanonicalPath() + "sd.txt");
            fos = new FileOutputStream(file.getCanonicalPath() + "/sd.txt", true);
            fos.write(str.getBytes());
            fos.write("\r\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化曲线图数据
     */
    private void initCurveChart1() {
        String[] xLabel = {"1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1",
                "1", "1", "1", "1", "1", "1", "1", "1", "1", "1"};
        String[] yLabel = {"0", "400", "800", "1200", "1600", "2000", "2400", "2800", "3200", "3600", "4000", "4400"};
        int[] data1 = {data_map_x[0], data_map_x[1], data_map_x[2], data_map_x[3], data_map_x[4], data_map_x[5],
                data_map_x[6], data_map_x[7], data_map_x[8], data_map_x[9], data_map_x[10],
                data_map_x[11], data_map_x[12], data_map_x[13], data_map_x[14], data_map_x[15],
                data_map_x[16], data_map_x[17], data_map_x[18], data_map_x[19], data_map_x[20],
                data_map_x[21], data_map_x[22], data_map_x[23], data_map_x[24], data_map_x[25],
                data_map_x[26], data_map_x[27], data_map_x[28], data_map_x[29], data_map_x[30],
                data_map_x[31], data_map_x[32], data_map_x[33], data_map_x[34], data_map_x[35],
                data_map_x[36], data_map_x[37], data_map_x[38], data_map_x[39], data_map_x[40],
                data_map_x[41], data_map_x[42], data_map_x[43], data_map_x[44], data_map_x[45],
                data_map_x[46], data_map_x[47], data_map_x[48], data_map_x[49], data_map_x[50],
                data_map_x[51], data_map_x[52], data_map_x[53], data_map_x[54], data_map_x[55],
                data_map_x[56], data_map_x[57], data_map_x[58], data_map_x[59], data_map_x[60],
                data_map_x[61], data_map_x[62], data_map_x[63], data_map_x[64], data_map_x[65],
                data_map_x[66], data_map_x[67], data_map_x[68], data_map_x[69], data_map_x[70],
                data_map_x[71], data_map_x[72], data_map_x[73], data_map_x[74], data_map_x[75],
                data_map_x[76], data_map_x[77], data_map_x[78], data_map_x[79], data_map_x[80],
                data_map_x[81], data_map_x[82], data_map_x[83], data_map_x[84], data_map_x[85],
                data_map_x[86], data_map_x[87], data_map_x[88], data_map_x[89], data_map_x[90],
                data_map_x[91], data_map_x[92], data_map_x[93], data_map_x[94], data_map_x[95],
                data_map_x[96], data_map_x[97], data_map_x[98], data_map_x[99]};
        int[] data2 = {data_map_y[0], data_map_y[1], data_map_y[2], data_map_y[3], data_map_y[4], data_map_y[5],
                data_map_y[6], data_map_y[7], data_map_y[8], data_map_y[9], data_map_y[10],
                data_map_y[11], data_map_y[12], data_map_y[13], data_map_y[14], data_map_y[15],
                data_map_y[16], data_map_y[17], data_map_y[18], data_map_y[19], data_map_y[20],
                data_map_y[21], data_map_y[22], data_map_y[23], data_map_y[24], data_map_y[25],
                data_map_y[26], data_map_y[27], data_map_y[28], data_map_y[29], data_map_y[30],
                data_map_y[31], data_map_y[32], data_map_y[33], data_map_y[34], data_map_y[35],
                data_map_y[36], data_map_y[37], data_map_y[38], data_map_y[39], data_map_y[40],
                data_map_y[41], data_map_y[42], data_map_y[43], data_map_y[44], data_map_y[45],
                data_map_y[46], data_map_y[47], data_map_y[48], data_map_y[49], data_map_y[50],
                data_map_y[51], data_map_y[52], data_map_y[53], data_map_y[54], data_map_y[55],
                data_map_y[56], data_map_y[57], data_map_y[58], data_map_y[59], data_map_y[60],
                data_map_y[61], data_map_y[62], data_map_y[63], data_map_y[64], data_map_y[65],
                data_map_y[66], data_map_y[67], data_map_y[68], data_map_y[69], data_map_y[70],
                data_map_y[71], data_map_y[72], data_map_y[73], data_map_y[74], data_map_y[75],
                data_map_y[76], data_map_y[77], data_map_y[78], data_map_y[79], data_map_y[80],
                data_map_y[81], data_map_y[82], data_map_y[83], data_map_y[84], data_map_y[85],
                data_map_y[86], data_map_y[87], data_map_y[88], data_map_y[89], data_map_y[90],
                data_map_y[91], data_map_y[92], data_map_y[93], data_map_y[94], data_map_y[95],
                data_map_y[96], data_map_y[97], data_map_y[98], data_map_y[99]
        };
        int[] data3 = {data_map_z[0], data_map_z[1], data_map_z[2], data_map_z[3], data_map_z[4], data_map_z[5],
                data_map_z[6], data_map_z[7], data_map_z[8], data_map_z[9], data_map_z[10],
                data_map_z[11], data_map_z[12], data_map_z[13], data_map_z[14], data_map_z[15],
                data_map_z[16], data_map_z[17], data_map_z[18], data_map_z[19], data_map_z[20],
                data_map_z[21], data_map_z[22], data_map_z[23], data_map_z[24], data_map_z[25],
                data_map_z[26], data_map_z[27], data_map_z[28], data_map_z[29], data_map_z[30],
                data_map_z[31], data_map_z[32], data_map_z[33], data_map_z[34], data_map_z[35],
                data_map_z[36], data_map_z[37], data_map_z[38], data_map_z[39], data_map_z[40],
                data_map_z[41], data_map_z[42], data_map_z[43], data_map_z[44], data_map_z[45],
                data_map_z[46], data_map_z[47], data_map_z[48], data_map_z[49], data_map_z[50],
                data_map_z[51], data_map_z[52], data_map_z[53], data_map_z[54], data_map_z[55],
                data_map_z[56], data_map_z[57], data_map_z[58], data_map_z[59], data_map_z[60],
                data_map_z[61], data_map_z[62], data_map_z[63], data_map_z[64], data_map_z[65],
                data_map_z[66], data_map_z[67], data_map_z[68], data_map_z[69], data_map_z[70],
                data_map_z[71], data_map_z[72], data_map_z[73], data_map_z[74], data_map_z[75],
                data_map_z[76], data_map_z[77], data_map_z[78], data_map_z[79], data_map_z[80],
                data_map_z[81], data_map_z[82], data_map_z[83], data_map_z[84], data_map_z[85],
                data_map_z[86], data_map_z[87], data_map_z[88], data_map_z[89], data_map_z[90],
                data_map_z[91], data_map_z[92], data_map_z[93], data_map_z[94], data_map_z[95],
                data_map_z[96], data_map_z[97], data_map_z[98], data_map_z[99],
        };
        List<int[]> data = new ArrayList<>();
        List<Integer> color = new ArrayList<>();
        data.add(data1);
        color.add(R.color.color14);
        data.add(data2);
        color.add(R.color.color13);
        data.add(data3);
        color.add(R.color.color25);
        CustomCurveChart customCurveChart = new CustomCurveChart(this, xLabel, yLabel, data, color, false);
        customCurveChart1.addView(customCurveChart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompassManager.unbind();
    }
}


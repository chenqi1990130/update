package com.ycb.www.update;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String url = "http://www.ycb.com/m/YcbAndroid2.0.5.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        new AlertDialog.Builder(this).setMessage("您确定要更新吗?").setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(MainActivity.this, UpdateService.class);
                    intent.putExtra("downUrl", url);
                    startService(intent);

            }
        }).setNegativeButton("取消", null).show();
    }
}

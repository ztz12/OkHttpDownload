package com.wanandroid.zhangtianzhu.okhttpdownloaddemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class JumpMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump_main);
    }

    public void jump(View view){
        startActivity(new Intent(this,MainActivity.class));
    }
}

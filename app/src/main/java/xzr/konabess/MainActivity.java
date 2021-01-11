package xzr.konabess;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import xzr.konabess.utils.DialogUtil;

public class MainActivity extends Activity {
    AlertDialog waiting;
    boolean cross_device_debug=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ChipInfo.which= ChipInfo.type.unknown;

        try {
            setTitle(getTitle()+" "+getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        try {
            if(!cross_device_debug)
                KonaBessCore.cleanEnv(this);
            KonaBessCore.setupEnv(this);
        }catch (Exception e){
            DialogUtil.showError(this,"环境初始化失败");
            return;
        }

        new unpackLogic().start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {//如果申请权限回调的参数
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new backupBoot(this).start();
            } else {
                Toast.makeText(this, "存储权限申请未成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    LinearLayout mainView;
    LinearLayout showdView;
    void showMainView(){
        mainView=new LinearLayout(this);
        mainView.setOrientation(LinearLayout.VERTICAL);
        setContentView(mainView);

        LinearLayout toolbar=new LinearLayout(this);
        HorizontalScrollView toolbarScroll=new HorizontalScrollView(this);
        toolbarScroll.addView(toolbar);
        mainView.addView(toolbarScroll);

        LinearLayout editor=new LinearLayout(this);
        HorizontalScrollView editorScroll=new HorizontalScrollView(this);
        editorScroll.addView(editor);
        mainView.addView(editorScroll);

        showdView=new LinearLayout(this);
        showdView.setOrientation(LinearLayout.VERTICAL);
        mainView.addView(showdView);

        //ToolBar
        {
            Button button=new Button(this);
            button.setText("打包并刷入新镜像");
            toolbar.addView(button);
            button.setOnClickListener(v -> new repackLogic().start());
        }
        {
            Button button=new Button(this);
            button.setText("备份旧镜像");
            toolbar.addView(button);
            button.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("备份旧镜像")
                    .setMessage("将把旧镜像备份到/sdcard/"+KonaBessCore.boot_name+".img")
                    .setPositiveButton("确认", (dialog, which) -> {
                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                        }
                        else {
                            new backupBoot(MainActivity.this).start();
                        }
                    })
                    .setNegativeButton("取消",null)
                    .create().show());
        }
        {
            Button button=new Button(this);
            button.setText("帮助");
            toolbar.addView(button);
            button.setOnClickListener(v -> new AlertDialog.Builder(MainActivity.this)
                    .setTitle("帮助")
                    .setMessage(KonaBessStr.generic_help())
                    .setPositiveButton("好的",null)
                    .setNeutralButton("关于", (dialog, which) -> new AlertDialog.Builder(MainActivity.this)
                            .setTitle("关于")
                            .setMessage("作者：xzr467706992 (LibXZR)\n发布于 www.akr-developers.com\n")
                            .setPositiveButton("好的",null)
                            .setNeutralButton("访问AKR社区", (dialog1, which1) -> MainActivity.this.startActivity(new Intent(){{
                                setAction(Intent.ACTION_VIEW);
                                setData(Uri.parse("https://www.akr-developers.com/d/441"));
                            }})).create().show())
                    .create().show());
        }

        //Editor
        {
            Button button=new Button(this);
            button.setText("编辑GPU频率表");
            editor.addView(button);
            button.setOnClickListener(v -> new GpuTableEditor.gpuTableLogic(this,showdView).start());
        }
        if(ChipInfo.which!= ChipInfo.type.lahaina){
            Button button=new Button(this);
            button.setText("编辑GPU电压表");
            editor.addView(button);
            button.setOnClickListener(v -> new GpuVoltEditor.gpuVoltLogic(this,showdView).start());
        }
    }

    class backupBoot extends Thread{
        Activity activity;
        AlertDialog waiting;
        boolean is_err;

        public backupBoot(Activity activity){
            this.activity=activity;
        }
        public void run(){
            is_err=false;
            runOnUiThread(() -> {
                waiting=DialogUtil.getWaitDialog(activity,"正在备份镜像，请稍后");
                waiting.show();
            });
            try{
                KonaBessCore.backupBootImage(activity);
            }catch (Exception e){
                is_err=true;
            }
            runOnUiThread(() -> {
                waiting.dismiss();
                if(is_err)
                    DialogUtil.showError(activity,"备份失败");
                else
                    Toast.makeText(activity,"备份成功",Toast.LENGTH_SHORT).show();
            });

        }
    }


    class repackLogic extends Thread{
        boolean is_err;
        String error="";
        public void run(){
            is_err=false;
            {
                runOnUiThread(() -> {
                    waiting=DialogUtil.getWaitDialog(MainActivity.this,"正在打包Boot镜像，请稍后");
                    waiting.show();
                });

                try {
                    KonaBessCore.dts2bootImage(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                    error=e.getMessage();
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showDetailedError(MainActivity.this, "打包Boot镜像失败",error);
                });
                if (is_err)
                    return;
            }

            if(!cross_device_debug){
                runOnUiThread(() -> {
                    waiting=DialogUtil.getWaitDialog(MainActivity.this,"正在刷入Boot镜像，请给Root权限并稍作等待");
                    waiting.show();
                });

                try {
                    KonaBessCore.writeBootImage(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showError(MainActivity.this, "刷入Boot镜像失败，请检查Root权限");
                    else{
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("重启以完成更改")
                                .setMessage("您想要现在重启设备吗？")
                                .setPositiveButton("是", (dialog, which) -> {
                                    try {
                                        KonaBessCore.reboot();
                                    } catch (IOException e) {
                                        DialogUtil.showError(MainActivity.this,"重启失败，请检查Root权限");
                                    }
                                })
                                .setNegativeButton("否",null)
                                .create().show();
                    }
                });
            }
        }
    }

    class unpackLogic extends Thread{
        String error="";
        boolean is_err;
        public void run(){
            is_err=false;
            {
                runOnUiThread(() -> {
                    waiting=DialogUtil.getWaitDialog(MainActivity.this,"正在获取Boot镜像，请给Root权限并稍作等待");
                    waiting.show();
                });
                try {
                    if(!cross_device_debug)
                        KonaBessCore.getBootImage(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showError(MainActivity.this, "获取Boot镜像失败，请检查Root权限");
                });
                if (is_err)
                    return;
            }

            {
                runOnUiThread(() -> {
                    waiting=DialogUtil.getWaitDialog(MainActivity.this,"正在解包Boot镜像，请稍后");
                    waiting.show();
                });
                try {
                    KonaBessCore.bootImage2dts(MainActivity.this);
                } catch (Exception e) {
                    is_err = true;
                    error=e.getMessage();
                }
                runOnUiThread(() -> {
                    waiting.dismiss();
                    if (is_err)
                        DialogUtil.showDetailedError(MainActivity.this, "解包Boot镜像失败",error);
                });
                if (is_err)
                    return;
            }


            runOnUiThread(() -> {
                try {
                    if (!KonaBessCore.checkDevice(MainActivity.this)) {
                        DialogUtil.showError(MainActivity.this, "不兼容的设备，爬");
                        return;
                    }
                }
                catch (Exception e){
                    DialogUtil.showError(MainActivity.this, "检查平台信息时出现了错误");
                    return;
                }
                showMainView();
            });
        }
    }

}
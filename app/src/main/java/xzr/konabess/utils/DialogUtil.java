package xzr.konabess.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class DialogUtil {
    public static void showError(Activity activity, String text){
        new AlertDialog.Builder(activity)
                .setTitle("错误")
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton("确认", (dialog, which) -> activity.finish())
                .create().show();
    }

    public static void showDetailedError(Activity activity, String err,String detail){
        err+="\n（长按可复制错误内容）";
        ScrollView scrollView=new ScrollView(activity);
        TextView textView=new TextView(activity);
        textView.setTextIsSelectable(true);
        scrollView.addView(textView);
        textView.setText(detail);
        new AlertDialog.Builder(activity)
                .setTitle("错误")
                .setMessage(err)
                .setOnDismissListener(dialog -> activity.finish())
                .setView(scrollView)
                .create().show();
    }

    public static void showDetailedInfo(Activity activity,String title, String what,String detail){
        what+="\n（长按可复制内容）";
        ScrollView scrollView=new ScrollView(activity);
        TextView textView=new TextView(activity);
        textView.setTextIsSelectable(true);
        scrollView.addView(textView);
        textView.setText(detail);
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(what)
                .setView(scrollView)
                .create().show();
    }

    public static AlertDialog getWaitDialog(Context context,String text){
        LinearLayout linearLayout=new LinearLayout(context);
        ProgressBar progressBar=new ProgressBar(context);
        TextView textView=new TextView(context);
        linearLayout.addView(progressBar);
        linearLayout.addView(textView);
        textView.setText(text);

        return new AlertDialog.Builder(context)
                .setTitle("请稍后")
                .setCancelable(false)
                .setView(linearLayout)
                .create();
    }
}

package xzr.konabess.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import xzr.konabess.R;

public class DialogUtil {
    public static void showError(Activity activity, String text){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.error)
                .setMessage(text)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, (dialog, which) -> activity.finish())
                .create().show();
    }

    public static void showError(Activity activity, int text_res){
        showError(activity,activity.getResources().getString(text_res));
    }

    public static void showDetailedError(Activity activity, String err,String detail){
        err+="\n"+activity.getResources().getString(R.string.long_press_to_copy);
        ScrollView scrollView=new ScrollView(activity);
        TextView textView=new TextView(activity);
        textView.setTextIsSelectable(true);
        scrollView.addView(textView);
        textView.setText(detail);
        new AlertDialog.Builder(activity)
                .setTitle(R.string.error)
                .setMessage(err)
                .setOnDismissListener(dialog -> activity.finish())
                .setView(scrollView)
                .create().show();
    }

    public static void showDetailedError(Activity activity, int err,String detail) {
        showDetailedError(activity,activity.getResources().getString(err),detail);
    }

    public static void showDetailedInfo(Activity activity,String title, String what,String detail){
        what+="\n"+activity.getResources().getString(R.string.long_press_to_copy);
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

    public static void showDetailedInfo(Activity activity,int title, int what,String detail){
        showDetailedInfo(activity,activity.getResources().getString(title),activity.getResources().getString(what),detail);
    }

    public static AlertDialog getWaitDialog(Context context,int id){
        return getWaitDialog(context,context.getResources().getString(id));
    }

    public static AlertDialog getWaitDialog(Context context,String text){
        LinearLayout linearLayout=new LinearLayout(context);
        ProgressBar progressBar=new ProgressBar(context);
        TextView textView=new TextView(context);
        linearLayout.addView(progressBar);
        linearLayout.addView(textView);
        textView.setText(text);

        return new AlertDialog.Builder(context)
                .setTitle(R.string.please_wait)
                .setCancelable(false)
                .setView(linearLayout)
                .create();
    }
}

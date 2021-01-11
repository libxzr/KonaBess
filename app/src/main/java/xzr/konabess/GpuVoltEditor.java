package xzr.konabess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import xzr.konabess.adapters.ParamAdapter;
import xzr.konabess.utils.DialogUtil;
import xzr.konabess.utils.DtsHelper;

public class GpuVoltEditor {
    private static class opp{
        public long frequency;
        public long volt;
    }

    public static int levelint2int(long level) throws Exception{
        for(int i = 0; i< ChipInfo.rpmh_levels.levels().length; i++){
            if(ChipInfo.rpmh_levels.levels()[i]==level)
                return i;
        }
        throw new Exception();
    }

    public static String levelint2str(long level) throws Exception{
        return ChipInfo.rpmh_levels.level_str()[levelint2int(level)];
    }

    private static ArrayList<opp> opps;
    private static ArrayList<String> lines_in_dts;
    private static int opp_position;

    private static void init(Context context) throws IOException {
        lines_in_dts=new ArrayList<>();
        opps=new ArrayList<>();
        opp_position=-1;
        BufferedReader bufferedReader=new BufferedReader(new FileReader(new File(KonaBessCore.dts_path)));
        String s;
        while((s=bufferedReader.readLine())!=null){
            lines_in_dts.add(s);
        }
    }

    private static opp decode_opp(List<String> lines) throws Exception{
        opp opp=new opp();
        for(String line:lines){
            if(line.contains("opp-hz"))
                opp.frequency=DtsHelper.decode_int_line_hz(line).value;
            if(line.contains("opp-microvolt"))
                opp.volt=DtsHelper.decode_int_line(line).value;
        }
        return opp;
    }

    private static void decode() throws Exception{
        int i=-1;
        boolean isInGpuTable=false;
        int bracket=0;
        int start=-1;
        int end;
        while(++i<lines_in_dts.size()){
            String line=lines_in_dts.get(i).trim();
            if(line.equals(""))
                continue;

            if(ChipInfo.which== ChipInfo.type.kona||ChipInfo.which== ChipInfo.type.kona_old) {
                if (line.contains("gpu-opp-table_v2") && line.contains("{")) {
                    isInGpuTable = true;
                    bracket++;
                    continue;
                }
            } else if(ChipInfo.which== ChipInfo.type.msmnile){
                if (line.contains("gpu_opp_table_v2") && line.contains("{")) {
                    isInGpuTable = true;
                    bracket++;
                    continue;
                }
            }

            if(!isInGpuTable)
                continue;

            if(line.contains("opp-")&&line.contains("{")){
                start=i;
                if(opp_position<0)
                    opp_position=i;
                bracket++;
                continue;
            }

            if(line.contains("}")){
                bracket--;
                if(bracket==0)
                    break;
                if(bracket!=1)
                    throw new Exception();
                end=i;
                opps.add(decode_opp(lines_in_dts.subList(start,end+1)));
                lines_in_dts.subList(start,end+1).clear();
                i=start-1;
            }
        }
    }

    private static List<String> genBack(){
        ArrayList<String> ret=new ArrayList<>(lines_in_dts);
        ArrayList<String> table=new ArrayList<>();
        for(opp opp:opps){
            table.add("opp-"+opp.frequency+" {");
            table.add("opp-hz = <0x0 "+opp.frequency+">;");
            table.add("opp-microvolt = <"+opp.volt+">;");
            table.add("};");
        }
        ret.addAll(opp_position,table);
        return ret;
    }

    private static void writeOut(Context context,List<String> new_dts) throws IOException{
        File file=new File(KonaBessCore.dts_path);
        file.createNewFile();
        BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file));
        for(String s:new_dts){
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private static View generateToolBar(Activity activity){
        LinearLayout toolbar=new LinearLayout(activity);
        HorizontalScrollView horizontalScrollView=new HorizontalScrollView(activity);
        horizontalScrollView.addView(toolbar);

        {
            Button button=new Button(activity);
            button.setText("保存GPU电压表修改");
            toolbar.addView(button);
            button.setOnClickListener(v -> {
                try {
                    writeOut(activity,genBack());
                    Toast.makeText(activity,"保存成功",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    DialogUtil.showError(activity,"保存修改失败");
                }
            });
        }
        return horizontalScrollView;
    }
    private static void generateAVolt(Activity activity,LinearLayout page,int voltn) throws Exception{
        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title="返回上一级";
            subtitle="";
        }});

        items.add(new ParamAdapter.item(){{
            title="频率";
            subtitle=opps.get(voltn).frequency+"";
        }});

        items.add(new ParamAdapter.item(){{
            title="电压等级";
            subtitle=levelint2str(opps.get(voltn).volt);
        }});

        listView.setAdapter(new ParamAdapter(items,activity));
        listView.setOnItemClickListener((parent, view, position, idd) -> {
            if(position==0)
                generateVolts(activity,page);
            if(position==1){
                EditText editText=new EditText(activity);
                editText.setText(opps.get(voltn).frequency+"");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(activity)
                        .setTitle("编辑频率")
                        .setMessage("这是指这个电压挡位对应的频率。\n" +
                                "此处频率的单位是Hz，也就是说在MHz频率后面加上6个零，您可以在上一级菜单中以MHz查看频率。\n" +
                                "如果您搞不清楚要加几个零，那么建议只修改数字的前几位。")
                        .setView(editText)
                        .setPositiveButton("保存", (dialog, which) -> {
                            try {
                                opps.get(voltn).frequency = Long.parseLong(editText.getText().toString());
                                generateAVolt(activity, page, voltn);
                            }catch (Exception e){
                                DialogUtil.showError(activity,"保存失败");
                            }
                        })
                        .setNegativeButton("取消",null)
                        .create().show();
            }
            if(position==2){
                try {
                    Spinner spinner = new Spinner(activity);
                    spinner.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ChipInfo.rpmh_levels.level_str()));
                    spinner.setSelection(levelint2int(opps.get(voltn).volt));

                    new AlertDialog.Builder(activity)
                            .setTitle(KonaBessStr.editVolt.title)
                            .setView(spinner)
                            .setMessage(KonaBessStr.editVolt.msg)
                            .setPositiveButton("保存", (dialog, which) -> {
                                opps.get(voltn).volt= ChipInfo.rpmh_levels.levels()[spinner.getSelectedItemPosition()];
                                try {
                                    generateAVolt(activity,page,voltn);
                                } catch (Exception e) {
                                    DialogUtil.showError(activity,"修改电压失败");
                                }
                            })
                            .setNegativeButton("取消",null)
                            .create().show();

                }catch (Exception e){
                    DialogUtil.showError(activity,"获取电压失败");
                }
            }
        });

        page.removeAllViews();
        page.addView(listView);
    }
    private static void generateVolts(Activity activity,LinearLayout page){
        ListView listView=new ListView(activity);
        ArrayList<ParamAdapter.item> items=new ArrayList<>();

        items.add(new ParamAdapter.item(){{
            title="新建";
            subtitle="增加新的电压等级";
        }});

        for(opp opp:opps){
            items.add(new ParamAdapter.item(){{
                title=opp.frequency/1000000+"MHz";
                subtitle="";
            }});
        }

        items.add(new ParamAdapter.item(){{
            title="新建";
            subtitle="增加新的电压等级";
        }});

        listView.setAdapter(new ParamAdapter(items,activity));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(position==opps.size()+1){
                opp new_opp=new opp(){{
                    frequency=opps.get(opps.size()-1).frequency;
                    volt=opps.get(opps.size()-1).volt;
                }};
                opps.add(opps.size()-1,new_opp);
                generateVolts(activity,page);
                return;
            }
            if(position==0){
                opp new_opp=new opp(){{
                   frequency=opps.get(0).frequency;
                   volt=opps.get(0).volt;
                }};
                opps.add(0,new_opp);
                generateVolts(activity,page);
                return;
            }
            position--;
            try {
                generateAVolt(activity,page,position);
            } catch (Exception e) {
                DialogUtil.showError(activity,"获取电压错误");
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if(position==opps.size()+1)
                return true;
            if(position==0)
                return true;
            position--;
            int finalPosition = position;
            new AlertDialog.Builder(activity)
                    .setTitle("删除电压")
                    .setMessage("你确定要删除" + opps.get(position).frequency / 1000000 + "MHz对应的电压吗？")
                    .setPositiveButton("确认删除", (dialog, which) -> {
                        opps.remove(finalPosition);
                        generateVolts(activity,page);
                    })
                    .setNegativeButton("取消", null)
                    .create().show();
            return true;
        });

        page.removeAllViews();
        page.addView(listView);
    }

    static class gpuVoltLogic extends Thread{
        Activity activity;
        AlertDialog waiting;
        LinearLayout showedView;
        LinearLayout page;
        public gpuVoltLogic(Activity activity, LinearLayout showedView){
            this.activity=activity;
            this.showedView=showedView;
        }
        public void run(){
            activity.runOnUiThread(() -> {
                waiting= DialogUtil.getWaitDialog(activity,"正在获取GPU电压表，请稍后");
                waiting.show();
            });

            try{
                init(activity);
                decode();
            }catch (Exception e){
                activity.runOnUiThread(() -> DialogUtil.showError(activity,"获取GPU电压表失败"));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                showedView.addView(generateToolBar(activity));
                page=new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                generateVolts(activity,page);
                showedView.addView(page);
            });

        }
    }
}

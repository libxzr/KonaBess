package xzr.konabess;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
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
    private static class opp {
        public long frequency;
        public long volt;
    }

    public static int levelint2int(long level) throws Exception {
        for (int i = 0; i < ChipInfo.rpmh_levels.levels().length; i++) {
            if (ChipInfo.rpmh_levels.levels()[i] == level)
                return i;
        }
        throw new Exception();
    }

    public static String levelint2str(long level) throws Exception {
        return ChipInfo.rpmh_levels.level_str()[levelint2int(level)];
    }

    private static ArrayList<opp> opps;
    private static ArrayList<String> lines_in_dts;
    private static int opp_position;

    public static void init() throws IOException {
        lines_in_dts = new ArrayList<>();
        opps = new ArrayList<>();
        opp_position = -1;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(KonaBessCore.dts_path)));
        String s;
        while ((s = bufferedReader.readLine()) != null) {
            lines_in_dts.add(s);
        }
    }

    private static opp decode_opp(List<String> lines) throws Exception {
        opp opp = new opp();
        for (String line : lines) {
            if (line.contains("opp-hz"))
                opp.frequency = DtsHelper.decode_int_line_hz(line).value;
            if (line.contains("opp-microvolt"))
                opp.volt = DtsHelper.decode_int_line(line).value;
        }
        return opp;
    }

    public static void decode() throws Exception {
        int i = -1;
        boolean isInGpuTable = false;
        int bracket = 0;
        int start = -1;
        int end;
        while (++i < lines_in_dts.size()) {
            String line = lines_in_dts.get(i).trim();
            if (line.equals(""))
                continue;

            if (ChipInfo.which == ChipInfo.type.kona || ChipInfo.which == ChipInfo.type.kona_singleBin) {
                if (line.contains("gpu-opp-table_v2") && line.contains("{")) {
                    isInGpuTable = true;
                    bracket++;
                    continue;
                }
            } else if (ChipInfo.which == ChipInfo.type.msmnile || ChipInfo.which == ChipInfo.type.msmnile_singleBin) {
                if (line.contains("gpu_opp_table_v2") && line.contains("{")) {
                    isInGpuTable = true;
                    bracket++;
                    continue;
                }
            } else if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2 || ChipInfo.which == ChipInfo.type.lagoon) {
                if (line.contains("gpu-opp-table {")) {
                    isInGpuTable = true;
                    bracket++;
                    continue;
                }
            }

            if (!isInGpuTable)
                continue;

            if (line.contains("opp-") && line.contains("{")) {
                start = i;
                if (opp_position < 0)
                    opp_position = i;
                bracket++;
                continue;
            }

            if (line.contains("}")) {
                bracket--;
                if (bracket == 0)
                    break;
                if (bracket != 1)
                    throw new Exception();
                end = i;
                opps.add(decode_opp(lines_in_dts.subList(start, end + 1)));
                lines_in_dts.subList(start, end + 1).clear();
                i = start - 1;
            }
        }
    }

    public static List<String> genTable() {
        ArrayList<String> table = new ArrayList<>();
        for (opp opp : opps) {
            table.add("opp-" + opp.frequency + " {");
            table.add("opp-hz = <0x0 " + opp.frequency + ">;");
            table.add("opp-microvolt = <" + opp.volt + ">;");
            table.add("};");
        }
        return table;
    }

    public static List<String> genBack(List<String> table) {
        ArrayList<String> ret = new ArrayList<>(lines_in_dts);
        ret.addAll(opp_position, table);
        return ret;
    }

    public static void writeOut(List<String> new_dts) throws IOException {
        File file = new File(KonaBessCore.dts_path);
        file.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (String s : new_dts) {
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
    }

    private static View generateToolBar(Activity activity) {
        LinearLayout toolbar = new LinearLayout(activity);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(activity);
        horizontalScrollView.addView(toolbar);

        {
            Button button = new Button(activity);
            button.setText(R.string.save_volt_table);
            toolbar.addView(button);
            button.setOnClickListener(v -> {
                try {
                    writeOut(genBack(genTable()));
                    Toast.makeText(activity, R.string.save_success, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.save_failed);
                }
            });
        }
        return horizontalScrollView;
    }

    private static void generateAVolt(Activity activity, LinearLayout page, int voltn) throws Exception {
        ((MainActivity) activity).onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                generateVolts(activity, page);
            }
        };

        ListView listView = new ListView(activity);
        ArrayList<ParamAdapter.item> items = new ArrayList<>();

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.back);
            subtitle = "";
        }});

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.freq);
            subtitle = opps.get(voltn).frequency + "";
        }});

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.volt);
            subtitle = levelint2str(opps.get(voltn).volt);
        }});

        listView.setAdapter(new ParamAdapter(items, activity));
        listView.setOnItemClickListener((parent, view, position, idd) -> {
            if (position == 0)
                generateVolts(activity, page);
            if (position == 1) {
                EditText editText = new EditText(activity);
                editText.setText(opps.get(voltn).frequency + "");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.edit)
                        .setMessage(R.string.volt_freq_msg)
                        .setView(editText)
                        .setPositiveButton(R.string.save, (dialog, which) -> {
                            try {
                                opps.get(voltn).frequency = Long.parseLong(editText.getText().toString());
                                generateAVolt(activity, page, voltn);
                            } catch (Exception e) {
                                DialogUtil.showError(activity, R.string.save_failed);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create().show();
            }
            if (position == 2) {
                try {
                    Spinner spinner = new Spinner(activity);
                    spinner.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, ChipInfo.rpmh_levels.level_str()));
                    spinner.setSelection(levelint2int(opps.get(voltn).volt));

                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.edit)
                            .setView(spinner)
                            .setMessage(R.string.editvolt_msg)
                            .setPositiveButton(R.string.save, (dialog, which) -> {
                                opps.get(voltn).volt = ChipInfo.rpmh_levels.levels()[spinner.getSelectedItemPosition()];
                                try {
                                    generateAVolt(activity, page, voltn);
                                } catch (Exception e) {
                                    DialogUtil.showError(activity, R.string.save_failed);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .create().show();

                } catch (Exception e) {
                    DialogUtil.showError(activity, R.string.error_occur);
                }
            }
        });

        page.removeAllViews();
        page.addView(listView);
    }

    private static void generateVolts(Activity activity, LinearLayout page) {
        ((MainActivity) activity).onBackPressedListener = new MainActivity.onBackPressedListener() {
            @Override
            public void onBackPressed() {
                ((MainActivity) activity).showMainView();
            }
        };

        ListView listView = new ListView(activity);
        ArrayList<ParamAdapter.item> items = new ArrayList<>();

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.new_item);
            subtitle = activity.getResources().getString(R.string.new_desc_volt);
        }});

        for (opp opp : opps) {
            items.add(new ParamAdapter.item() {{
                title = opp.frequency / 1000000 + "MHz";
                subtitle = "";
            }});
        }

        items.add(new ParamAdapter.item() {{
            title = activity.getResources().getString(R.string.new_item);
            subtitle = activity.getResources().getString(R.string.new_desc_volt);
        }});

        listView.setAdapter(new ParamAdapter(items, activity));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == opps.size() + 1) {
                opp new_opp = new opp() {{
                    frequency = opps.get(opps.size() - 1).frequency;
                    volt = opps.get(opps.size() - 1).volt;
                }};
                opps.add(opps.size() - 1, new_opp);
                generateVolts(activity, page);
                return;
            }
            if (position == 0) {
                opp new_opp = new opp() {{
                    frequency = opps.get(0).frequency;
                    volt = opps.get(0).volt;
                }};
                opps.add(0, new_opp);
                generateVolts(activity, page);
                return;
            }
            position--;
            try {
                generateAVolt(activity, page, position);
            } catch (Exception e) {
                DialogUtil.showError(activity, R.string.error_occur);
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position == opps.size() + 1)
                return true;
            if (position == 0)
                return true;
            position--;
            int finalPosition = position;
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.remove)
                    .setMessage(String.format(activity.getResources().getString(R.string.remove_msg_volt), opps.get(position).frequency / 1000000))
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        opps.remove(finalPosition);
                        generateVolts(activity, page);
                    })
                    .setNegativeButton(R.string.no, null)
                    .create().show();
            return true;
        });

        page.removeAllViews();
        page.addView(listView);
    }

    static class gpuVoltLogic extends Thread {
        Activity activity;
        AlertDialog waiting;
        LinearLayout showedView;
        LinearLayout page;

        public gpuVoltLogic(Activity activity, LinearLayout showedView) {
            this.activity = activity;
            this.showedView = showedView;
        }

        public void run() {
            activity.runOnUiThread(() -> {
                waiting = DialogUtil.getWaitDialog(activity, R.string.getting_volt);
                waiting.show();
            });

            try {
                init();
                decode();
            } catch (Exception e) {
                activity.runOnUiThread(() -> DialogUtil.showError(activity, R.string.getting_volt_failed));
            }

            activity.runOnUiThread(() -> {
                waiting.dismiss();
                showedView.removeAllViews();
                showedView.addView(generateToolBar(activity));
                page = new LinearLayout(activity);
                page.setOrientation(LinearLayout.VERTICAL);
                generateVolts(activity, page);
                showedView.addView(page);
            });

        }
    }
}

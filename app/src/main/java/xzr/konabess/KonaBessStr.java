package xzr.konabess;


import android.app.Activity;

public class KonaBessStr {
    public static String convert_bins(int which, Activity activity) throws Exception {
        if (ChipInfo.which == ChipInfo.type.kona)
            return convert_bins_kona(which, activity);
        else if (ChipInfo.which == ChipInfo.type.kona_singleBin)
            return convert_bins_kona_singleBin(which, activity);
        else if (ChipInfo.which == ChipInfo.type.msmnile)
            return convert_bins_msmnile(which, activity);
        else if (ChipInfo.which == ChipInfo.type.msmnile_singleBin)
            return convert_bins_msmnile_singleBin(which, activity);
        else if (ChipInfo.which == ChipInfo.type.lahaina)
            return convert_bins_lahaina(which, activity);
        else if (ChipInfo.which == ChipInfo.type.lahaina_singleBin)
            return convert_bins_lahaina_singleBin(which, activity);
        else if (ChipInfo.which == ChipInfo.type.lito_v1 || ChipInfo.which == ChipInfo.type.lito_v2)
            return convert_bins_lito(which, activity);
        else if (ChipInfo.which == ChipInfo.type.lagoon)
            return convert_bins_lagoon(which, activity);
        else if (ChipInfo.which == ChipInfo.type.shima)
            return convert_bins_shima(which, activity);
        else if (ChipInfo.which == ChipInfo.type.yupik)
            return convert_bins_yupik(which, activity);
        else if (ChipInfo.which == ChipInfo.type.waipio_singleBin)
            return convert_bins_waipio_singleBin(which, activity);
        else if (ChipInfo.which == ChipInfo.type.cape_singleBin)
            return convert_bins_cape_singleBin(which, activity);
        else if (ChipInfo.which == ChipInfo.type.kalama)
            return convert_bins_kalama(which, activity);

        throw new Exception();
    }

    public static String convert_bins_kona(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sdm865);
            case 1:
                return activity.getResources().getString(R.string.sdm865p);
            case 2:
                return activity.getResources().getString(R.string.sdm865m);
            case 3:
                return activity.getResources().getString(R.string.sd870);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_kona_singleBin(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sdm865_singlebin);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_msmnile(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sdm855);
            case 1:
                return activity.getResources().getString(R.string.sdm855p);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_msmnile_singleBin(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sdm855_singlebin);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_lahaina(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sdm888);
            case 3:
                return activity.getResources().getString(R.string.sdm888p);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_lahaina_singleBin(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sdm888_singlebin);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_lito(int which, Activity activity) {
        switch (which) {
            case 1:
                return activity.getResources().getString(R.string.sd765g);
            case 3:
                return activity.getResources().getString(R.string.sd765);

        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_lagoon(int which, Activity activity) {
        switch (which) {
            case 2:
                return activity.getResources().getString(R.string.sdm750g);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_shima(int which, Activity activity) {
        switch (which) {
            case 1:
                return activity.getResources().getString(R.string.sd780g);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_yupik(int which, Activity activity) {
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_waipio_singleBin(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sd8g1_singlebin);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_cape_singleBin(int which, Activity activity) {
        switch (which) {
            case 0:
                return activity.getResources().getString(R.string.sd8g1p_singlebin);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_bins_kalama(int which, Activity activity) {
        switch (which) {
            case 0:
            case 1:
                return activity.getResources().getString(R.string.sd8g2);
        }
        return activity.getResources().getString(R.string.unknown_table) + which;
    }

    public static String convert_level_params(String input, Activity activity) {
        input = input.replace("qcom,", "");
        if (input.equals("gpu-freq"))
            return activity.getResources().getString(R.string.freq);
        if (input.equals("level"))
            return activity.getResources().getString(R.string.volt);

        return input;
    }

    public static String help(String what, Activity activity) {
        if (what.equals("qcom,gpu-freq"))
            return ChipInfo.shouldIgnoreVoltTable(ChipInfo.which)
                    ? activity.getResources().getString(R.string.help_gpufreq_aio)
                    : activity.getResources().getString(R.string.help_gpufreq);
        if (what.contains("bus"))
            return activity.getResources().getString(R.string.help_bus);
        if (what.contains("acd"))
            return activity.getResources().getString(R.string.help_acd);
        return "";
    }

    public static String generic_help(Activity activity) {
        return ChipInfo.shouldIgnoreVoltTable(ChipInfo.which)
                ? activity.getResources().getString(R.string.help_msg_aio)
                : activity.getResources().getString(R.string.help_msg);
    }
}

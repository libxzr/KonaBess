package xzr.konabess;


import android.app.Activity;

public class ChipInfo {
    public enum type{
        kona,
        kona_singleBin,
        msmnile,
        msmnile_singleBin,
        lahaina_singleBin,
        lito_v1,lito_v2,
        unknown
    }

    public static String name2chipdesc(String name, Activity activity){
        type t=type.valueOf(name);
        return name2chipdesc(t,activity);
    }

    public static String name2chipdesc(type t, Activity activity){
        switch (t){
            case kona:
                return activity.getResources().getString(R.string.sdm865_series);
            case kona_singleBin:
                return activity.getResources().getString(R.string.sdm865_singlebin);
            case msmnile:
                return activity.getResources().getString(R.string.sdm855_series);
            case msmnile_singleBin:
                return activity.getResources().getString(R.string.sdm855_singlebin);
            case lahaina_singleBin:
                return activity.getResources().getString(R.string.sdm888_singlebin);
            case lito_v1:
                return activity.getResources().getString(R.string.lito_v1_series);
            case lito_v2:
                return activity.getResources().getString(R.string.lito_v2_series);
        }
        return activity.getResources().getString(R.string.unknown);
    }

    public static type which;

    public static class rpmh_levels{
        public static int[] levels(){
            if(ChipInfo.which==type.kona||ChipInfo.which==type.kona_singleBin)
                return rpmh_levels_kona.levels;
            else if(ChipInfo.which==type.msmnile||ChipInfo.which==type.msmnile_singleBin)
                return rpmh_levels_msmnile.levels;
            else if(ChipInfo.which==type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.levels;
            else if(ChipInfo.which==type.lito_v1 || ChipInfo.which==type.lito_v2)
                return rpmh_levels_lito.levels;

            return new int[]{};
        }
        public static String[] level_str(){
            if(ChipInfo.which==type.kona||ChipInfo.which==type.kona_singleBin)
                return rpmh_levels_kona.level_str;
            else if(ChipInfo.which==type.msmnile||ChipInfo.which==type.msmnile_singleBin)
                return rpmh_levels_msmnile.level_str;
            else if(ChipInfo.which==type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.level_str;
            else if(ChipInfo.which==type.lito_v1 || ChipInfo.which==type.lito_v2)
                return rpmh_levels_lito.level_str;

            return new String[]{};
        }
    }

    private static class rpmh_levels_kona{
        public static final int[] levels={48,64,80,96,128,144,192,224,256,320,336,384,416};
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_msmnile{
        private static final int RPMH_REGULATOR_LEVEL_OFFSET=1;
        public static final int[] levels={
                48+RPMH_REGULATOR_LEVEL_OFFSET,
                64+RPMH_REGULATOR_LEVEL_OFFSET,
                128+RPMH_REGULATOR_LEVEL_OFFSET,
                192+RPMH_REGULATOR_LEVEL_OFFSET,
                224+RPMH_REGULATOR_LEVEL_OFFSET,
                256+RPMH_REGULATOR_LEVEL_OFFSET,
                320+RPMH_REGULATOR_LEVEL_OFFSET,
                336+RPMH_REGULATOR_LEVEL_OFFSET,
                352+RPMH_REGULATOR_LEVEL_OFFSET,
                384+RPMH_REGULATOR_LEVEL_OFFSET,
                416+RPMH_REGULATOR_LEVEL_OFFSET
        };
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "SVS",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "NOM_L3",
                "TURBO",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_lahaina_singleBin{
        public static final int[] levels={48,64,80,96,128,144,192,224,256,320,336,384,416};
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_lito{
        public static final int[] levels={48,64,80,96,128,144,192,224,256,320,336,384,416};
        public static final String[] level_str={
                "MIN_SVS",
                "LOW_SVS",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L1"
        };
    }

}

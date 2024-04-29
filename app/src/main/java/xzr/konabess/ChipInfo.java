package xzr.konabess;


import android.app.Activity;

public class ChipInfo {
    public enum type {
        kona,
        kona_singleBin,
        msmnile,
        msmnile_singleBin,
        lahaina,
        lahaina_singleBin,
        lito_v1, lito_v2,
        lagoon,
        shima,
        yupik,
        waipio_singleBin,
        cape_singleBin,
        kalama,
        diwali,
        ukee_singleBin,
        pineapple,
        cliffs_singleBin,
        cliffs_7_singleBin,
        kalama_sg_singleBin,
        unknown
    }

    public static int getMaxTableLevels(type type) {
        if (type == ChipInfo.type.cape_singleBin || type == ChipInfo.type.waipio_singleBin
                || type == ChipInfo.type.kalama || type == ChipInfo.type.diwali
                || type == ChipInfo.type.ukee_singleBin || type == ChipInfo.type.pineapple
                || type == ChipInfo.type.cliffs_singleBin || type == ChipInfo.type.cliffs_7_singleBin
                || type == ChipInfo.type.kalama_sg_singleBin)
            return 16;
        return 11;
    }

    public static boolean shouldIgnoreVoltTable(type type) {
        return type == ChipInfo.type.lahaina || type == ChipInfo.type.lahaina_singleBin
                || type == ChipInfo.type.shima || type == ChipInfo.type.yupik
                || type == ChipInfo.type.waipio_singleBin || type == ChipInfo.type.cape_singleBin
                || type == ChipInfo.type.kalama || type == ChipInfo.type.diwali
                || type == ChipInfo.type.ukee_singleBin || type == ChipInfo.type.pineapple
                || type == ChipInfo.type.cliffs_singleBin || type == ChipInfo.type.cliffs_7_singleBin
                || type == ChipInfo.type.kalama_sg_singleBin;
    }

    public static boolean checkChipGeneral(type input) {
        type now = which;
        if (now == type.lito_v2)
            now = type.lito_v1;
        if (input == type.lito_v2)
            input = type.lito_v1;
        return input == now;
    }

    public static String name2chipdesc(String name, Activity activity) {
        type t = type.valueOf(name);
        return name2chipdesc(t, activity);
    }

    public static String name2chipdesc(type t, Activity activity) {
        switch (t) {
            case kona:
                return activity.getResources().getString(R.string.sdm865_series);
            case kona_singleBin:
                return activity.getResources().getString(R.string.sdm865_singlebin);
            case msmnile:
                return activity.getResources().getString(R.string.sdm855_series);
            case msmnile_singleBin:
                return activity.getResources().getString(R.string.sdm855_singlebin);
            case lahaina:
                return activity.getResources().getString(R.string.sdm888);
            case lahaina_singleBin:
                return activity.getResources().getString(R.string.sdm888_singlebin);
            case lito_v1:
                return activity.getResources().getString(R.string.lito_v1_series);
            case lito_v2:
                return activity.getResources().getString(R.string.lito_v2_series);
            case lagoon:
                return activity.getResources().getString(R.string.lagoon_series);
            case shima:
                return activity.getResources().getString(R.string.sd780g);
            case yupik:
                return activity.getResources().getString(R.string.sd778g);
            case waipio_singleBin:
                return activity.getResources().getString(R.string.sd8g1_singlebin);
            case cape_singleBin:
                return activity.getResources().getString(R.string.sd8g1p_singlebin);
            case kalama:
                return activity.getResources().getString(R.string.sd8g2);
            case diwali:
                return activity.getResources().getString(R.string.sd7g1);
            case ukee_singleBin:
                return activity.getResources().getString(R.string.sd7g2);
            case pineapple:
                return activity.getResources().getString(R.string.sd8g3);
            case cliffs_singleBin:
                return activity.getResources().getString(R.string.sd8sg3);
            case cliffs_7_singleBin:
                return activity.getResources().getString(R.string.sd7pg3);
            case kalama_sg_singleBin:
                return activity.getResources().getString(R.string.sdg3xg2);
        }
        return activity.getResources().getString(R.string.unknown);
    }

    public static type which;

    public static class rpmh_levels {
        public static int[] levels() {
            if (ChipInfo.which == type.kona || ChipInfo.which == type.kona_singleBin)
                return rpmh_levels_kona.levels;
            else if (ChipInfo.which == type.msmnile || ChipInfo.which == type.msmnile_singleBin)
                return rpmh_levels_msmnile.levels;
            else if (ChipInfo.which == type.lahaina)
                return rpmh_levels_lahaina.levels;
            else if (ChipInfo.which == type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.levels;
            else if (ChipInfo.which == type.lito_v1 || ChipInfo.which == type.lito_v2)
                return rpmh_levels_lito.levels;
            else if (ChipInfo.which == type.lagoon)
                return rpmh_levels_lagoon.levels;
            else if (ChipInfo.which == type.shima)
                return rpmh_levels_shima.levels;
            else if (ChipInfo.which == type.yupik)
                return rpmh_levels_yupik.levels;
            else if (ChipInfo.which == type.waipio_singleBin)
                return rpmh_levels_waipio.levels;
            else if (ChipInfo.which == type.cape_singleBin)
                return rpmh_levels_cape.levels;
            else if (ChipInfo.which == type.kalama)
                return rpmh_levels_kalama.levels;
            else if (ChipInfo.which == type.diwali)
                return rpmh_levels_diwali.levels;
            else if (ChipInfo.which == type.ukee_singleBin)
                return rpmh_levels_ukee.levels;
            else if (ChipInfo.which == type.pineapple)
                return rpmh_levels_pineapple.levels;
            else if (ChipInfo.which == type.cliffs_singleBin
                    || ChipInfo.which == type.cliffs_7_singleBin)
                return rpmh_levels_cliffs.levels;
            else if (ChipInfo.which == type.kalama_sg_singleBin)
                return rpmh_levels_kalama.levels;

            return new int[]{};
        }

        public static String[] level_str() {
            if (ChipInfo.which == type.kona || ChipInfo.which == type.kona_singleBin)
                return rpmh_levels_kona.level_str;
            else if (ChipInfo.which == type.msmnile || ChipInfo.which == type.msmnile_singleBin)
                return rpmh_levels_msmnile.level_str;
            else if (ChipInfo.which == type.lahaina)
                return rpmh_levels_lahaina.level_str;
            else if (ChipInfo.which == type.lahaina_singleBin)
                return rpmh_levels_lahaina_singleBin.level_str;
            else if (ChipInfo.which == type.lito_v1 || ChipInfo.which == type.lito_v2)
                return rpmh_levels_lito.level_str;
            else if (ChipInfo.which == type.lagoon)
                return rpmh_levels_lagoon.level_str;
            else if (ChipInfo.which == type.shima)
                return rpmh_levels_shima.level_str;
            else if (ChipInfo.which == type.yupik)
                return rpmh_levels_yupik.level_str;
            else if (ChipInfo.which == type.waipio_singleBin)
                return rpmh_levels_waipio.level_str;
            else if (ChipInfo.which == type.cape_singleBin)
                return rpmh_levels_cape.level_str;
            else if (ChipInfo.which == type.kalama)
                return rpmh_levels_kalama.level_str;
            else if (ChipInfo.which == type.diwali)
                return rpmh_levels_diwali.level_str;
            else if (ChipInfo.which == type.ukee_singleBin)
                return rpmh_levels_ukee.level_str;
            else if (ChipInfo.which == type.pineapple)
                return rpmh_levels_pineapple.level_str;
            else if (ChipInfo.which == type.cliffs_singleBin
                    || ChipInfo.which == type.cliffs_7_singleBin)
                return rpmh_levels_cliffs.level_str;
            else if (ChipInfo.which == type.kalama_sg_singleBin)
                return rpmh_levels_kalama.level_str;

            return new String[]{};
        }
    }

    private static class rpmh_levels_kona {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_msmnile {
        private static final int RPMH_REGULATOR_LEVEL_OFFSET = 1;
        public static final int[] levels = {
                16 + RPMH_REGULATOR_LEVEL_OFFSET,
                48 + RPMH_REGULATOR_LEVEL_OFFSET,
                56 + RPMH_REGULATOR_LEVEL_OFFSET,
                64 + RPMH_REGULATOR_LEVEL_OFFSET,
                80 + RPMH_REGULATOR_LEVEL_OFFSET,
                96 + RPMH_REGULATOR_LEVEL_OFFSET,
                128 + RPMH_REGULATOR_LEVEL_OFFSET,
                144 + RPMH_REGULATOR_LEVEL_OFFSET,
                192 + RPMH_REGULATOR_LEVEL_OFFSET,
                224 + RPMH_REGULATOR_LEVEL_OFFSET,
                256 + RPMH_REGULATOR_LEVEL_OFFSET,
                320 + RPMH_REGULATOR_LEVEL_OFFSET,
                336 + RPMH_REGULATOR_LEVEL_OFFSET,
                352 + RPMH_REGULATOR_LEVEL_OFFSET,
                384 + RPMH_REGULATOR_LEVEL_OFFSET,
                400 + RPMH_REGULATOR_LEVEL_OFFSET,
                416 + RPMH_REGULATOR_LEVEL_OFFSET
        };
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_lahaina {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416, 432, 448, 464};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1",
                "TURBO_L2",
                "SUPER_TURBO",
                "SUPER_TURBO_NO_CPR"
        };
    }

    private static class rpmh_levels_lahaina_singleBin {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_lito {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_lagoon {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_shima {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_yupik {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_waipio {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_cape {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_diwali {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_ukee {
        public static final int[] levels = {16, 48, 56, 64, 80, 96, 128, 144, 192, 224, 256, 320,
                336, 352, 384, 400, 416};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D1",
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
                "NOM_L3",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1"
        };
    }

    private static class rpmh_levels_kalama {
        public static final int[] levels = {16, 48, 52, 56, 60, 64, 72, 80, 96, 128, 144, 192,
                224, 256, 288, 320, 336, 384, 400, 416, 432, 448, 464, 480};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D2",
                "LOW_SVS_D1",
                "LOW_SVS_D0",
                "LOW_SVS",
                "LOW_SVS_P1",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L0",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1",
                "TURBO_L2",
                "TURBO_L3",
                "SUPER_TURBO",
                "SUPER_TURBO_NO_CPR"
        };
    }

    private static class rpmh_levels_pineapple {
        public static final int[] levels = {16, 48, 52, 56, 60, 64, 72, 80, 96, 128, 144, 192,
                224, 256, 288, 320, 336, 384, 400, 416, 432, 448, 464, 480};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D2",
                "LOW_SVS_D1",
                "LOW_SVS_D0",
                "LOW_SVS",
                "LOW_SVS_P1",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L0",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1",
                "TURBO_L2",
                "TURBO_L3",
                "SUPER_TURBO",
                "SUPER_TURBO_NO_CPR"
        };
    }

    private static class rpmh_levels_cliffs {
        public static final int[] levels = {16, 48, 52, 56, 60, 64, 72, 80, 96, 128, 144, 192,
                224, 256, 288, 320, 336, 384, 400, 416, 432, 448, 464, 480};
        public static final String[] level_str = {
                "RETENTION",
                "MIN_SVS",
                "LOW_SVS_D2",
                "LOW_SVS_D1",
                "LOW_SVS_D0",
                "LOW_SVS",
                "LOW_SVS_P1",
                "LOW_SVS_L1",
                "LOW_SVS_L2",
                "SVS",
                "SVS_L0",
                "SVS_L1",
                "SVS_L2",
                "NOM",
                "NOM_L0",
                "NOM_L1",
                "NOM_L2",
                "TURBO",
                "TURBO_L0",
                "TURBO_L1",
                "TURBO_L2",
                "TURBO_L3",
                "SUPER_TURBO",
                "SUPER_TURBO_NO_CPR"
        };
    }
}

package uz.fido.pfexchange.config;

import lombok.Getter;

@Getter
public enum Authority {
    ADMIN_PANEL(Codes.ADMIN_PANEL, "Admin panel"),
    GET_CHARGE_INFO(Codes.GET_CHARGE_INFO, "Qarzdorlik ma'lumotini olish"),
    GET_CHARGE_HIST(Codes.GET_CHARGE_HIST, "Qarzdorlik tarixini olish"),
    MIP_PAY_TYPE_INFO(Codes.MIP_PAY_TYPE_INFO, "To'lov turi ma'lumotini olish"),
    MIP_PAY_TYPE_CHANGE(Codes.MIP_PAY_TYPE_CHANGE, "To'lov turini o'zgartirish"),
    GET_MIP_INFO(Codes.GET_MIP_INFO, "Pensiya ma'lumotini olish"),
    GET_PERSON_ABROAD_STATUS(Codes.GET_PERSON_ABROAD_STATUS, "Pensiya oluvchi holatini tekshirish"),
    RESTORE_PERSON_ABROAD_STATUS(
            Codes.RESTORE_PERSON_ABROAD_STATUS, "Pensiya oluvchi holatini tiklash"),
    GET_STATISTICS(Codes.GET_STATISTICS, "Statistika ma'lumotlarini olish"),
    INTERNAL_MILITARY_SEND_REQUEST(
            Codes.INTERNAL_MILITARY_SEND_REQUEST, "Harbiy ish tajribasi so'rov yuborish"),
    INTERNAL_MINYUST_FAMILY_PROCESS_ONE_BATCH(
            Codes.INTERNAL_MINYUST_FAMILY_PROCESS_ONE_BATCH, "Bitta batch ni qayta ishlash"),
    INTERNAL_MINYUST_FAMILY_START_PROCESSING(
            Codes.INTERNAL_MINYUST_FAMILY_START_PROCESSING, "Uzluksiz qayta ishlashni boshlash"),
    INTERNAL_MINYUST_FAMILY_STOP_PROCESSING(
            Codes.INTERNAL_MINYUST_FAMILY_STOP_PROCESSING, "Qayta ishlashni to'xtatish"),
    INTERNAL_MINYUST_FAMILY_GET_STATUS(
            Codes.INTERNAL_MINYUST_FAMILY_GET_STATUS, "Jarayon holatini olish"),
    INTERNAL_MINYUST_FAMILY_GET_PROGRESS(
            Codes.INTERNAL_MINYUST_FAMILY_GET_PROGRESS, "Jarayon progressini olish"),
    INTERNAL_MINYUST_FAMILY_RECOVER_STUCK(
            Codes.INTERNAL_MINYUST_FAMILY_RECOVER_STUCK, "Tiqilib qolgan yozuvlarni tiklash"),
    INTERNAL_MINYUST_FAMILY_CONFIG(
            Codes.INTERNAL_MINYUST_FAMILY_CONFIG, "Konfiguratsiya sozlamalarini olish"),
    INTERNAL_TEST_ENDPOINT(Codes.INTERNAL_TEST_ENDPOINT, "Tashqi endpoint ni tekshirish"),
    INTERNAL_MIP_PUSH_DELIVERY_STATISTICS_DAILY(
            Codes.INTERNAL_MIP_PUSH_DELIVERY_STATISTICS_DAILY,
            "MIP Push xizmatidan kunlik statistikalarini olish"),
    INTERNAL_MIP_PUSH_PING(Codes.INTERNAL_MIP_PUSH_PING, "MIP Push xizmatiga ping yuborish"),
    INTERNAL_MIP_PUSH_TOKEN(Codes.INTERNAL_MIP_PUSH_TOKEN, "MIP Push xizmati uchun token olish"),
    INTERNAL_MIP_PUSH_PUSH(Codes.INTERNAL_MIP_PUSH_PUSH, "MIP Push xizmatiga ma'lumot yuborish"),
    INTERNAL_MIP_PUSH_DETAILED_REPORT(Codes.INTERNAL_MIP_PUSH_DETAILED_REPORT, "MIP Push xizmatidan so'rov hisobotini olish");

    private final String code;
    private final String displayName;

    Authority(String code, String displayName) {
        if (!this.name().equals(code)) {
            throw new IllegalArgumentException(
                    "Authority code mismatch: enum=" + this.name() + ", code=" + code);
        }
        this.code = code;
        this.displayName = displayName;
    }

    public static final class Codes {

        // ADMIN
        public static final String ADMIN_PANEL = "ADMIN_PANEL";

        // CHARGE
        public static final String GET_CHARGE_INFO = "GET_CHARGE_INFO";
        public static final String GET_CHARGE_HIST = "GET_CHARGE_HIST";

        // PAY TYPE
        public static final String MIP_PAY_TYPE_INFO = "MIP_PAY_TYPE_INFO";
        public static final String MIP_PAY_TYPE_CHANGE = "MIP_PAY_TYPE_CHANGE";
        public static final String GET_MIP_INFO = "GET_MIP_INFO";

        public static final String GET_PERSON_ABROAD_STATUS = "GET_PERSON_ABROAD_STATUS";
        public static final String RESTORE_PERSON_ABROAD_STATUS = "RESTORE_PERSON_ABROAD_STATUS";

        // STATISTICS
        public static final String GET_STATISTICS = "GET_STATISTICS";

        // INTERNAL
        public static final String INTERNAL_MILITARY_SEND_REQUEST =
                "INTERNAL_MILITARY_SEND_REQUEST";
        public static final String INTERNAL_MINYUST_FAMILY_PROCESS_ONE_BATCH =
                "INTERNAL_MINYUST_FAMILY_PROCESS_ONE_BATCH";
        public static final String INTERNAL_MINYUST_FAMILY_START_PROCESSING =
                "INTERNAL_MINYUST_FAMILY_START_PROCESSING";
        public static final String INTERNAL_MINYUST_FAMILY_STOP_PROCESSING =
                "INTERNAL_MINYUST_FAMILY_STOP_PROCESSING";
        public static final String INTERNAL_MINYUST_FAMILY_GET_STATUS =
                "INTERNAL_MINYUST_FAMILY_GET_STATUS";
        public static final String INTERNAL_MINYUST_FAMILY_GET_PROGRESS =
                "INTERNAL_MINYUST_FAMILY_GET_PROGRESS";
        public static final String INTERNAL_MINYUST_FAMILY_RECOVER_STUCK =
                "INTERNAL_MINYUST_FAMILY_RECOVER_STUCK";
        public static final String INTERNAL_MINYUST_FAMILY_CONFIG =
                "INTERNAL_MINYUST_FAMILY_CONFIG";
        public static final String INTERNAL_TEST_ENDPOINT = "INTERNAL_TEST_ENDPOINT";
        public static final String INTERNAL_MIP_PUSH_TOKEN = "INTERNAL_MIP_PUSH_TOKEN";
        public static final String INTERNAL_MIP_PUSH_DELIVERY_STATISTICS_DAILY =
                "INTERNAL_MIP_PUSH_DELIVERY_STATISTICS_DAILY";
        public static final String INTERNAL_MIP_PUSH_PING = "INTERNAL_MIP_PUSH_PING";
        public static final String INTERNAL_MIP_PUSH_PUSH = "INTERNAL_MIP_PUSH_PUSH";
        public static final String INTERNAL_MIP_PUSH_DETAILED_REPORT = "INTERNAL_MIP_PUSH_DETAILED_REPORT";

        // MIB
        public static final String SEND_CANCEL_DEBT = "SEND_CANCEL_DEBT";


        private Codes() {}
    }
}

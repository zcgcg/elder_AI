package com.daisy.health.ai;

import java.util.regex.Pattern;

/** Removes common personal identifiers and credentials before chat text leaves this server. */
final class SensitiveDataRedactor {
    private static final String REDACTED = "[REDACTED]";
    private static final Pattern MOBILE = Pattern.compile("(?<!\\d)1[3-9]\\d{9}(?!\\d)");
    private static final Pattern ID_CARD = Pattern.compile("(?<!\\d)(?:\\d{17}[0-9Xx]|\\d{15})(?!\\d)");
    private static final Pattern JWT = Pattern.compile("(?i)\\beyJ[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b");
    private static final Pattern BEARER = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._-]{8,}");
    private static final Pattern SECRET_KEY = Pattern.compile("(?i)\\b(?:sk|ak)-[A-Za-z0-9_-]{8,}\\b");
    private static final Pattern LABELED_SECRET = Pattern.compile("(?i)(api[_ ]?key|密钥|秘钥)\\s*(?:[:：=]|是|为)\\s*[A-Za-z0-9._-]{8,}");
    private static final Pattern LABELED_ADDRESS = Pattern.compile("(家庭住址|住址|地址)\\s*(?:[:：=]|是|为)\\s*[^，,。；;！？!?\\r\\n]{2,80}");
    private static final Pattern FIRST_PERSON_ADDRESS = Pattern.compile("(我家在|家住|我住在)\\s*[^，,。；;！？!?\\r\\n]{1,80}(?:省|市|区|县|镇|乡|村|街道|路|街|弄|巷|号)[^，,。；;！？!?\\r\\n]{0,30}");
    private static final Pattern EMERGENCY_CONTACT = Pattern.compile("(紧急联系人|应急联系人)\\s*(?:[:：=]|是|为)\\s*[^，,。；;！？!?\\r\\n]{1,50}");
    private static final Pattern NATURAL_EMERGENCY_CONTACT = Pattern.compile("(我的)?(紧急联系人|应急联系人)\\s*(?:叫|姓名是|名字是)\\s*[^，,。；;！？!?\\r\\n]{1,50}");
    private static final Pattern LABELED_PHONE = Pattern.compile("(手机号码|手机号|联系电话|电话)\\s*(?:[:：=]|是|为)\\s*[0-9()（）+ -]{6,24}");
    private static final Pattern ACCOUNT_ID = Pattern.compile("(?i)(account[_ ]?id|账号ID|账号编号)\\s*(?:[:：=]|是|为)\\s*[A-Za-z0-9_-]{1,64}");

    private SensitiveDataRedactor() {
    }

    static String redact(String value) {
        if (value == null || value.isEmpty()) return "";
        String redacted = LABELED_ADDRESS.matcher(value).replaceAll("$1：" + REDACTED);
        redacted = FIRST_PERSON_ADDRESS.matcher(redacted).replaceAll("$1" + REDACTED);
        redacted = EMERGENCY_CONTACT.matcher(redacted).replaceAll("$1：" + REDACTED);
        redacted = NATURAL_EMERGENCY_CONTACT.matcher(redacted).replaceAll("$1$2：" + REDACTED);
        redacted = LABELED_PHONE.matcher(redacted).replaceAll("$1：" + REDACTED);
        redacted = ACCOUNT_ID.matcher(redacted).replaceAll("$1=" + REDACTED);
        redacted = MOBILE.matcher(redacted).replaceAll(REDACTED);
        redacted = ID_CARD.matcher(redacted).replaceAll(REDACTED);
        redacted = JWT.matcher(redacted).replaceAll(REDACTED);
        redacted = BEARER.matcher(redacted).replaceAll(REDACTED);
        redacted = SECRET_KEY.matcher(redacted).replaceAll(REDACTED);
        return LABELED_SECRET.matcher(redacted).replaceAll("$1=" + REDACTED);
    }
}

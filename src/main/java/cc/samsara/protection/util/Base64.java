package cc.samsara.protection.util;

/**
 * Base64 工具桩。
 * 使用 java.util.Base64 作为底层实现。
 * 后续如需替换为自定义 Base64 实现，直接修改此文件即可。
 */
public final class Base64 {

    private Base64() {}

    /**
     * Decode a Base64 encoded string.
     * @param encoded Base64 encoded string
     * @return decoded bytes
     */
    public static byte[] decode(String encoded) {
        return java.util.Base64.getDecoder().decode(encoded);
    }
}

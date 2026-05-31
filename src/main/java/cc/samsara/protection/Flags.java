package cc.samsara.protection;

import club.serenityutils.cloudconfigs.api.ICloudConfig;
import club.serenityutils.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证状态标志桩。
 * 提供编译所需的最小接口，后续替换为新认证系统时直接修改此文件。
 */
public final class Flags {

    /** 云端配置列表 */
    public static final List<ICloudConfig> cloudConfigs = new ArrayList<>();

    /** 当前用户信息 */
    public static User user = null;

    private Flags() {}
}

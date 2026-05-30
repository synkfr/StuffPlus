package me.ayosynk.stuff.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;

@Names(strategy = NameStrategy.HYPHEN_CASE)
public class PluginConfig extends OkaeriConfig {

    @Comment("Storage type: SQLITE or MYSQL")
    private String storageType = "sqlite";

    @Comment("MySQL Connection Details (only used if storage-type is MYSQL)")
    private String mysqlHost = "localhost";
    private int mysqlPort = 3306;
    private String mysqlDatabase = "minecraft";
    private String mysqlUsername = "root";
    private String mysqlPassword = "password";
    private int mysqlPoolSize = 10;
    private boolean mysqlUseSsl = false;

    @Comment("Vanish settings")
    private boolean vanishSilentContainerClicks = true;
    private boolean vanishIgnorePressurePlates = true;
    private boolean vanishDisableMobTargeting = true;
    private boolean vanishDisableItemPickup = true;

    public String getStorageType() {
        return storageType;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public int getMysqlPoolSize() {
        return mysqlPoolSize;
    }

    public boolean isMysqlUseSsl() {
        return mysqlUseSsl;
    }

    public boolean isVanishSilentContainerClicks() {
        return vanishSilentContainerClicks;
    }

    public boolean isVanishIgnorePressurePlates() {
        return vanishIgnorePressurePlates;
    }

    public boolean isVanishDisableMobTargeting() {
        return vanishDisableMobTargeting;
    }

    public boolean isVanishDisableItemPickup() {
        return vanishDisableItemPickup;
    }
}

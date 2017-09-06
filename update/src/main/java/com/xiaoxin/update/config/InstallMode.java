package com.xiaoxin.update.config;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

public enum InstallMode {

    ROOT("root"), PM("pm"), SYSTEM("system");

    private String name;

    InstallMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "InstallMode{" +
                "name='" + name + '\'' +
                '}';
    }

}

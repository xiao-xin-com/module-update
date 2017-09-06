package com.xiaoxin.update;

import com.xiaoxin.update.bean.VersionInfo;

/**
 * Created by liyuanbiao on 2016/9/18.
 */

public interface VersionInfoProvider {
    VersionInfo provider(String result);
}

package com.xiaoxin.update;

import com.xiaoxin.update.bean.XXVersionInfo;

/**
 * Created by liyuanbiao on 2016/9/18.
 */

public interface XXVersionInfoProvider {
    XXVersionInfo provider(String result);
}

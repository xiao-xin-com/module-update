package com.xiaoxin.update.util;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by liyuanbiao on 2016/9/17.
 */

public class CmdUtil {
    private static final String TAG = "CmdUtil";

    /**
     * 执行终端命令
     *
     * @param cmd
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int execRootCmdSilent(String cmd) throws IOException, InterruptedException {
        int result = -1;
        DataOutputStream dos = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            Log.i(TAG, cmd);
            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            p.waitFor();
            result = p.exitValue();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 检查设备是否被root
     *
     * @return
     */
    public static boolean isRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();
            int i = process.waitFor();
            if (0 == i) {
                process = Runtime.getRuntime().exec("su");
                return true;
            }

        } catch (Exception e) {
            return false;
        }
        return false;

    }
}

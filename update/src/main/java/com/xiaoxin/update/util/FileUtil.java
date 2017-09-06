package com.xiaoxin.update.util;

import android.os.Environment;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;

/**
 * Created by liyuanbiao on 2016/3/2.
 */
public class FileUtil {

    private static final String TAG = "DkFileUtil";
    public static final long ONE_KB = 1024;

    public static final BigInteger ONE_KB_BI = BigInteger.valueOf(ONE_KB);

    public static final long ONE_MB = ONE_KB * ONE_KB;

    public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    public static final long ONE_GB = ONE_KB * ONE_MB;

    public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

    public static final long ONE_TB = ONE_KB * ONE_GB;

    public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

    public static final long ONE_PB = ONE_KB * ONE_TB;

    public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

    public static final long ONE_EB = ONE_KB * ONE_PB;

    public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);

    public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

    public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);

    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static File newFile(String... paths) {
        return getFile(paths);
    }

    public static String getFilePath(String... paths) {
        return getFile(paths).getAbsolutePath();
    }

    public static boolean checkSdCard() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }

    public static File getFile(File directory, String... names) {
        if (directory == null) {
            throw new NullPointerException("directorydirectory must not be null");
        }
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = directory;
        for (String name : names) {
            file = new File(file, name);
        }
        return file;
    }


    public static File getFile(String... names) {
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = null;
        for (String name : names) {
            if (file == null) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }

    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static String byteCountToDisplaySize(BigInteger size) {
        String displaySize;

        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_EB_BI)) + " EB";
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_PB_BI)) + " PB";
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_TB_BI)) + " TB";
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_GB_BI)) + " GB";
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_MB_BI)) + " MB";
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_KB_BI)) + " KB";
        } else {
            displaySize = String.valueOf(size) + " M";
        }
        return displaySize;
    }


    public static void closeQuiet(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * byte单位换算
     *
     * @param size
     * @return
     */
    public static String byteCountToDisplaySize(long size) {
        return byteCountToDisplaySize(BigInteger.valueOf(size));
    }

    /**
     * 获取文件大小
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        long fileSize = 0;
        if (file.isFile()) {
            fileSize = file.length();
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    fileSize += getFileSize(f);
                }
            }
        }
        return fileSize;
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public static boolean delete(File file) {
        boolean result = true;
        if (file.isFile()) {
            result = file.delete();
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    result &= delete(f);
                }
            }
        }
        return result;
    }

    public static boolean copy(InputStream in, OutputStream out) {
        boolean flag = true;
        int len;
        byte[] buf = new byte[1024 * 4];
        try {
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (IOException e) {
            flag = false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return flag;
    }

    public static boolean copy(InputStream in, File target) {
        File parentFile = target.getParentFile();
        if (!parentFile.exists()) parentFile.mkdirs();
        try {
            return copy(in, new FileOutputStream(target));
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public static void mkdirsParent(File file) {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

}

package com.xiaoxin.update.task.install;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.os.RemoteException;

import com.xiaoxin.update.helper.ListenerHelper;
import com.xiaoxin.update.helper.UpdateStatusChangeObserver;
import com.xiaoxin.update.listener.UpdateStatus;
import com.xiaoxin.update.util.UpdateLog;
import com.xiaoxin.update.util.UpdateUtil;

import java.io.File;

/**
 * Created by liyuanbiao on 2017/9/6.
 */

class PmInstallTask extends InstallTask {

    public static final int INSTALL_SUCCEEDED = 1;
    public static final int INSTALL_FAILED_ALREADY_EXISTS = -1;
    public static final int INSTALL_FAILED_INVALID_APK = -2;
    public static final int INSTALL_FAILED_INVALID_URI = -3;
    public static final int INSTALL_FAILED_INSUFFICIENT_STORAGE = -4;
    public static final int INSTALL_FAILED_DUPLICATE_PACKAGE = -5;
    public static final int INSTALL_FAILED_NO_SHARED_USER = -6;
    public static final int INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7;
    public static final int INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8;
    public static final int INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9;
    public static final int INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10;
    public static final int INSTALL_FAILED_DEXOPT = -11;
    public static final int INSTALL_FAILED_OLDER_SDK = -12;
    public static final int INSTALL_FAILED_CONFLICTING_PROVIDER = -13;
    public static final int INSTALL_FAILED_NEWER_SDK = -14;
    public static final int INSTALL_FAILED_TEST_ONLY = -15;
    public static final int INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16;
    public static final int INSTALL_FAILED_MISSING_FEATURE = -17;
    public static final int INSTALL_FAILED_CONTAINER_ERROR = -18;
    public static final int INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19;
    public static final int INSTALL_FAILED_MEDIA_UNAVAILABLE = -20;
    public static final int INSTALL_FAILED_VERIFICATION_TIMEOUT = -21;
    public static final int INSTALL_FAILED_VERIFICATION_FAILURE = -22;
    public static final int INSTALL_FAILED_PACKAGE_CHANGED = -23;
    public static final int INSTALL_FAILED_UID_CHANGED = -24;
    public static final int INSTALL_FAILED_VERSION_DOWNGRADE = -25;
    public static final int INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE = -26;
    public static final int INSTALL_PARSE_FAILED_NOT_APK = -100;
    public static final int INSTALL_PARSE_FAILED_BAD_MANIFEST = -101;
    public static final int INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION = -102;
    public static final int INSTALL_PARSE_FAILED_NO_CERTIFICATES = -103;
    public static final int INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES = -104;
    public static final int INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING = -105;
    public static final int INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME = -106;
    public static final int INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID = -107;
    public static final int INSTALL_PARSE_FAILED_MANIFEST_MALFORMED = -108;
    public static final int INSTALL_PARSE_FAILED_MANIFEST_EMPTY = -109;
    public static final int INSTALL_FAILED_INTERNAL_ERROR = -110;
    public static final int INSTALL_FAILED_USER_RESTRICTED = -111;
    public static final int INSTALL_FAILED_DUPLICATE_PERMISSION = -112;
    public static final int INSTALL_FAILED_NO_MATCHING_ABIS = -113;
    public static final int NO_NATIVE_LIBRARIES = -114;
    public static final int INSTALL_FAILED_ABORTED = -115;

    private Context context;
    private IPackageInstallObserver observer;

    PmInstallTask(Context context, String filePath, IPackageInstallObserver observer) {
        super(filePath);
        this.context = context;
        this.observer = observer;
    }

    @Override
    public void run() {
        UpdateLog.d("PmInstallTask run() called");
        UpdateStatusChangeObserver statusChangeObserver = getStatusChangeObserver();
        try {
            UpdateLog.d("PmInstallTask run() start");
            dispatchOnStart();
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_START);
            UpdateUtil.startPmInstall(context, new File(getFilePath()), new PackageInstallObserver(observer));
        } catch (Exception e) {
            dispatchOnError(e);
            statusChangeObserver.onUpdateStatusChange(UpdateStatus.STATUS_INSTALL_ERROR);
            UpdateLog.e("PmInstallTask run: ", e);
        }
    }

    private class PackageInstallObserver extends IPackageInstallObserver.Stub {

        private IPackageInstallObserver observer;

        PackageInstallObserver(IPackageInstallObserver observer) {
            this.observer = observer;
        }

        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            if (observer != null) {
                observer.packageInstalled(packageName, returnCode);
            }
            UpdateStatusChangeObserver statusChangeObserver = ListenerHelper.getStatusChangeObserver();
            statusChangeObserver.onUpdateStatusChange(returnCode == INSTALL_SUCCEEDED ?
                    UpdateStatus.STATUS_INSTALL_COMPLETE : UpdateStatus.STATUS_INSTALL_ERROR);
            if (returnCode == INSTALL_SUCCEEDED) {
                UpdateLog.d("PmInstallTask run() complete");
                dispatchOnComplete();
            } else {
                UpdateLog.d("PmInstallTask run() error " +
                        "packageName: " + packageName + "\treturnCode: " + returnCode);
                dispatchOnError(new IllegalStateException(packageName + " : " + returnCode));
            }

        }
    }
}

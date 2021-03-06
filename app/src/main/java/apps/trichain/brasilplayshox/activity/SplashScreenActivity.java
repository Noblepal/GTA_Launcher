package apps.trichain.brasilplayshox.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import apps.trichain.brasilplayshox.R;
import apps.trichain.brasilplayshox.databinding.ActivitySplashScreenBinding;
import apps.trichain.brasilplayshox.fragment.WebViewFragment;
import apps.trichain.brasilplayshox.model.Links;
import apps.trichain.brasilplayshox.service.FileDownloadService;
import apps.trichain.brasilplayshox.util.NetworkController;
import apps.trichain.brasilplayshox.util.SharedPrefsManager;
import apps.trichain.brasilplayshox.util.util;
import apps.trichain.brasilplayshox.viewModel.GameViewModel;

import static apps.trichain.brasilplayshox.util.util.ANDROID_DATA_DIR;
import static apps.trichain.brasilplayshox.util.util.ANDROID_OBB_DIR;
import static apps.trichain.brasilplayshox.util.util.APK_FILE;
import static apps.trichain.brasilplayshox.util.util.BRASIL_PLAY_SHOX_DIR;
import static apps.trichain.brasilplayshox.util.util.DATA_FILE;
import static apps.trichain.brasilplayshox.util.util.DATA_FILE_PATH;
import static apps.trichain.brasilplayshox.util.util.GTA_SA_PACKAGE_NAME;
import static apps.trichain.brasilplayshox.util.util.OBB_FILE;
import static apps.trichain.brasilplayshox.util.util.OBB_FILE_PATH;
import static apps.trichain.brasilplayshox.util.util.humanify;
import static apps.trichain.brasilplayshox.util.util.saveNickName;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 325;
    private static final int VIEW_DEFAULT = 0;
    private static final int VIEW_DOWNLOADING = 1;
    private static final int VIEW_UPDATE = 2;
    private static final int VIEW_APP_NOT_INSTALLED = 3;
    private static final int DATA_NOT_FOUND = 4;
    private static final int OBB_NOT_FOUND = 5;
    private static final String FILE_DATA = "file_data";
    private static final String FILE_OBB = "file_obb";
    private static final String FILE_APK = "file_apk";

    private GameViewModel viewModel;
    private NetworkController networkController;
    private DatabaseReference dbReference;
    private ValueEventListener urlEventListener;
    private Links links, mLinks;
    private SharedPrefsManager sharedPrefsManager;
    private String serverFilePath = "";
    private boolean hasDownloadedData;
    private boolean hasDownloadedOBB;
    private ActivitySplashScreenBinding b;
    private static final String TAG = "SplashScreenActivity";


    private AppUpdateManager appUpdateManager;
    private static final int RC_APP_UPDATE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);

        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.registerListener(installListener);

        checkUpdate();

        dbReference = FirebaseDatabase.getInstance().getReference();
        sharedPrefsManager = SharedPrefsManager.getInstance(this);
        networkController = NetworkController.getInstance(this);
        PackageManager manager = getPackageManager();

        /*Check if user launched the app for the
         * first time and download links*/
        downloadLinks();

        if (sharedPrefsManager.isFirstTimeLaunch()) {
            Log.e(TAG, "onCreate: First time launch ");
            checkForUpdates();
        } else {
            links = Links.create(sharedPrefsManager.getLinks());
            Log.e(TAG, "onCreate: Not first time launch, links: " + links);
        }

        verifyNickName();

        //Init view model
        ViewModelProvider.AndroidViewModelFactory factory =
                new ViewModelProvider.AndroidViewModelFactory(this.getApplication());
        viewModel = new ViewModelProvider(this, factory).get(GameViewModel.class);


        //Check if com.rockstargames.gtasa is installed on device
        viewModel.setIsPackageInstalled(util.isPackageInstalled(GTA_SA_PACKAGE_NAME, manager));

        /*Check write external storage permissions*/
        if (checkPermissions()) {
            Log.i(TAG, "onCreate: Permissions granted");
        } else {
            mRequestPermissions();
        }


        /*Show relevant views if package is installed or not*/
        viewModel.getIsPackageInstalled().observe(this, isPackageInstalled -> {
            if (isPackageInstalled) {
                Log.e(TAG, "onCreate: Package is installed");
                if (!util.isDataPathExists()) {
                    Log.e(TAG, "onCreate: DATA not found");
                    toggleViews(DATA_NOT_FOUND);
                } else if (!util.isOBBPathExists()) {
                    Log.e(TAG, "onCreate: OBB not found");
                    toggleViews(OBB_NOT_FOUND);
                } else {
                    Log.e(TAG, "onCreate: Everything up to date");
                    toggleViews(VIEW_DEFAULT);
                }
            } else {
                toggleViews(VIEW_APP_NOT_INSTALLED);
                b.btnDownloadGTA.setText(getResources().getString(R.string.download_gta, "_Latest" /*String.valueOf(links.getAppVersion())*/));
                b.btnDownloadGTA.setOnClickListener(v -> {
                    downloadFile(FILE_APK);
                });
            }
        });

        viewModel.getmIsExtraxtingFiles().observe(this, isExtractingFiles -> {
            if (isExtractingFiles) {
                b.tvDownloadProgress.setText(R.string.extracting_files);
                b.pbDownloading.setIndeterminate(true);
            } else {
                b.pbDownloading.setIndeterminate(false);
                if (hasDownloadedData) {
                    hasDownloadedData = false;
                    //download next file
                    getOBBFileURL();
                } else {
                    Log.e(TAG, "doInBackground: Download is complete: file(s)");
                    b.pbDownloading.setProgress(100);
                    Toast.makeText(this, R.string.download_complete, Toast.LENGTH_SHORT).show();
                    sharedPrefsManager.setHasDownloadedAllFiles(true);
                    sharedPrefsManager.saveUpdateDate(Calendar.getInstance().getTime().toString());
                    sharedPrefsManager.updateLinks(mLinks);
                    toggleViews(VIEW_DEFAULT);
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    /*util.hideView(b.clAppNotInstalled, true);
                    util.hideView(b.llDownloading);, true*/
                }

            }
        });

        /*if (!sharedPrefsManager.checkHasDownloadedAllFiles()) {
            toggleViews(VIEW_DEFAULT);
            *//*util.showView(b.clAppNotInstalled);
            util.hideView(b.llDownloading);*//*
        } else {
            util.hideView(b.clAppNotInstalled, true);
            util.showView(b.llDownloading, true);
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }*/

        /*startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();*/

    }

    private void verifyNickName() {
        if (checkHasSavedNickName()) {
            View mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save_nickname, null);
            TextInputEditText edtNickName = mDialogView.findViewById(R.id.edtDialogNickName);
            edtNickName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
            edtNickName.setOnFocusChangeListener((v, hasFocus) -> {
                if (edtNickName.getText().toString().trim().length() < 3)
                    edtNickName.setError("Minimum length is 3");
                else
                    edtNickName.setError(null);

            });
            new AlertDialog.Builder(this)
                    .setTitle("Entre com seu apelido")
                    .setView(mDialogView)
                    .setCancelable(false)
                    .setPositiveButton("Salvar", (dialog, which) -> {
                        String mNickName = edtNickName.getText().toString().trim();
                        if (!TextUtils.isEmpty(mNickName)) {
                            if (mNickName.length() < 3) {
                                Toast.makeText(this, "O comprimento mínimo é 3", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.i(TAG, "onCreateView: Saving nickname: " + mNickName);
                                saveNickName(this, mNickName);
                            }
                        } else {

                            Toast.makeText(this, "Digite o apelido", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
        }
    }

    private InputFilter filter = (source, start, end, dest, dstart, dend) -> {

        String blockCharacterSet = ".,;@~#^|$%&*!()<>?/:\"'|+= ";
        if (source != null && blockCharacterSet.contains(("" + source))) {
            return "";
        }
        return null;
    };

    private boolean checkHasSavedNickName() {
        return TextUtils.isEmpty(sharedPrefsManager.getNickName());
    }


    private void toggleViews(int viewID) {
        util.hideView(b.llUpdateData, true);
        util.hideView(b.clAppNotInstalled, true);
        util.hideView(b.clDataNotFound, true);
        util.hideView(b.clOBBNotFound, true);
        util.hideView(b.llDefault, true);
        util.hideView(b.llDownloading, true);
        switch (viewID) {
            case VIEW_DOWNLOADING:
                util.showView(b.llDownloading, true);
                break;
            case VIEW_APP_NOT_INSTALLED:
                util.showView(b.clAppNotInstalled, true);
                b.btnDownloadGTA.setOnClickListener(v -> downloadFile(FILE_APK));
                break;
            case VIEW_UPDATE:
                util.showView(b.llUpdateData, true);
                b.tvUpdateData.setOnClickListener(v -> downloadFile(FILE_DATA));
                break;
            case DATA_NOT_FOUND:
                util.showView(b.clDataNotFound, true);
                b.btnDownloadData.setOnClickListener(v -> downloadFile(FILE_DATA));
                break;
            case OBB_NOT_FOUND:
                util.showView(b.clOBBNotFound, true);
                b.btnDownloadOBB.setOnClickListener(v -> downloadFile(FILE_OBB));
                break;
            default://TODO
                util.showView(b.llDefault, true);
                b.tvProceed.setOnClickListener(v -> {
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                });
        }
    }

    /*Download Data and OBB links from firebase*/
    private void downloadLinks() {
        urlEventListener = dbReference.child("links").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLinks = dataSnapshot.getValue(Links.class);

                assert mLinks != null;
                sharedPrefsManager.updateLinks(mLinks);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
        sharedPrefsManager.setIsFirstTimeLaunch(false);
    }

    private void checkForUpdates() {
        Toast.makeText(this, "Checking for updates", Toast.LENGTH_SHORT).show();
        urlEventListener = dbReference.child("links").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLinks = dataSnapshot.getValue(Links.class);
                assert mLinks != null;
                if (mLinks.getUpdateVersion() > links.getUpdateVersion()) {
                    //Toast.makeText(SplashScreenActivity.this, "Update available", Toast.LENGTH_SHORT).show();
                    util.showView(b.tvUpdateData, true);
                    b.tvUpdateData.setOnClickListener(v -> {
                        toggleViews(VIEW_UPDATE);
                        downloadFile(FILE_DATA);
                    });

                    b.tvSkip.setOnClickListener(c -> {
                        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void getOBBFileURL() {
        downloadOBBFile();
        viewModel.getDownloadURL().observe(this, obbDownloadURL -> {
            //serverFilePath = links.getObbURL();
            serverFilePath = obbDownloadURL;
            if (!hasDownloadedData) {
                downloadFile(FILE_OBB);
                hasDownloadedOBB = true;
            }
        });
    }

    private void downloadFile(String fileToDownload) {
        toggleViews(VIEW_DOWNLOADING);
        if (links == null) {
            downloadLinks();
            Toast.makeText(this, "Tente novamente", Toast.LENGTH_SHORT).show();
            return;
        }
        b.tvDownloadProgress.setText(R.string.preparing);
        b.pbDownloading.setIndeterminate(true);
        String downloadedFileName = "";
        if (fileToDownload.equals(FILE_DATA)) {
            serverFilePath = links.getDataURL();
            Log.e(TAG, "downloadFile: Data URL: " + serverFilePath);
            downloadedFileName = DATA_FILE;
        } else if (fileToDownload.equals(FILE_OBB)) {//Download OBB File
            serverFilePath = links.getObbURL();
            downloadedFileName = OBB_FILE;
            Log.e(TAG, "downloadFile: OBB URL: " + serverFilePath);
        } else {
            serverFilePath = links.getApkURL();
            Log.e(TAG, "downloadFile: APK URL: " + serverFilePath);
            downloadedFileName = APK_FILE;
        }

        //serverFilePath = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-zip-file.zip";

        String downloadPath = util.getDownloadedPath().getPath();
        File file = new File(downloadPath, downloadedFileName);

        String localPath = file.getPath();
        String unzipPath = util.getDataDir().getPath();

        FileDownloadService.DownloadRequest downloadRequest = new FileDownloadService.DownloadRequest(serverFilePath, localPath);
        downloadRequest.setRequiresUnzip(false);
        downloadRequest.setDeleteZipAfterExtract(false);
        downloadRequest.setUnzipAtFilePath(unzipPath);

        FileDownloadService.OnDownloadStatusListener listener = new FileDownloadService.OnDownloadStatusListener() {

            @Override
            public void onDownloadStarted() {
                b.pbDownloading.setIndeterminate(false);
            }

            @Override
            public void onDownloadCompleted() {
                if (fileToDownload.equals(FILE_DATA))
                    unpackZip(true);
                else if (fileToDownload.equals(FILE_OBB)) unpackZip(false);
                else openAppInstaller();
            }

            @Override
            public void onDownloadFailed() {
                b.tvDownloadProgress.setText(getResources().getString(R.string.download_failed,
                        fileToDownload.equals(FILE_DATA) ? links.getDataURL() : links.getObbURL()));
                b.pbDownloading.setIndeterminate(false);
                b.pbDownloading.setProgress(0);
            }

            @Override
            public void onDownloadProgress(int lengthOfFile, int progress) {
                b.pbDownloading.setProgress(progress);
                if (fileToDownload.equals(FILE_DATA)) {
                    b.tvDownloadProgress.setText(getResources().getString(R.string.downloading, "GTA Data File", humanify(lengthOfFile), links.getAppVersion(), progress));
                } else if (fileToDownload.equals(FILE_OBB)) {
                    b.tvDownloadProgress.setText(getResources().getString(R.string.downloading, "GTA Additional OBB Files", humanify(lengthOfFile), links.getAppVersion(), progress));
                } else {
                    b.tvDownloadProgress.setText(getResources().getString(R.string.downloading, "GTA APK File", humanify(lengthOfFile), links.getAppVersion(), progress));
                }
            }
        };

        FileDownloadService.FileDownloader downloader = FileDownloadService.FileDownloader.getInstance(downloadRequest, listener);
        downloader.download(this);
    }

    private void openAppInstaller() {
        /*File file = new File(BRASIL_PLAY_SHOX_DIR, APK_FILE);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider", file),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);*/
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(BRASIL_PLAY_SHOX_DIR, APK_FILE)),"application/vnd.android.package-archive");
        startActivity(intent);*/
        Toast.makeText(this, R.string.navigate_to_directory,
                Toast.LENGTH_LONG).show();
    }

    private void downloadOBBFile() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("webview");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = new WebViewFragment();
        dialogFragment.show(ft, "webview");
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void mRequestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    InputStream is = null;
    ZipInputStream zis;
    String downloadedPath = "", extractPath = "";
    File extract_target = new File(Environment.getExternalStorageDirectory() + ANDROID_DATA_DIR);
    File downloaded_path = new File(Environment.getExternalStorageDirectory() + BRASIL_PLAY_SHOX_DIR);

    @SuppressLint("StaticFieldLeak")
    private void unpackZip(boolean isDataFile) {
        viewModel.setIsExtractingFiles(true);

        if (!isDataFile) {//Is OBB File
            extract_target = new File(Environment.getExternalStorageDirectory() + ANDROID_OBB_DIR);
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (!downloaded_path.exists()) downloaded_path.mkdirs();
                if (!extract_target.exists()) extract_target.mkdirs();

                downloadedPath = downloaded_path.getPath();
                extractPath = extract_target.getPath() + "/";

                try {
                    if (isDataFile)
                        is = new FileInputStream(downloaded_path + DATA_FILE_PATH);
                    else is = new FileInputStream(downloaded_path + OBB_FILE_PATH);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(SplashScreenActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    String filename;
                    //is = new FileInputStream(path + zip_name);
                    zis = new ZipInputStream(new BufferedInputStream(is));
                    ZipEntry ze;
                    byte[] buffer = new byte[1024];
                    int count;

                    while ((ze = zis.getNextEntry()) != null) {
                        filename = ze.getName();

                        // Need to create directories if not exists, or
                        // it will generate an Exception...
                        if (ze.isDirectory()) {
                            File fmd = new File(extractPath + filename);
                            fmd.mkdirs();
                            continue;
                        }

                        FileOutputStream fout = new FileOutputStream(extractPath + filename);

                        while ((count = zis.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }

                        fout.close();
                        zis.closeEntry();
                    }

                    zis.close();
                    runOnUiThread(() -> {
                        hasDownloadedData = isDataFile;
                        if (isDataFile) {
                            sharedPrefsManager.setIsDataFileDownloaded(true);
                        } else {
                            sharedPrefsManager.setIsOBBFileDownloaded(true);
                        }
                        viewModel.setIsExtractingFiles(false);
                        Log.e(TAG, "doInBackground: downloaded file(s)");
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission granted");
            } else {
                Toast.makeText(this, R.string.permission_renied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotification(String title, String body) {
        int notificationId = 0;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(path);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "DOWNLOAD_NOTIFICATION";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Downloading",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }

        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            dbReference.removeEventListener(urlEventListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkUpdate() {
        Task appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        Log.d(TAG, "checkUpdate: Checking for updates...");

        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    // Request the update.
                    Log.d(TAG, "Update available!");

                    try {
                        appUpdateManager.startUpdateFlowForResult(result,
                                AppUpdateType.FLEXIBLE, SplashScreenActivity.this, RC_APP_UPDATE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "No Update available!");
                }
            }
        });
    }

    private InstallStateUpdatedListener installListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d(TAG, "An update has been downloaded");
                showSnackBarForCompleteUpdate();
            } else if (state.installStatus() == InstallStatus.INSTALLED) {
                if (appUpdateManager != null) {
                    appUpdateManager.unregisterListener(installListener);
                }
            }
        }
    };

    private void showSnackBarForCompleteUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.rootLayout),
                "New update is ready!",
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (appUpdateManager != null) {
                appUpdateManager.completeUpdate();
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorWhite));
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: app download failed");
            } else {
                Log.e(TAG, "onActivityResult: app download complete");
            }
        }
    }

}

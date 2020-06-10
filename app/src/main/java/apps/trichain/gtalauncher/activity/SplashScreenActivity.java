package apps.trichain.gtalauncher.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

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

import apps.trichain.gtalauncher.R;
import apps.trichain.gtalauncher.databinding.ActivitySplashScreenBinding;
import apps.trichain.gtalauncher.fragment.WebViewFragment;
import apps.trichain.gtalauncher.model.Links;
import apps.trichain.gtalauncher.service.FileDownloadService;
import apps.trichain.gtalauncher.util.NetworkController;
import apps.trichain.gtalauncher.util.SharedPrefsManager;
import apps.trichain.gtalauncher.util.util;
import apps.trichain.gtalauncher.viewModel.GameViewModel;

import static apps.trichain.gtalauncher.util.util.ANDROID_DATA_DIR;
import static apps.trichain.gtalauncher.util.util.ANDROID_OBB_DIR;
import static apps.trichain.gtalauncher.util.util.APK_FILE;
import static apps.trichain.gtalauncher.util.util.BRASIL_PLAY_SHOX_DIR;
import static apps.trichain.gtalauncher.util.util.DATA_FILE;
import static apps.trichain.gtalauncher.util.util.DATA_FILE_PATH;
import static apps.trichain.gtalauncher.util.util.GTA_SA_PACKAGE_NAME;
import static apps.trichain.gtalauncher.util.util.OBB_FILE;
import static apps.trichain.gtalauncher.util.util.OBB_FILE_PATH;
import static apps.trichain.gtalauncher.util.util.humanify;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 325;
    private static final int VIEW_DEFAULT = 0;
    private static final int VIEW_DOWNLOAD = 1;
    private static final int VIEW_UPDATE = 2;
    private static final int VIEW_APP_NOT_INSTALLED = 3;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);

        dbReference = FirebaseDatabase.getInstance().getReference();
        sharedPrefsManager = SharedPrefsManager.getInstance(this);
        networkController = NetworkController.getInstance(this);
        PackageManager manager = getPackageManager();

        /*Check if user launched the app for the
         * first time and download links*/
        if (sharedPrefsManager.checkIsFirstTimeLaunch()) {
            Log.e(TAG, "onCreate: First time launch");
            downloadLinks();
        } else {
            links = Links.create(sharedPrefsManager.getLinks());
            Log.e(TAG, "onCreate: Not first time launch. Links: " + links);
            checkForUpdates();
        }

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
                toggleViews(VIEW_DEFAULT);
            } else {
                toggleViews(VIEW_APP_NOT_INSTALLED);
                b.btnDownloadGTA.setText(getResources().getString(R.string.download_gta, "_Latest" /*String.valueOf(links.getAppVersion())*/));
                b.btnDownloadGTA.setOnClickListener(v -> {
                    toggleViews(VIEW_DOWNLOAD);
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

    private void toggleViews(int viewID) {
        switch (viewID) {
            case VIEW_DOWNLOAD:
                util.hideView(b.llUpdateData, true);
                util.hideView(b.clAppNotInstalled, true);
                util.hideView(b.llDefault, true);
                util.showView(b.llDownloading, true);
                break;
            case VIEW_APP_NOT_INSTALLED:
                util.hideView(b.llUpdateData, true);
                util.showView(b.clAppNotInstalled, true);
                util.hideView(b.llDownloading, false);
                util.hideView(b.llDefault, true);
                break;
            case VIEW_UPDATE:
                util.showView(b.llUpdateData, true);
                util.hideView(b.clAppNotInstalled, true);
                util.hideView(b.llDownloading, false);
                util.hideView(b.llDefault, true);
                break;
            default://TODO
                util.hideView(b.llUpdateData, true);
                util.hideView(b.clAppNotInstalled, true);
                util.hideView(b.llDownloading, false);
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
        b.tvDownloadProgress.setText(R.string.preparing);
        b.pbDownloading.setIndeterminate(true);
        String downloadedFileName = "";
        if (fileToDownload.equals(FILE_DATA)) {
            serverFilePath = links.getDataURL();
            Log.e(TAG, "downloadFile: Data URL: " + serverFilePath);
            //serverFilePath = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-zip-file.zip";
            downloadedFileName = DATA_FILE;
        } else if (fileToDownload.equals(FILE_OBB)) {//Download OBB File
            downloadedFileName = OBB_FILE;
        } else {
            serverFilePath = "https://dl.apkhere.com/down.do/com.rockstargames.gtasa_1.08_paid?code=00c5cd28118695180c15ea360b16621e";
            downloadedFileName = APK_FILE;
        }

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
                b.tvDownloadProgress.setText(R.string.download_failed);
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

    @Override
    public void onStop() {
        super.onStop();
        try {
            dbReference.removeEventListener(urlEventListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

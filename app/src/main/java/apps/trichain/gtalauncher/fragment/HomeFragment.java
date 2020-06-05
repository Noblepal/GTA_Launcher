package apps.trichain.gtalauncher.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import apps.trichain.gtalauncher.databinding.FragmentHomeBinding;
import apps.trichain.gtalauncher.model.Links;
import apps.trichain.gtalauncher.service.FileDownloadService;
import apps.trichain.gtalauncher.util.NetworkController;
import apps.trichain.gtalauncher.util.SharedPrefsManager;
import apps.trichain.gtalauncher.util.util;
import apps.trichain.gtalauncher.viewModel.GameViewModel;

import static apps.trichain.gtalauncher.util.util.humanify;

public class HomeFragment extends Fragment {

    private static final int REQUEST_CODE = 325;

    private static final String DATA_FILE = "GTA_DATA_ZIP.zip";
    private static final String OBB_FILE = "GTA_DATA_ZIP.zip";
    private static final String DATA_FILE_PATH = "/" + DATA_FILE;
    private static final String OBB_FILE_PATH = "/" + OBB_FILE;
    private FragmentHomeBinding b;
    private Context c;
    private GameViewModel viewModel;
    private NetworkController networkController;
    private static final String TAG = "HomeFragment";
    private DatabaseReference dbReference;
    private ValueEventListener urlEventListener;
    private Links links;
    private SharedPrefsManager sharedPrefsManager;
    private String serverFilePath = "";
    private boolean hasDownloadedData;
    private boolean hasDownloadedOBB;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        c = getContext();
        networkController = NetworkController.getInstance(getContext());

        dbReference = FirebaseDatabase.getInstance().getReference();
        sharedPrefsManager = SharedPrefsManager.getInstance(getContext());

        updateURLs();

        ViewModelProvider.AndroidViewModelFactory factory =
                new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());

        viewModel = new ViewModelProvider(getActivity(), factory).get(GameViewModel.class);


        viewModel.getIsPackageInstalled().observe(getViewLifecycleOwner(), isPackageInstalled -> {
            if (isPackageInstalled) {
                util.showView(b.llLaunchGameParent);
                util.hideView(b.clDownloading);
                util.hideView(b.clAppNotInstalled);
            } else {
                util.showView(b.clAppNotInstalled);
                util.hideView(b.clDownloading);
                util.hideView(b.llLaunchGameParent);
            }
        });

        if (sharedPrefsManager.checkHasDownloadedAllFiles()) {
            b.tvMDate.setText(sharedPrefsManager.getLastUpdatedDate());
            b.llLaunchGame.setOnClickListener(v -> {
                try {
                    Intent launchIntent = HomeFragment.this.getActivity().getPackageManager().getLaunchIntentForPackage("com.rockstargames.gtasa");
                    HomeFragment.this.startActivity(launchIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(c, "GTA SA not installed", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            util.showView(b.clAppNotInstalled);
        }

        b.btnDownloadGTA.setText(getContext().getResources().getString(R.string.download_gta, "1.08"));
        b.btnDownloadGTA.setOnClickListener(v -> {
            util.hideView(b.clAppNotInstalled);
            util.hideView(b.llLaunchGameParent);
            util.showView(b.clDownloading);
            Toast.makeText(c, "Downloading...", Toast.LENGTH_SHORT).show();
            downloadFile(true);
        });

        b.tvUpdateData.setOnClickListener(v -> {
            util.hideView(b.llLaunchGameParent);
            util.hideView(b.clAppNotInstalled);
            util.showView(b.clDownloading);
            Toast.makeText(c, "Downloading...", Toast.LENGTH_SHORT).show();
            downloadFile(true);
        });

        if (checkPermissions()) {
            //TODO
        } else {
            mRequestPermissions();
        }

        viewModel.getmIsExtraxtingFiles().observe(getViewLifecycleOwner(), isExtractingFiles -> {
            if (isExtractingFiles) {
                b.tvDownloadProgress.setText("Extracting files...");
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
                    Toast.makeText(c, "Download Complete", Toast.LENGTH_SHORT).show();
                    sharedPrefsManager.setHasDonwloadedAllFiles(true);
                    sharedPrefsManager.saveUpdateDate(Calendar.getInstance().getTime().toString());

                    util.showView(b.llLaunchGameParent);
                    util.hideView(b.clAppNotInstalled);
                    util.hideView(b.clDownloading);

                }

            }
        });


        return b.getRoot();
    }


    private void updateURLs() {
        urlEventListener = dbReference.child("links").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                links = dataSnapshot.getValue(Links.class);
                if (links != null) {
                    sharedPrefsManager.updateLinks(links);
                }
                Toast.makeText(c, "Bases updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    private void getOBBFileURL() {
        downloadOBBFile();
        viewModel.getDownloadURL().observe(getViewLifecycleOwner(), obbDownloadURL -> {
            //serverFilePath = links.getObbURL();
            serverFilePath = obbDownloadURL;
            if (!hasDownloadedData)
                downloadFile(false);
            hasDownloadedOBB = true;
        });
    }

    private void downloadFile(boolean isDataFile) {
        b.tvDownloadProgress.setText("Preparing...");
        b.pbDownloading.setIndeterminate(true);
        String downloadedFileName = "";
        if (isDataFile) {
            serverFilePath = links.getDataURL();
            Log.e(TAG, "downloadFile: Data URL: " + serverFilePath);
            //serverFilePath = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-zip-file.zip";
            downloadedFileName = DATA_FILE;
        } else {//Download OBB File
            downloadedFileName = OBB_FILE;
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
                unpackZip(isDataFile);
            }

            @Override
            public void onDownloadFailed() {
                b.tvDownloadProgress.setText("Download Failed. Link has expired!");
                b.pbDownloading.setIndeterminate(false);
                b.pbDownloading.setProgress(0);
            }

            @Override
            public void onDownloadProgress(int lengthOfFile, int progress) {
                b.pbDownloading.setProgress(progress);
                b.tvDownloadProgress.setText(isDataFile ?
                        c.getResources().getString(R.string.downloading, "GTA GTA Data File", humanify(lengthOfFile), links.getAppVersion(), progress)
                        : c.getResources().getString(R.string.downloading, "GTA Additional OBB Files", humanify(lengthOfFile), links.getAppVersion(), progress));
            }

        };

        FileDownloadService.FileDownloader downloader = FileDownloadService.FileDownloader.getInstance(downloadRequest, listener);
        downloader.download(c);
    }

    private void downloadOBBFile() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        Fragment prev = getChildFragmentManager().findFragmentByTag("webview");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = new WebViewFragment();
        dialogFragment.show(ft, "webview");
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void mRequestPermissions() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    InputStream is = null;
    ZipInputStream zis;
    String downloadedPath = "", extractPath = "";
    File extract_target = new File(Environment.getExternalStorageDirectory() + "/Android/data/");
    File downloaded_path = new File(Environment.getExternalStorageDirectory() + "/Brasil Play Shox");

    @SuppressLint("StaticFieldLeak")
    private void unpackZip(boolean isDataFile) {
        viewModel.setIsExtractingFiles(true);

        if (!isDataFile) {//Is OBB File
            extract_target = new File(Environment.getExternalStorageDirectory() + "/Android/obb/");
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
                    Toast.makeText(c, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
                    getActivity().runOnUiThread(() -> {
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
                Toast.makeText(c, "Permission denied", Toast.LENGTH_SHORT).show();
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


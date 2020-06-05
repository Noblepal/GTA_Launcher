package apps.trichain.gtalauncher.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class GameViewModel extends ViewModel {

    /*Checking if package is installed*/
    private MutableLiveData<Boolean> mPackageInstalled = new MutableLiveData<>();

    private LiveData<Boolean> mLiveData = Transformations.map(mPackageInstalled, isPackageInstalled -> isPackageInstalled);

    public void setIsPackageInstalled(Boolean isPackageInstalled) {
        mPackageInstalled.setValue(isPackageInstalled);
    }

    public LiveData<Boolean> getIsPackageInstalled() {
        return mLiveData;
    }

    /*Set app is downloading*/
    private MutableLiveData<Boolean> mIsGameDownloading = new MutableLiveData<>();

    private LiveData<Boolean> mDownloading = Transformations.map(mIsGameDownloading, isDownloading -> isDownloading);

    public void setIsAppDownloading(Boolean isDownloading) {
        mIsGameDownloading.setValue(isDownloading);
    }

    public LiveData<Boolean> getIsAppDownloading() {
        return mDownloading;
    }

    /*Extracting files*/
    private MutableLiveData<Boolean> isExtractingFiles = new MutableLiveData<>();

    private LiveData<Boolean> mIsExtraxtingFiles = Transformations.map(isExtractingFiles, isExtractingFiles -> isExtractingFiles);

    public void setIsExtractingFiles(Boolean bIsExtractingFiles) {
        isExtractingFiles.setValue(bIsExtractingFiles);
    }

    public LiveData<Boolean> getmIsExtraxtingFiles() {
        return mIsExtraxtingFiles;
    }

    /*Extracting files*/
    private MutableLiveData<String> stringMLData = new MutableLiveData<>();

    private LiveData<String> stringLData = Transformations.map(stringMLData, isExtractingFiles -> isExtractingFiles);

    public void updateDownloadURL(String downloadURL) {
        stringMLData.setValue(downloadURL);
    }

    public LiveData<String> getDownloadURL() {
        return stringLData;
    }

}

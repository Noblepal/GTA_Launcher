package apps.trichain.gtalauncher.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class Links {
    private Float appVersion;
    private String dataURL;
    private String obbURL;
    private String realOBBURL;
    private int updateVersion;

    public Links() {
    }

    public String getRealOBBURL() {
        return realOBBURL;
    }

    public void setRealOBBURL(String realOBBURL) {
        this.realOBBURL = realOBBURL;
    }

    public Float getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Float appVersion) {
        this.appVersion = appVersion;
    }

    public String getDataURL() {
        return dataURL;
    }

    public void setDataURL(String dataURL) {
        this.dataURL = dataURL;
    }

    public String getObbURL() {
        return obbURL;
    }

    public void setObbURL(String obbURL) {
        this.obbURL = obbURL;
    }

    public int getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(int updateVersion) {
        this.updateVersion = updateVersion;
    }

    public String serialize() {
        return new Gson().toJson(this);
    }

    public static Links create(String mLinks) {
        return new Gson().fromJson(mLinks, Links.class);
    }

    @NonNull
    @Override
    public String toString() {
        return this.serialize();
    }
}

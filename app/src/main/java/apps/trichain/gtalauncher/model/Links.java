package apps.trichain.gtalauncher.model;

import com.google.gson.Gson;

public class Links {
    private Float appVersion;
    private String dataURL;
    private String obbURL;
    private String realOBBURL;

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

    public String serialize() {
        return new Gson().toJson(this);
    }

    public static Links create(String mLinks) {
        return new Gson().fromJson(mLinks, Links.class);
    }
}

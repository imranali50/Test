package com.findmykids.tracker.panda.util;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UtilJ {
    public static MultipartBody.Part getFileType(File fileImage, int type) {
        RequestBody requestBody = RequestBody.create(MediaType.parse((type == 2) ? "image/jpeg" : (type == 3) ? "video/mp4" : "audio/mpeg"), fileImage);
        return MultipartBody.Part.createFormData("file", fileImage.getName(), requestBody);
    }

    public static RequestBody getRequestTextBody(String type) {
        return RequestBody.create(MediaType.parse("text/plain"), type);
    }

}

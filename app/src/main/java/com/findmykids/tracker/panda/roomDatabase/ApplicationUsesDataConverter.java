package com.findmykids.tracker.panda.roomDatabase;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class ApplicationUsesDataConverter {

    @TypeConverter
    public static AppInfoFilter storedStringToMyObjects(String data) {
        Gson gson = new Gson();
        if (data == null) {
            return null;
        }
        Type listType = new TypeToken<AppInfoFilter>() {}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String myObjectsToStoredString(AppInfoFilter myObjects) {
        Gson gson = new Gson();
        return gson.toJson(myObjects);
    }


 }
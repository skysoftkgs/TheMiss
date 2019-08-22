package com.ghebb.fbphotopicker;


public class Constants {

    // Facebook permissions
    public static final String[] FACEBOOK_PERMS = new String[] { "user_about_me", "user_photos" };

    // Facebook Photo Album graph params
    public static final String FB_FIELDS_PARAM = "fields";
    public static final String FB_ALBUM_FIELDS = "albums.limit(100).fields(id,name,photos.limit(25).fields(id,icon,picture,source,name,height,width),count,cover_photo)";
    public static final String FB_PHOTO_FIELDS = "photos.limit(25).fields(id,icon,picture,source,name,height,width)";
}

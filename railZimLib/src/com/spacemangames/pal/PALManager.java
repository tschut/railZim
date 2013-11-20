package com.spacemangames.pal;

public class PALManager {
    private static IResourceHandler mResourceHandler = null;
    private static IBitmapFactory   mBitmapFactory   = null;
    private static ILog             mLog             = null;

    public static void setResourceHandler(IResourceHandler aResourceHandler) {
        assert mResourceHandler == null;

        mResourceHandler = aResourceHandler;
    }

    public static IResourceHandler getResourceHandler() {
        assert mResourceHandler != null;

        return mResourceHandler;
    }

    public static void setBitmapFactory(IBitmapFactory aBitmapFactory) {
        assert mBitmapFactory == null;

        mBitmapFactory = aBitmapFactory;
    }

    public static IBitmapFactory getBitmapFactory() {
        assert mBitmapFactory != null;

        return mBitmapFactory;
    }

    public static void setLog(ILog aLog) {
        assert mLog == null;

        mLog = aLog;
    }

    public static ILog getLog() {
        assert mLog != null;

        return mLog;
    }
}

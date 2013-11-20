package com.spacemangames.pal;

public interface IBitmap {
    public void releaseLazyMemory();

    public int getWidth();

    public int getHeight();

    public String getName();
}

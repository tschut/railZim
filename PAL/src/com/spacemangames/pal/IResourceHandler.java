package com.spacemangames.pal;

import java.util.ArrayList;

import com.spacemangames.library.SpaceLevel;

public interface IResourceHandler {
    // load all levels from the defaultlevels.xml file
    public void preloadAllLevels(ArrayList<SpaceLevel> aLevels);
}

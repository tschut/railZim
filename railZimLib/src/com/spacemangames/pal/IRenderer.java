package com.spacemangames.pal;

import java.util.List;

import com.spacemangames.library.SpaceBackgroundObject;
import com.spacemangames.library.SpaceObject;

public interface IRenderer {
    public void doDraw(List<SpaceObject> aObjects, SpaceBackgroundObject aBackgroundObject);

    public void doDraw(SpaceBackgroundObject object);

    public void doDraw(SpaceObject aObject);
}

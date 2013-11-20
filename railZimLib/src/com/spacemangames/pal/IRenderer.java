package com.spacemangames.pal;

import java.util.List;

import com.spacemangames.library.SpaceBackgroundObject;
import com.spacemangames.library.SpaceManObject;
import com.spacemangames.library.SpaceObject;

public interface IRenderer {
    // Calling this assumes that all platform-specific stuff is already set!
    public void doDraw(List<SpaceObject> aObjects, SpaceBackgroundObject aBackgroundObject);

    public void doDraw(SpaceObject aObject);

    public void doDraw(SpaceBackgroundObject aObject);

    public void doDraw(SpaceManObject aObject);
}

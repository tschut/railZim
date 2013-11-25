package com.spacemangames.railzim.pal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.spacemangames.framework.CircularMoveProperties;
import com.spacemangames.framework.IMoveProperties;
import com.spacemangames.framework.NullMoveProperties;
import com.spacemangames.library.SpaceLevel;
import com.spacemangames.math.PointF;
import com.spacemangames.pal.IResourceHandler;
import com.spacemangames.pal.PALManager;
import com.spacemangames.railzim.R;
import com.spacemangames.railzim.pal.levelsjson.LevelJson;
import com.spacemangames.railzim.pal.levelsjson.LevelsJson;
import com.spacemangames.railzim.pal.levelsjson.ObjectJson;
import com.spacemangames.railzim.ui.SpaceApp;

public class AndroidResourceHandler implements IResourceHandler {
    private static final String TAG = "AndroidResourceHandler";

    @Override
    public void preloadAllLevels(ArrayList<SpaceLevel> aLevels) {
        LevelsJson levelsJson = getLevelsJson(R.raw.defaultlevels);
        preloadLevels(aLevels, levelsJson);
    }

    public LevelsJson getLevelsJson(int resource) {
        String json = getJson(resource);
        Gson gson = new Gson();
        return gson.fromJson(json, LevelsJson.class);
    }

    private String getJson(int resource) {
        InputStream is = SpaceApp.mAppContext.getResources().openRawResource(resource);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

        return writer.toString();
    }

    public void preloadLevels(List<SpaceLevel> levels, LevelsJson levelsJson) {
        for (LevelJson levelJson : levelsJson.getLevels()) {
            SpaceLevel level = new SpaceLevel();
            level.id = levelJson.getId();
            level.name = levelJson.getName();
            level.setStartCenterX(levelJson.getStartCenterX());
            level.setStartCenterY(levelJson.getStartCenterY());
            level.setSilver(levelJson.getSilver());
            level.setGold(levelJson.getGold());

            level.addBackground();
            for (ObjectJson objectJson : levelJson.getObjects()) {
                IMoveProperties moveProperties;
                if (objectJson.getMove()) {
                    CircularMoveProperties circularMoveProperties = new CircularMoveProperties();
                    circularMoveProperties.setDegreesPerSecond(objectJson.getMoveDps());
                    circularMoveProperties.setOffset(objectJson.getMoveOffset());
                    circularMoveProperties.setRadius(objectJson.getMoveRadius());
                    moveProperties = circularMoveProperties;
                } else {
                    moveProperties = new NullMoveProperties();
                }
                String type = objectJson.getType();
                PointF position = new PointF(objectJson.getPosx(), objectJson.getPosy());
                String lBitmap = objectJson.getBitmap();
                String lArrowBitmap = objectJson.getArrowBitmap();
                int lCollisionSize = objectJson.getCollisionSize();

                if (type.equals("spaceman")) {
                    level.addSpaceMan(position, lBitmap, lArrowBitmap, lCollisionSize, moveProperties);
                } else {
                    PALManager.getLog().e(TAG, "Unexpected object type: " + type);
                }
            }
            levels.add(level.id, level);
        }
    }
}

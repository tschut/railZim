package com.spacemangames.gravisphere.pal.levelsjson;

import java.util.List;

public class LevelJson {
    private int              id;

    private int              silver;
    private int              gold;

    private int              startCenterX;
    private int              startCenterY;

    private String           name;

    private String           predictionBitmap;

    private List<ObjectJson> objects;

    private BackgroundJson   background;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSilver() {
        return silver;
    }

    public void setSilver(int silver) {
        this.silver = silver;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getStartCenterX() {
        return startCenterX;
    }

    public void setStartCenterX(int startCenterX) {
        this.startCenterX = startCenterX;
    }

    public int getStartCenterY() {
        return startCenterY;
    }

    public void setStartCenterY(int startCenterY) {
        this.startCenterY = startCenterY;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPredictionBitmap() {
        return predictionBitmap;
    }

    public void setPredictionBitmap(String predictionBitmap) {
        this.predictionBitmap = predictionBitmap;
    }

    public List<ObjectJson> getObjects() {
        return objects;
    }

    public void setObjects(List<ObjectJson> objects) {
        this.objects = objects;
    }

    public BackgroundJson getBackground() {
        return background;
    }

    public void setBackground(BackgroundJson background) {
        this.background = background;
    }
}

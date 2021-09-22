package com.deo.flapdedit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import static com.deo.flapdedit.DUtils.clearLog;

public class Main extends Game {
    
    @Override
    public void create() {
        AssetManager assetManager = new AssetManager();
        SpriteBatch batch = new SpriteBatch();
        assetManager.load("menuButtons.atlas", TextureAtlas.class);
        assetManager.load("shopButtons.atlas", TextureAtlas.class);
        assetManager.load("slots.atlas", TextureAtlas.class);
        assetManager.load("workshop.atlas", TextureAtlas.class);
        assetManager.load("ui.atlas", TextureAtlas.class);
        assetManager.load("font.fnt", BitmapFont.class);
        assetManager.load("font2.fnt", BitmapFont.class);
        assetManager.load("font2(old).fnt", BitmapFont.class);
        assetManager.load("font_white.fnt", BitmapFont.class);
        while(!assetManager.isFinished()){
            assetManager.update();
        }
        clearLog();
        this.setScreen(new ModeSelectScreen(this, batch, assetManager));
    }
}

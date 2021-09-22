package com.deo.flapdedit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.deo.flapdedit.DUtils.updateCamera;

public class EnemyEditScreen implements Screen {
    
    Preferences prefs = Gdx.app.getPreferences("deltacore_edit_prefs");
    
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ScreenViewport viewport;
    private final Stage stage;
    private final TextureAtlas enemies;
    private final ShapeRenderer shapeRenderer;
    
    private final int fireEffectCount;
    private final int[] fireOffsetsX;
    private final int[] fireOffsetsY;
    Array<Float> fireParticleEffectAngles;
    Array<Float> fireParticleEffectDistances;
    
    private float bulletOffsetX;
    private float bulletOffsetY;
    private float bulletWidth;
    private float bulletHeight;
    private float bulletAngle;
    private float bulletDistance;
    
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private int currentFlame;
    private final Label offsetLabel;
    
    private float droneOffsetX;
    private float droneOffsetY;
    private float droneWidth;
    private float droneHeight;
    private float droneAngle;
    private float droneDistance;
    
    private final Game game;
    private final AssetManager assetManager;
    private final JsonEntry enemyData;
    private final Screen prev;
    
    BitmapFont font;
    
    EnemyEditScreen(SpriteBatch batch, AssetManager assetManager, final Game game, JsonEntry enemyData, final Screen prev) {
        this.batch = batch;
        this.game = game;
        this.assetManager = assetManager;
        this.enemyData = enemyData;
        this.prev = prev;
        
        font = assetManager.get("font2(old).fnt");
        
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);
        
        enemies = new TextureAtlas(Gdx.files.absolute(prefs.getString("root") + "/enemies.atlas"));
        
        Image enemy;
        String texture = enemyData.getString("noTexture", "texture");
        if (texture.endsWith(".atlas")) {
            enemy = new Image(new TextureAtlas(Gdx.files.absolute(prefs.getString("root").replace("enemies", "") + "/" + texture)).getRegions().get(0));
        } else {
            enemy = new Image(enemies.findRegion(texture));
        }
        
        width = enemyData.getFloat(0, "width");
        height = enemyData.getFloat(0, "height");
        x = 400 - width / 2;
        y = 240 - height / 2;
        enemy.setBounds(x, y, width, height);
        stage.addActor(enemy);
        
        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("workshopRed");
        
        offsetLabel = uiComposer.addText("", font, 0.31f);
        offsetLabel.setSize(400, 330);
        offsetLabel.setPosition(210, 150);
        
        TextButton closeEditor = uiComposer.addTextButton("workshopRed", "exit", 0.2f);
        closeEditor.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(prev);
            }
        });
        closeEditor.setSize(120, 30);
        stage.addActor(closeEditor);
        stage.addActor(offsetLabel);
        
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        
        fireParticleEffectAngles = new Array<>();
        fireParticleEffectDistances = new Array<>();
        
        fireEffectCount = enemyData.get("fire").getInt(0, "count");
        
        fireOffsetsX = new int[fireEffectCount];
        fireOffsetsY = new int[fireEffectCount];
        fireParticleEffectDistances.setSize(fireEffectCount);
        fireParticleEffectAngles.setSize(fireEffectCount);
        
        for (int i = 0; i < fireEffectCount; i++) {
            fireOffsetsX[i] = enemyData.getIntArray(new int[]{0, 0}, "fire", "offset" + i)[0];
            fireOffsetsY[i] = enemyData.getIntArray(new int[]{0, 0}, "fire", "offset" + i)[1];
            fireParticleEffectAngles.set(i, MathUtils.atan2(fireOffsetsY[i], fireOffsetsX[i]) * MathUtils.radiansToDegrees);
            fireParticleEffectDistances.set(i, (float) Math.sqrt(fireOffsetsY[i] * fireOffsetsY[i] + fireOffsetsX[i] * fireOffsetsX[i]));
        }
        
        if (enemyData.getBoolean(false, "spawnsBullets")) {
            bulletOffsetX = enemyData.getIntArray(new int[]{0, 0}, "bullet", "offset")[0];
            bulletOffsetY = enemyData.getIntArray(new int[]{0, 0}, "bullet", "offset")[1];
            bulletHeight = enemyData.getFloat(0, "bullet", "height");
            bulletWidth = enemyData.getFloat(0, "bullet", "width");
            bulletOffsetX += bulletWidth / 2;
            bulletOffsetY += bulletHeight / 2;
            bulletAngle = MathUtils.atan2(bulletOffsetY, bulletOffsetX) * MathUtils.radiansToDegrees;
            bulletDistance = (float) Math.sqrt(bulletOffsetY * bulletOffsetY + bulletOffsetX * bulletOffsetX);
        }
        
        if (enemyData.getBoolean(false, "spawnsDrones")) {
            droneOffsetX = enemyData.get("droneSpawnOffset").asIntArray()[0];
            droneOffsetY = enemyData.get("droneSpawnOffset").asIntArray()[1];
            droneHeight = enemyData.getFloat(0, enemyData.getString(enemyData.name, "droneType"), "height");
            droneWidth = enemyData.getFloat(0, enemyData.getString(enemyData.name, "droneType"), "width");
            droneOffsetX += droneWidth / 2;
            droneOffsetY += droneHeight;
            droneAngle = MathUtils.atan2(droneOffsetY, droneOffsetX) * MathUtils.radiansToDegrees;
            droneDistance = (float) Math.sqrt(droneOffsetY * droneOffsetY + droneOffsetX * droneOffsetX);
        }
        
        offsetLabel.setText("xOffset: " + fireOffsetsX[0] + "\n" + "yOffset: " + fireOffsetsY[0]);
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.5f, 0.7f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        batch.begin();
        font.getData().setScale(0.3f);
        font.draw(batch, "Cyan - fire\nRed - bullet\nGreen - drone", 0, 430);
        batch.end();
        
        stage.draw();
        stage.act(delta);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        shapeRenderer.setColor(new Color().set(0, 1, 1, 0.5f));
        for (int i = 0; i < fireEffectCount; i++) {
            shapeRenderer.circle(x + width / 2 + MathUtils.cosDeg(fireParticleEffectAngles.get(i)) * fireParticleEffectDistances.get(i), y + height / 2 + MathUtils.sinDeg(fireParticleEffectAngles.get(i)) * fireParticleEffectDistances.get(i), 3);
        }
        shapeRenderer.setColor(new Color().set(1, 0, 0, 0.5f));
        shapeRenderer.circle(x + width / 2 + MathUtils.cosDeg(bulletAngle) * bulletDistance, y + height / 2 + MathUtils.sinDeg(bulletAngle) * bulletDistance, 3);
        
        shapeRenderer.setColor(new Color().set(0, 1, 0, 0.5f));
        shapeRenderer.circle(x + width / 2 + MathUtils.cosDeg(droneAngle) * droneDistance, y + height / 2 + MathUtils.sinDeg(droneAngle) * droneDistance, 3);
        
        shapeRenderer.end();
        
        int yOffset = 0;
        int xOffset = 0;
        
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            yOffset = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            yOffset = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            xOffset = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            xOffset = 1;
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            currentFlame++;
            if (currentFlame >= fireEffectCount + 2) {
                currentFlame = 0;
            }
            if (currentFlame == fireEffectCount) {
                offsetLabel.setText("xOffset: " + (bulletOffsetX - bulletWidth / 2) + "\n" + "yOffset: " + (bulletOffsetY - bulletHeight / 2));
            } else if (currentFlame == fireEffectCount + 1) {
                offsetLabel.setText("xOffset: " + (droneOffsetX - droneWidth / 2) + "\n" + "yOffset: " + (droneOffsetY - droneHeight));
            } else {
                offsetLabel.setText("xOffset: " + fireOffsetsX[currentFlame] + "\n" + "yOffset: " + fireOffsetsY[currentFlame]);
            }
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.T)) {
            game.setScreen(new EnemyEditScreen(batch, assetManager, game, enemyData, prev));
        }
        
        if (xOffset != 0 || yOffset != 0) {
            if (currentFlame < fireEffectCount) {
                fireOffsetsX[currentFlame] += xOffset;
                fireOffsetsY[currentFlame] += yOffset;
                offsetLabel.setText("xOffset: " + fireOffsetsX[currentFlame] + "\n" + "yOffset: " + fireOffsetsY[currentFlame]);
                fireParticleEffectAngles.set(currentFlame, MathUtils.atan2(fireOffsetsY[currentFlame], fireOffsetsX[currentFlame]) * MathUtils.radiansToDegrees);
                fireParticleEffectDistances.set(currentFlame, (float) Math.sqrt(fireOffsetsY[currentFlame] * fireOffsetsY[currentFlame] + fireOffsetsX[currentFlame] * fireOffsetsX[currentFlame]));
            } else if (currentFlame == fireEffectCount) {
                bulletOffsetX += xOffset;
                bulletOffsetY += yOffset;
                offsetLabel.setText("xOffset: " + (bulletOffsetX - bulletWidth / 2) + "\n" + "yOffset: " + (bulletOffsetY - bulletHeight / 2));
                bulletAngle = MathUtils.atan2(bulletOffsetY, bulletOffsetX) * MathUtils.radiansToDegrees;
                bulletDistance = (float) Math.sqrt(bulletOffsetY * bulletOffsetY + bulletOffsetX * bulletOffsetX);
            } else if (currentFlame == fireEffectCount + 1) {
                droneOffsetX += xOffset;
                droneOffsetY += yOffset;
                offsetLabel.setText("xOffset: " + (droneOffsetX - droneWidth / 2) + "\n" + "yOffset: " + (droneOffsetY - droneHeight));
                droneAngle = MathUtils.atan2(droneOffsetY, droneOffsetX) * MathUtils.radiansToDegrees;
                droneDistance = (float) Math.sqrt(droneOffsetY * droneOffsetY + droneOffsetX * droneOffsetX);
            }
        }
    }
    
    @Override
    public void resize(int width, int height) {
        updateCamera(camera, viewport, width, height);
    }
    
    @Override
    public void pause() {
    
    }
    
    @Override
    public void resume() {
    
    }
    
    @Override
    public void hide() {
    
    }
    
    @Override
    public void dispose() {
        enemies.dispose();
    }
}

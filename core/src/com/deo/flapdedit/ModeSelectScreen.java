package com.deo.flapdedit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.File;
import java.util.logging.FileHandler;

import javax.swing.JFileChooser;

import static com.deo.flapdedit.DUtils.updateCamera;

public class ModeSelectScreen implements Screen {
    
    Preferences prefs = Gdx.app.getPreferences("deltacore_edit_prefs");
    
    private final SpriteBatch batch;
    private final Stage stage;
    private final OrthographicCamera camera;
    private final ScreenViewport viewport;
    private final ShapeRenderer shapeRenderer;
    private float rotation;
    
    ModeSelectScreen(final Game game, final SpriteBatch batch, final AssetManager assetManager){
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        
        this.batch = batch;
        stage = new Stage(viewport);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setColor(Color.valueOf("#00FF44"));
        Gdx.gl20.glLineWidth(5);

        UIComposer uiComposer = new UIComposer(assetManager);
        uiComposer.loadStyles("defaultLight", "workshopRed");

        final TextButton timelineModeButton = uiComposer.addTextButton("defaultLight", "Enemy Spawn Timeline", 0.28f);
        final TextButton enemyEditorButton = uiComposer.addTextButton("defaultLight", "Enemy Model Editor", 0.28f);
        TextButton closeEditorButton = uiComposer.addTextButton("workshopRed", "exit", 0.2f);
        final TextButton chooseConfigLocationButton = uiComposer.addTextButton("defaultLight", "Chose config path", 0.3f);
        
        timelineModeButton.setSize(250, 60);
        enemyEditorButton.setSize(250, 60);
        chooseConfigLocationButton.setSize(250, 60);
        closeEditorButton.setSize(120, 30);

        stage.addActor(timelineModeButton);
        stage.addActor(enemyEditorButton);
        stage.addActor(closeEditorButton);
        stage.addActor(chooseConfigLocationButton);
        chooseConfigLocationButton.setPosition(275.0f, 315);
        timelineModeButton.setPosition(275.0f, 245);
        enemyEditorButton.setPosition(275.0f, 175);
        
        if(!prefs.contains("root")) {
            enemyEditorButton.setTouchable(Touchable.disabled);
            timelineModeButton.setTouchable(Touchable.disabled);
            enemyEditorButton.setColor(Color.GRAY);
            timelineModeButton.setColor(Color.GRAY);
        }
        
        chooseConfigLocationButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                callFileChooser(new FileDialogueAction(){
                    @Override
                    void performAction(File[] selectedFiles) {
                        if(selectedFiles[0].exists() ){
                            prefs.putString("root", selectedFiles[0].getAbsolutePath());
                            prefs.flush();
                            enemyEditorButton.setTouchable(Touchable.enabled);
                            timelineModeButton.setTouchable(Touchable.enabled);
                            enemyEditorButton.setColor(Color.WHITE);
                            timelineModeButton.setColor(Color.WHITE);
                        }
                    }
                }, false);
            }
        });
        
        closeEditorButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        timelineModeButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new EnemyTimelineScreen(batch, assetManager, game));
            }
        });
    }
    
    void callFileChooser(FileDialogueAction action, boolean multiSelect) {
        JFileChooser jFileChooser = new JFileChooser(new File("C:\\"));
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setMultiSelectionEnabled(multiSelect);
        
        int returnVal = jFileChooser.showOpenDialog(null);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (multiSelect) {
                action.performAction(jFileChooser.getSelectedFiles());
            } else {
                action.performAction(new File[]{jFileChooser.getSelectedFile()});
            }
        }
        
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

        rotation += 1;

        shapeRenderer.begin();

        shapeRenderer.triangle( 400 - MathUtils.cosDeg(rotation) * 300,
                                240 - MathUtils.sinDeg(rotation) * 150,
                                400 - MathUtils.cosDeg(rotation + 120) * 300,
                                240 - MathUtils.sinDeg(rotation + 120) * 150,
                                400 - MathUtils.cosDeg(rotation + 240) * 300,
                                240 - MathUtils.sinDeg(rotation + 240) * 150);

        shapeRenderer.triangle( 400 - MathUtils.cosDeg(rotation + 60) * 80,
                                240 - MathUtils.sinDeg(rotation + 60) * 160,
                                400 - MathUtils.cosDeg(rotation + 180) * 80,
                                240 - MathUtils.sinDeg(rotation + 180) * 160,
                                400 - MathUtils.cosDeg(rotation + 300) * 80,
                                240 - MathUtils.sinDeg(rotation + 300) * 160);

        shapeRenderer.polygon(new float[]{

                400 - MathUtils.cosDeg(rotation) * 300,
                240 - MathUtils.sinDeg(rotation) * 150,

                400 - MathUtils.cosDeg(rotation + 60) * 80,
                240 - MathUtils.sinDeg(rotation + 60) * 160,

                400 - MathUtils.cosDeg(rotation + 120) * 300,
                240 - MathUtils.sinDeg(rotation + 120) * 150,

                400 - MathUtils.cosDeg(rotation + 180) * 80,
                240 - MathUtils.sinDeg(rotation + 180) * 160,

                400 - MathUtils.cosDeg(rotation + 240) * 300,
                240 - MathUtils.sinDeg(rotation + 240) * 150,

                400 - MathUtils.cosDeg(rotation + 300) * 80,
                240 - MathUtils.sinDeg(rotation + 300) * 160});

        shapeRenderer.end();

        stage.draw();
        stage.act(delta);

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
        shapeRenderer.dispose();
    }
}

class FileDialogueAction {
    void performAction(File[] selectedFiles) {
    
    }
}
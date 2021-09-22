package com.deo.flapdedit;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import static com.deo.flapdedit.DUtils.updateCamera;

public class EnemyTimelineScreen implements Screen {
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
    private JsonValue enemyJsonData;
    private Stage stage;
    private Array<Image> enemySpawnRangesImages;
    private Array<Float> enemySpawnRangesWidths;
    private Array<Float> enemySpawnRangesXs;
    private Label enemyInfo;

    public EnemyTimelineScreen(final SpriteBatch batch, final AssetManager assetManager, final Game game) {
        this.batch = batch;
        camera = new OrthographicCamera(800, 480);
        viewport = new ScreenViewport(camera);
        enemyJsonData = new JsonReader().parse(Gdx.files.external("Downloads/flappyDemon - Copy/android/assets/enemies/enemies.json"));
        final Array<TextButton> enemies = new Array<>();
        Array<int[]> enemySpawnRanges = new Array<>();
        enemySpawnRangesImages = new Array<>();
        enemySpawnRangesWidths = new Array<>();
        enemySpawnRangesXs = new Array<>();

        String[] colors = new String[]{"#ff000088", "#22ff0088", "#00ffea88",
                "#fff70088", "#2b00ff88", "#ff730088",
                "#dd00ff88", "#b7ff0088", "#0095ff88", "#ff005588"};

        UIComposer uiComposer = new UIComposer(assetManager);

        uiComposer.loadStyles("sliderDefaultNormal", "defaultLight", "workshopRed");

        Table slider = uiComposer.addSlider("sliderDefaultNormal", 1, 40, 0.1f, "Timeline Scale: ", "", "timelineScale");

        slider.setPosition(0, 75);
        slider.align(Align.left);

        final Slider timelineScaleSlider = (Slider) slider.getChild(0);

        final float timelineScale = timelineScaleSlider.getValue();

        stage = new Stage(viewport);

        Pixmap pixmap = new Pixmap(800, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("#999999"));
        pixmap.fill();
        TextureRegionDrawable BarBackgroundBlack = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        stage.addActor(new Image(BarBackgroundBlack));

        Table enemyButtonsHolder = new Table();

        enemyInfo = uiComposer.addText("Enemy Info: ", (BitmapFont) assetManager.get("font2(old).fnt"), 0.28f);

        enemyInfo.setSize(400, 330);
        enemyInfo.setPosition(210, 100);
        enemyInfo.setAlignment(Align.topLeft, Align.left);

        ScrollPane enemyInfoScrollPane = new ScrollPane(enemyInfo);

        enemyInfoScrollPane.setSize(400, 330);
        enemyInfoScrollPane.setPosition(205, 150);

        stage.addActor(enemyInfoScrollPane);

        for (int i = 0; i < enemyJsonData.size; i++) {
            enemySpawnRanges.add(enemyJsonData.get(i).get("spawnConditions").get("score").asIntArray());

            float x = enemySpawnRanges.get(enemySpawnRanges.size - 1)[0];
            float width = enemySpawnRanges.get(enemySpawnRanges.size - 1)[1] - x;

            enemySpawnRangesWidths.add(width);
            enemySpawnRangesXs.add(x);

            x = x / timelineScale;
            width = width / timelineScale;

            Pixmap pixmap2 = new Pixmap(45, 45, Pixmap.Format.RGBA8888);
            pixmap2.setColor(Color.valueOf(colors[i]));
            pixmap2.fill();
            TextureRegionDrawable timelineFragment = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap2)));
            pixmap2.dispose();

            final Image timelineImageFragment = new Image(timelineFragment);
            timelineImageFragment.setX(x);
            timelineImageFragment.setY(2.5f);
            timelineImageFragment.setWidth(width);
            timelineImageFragment.setHeight(35);

            enemySpawnRangesImages.add(timelineImageFragment);

            final int finalI = i;

            timelineImageFragment.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    String info = enemyJsonData.get(finalI).name + " Info: \n";
                    boolean canShoot = enemyJsonData.get(finalI).get("normal").get("body").get("spawnsBullets").asBoolean();
                    info = info.concat("Speed: " + enemyJsonData.get(finalI).get("normal").get("body").get("speed").asString() + "\n");
                    info = info.concat("Health: " + enemyJsonData.get(finalI).get("normal").get("body").get("health").asString() + "\n");
                    if (canShoot) {
                        info = info.concat("Can Aim: " + enemyJsonData.get(finalI).get("normal").get("body").get("canAim").asString() + "\n");
                    }
                    info = info.concat("Has Animation: " + enemyJsonData.get(finalI).get("normal").get("body").get("hasAnimation").asString() + "\n");
                    info = info.concat("Homing: " + enemyJsonData.get(finalI).get("normal").get("body").get("homing").asString() + "\n");
                    boolean spawnsDrones = enemyJsonData.get(finalI).get("normal").get("body").get("spawnsDrones").asBoolean();
                    info = info.concat("Spawns Drones: " + spawnsDrones + "\n");
                    if (spawnsDrones) {
                        info = info.concat("Drone Type: " + enemyJsonData.get(finalI).get("normal").get("body").get("droneType").asString() + "\n");
                        info = info.concat("Drones Per Spawn: " + enemyJsonData.get(finalI).get("normal").get("body").get("dronesPerSpawn").asString() + "\n");
                        info = info.concat("Drone Spawn Delay: " + enemyJsonData.get(finalI).get("normal").get("body").get("droneSpawnDelay").asString() + "\n");
                    }
                    info = info.concat("Shoots: " + canShoot + "\n");
                    if (canShoot) {
                        info = info.concat("Shooting Delay: " + enemyJsonData.get(finalI).get("normal").get("body").get("shootingDelay").asString() + "\n");
                        info = info.concat("Spread: " + enemyJsonData.get(finalI).get("normal").get("bullet").get("spread").asString() + "\n");
                        info = info.concat("Bullet Speed: " + enemyJsonData.get(finalI).get("normal").get("bullet").get("speed").asString() + "\n");
                        info = info.concat("Damage: " + enemyJsonData.get(finalI).get("normal").get("bullet").get("damage").asString() + "\n");
                        info = info.concat("Bullets Per Shot: " + enemyJsonData.get(finalI).get("normal").get("bullet").get("bulletsPerShot").asString() + "\n");
                    }
                    info = info.concat("Spawn Delay: " + enemyJsonData.get(finalI).get("spawnDelay").asString() + "\n");
                    info = info.concat("Spawn Height: from " + enemyJsonData.get(finalI).get("spawnHeight").asIntArray()[0] + " to " + enemyJsonData.get(finalI).get("spawnHeight").asIntArray()[1] + "\n");
                    info = info.concat("Score Spawn Conditions: from " + enemyJsonData.get(finalI).get("spawnConditions").get("score").asIntArray()[0] + " to " + enemyJsonData.get(finalI).get("spawnConditions").get("score").asIntArray()[1] + "\n");
                    info = info.concat("Enemies killed Spawn Conditions: from " + enemyJsonData.get(finalI).get("spawnConditions").get("enemiesKilled").asIntArray()[0] + " to " + enemyJsonData.get(finalI).get("spawnConditions").get("enemiesKilled").asIntArray()[1] + "\n");
                    for (int i = 0; i < enemySpawnRangesImages.size; i++) {
                        if (i == finalI) {
                            enemySpawnRangesImages.get(i).setHeight(45);
                            enemySpawnRangesImages.get(i).setDebug(true);
                        } else {
                            enemySpawnRangesImages.get(i).setHeight(35);
                            enemySpawnRangesImages.get(i).setDebug(false);
                        }
                    }
                    enemyInfo.setText(info);
                }
            });

            stage.addActor(timelineImageFragment);

            TextButton enemyButton = uiComposer.addTextButton("defaultLight", enemyJsonData.get(i).name, 0.28f);
            enemyButton.addListener(timelineImageFragment.getListeners().get(timelineImageFragment.getListeners().size-1));
            enemyButton.addListener(new ActorGestureListener(){
                @Override
                public boolean longPress(Actor actor, float x, float y) {
                    game.setScreen(new EnemyEditScreen(batch, assetManager, game, enemyJsonData.get(finalI), EnemyTimelineScreen.this, "normal"));
                    return true;
                }
            });
            enemyButtonsHolder.add(enemyButton).width(190).padBottom(5).row();
            enemies.add(enemyButton);
        }

        TextButton closeEditor = uiComposer.addTextButton("workshopRed", "exit", 0.2f);
        closeEditor.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ModeSelectScreen(game, batch, assetManager));
            }
        });
        closeEditor.setBounds(680, 450, 120, 30);
        stage.addActor(closeEditor);

        timelineScaleSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                for (int i = 0; i < enemySpawnRangesImages.size; i++) {
                    enemySpawnRangesImages.get(i).setWidth(enemySpawnRangesWidths.get(i) / timelineScaleSlider.getValue());
                    enemySpawnRangesImages.get(i).setX(enemySpawnRangesXs.get(i) / timelineScaleSlider.getValue());
                }
            }
        });

        stage.addActor(slider);

        ScrollPane enemyScrollPane = new ScrollPane(enemyButtonsHolder);

        enemyScrollPane.setY(100);
        enemyScrollPane.setSize(200, 380);

        stage.addActor(enemyScrollPane);

    }

    @Override
    public void dispose() {
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
}

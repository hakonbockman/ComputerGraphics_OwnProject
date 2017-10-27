package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

public class MainGameLoop {
	
	public static void main(String[] args) {
		
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		Random random = new Random();
		MasterRenderer renderer = new MasterRenderer();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
	// Light
		Light light = new Light(new Vector3f(3000, 2000, 500), new Vector3f(1, 1, 1));
	// Light end	
	// Player
		TexturedModel personModel = loadTexturedModel("models/person", "textures/playerTexture", 1, loader);		
		Player player = new Player(personModel, new Vector3f( 100f, 0.0f, -50f), 0f, 0f, 0f, 1.0f);
		Camera camera = new Camera(player);
	// Player end	
		
	// Terrain
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("textures/terrain/grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("textures/terrain/mud"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("textures/terrain/path"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("textures/terrain/pinkFlowers"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, bTexture, gTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("textures/terrain/blendMap"));
		
		
		
		Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap,"textures/terrain/heightmap");
		Terrain terrain2 = new Terrain(-1, -1, loader, texturePack, blendMap,"textures/terrain/heightmap");
		Terrain terrain3 = new Terrain(-1, 0, loader, texturePack, blendMap,"textures/terrain/heightmap");
		Terrain terrain4 = new Terrain(0, 0, loader, texturePack, blendMap,"textures/terrain/heightmap");
		
		List<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(terrain);
		terrains.add(terrain2);
		terrains.add(terrain3);
		terrains.add(terrain4);
		
	// Terrain end	
		
	// Models
		// Environment Models
			TexturedModel tree = loadTexturedModel("models/tree", "textures/tree", 1, loader);
			TexturedModel blueDevil = loadTexturedModel("models/blueDevil/bluedevil", "models/blueDevil/bluedevil", 1, loader);
			TexturedModel tree2 = loadTexturedModel("models/lowPolyTree", "textures/lowPolyTree", 1, loader);
			TexturedModel fern = loadTexturedModel("models/fern", "textures/fern_atlas_texture", 2, loader);
			fern.getTexture().setHasTransparency(true);		
			TexturedModel grass = loadTexturedModel("models/grassModel", "textures/diffuse", 3, loader);
			grass.getTexture().setHasTransparency(true);		
			TexturedModel flower = loadTexturedModel("models/grassModel", "textures/flower", 1, loader);
			flower.getTexture().setHasTransparency(true);
		
			List<Entity> entities = new ArrayList<Entity>();
			
			for(int i = 0; i < 100; i++) {
				float x;
				float z = random.nextFloat() * ( -800);
				entities.add(new Entity(tree,  new Vector3f(x = random.nextFloat() * 800, terrain.getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 7.0f));
				entities.add(new Entity(tree2, new Vector3f(x = random.nextFloat() * 800, terrain.getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.0f));
				entities.add(new Entity(grass, random.nextInt(6), new Vector3f(x = random.nextFloat() * 800, terrain.getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.5f));
				entities.add(new Entity(flower, new Vector3f(x = random.nextFloat() * 800, terrain.getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.5f));
				entities.add(new Entity(fern, random.nextInt(4),  new Vector3f(x = random.nextFloat() * 800, terrain.getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.0f));
				
				if(i / 10 == 0 ) {
					entities.add(new Entity(blueDevil, random.nextInt(4),  new Vector3f(x = random.nextFloat() * 800, terrain.getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.0f));
				}
			}
		// Environment Models end
		// Gui models
			
			List<GuiTexture> guis = new ArrayList<GuiTexture>();
			GuiTexture gui = new GuiTexture(loader.loadTexture("textures/health"), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f));
			guis.add(gui);
			
		// Gui models end
	// Models end
		
	// MainLoop
		while(!Display.isCloseRequested()){
			
			collisionMultipleTerrains(player, terrains);
			renderer.processEntity(player);
			camera.Move();
			renderTerrain(renderer, terrains);
			renderEntities(renderer, entities);
			
			renderer.render(light, camera);
			guiRenderer.render(guis);
			
			DisplayManager.updateDisplay();
			
			if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
				break;
			}
		}
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
	

	
	private static void renderEntities(MasterRenderer renderer, List<Entity> entities) {
		for(Entity entity: entities) {
			renderer.processEntity(entity);
		}
	}
	
	private static void renderTerrain(MasterRenderer renderer, List<Terrain> terrains) {
		for(Terrain terrain: terrains) {
			renderer.processTerrain(terrain);
		}
	}
	
	private static void collisionMultipleTerrains(Player player, List<Terrain> terrains) {
		int px = (int) player.getPosition().x;
		int pz = (int) player.getPosition().z;
		
		if( px <= 800 && px >= 0 && pz >= -800 && pz <= 0) {       // x: 0 to 800 and z: 0 to -800
			player.move(terrains.get(0));
		}else if ( px >= -800 && px <= 0 && pz >= -800 && pz <= 0 ) {	// x: 0 to -800 and z: 0 to -800
			player.move(terrains.get(1));
		}else if ( px >= -800 && px <= 0 && pz <= 800 && pz >= 0 ) {	// x: 0 to -800 and z: 0 to 800
			player.move(terrains.get(2));
		}else if ( px <= 800 && px >= 0 && pz <= 800 && pz >= 0 ) {	// x: 0 to 800 and z: 0 to 800
			player.move(terrains.get(3));
		}
	}
	
	private static TexturedModel loadTexturedModel(String modelFileName, String textureFileName, int numberOfRows, Loader loader) {
		final ModelData data = OBJFileLoader.loadOBJ(modelFileName);
		final RawModel rawModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(),data.getIndices());
		
		ModelTexture temp = new ModelTexture(loader.loadTexture(textureFileName));
		if( numberOfRows > 1) {
			temp.setNumberOfRows(numberOfRows);
		}
		return new TexturedModel(rawModel,temp);
	}
	
}

/************************************************************* CODE GRAVEYARD *********************************************************************
 
 		
START------------------------ STARS, PLANETS AND OUR BELOVED SUN --------------------------------------			
			
		RawModel dragonModel = OBJLoader.loadObjModel("models/dragon/dragon", loader);
		RawModel sphere = OBJLoader.loadObjModel("models/star", loader);
		RawModel cube = OBJLoader.loadObjModel("models/cube", loader);
		RawModel crumbled = OBJLoader.loadObjModel("models/crumbled", loader);
		RawModel treeModel = OBJLoader.loadObjModel("models/tree", loader);
		
		RawModel dragonModel = OBJLoader.loadObjModel("models/dragon/dragon", loader);
		// Dragon
		TexturedModel dragonTexturedModel = new TexturedModel(dragonModel, new ModelTexture(loader.loadTexture("textures/colours/red")));
		// Star
		TexturedModel starTexturedModel = new TexturedModel(sphere, new ModelTexture(loader.loadTexture("textures/colours/white")));
		ModelTexture stars_texture = starTexturedModel.getTexture();
		stars_texture.setReflectivity(1000);
		stars_texture.setShineDamper(1000);
		// Moon
		TexturedModel moonTexturedModel = new TexturedModel(sphere, new ModelTexture(loader.loadTexture("textures/colours/blue")));				
		// Planet
		TexturedModel planetTexturedModel = new TexturedModel(sphere, new ModelTexture(loader.loadTexture("textures/colours/brown")));		
		// Sun
		TexturedModel sunTexturedModel = new TexturedModel(sphere, new ModelTexture(loader.loadTexture("textures/colours/yellow")));		
		// Womp
		TexturedModel wompTexturedModel = new TexturedModel(cube, new ModelTexture(loader.loadTexture("textures/womp")));
		ModelTexture womp_texture = wompTexturedModel.getTexture();
		womp_texture.setReflectivity(1000);
		womp_texture.setShineDamper(1000);
		// Metal_Cube
		TexturedModel metalTexturedModel = new TexturedModel(cube, new ModelTexture(loader.loadTexture("textures/metal")));
		ModelTexture metal_texture = metalTexturedModel.getTexture();
		metal_texture.setReflectivity(1000);
		metal_texture.setShineDamper(1000);
		// Eye_Cube
		TexturedModel eyeTexturedModel = new TexturedModel(cube, new ModelTexture(loader.loadTexture("textures/eye")));
		ModelTexture eye_texture = eyeTexturedModel.getTexture();
		eye_texture.setReflectivity(1000);
		eye_texture.setShineDamper(1000);
		// Stone_Cube
		TexturedModel stoneTexturedModel = new TexturedModel(crumbled, new ModelTexture(loader.loadTexture("textures/stone")));
		ModelTexture stone_texture = stoneTexturedModel.getTexture();
		stone_texture.setReflectivity(10);
		stone_texture.setShineDamper(10);
		// Tree
		TexturedModel treeTexturedModel = new TexturedModel(treeModel, new ModelTexture(loader.loadTexture("textures/tree")));
		ModelTexture tree_texture = treeTexturedModel.getTexture();
		tree_texture.setReflectivity(10);
		tree_texture.setShineDamper(10);
		
		List<Entity> allStars = new ArrayList<Entity>();
		List<Entity> allPlanets = new ArrayList<Entity>();
		List<Entity> allMoons = new ArrayList<Entity>();
		List<Entity> allTrees = new ArrayList<Entity>();
		
		Random random = new Random();
		
		for(int i = 0; i < 20000; i++) {
			float x = random.nextFloat() * 30000 - 15000;
			float y = random.nextFloat() * 30000 - 15000;
			float z = random.nextFloat() * 30000 - 15000;
			allStars.add(
					new Entity(starTexturedModel, new Vector3f(x, y, z), random.nextFloat() * 180f,
							random.nextFloat() * 180f, 0f, 1f));
		}
		for(int i = 0; i < 2000; i++) {
			float x = random.nextFloat() * 30000 - 15000;
			float y = random.nextFloat() * 30000 - 15000;
			float z = random.nextFloat() * 30000 - 15000;
			allMoons.add(
					new Entity(moonTexturedModel, new Vector3f(x, y, z), random.nextFloat() * 180f,
							random.nextFloat() * 180f, 0f, 10f));
		}
		for(int i = 0; i < 500; i++) {
			float x = random.nextFloat() * 30000 - 15000;
			float y = random.nextFloat() * 30000 - 15000;
			float z = random.nextFloat() * 30000 - 15000;
			allPlanets.add(
					new Entity(planetTexturedModel, new Vector3f(x, y, z), random.nextFloat() * 180f,
							random.nextFloat() * 180f, 0f, 50f));
		}
		for(int i = 0; i < 500; i++) {
			float x = random.nextFloat() * 30000 - 15000;
			float y = random.nextFloat() * 30000 - 15000;
			float z = random.nextFloat() * 30000 - 15000;
			allTrees.add(
					new Entity(treeTexturedModel, new Vector3f(x, y, z), 0f, 0f, 0f, 5f));
		}
		
		Entity dragon = new Entity(dragonTexturedModel, new Vector3f(10, -100, +50), 0f, 0f, 0f, 10f);		
		Entity sun = new Entity(sunTexturedModel, new Vector3f(200, 400, -1200), 180f,180f, 0f, 500f);
		Entity womp = new Entity(wompTexturedModel, new Vector3f(200, 400, -100), 180f,180f, 0f, 50f);
		Entity metal = new Entity(metalTexturedModel, new Vector3f(400, 200, -100), 180f,180f, 0f, 30f);
		Entity eye = new Entity(eyeTexturedModel, new Vector3f(50, 50, -10), 180f,180f, 0f, 10f);
		Entity stone = new Entity(stoneTexturedModel, new Vector3f(0, 0, -100), 180f,180f, 0f, 30f);
		
		
			renderer.processEntity(sun);
				sun.increaseRotation(0f, 0.01f, 0f);
			renderer.processEntity(womp);
				womp.increaseRotation(0.2f, 0.2f, 0.2f);
			renderer.processEntity(metal);
				metal.increasePosition(0.05f, 0.05f, 0.05f);
			renderer.processEntity(eye);
			renderer.processEntity(dragon);
			renderer.processEntity(stone);
			
			
			for(Entity star: allStars) {
				renderer.processEntity(star);
			}
			for(Entity planet1: allPlanets) {
				renderer.processEntity(planet1);
				planet1.increaseRotation(0f, 0.1f, 0f);
			}
			for(Entity moon1: allMoons) {
				renderer.processEntity(moon1);
				moon1.increaseRotation(0f, 0.2f, 0f);
			}
			for(Entity trees: allTrees) {
				renderer.processEntity(trees);
				trees.increaseRotation(1f, 1f, 1f);
			}
			
END-------------------------- STARS, PLANETS AND OUR BELOVED SUN  --------------------------------------
		
		
		//Entity worldEntity = new Entity(starTexturedModel, new Vector3f(0, 0, 0), 0, 0, 0, 1);
		
		//ModelTexture texture = staticModel.getTexture();
		//texture.setShineDamper(10);
		//texture.setReflectivity(1);
		//Entity entity = new Entity(staticModel, new Vector3f(0, -5, -30), 0, 0, 0, 1);
 
 
  		float[] vertices = {
			-0.5f,0.5f,0,	//V0
			-0.5f,-0.5f,0,	//V1
			0.5F,-0.5f,0,	//V2
			0.5f,0.5f,0
		};
		
		int[] indices = {
				0,1,3,	// top left triangle (V0, V1, V3)
				3,1,2	// Bottom right triangle (V3, V1, V2)
		};
		
		float[] textureCoords = {
				0,0,	//V0
				0,1,	//V1
				1,1,	//V2
				1,0		//V3
		};
		
  		float[] vertices = {			
				-0.5f,0.5f,-0.5f,	
				-0.5f,-0.5f,-0.5f,	
				0.5f,-0.5f,-0.5f,	
				0.5f,0.5f,-0.5f,		
				
				-0.5f,0.5f,0.5f,	
				-0.5f,-0.5f,0.5f,	
				0.5f,-0.5f,0.5f,	
				0.5f,0.5f,0.5f,
				
				0.5f,0.5f,-0.5f,	
				0.5f,-0.5f,-0.5f,	
				0.5f,-0.5f,0.5f,	
				0.5f,0.5f,0.5f,
				
				-0.5f,0.5f,-0.5f,	
				-0.5f,-0.5f,-0.5f,	
				-0.5f,-0.5f,0.5f,	
				-0.5f,0.5f,0.5f,
				
				-0.5f,0.5f,0.5f,
				-0.5f,0.5f,-0.5f,
				0.5f,0.5f,-0.5f,
				0.5f,0.5f,0.5f,
				
				-0.5f,-0.5f,0.5f,
				-0.5f,-0.5f,-0.5f,
				0.5f,-0.5f,-0.5f,
				0.5f,-0.5f,0.5f
				
		};
		
		float[] textureCoords = {
				
				0,0,
				0,1,
				1,1,
				1,0,			
				0,0,
				0,1,
				1,1,
				1,0,			
				0,0,
				0,1,
				1,1,
				1,0,
				0,0,
				0,1,
				1,1,
				1,0,
				0,0,
				0,1,
				1,1,
				1,0,
				0,0,
				0,1,
				1,1,
				1,0

				
		};
		
		int[] indices = {
				0,1,3,	
				3,1,2,	
				4,5,7,
				7,5,6,
				8,9,11,
				11,9,10,
				12,13,15,
				15,13,14,	
				16,17,19,
				19,17,18,
				20,21,23,
				23,21,22

		};
		
  
 
************************************************************** CODE GRAVEYARD **********************************************************************/
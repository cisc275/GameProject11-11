package game;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.stream.Collectors;

/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

/*Class: ClapperRailGameState
 * -class that acts as the Model of the ClapperRail GameMode
 * -keeps track of the data for the ClapperRail mini-game
 */
public class ClapperRailGameState extends GameState {
	//Derek's height,width are used as RATIO multipliers to achieve the same ratio/scaled down version for ALL montiors/laptops
	//It takes how it looks on derek's laptop and we multiply that ratio by user's laptop/monitor dimensions making the clapperrail game look
	//exactly as how it does on Derek's laptop
	static double dereks_height = 1080.0;
	static double dereks_width = 1920.0;
	
	
	private ClapperRail CR;
	private ArrayList<Platform> platforms;
	private Flood flood;
	public static int GROUND = (int)((974d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT);
	static final String ENERGY_TEXT = "Health: ";
	static final String SCORE_TEXT = "Score: ";
	static final String MATERIALS_TEXT = "x ";
	static final int ENERGY_FONT_SIZE = 40;
	static final int MATERIAL_FONT_SIZE = 40;
	static final int SCORE_FONT_SIZE = 40;
	private static final int SPAWN_CHANCE = 5;
	int BackgroundX = 5;
	
	/*
	 * if its the start of the game, show the tutorial.
	 */
	private boolean start = true;
	
	private boolean waitingOnQuestion = false;
	
	//194 is a very important magic number!
	//the jump height is 300, the ground position of the bird is 494, 
	//494-300=194. That is what this stems from. Becuase if you are at the bottom of the screen jumping, we want 
	//the screen to not move, but any higher, and we want it to move.
//	(int)((974d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)
	private static final int MOVE_SCREEN_HEIGHT = (int)((568/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT);
	

	// GAME_TIME: (NOTE: ALL TIMING IS DONE IN MILLISECONDS)
	// EX: GameTimer.ONE_SECOND == 1000 for 1000 milliseconds as
	// this is what the Java.util.Timer takes in
	
	
	private Platform current_platform =null;

	/**
	 * @author Derek 
	 * @param controller
	 */
	public ClapperRailGameState(Controller controller) {
		super(controller);
		Dimension d  = Toolkit.getDefaultToolkit().getScreenSize();
		GROUND=((int)d.getHeight())-(int)((106/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT);
		this.CR = new ClapperRail();
		this.platforms = new ArrayList<>();
		int flood_y = (int)((1180d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT);
		this.flood = new Flood(0,flood_y);

		//Adds in the intial platforms
		this.addPlatforms();

	}


	 /* @author Derek 
	 * @return
	 * -returns the CR property (our clapper rail)
	 */
	public ClapperRail getCR() {
		return CR;
	}


	
	
	
	/** @author Derek
	 *  @param
	 * Every object on the screen, (the bird, and the platforms)
	 * must move down at a constant rate to simulate you rising.
	 * 
	 * So, whenever the bird is above some point on the screen, we will shift
	 * every platform, and the bird, some constant amount, until the bird is below
	 * that arbitrary point on the screen.
	 * 
	 * So, when you are above that point, it will feel as if you are forever going up.
	 */
	
	public void objectShift(){
		for(Platform p : platforms){
			p.move();
		}
		flood.move();
		CR.move(0, (int)((5/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT));
		CR.setScore(CR.getScore()+CR.getScoreIncrease());
		int amount_of_xpixels =5;
		//controller.moveClapperBackground(amount_of_xpixels);
	}
	

	/**
	 *@author Derek 
	 * @param
	 * Do what happens ontick.
	 */
	@Override
	public void ontick() {
		if(waitingOnQuestion){
			return;
		}
		if(start){
			tutorialUpdate();
			return;
		}
		CR.ontick(platforms);

		
		if(CR.getPosition().getY() < ClapperRailGameState.MOVE_SCREEN_HEIGHT){
			objectShift();
		}
		handleLeftRightMovement();
		moveBackground();
		if (this.getIsGameRunning()) {
			checkOnPlatform2();
			checkFood();
			checkMaterials();
			checkFlood();
			checkQuestions();
			if(this.CR.getEnergy() <= 0) {
				this.CR.gameOver = true;
			}
		}
	}
	
	/** @author Derek
	 *  @param
	 *  -Handles the movement of the ClapperRail when moving left,right 
	 */
	public void handleLeftRightMovement(){
		int x = CR.getLeftRightState();
		switch(x){
		case 1 : CR.moveRight();break;
		case -1 : CR.moveLeft();break;
		}
	}

	/**
	 * @author Derek 
	 * @param o
	 * @see game.GameState#addGameObject(game.GameObject)
	 */
	@Override
	public void addGameObject(GameObject o) {
		// TODO Auto-generated method stub

	}


	/* (non-Javadoc)
	 * @see game.GameState#getBackgroundX()
	 * -Returns the BackgroundX int property representing at what level the background is
	 */
	public int getBackgroundX() {
		return this.BackgroundX;
	}

	
	/**@author Miguel
	 * -sets the CRTutMode property to true representing that there is currently a tutorial playing in Clapper Rail
	 */
	public void tutorialUpdate(){
		controller.setCRTutMode(true);
	}
	

	/**@author Miguel
	 * -moves the background 
	 */
	public void moveBackground() {
		this.setBackgroundX((this.BackgroundX % 1000) + this.getCR().getVelocity().getXSpeed());
	}
	/**
	 * @author Derek 
	 * @see game.GameState#getUpdateableGameObjects()
	 */
	@Override
	public ArrayList<GameObject> getUpdateableGameObjects() {
		ArrayList<GameObject> output = new ArrayList<>();
		output.add(CR);
		output.add(flood);
		// this.addPlatforms();
		output.addAll(platforms);
//		output.addAll(food);
//		output.addAll(materials);
		/*
		 * for(Platform p:platforms) { checkOnPlatform(p); }
		 */

		return output;
	}

	
	/** @author Jake/Miguel
	 *  @param 
	 *  Iterates through platforms arraylist checking if the current ClapperRail position is touching
	 *  a platform. 
	 */
	public void checkOnPlatform2() {
		//System.out.println(platforms.size());

		for(Platform p:platforms) {
			if(this.current_platform!=null) {
				//System.out.println(this.current_platform.getPosition());
				
			}
			
		
			//System.out.print(p.getPosition());
			
			if(p.touchPlatform(this.CR.getPosition())&&this.current_platform==null) {
				this.CR.setIsFalling(false);
				this.current_platform = p;
				break;
			}
			else if(!p.touchPlatform(this.CR.getPosition())&&this.current_platform!=p) {
				this.CR.setIsFalling(true);
				this.current_platform = null;
			}

		}
	}
	
	/** @author Derek
	 *  @param
	 *  Iterates through platforms and checks if the current ClapperRail position is touching any
	 *  existent question box on the platform
	 */
	public void checkQuestions(){
		Collection<Platform> filtered = platforms.stream().filter(p -> p.getQuestion()!=null).collect(Collectors.toList());
		Iterator<Platform> plat_it = filtered.iterator();
		while(plat_it.hasNext()){
			Platform pl = plat_it.next();
			ClapperQuestion q = pl.getQuestion();
			
			if(q.touchObject(this.CR.getPosition(),ClapperQuestion.RADIUS)){
				pl.removeQuestion();
				invokeRandomQuestion();
			}
		}
	}
	public void setWaitingOnQuestion(boolean b){
		this.waitingOnQuestion=b;
	}
	public boolean getWaitingOnQuestion(){
		return this.waitingOnQuestion;
	}


	/** @author Derek
	 *  @param
	 *  
	 */
	public void invokeRandomQuestion(){
		controller.setClapperNotMoving();
		setWaitingOnQuestion(true);
		boolean correct = controller.randomClapperQuestion();
		if(correct){
			CR.setMaterialCount(CR.getMaterialCount()+10);
		}
		setWaitingOnQuestion(false);
	}
	
	
	/** @author Jake
	 *  @param
	 *  Iterates through the platforms and checks if the current ClapperRail position is touching any
	 *  existent Food on the platform
	 */
	public void checkFood() {
		Collection<Platform> filtered = platforms.stream().filter(p -> p.getFood()!=null).collect(Collectors.toList());
		Iterator<Platform> plat_it = filtered.iterator();
		while(plat_it.hasNext()){
			Platform pl = plat_it.next();
			Food f = pl.getFood();
			
			if(f.touchObject(this.CR.getPosition(),Food.RADIUS)) {
				pl.removeFood();
				this.CR.gainEnergy();
			}
			
		}
	}
	/** @author Jake
	 *  @param
	 *  Iterates through the platforms and checks if the current ClapperRail position is touching any
	 *  existent Material on the platform
	 */
	public void checkMaterials() {
		Collection<Platform> filtered = platforms.stream().filter(p -> p.getMaterial()!=null).collect(Collectors.toList());
		Iterator<Platform> plat_it = filtered.iterator();
		
		while(plat_it.hasNext()){
			Platform pl = plat_it.next();
			Material m = pl.getMaterial();
			
			if(m.touchObject(this.CR.getPosition(),Material.RADIUS)) {
				pl.removeMaterial();
				this.CR.setMaterialCount(this.CR.getMaterialCount()+1);
			}
			
		}
	}
	
	/** @author Jake
	 *  @param
	 *  Checks to see if current ClapperRail position is less than or equal to Flood position.
	 *  If so, decrements the ClapperRail energy. If not, increases Flood. 
	 */
	public void checkFlood() {
		if(CR.getPosition().getY() >= ClapperRailGameState.GROUND-5){
			if(flood.getPosition().getY() <= (int)((1080/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)){
				CR.setEnergy(CR.getEnergy()-ClapperRail.ENERGY_LOSS);
			}
			if(flood.getPosition().getY() > (int)((880d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)){
				flood.increaseFlood();
			}
		}	
		
	}
	/**@author Miguel
	 * @param b
	 * -sets the 'start' boolean to the inputted b boolean
	 */
	public void setStart(boolean b){
		start=b;
	}
	
	/**@author Miguel
	 * @return
	 * -returns the ArrayList of platforms
	 */
	public ArrayList<Platform> getPlatforms(){
		return this.platforms;
	}
	
	/**@author Miguel
	 * @return
	 * -retrieves the flood
	 */
	public Flood getFlood() {
		return this.flood;
	}

	/**@author Miguel
	 * @return
	 * -returns the 'start' property boolean
	 */
	public boolean getStart(){
		return this.start;
	}
	
	/** @author Derek
	 *  @param
	 *  Adds platforms to the game state.
	 */
	public void addPlatforms() {
			this.platforms.add(new Platform((int)((1620d/dereks_width)*GameScreen.PLAY_SCREEN_WIDTH), 0));
			this.platforms.add(new Platform((int)((180d/dereks_width)*GameScreen.PLAY_SCREEN_WIDTH), (int)((768d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)));
			this.platforms.add(new Platform((int)((540d/dereks_width)*GameScreen.PLAY_SCREEN_WIDTH), (int)((576d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)));
			this.platforms.add(new Platform((int)((900d/dereks_width)*GameScreen.PLAY_SCREEN_WIDTH),(int)((384d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)));
			this.platforms.add(new Platform((int)((1260d/dereks_width)*GameScreen.PLAY_SCREEN_WIDTH),(int)((192d/dereks_height)*GameScreen.PLAY_SCREEN_HEIGHT)));
	}
}

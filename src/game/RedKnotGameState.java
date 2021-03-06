package game;
/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

/*Class: RedKnotGameState
 * -extends the abstract class GameState (Model) 
 * -keeps track of the gamestate data of the RedKnot minigame
 */
public class RedKnotGameState extends GameState {
	//miguels_height,width are used as ratios to achieve the same look as it does on Miguel's laptop for ALL laptops/monitor dimensions
	static double miguels_width = 1000.0;
	static double miguels_height = 600.0;
	
	private RedKnot RK;
	private int score;
	
	//If true, draws a rectangle around all the GameObjects in the Screen showcasing their collision box (HitBox)
	private boolean debug_mode;
	

	//Enemy clouds
	private ArrayList<Cloud> clouds; 
	private final int AMOUNT_OF_ENEMYCLOUDS = 3;
	
	//FlockBirds
	private ArrayList<FlockBird> flock; 
	//The range which controls how low/high of a chance for a FlockBird to appear
	private final int FB_CHANCE_MAX = 1000; 
	private final int FB_CHANCE_LOW = 0;
	private final int FB_THRESHOLD = 10;
	
	private final int QC_CHANCE_LOW = 0;
	private final int QC_CHANCE_MAX= 2000;
	private final int QC_THRESHOLD = 5;
	
	private final int MAX_AMOUNT_OF_BIRDS_REMOVED = 5;
	
	//Variable to stop the game from continously removing FlockBirds from 'flock' when touching the same cloud
	private Cloud lastCloudTouched;
	
	
	//GAME_TIME: (NOTE: ALL TIMING IS DONE IN MILLISECONDS)
	//EX: GameTimer.ONE_SECOND == 1000 for 1000 milliseconds as 
	//this is what the Java.util.Timer takes in
	static final int MAX_GAME_TIME = GameTimer.ONE_MINUTE; //15 seconds (temporary)
	private GameTimer game_timer;
	private int current_time;
	public QuestionWindow current_quiz; //CHANGED TO PUBLIC
	private final String REDKNOTQUESTIONS_TEXTFILE = "RedKnotQuestions.txt";
	
	private long collisionTime = System.currentTimeMillis();
	
	//MAPSIZE
	static final int MAP_SIZE = (int)((150/miguels_width)*GameScreen.PLAY_SCREEN_WIDTH);
	
	/*Score final constants*/
	static final int COLLECTED_BIRD_SCORE = 200;
	static final int TOUCHED_CLOUD_SCORE = -200;
	static final int QUESTION_CORRECT = 1000;
	static final int AMOUNT_OF_BIRDS_ADDED = 25;
	
	//QUESTION WINDOWS
	static int QUESTIONBOX_WIDTH = (int)((300/miguels_width)*GameScreen.PLAY_SCREEN_WIDTH);
	static int QUESTIONBOX_HEIGHT = (int)((150/miguels_height)*GameScreen.PLAY_SCREEN_HEIGHT);
	
	
	//Tutorial
	public RKTutorialAction current_TA;
	public boolean doneTutorial; //tells the game that the tutorial is done
	public boolean turnOffTutorial; //is true then game does not play the tutorial, if false then game plays the tutorial
	public QuestionReader qr;
	
	//Ending (Handles the end dialogue box that returns the player to the main menu)
	public boolean reachedDestination = false;
	public boolean hasPressedEndDialogue = false;

	/**@author Miguel
	 * @param controller
	 */
	public RedKnotGameState(Controller controller){
		super(controller);
		this.lastCloudTouched = null;
		this.current_quiz = null;
		this.score=0; //Sets the initial score 
		this.RK = new RedKnot();
		this.flock = new ArrayList<>();
		this.clouds = new ArrayList<>();
		this.addGameObject(new GameObject(new Position(5,5), new Size(30,30), RedKnotAsset.BACKGROUND));
		debug_mode = false; //initially turns off debug mode
		
		//Setting Up Tutorial
		this.current_TA = null;
		this.doneTutorial = false;
		
		this.qr = new QuestionReader("resources/text_files/"+REDKNOTQUESTIONS_TEXTFILE);
		
		
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				
				if(getIsGameRunning()) {
					//System.out.println("TIMER");
					current_time+=GameTimer.ONE_SECOND;
					//System.out.println("GAMETIME RAN:"+current_time +" milliseconds");
					
					
					//INDICATING THE REDKNOT HAS SUCCESSFULLY MIGRATED TO ITS DESTINATION
					if(current_time>= MAX_GAME_TIME) {
						setIsGameRunning(false);
						reachedDestination = true;
					}
				}
			}
		};
		      
		
		//the game timer runs every second and updates the counter 'current_time'
		this.game_timer = new GameTimer(GameTimer.ONE_SECOND,task);

		if(this.current_TA==null) {
			RKTutorialAction up_key = new RKTutorialAction(new Thread(new Runnable() {
				@Override
				public void run() {
					isGameRunning = false;
					while(!isUp_key_pressed()) {
						//NOTE: DO NOT REMOVE PRINT STATEMENTS,
						//game is not able to pull boolean values without them
						System.out.println("UPKEY:"+isUp_key_pressed());
					}
					current_TA = null;
					isGameRunning = true;
					
					
					
					//DOWN KEY TUTORIAL------------------------
					RKTutorialAction down_key = new RKTutorialAction(new Thread(new Runnable() {
						@Override
						public void run() {
							isGameRunning = false;
							while(!isDown_key_pressed()) {
								//NOTE: DO NOT REMOVE PRINT STATEMENTS,
								//game is not able to pull boolean values without them
								System.out.println("UPKEY:"+isUp_key_pressed());
							}
							current_TA = null;
							isGameRunning = true;
							
						}}), new Position(RK.getPosition().getX()+RK.getSize().getWidth(),RK.getPosition().getY()),new Size(350,200), RedKnotAsset.DOWNARROWFLASH);
					
					createAndSetCurrentTA(down_key);
					
					
					
					
				}
			}), new Position(RK.getPosition().getX()+RK.getSize().getWidth(),RK.getPosition().getY()),new Size(350,200), RedKnotAsset.UPARROWFLASH);
			
			createAndSetCurrentTA(up_key);
		
		}
	}
	
	/**@author Miguel
	 * @param c
	 * -Creates the tutorial of once a Cloud reaches a certain distance to the player
	 */
	public void createCloudTutorial(Cloud c) {
		int tutorial_threshold = (int)(GameScreen.PLAY_SCREEN_WIDTH*.75);
		if(c.getPosition().getX()<tutorial_threshold && this.current_TA==null && this.doneTutorial==false) {
			RKTutorialAction cloud_tutorial = new RKTutorialAction(new Thread(new Runnable(){
				@Override
				public void run() {
					isGameRunning = false;

					GameTimer delay_timer = new GameTimer(new TimerTask() {
							@Override
							public void run() {
								while((isDown_key_pressed()==false&&isUp_key_pressed()==false)) {
									
									//NOTE: DO NOT REMOVE PRINT STATEMENTS,
									//game is not able to pull boolean values without them
									System.out.println("DOWNKEY:"+isDown_key_pressed());
									System.out.println("UPKEY:"+isUp_key_pressed());
								}
								current_TA = null;
								isGameRunning = true;
								doneTutorial = true; //indicates that the tutorial is completely done 
							}
						}, (int)(GameTimer.ONE_SECOND));
					
					
				}
			}), new Position(RK.getPosition().getX()+RK.getSize().getWidth(),RK.getPosition().getY()), new Size(350,200), RedKnotAsset.RKGOALS);
			
			createAndSetCurrentTA(cloud_tutorial);
		}
	}
	
	
	
	/**@author Miguel
	 * @return
	 * -returns whether or not the player pressed 'Ok' at the very end of the redknot game dialogue box
	 */
	public boolean isHasPressedEndDialogue() {
		return hasPressedEndDialogue;
	}

	/**@author Miguel
	 * @param hasPressedEndDialogue
	 * -tells the redknotgamestate that the player has pressed 'Ok' in the final dialogue box
	 */
	public void setHasPressedEndDialogue(boolean hasPressedEndDialogue) {
		this.hasPressedEndDialogue = hasPressedEndDialogue;
	}

	/**@author Miguel
	 * @param TA
	 * -Sets what the current tutorial action is (
	 */
	public void createAndSetCurrentTA(RKTutorialAction TA){
		this.current_TA = TA;
	}
	
	
	/**
	 * @param fb_iter
	 * @param FB
	 */
	public void collectBird(ListIterator<FlockBird> fb_iter, FlockBird FB) {
		fb_iter.add(FlockBird.spawnNearbyFlockBird(RK, FB));
	}
	

	/**@author Miguel
	 * 
	 */
	public void lostBird() {
		if(this.flock.size()>0) {
			this.flock.remove(0);
		}
	}
	
	
	/**@author Miguel
	 * @param p
	 * -Creates a QuestionWindow instance which is our game's Quiz questions
	 * -It allows the player to pick an option and be able to gain a reward if the player
	 *selects the correct choice (gains more birds, and a score bonus)
	 */
	public void createQuiz(Position p) {
		
		
			this.isGameRunning = false;
			//Testing the QuestionReader: (WORKS)
			
			//If there are quiz questions
			if(this.qr.getQuizQuestions().isEmpty()==false) {
				int random_index = Utility.randRangeInt(0, qr.getQuizQuestions().size()-1);
				QuizQuestion qq = this.qr.getQuizQuestions().get(random_index);
				Position set_pos = new Position(GameScreen.PLAY_SCREEN_HEIGHT,0);
		
				
				this.current_quiz = new QuestionWindow(p, new Size(300,200),qq.getQuestion(), qq.getAnswer(), qq.getResponses());
				
				this.qr.removeQuestion(random_index); //removes the quiz question after selecting it
				for(JRadioButton rb:this.current_quiz.getResponse_buttons()) {
					rb.addActionListener(new ActionListener() {
		        @Override
		        public void actionPerformed(ActionEvent e) {
		        	//System.out.println("SELECTED:"+rb.getText());
		        
		            
		            if(!rb.getText().equalsIgnoreCase(current_quiz.getAnswer())) {
		            	System.out.println("WRONG");
		            	current_quiz.dispose();
		            	current_quiz = null;
		            	isGameRunning = true;
		            	//System.exit(0);
		            	
		            }
		            else {
		            	isGameRunning = true;
		            	System.out.println("CORRECT");
		            	current_quiz.dispose(); //destroys the JFrame Question window
		            	current_quiz = null;
		            	score+=RedKnotGameState.QUESTION_CORRECT; //increments score because player got question correct
		            	int added_birds = AMOUNT_OF_BIRDS_ADDED; //test
		            	for(int i=0;i<added_birds;i++) {
		            		System.out.println("SPAWNING BIRD");
		            		flock.add(FlockBird.spawnNearbyFlockBird(RK,FlockBird.spawnRandomFlockBird(0, 0, 0)));
		            	}
		            }
	
	        }});}}
	}

//question_thread.run();



	/* (non-Javadoc)
	 * @see game.GameState#ontick()
	 */
	@Override
	public void ontick() {
		//Modify GameObjects, then GameObjects are passed to the controller	
		//Only runs the game if the game is currently running
		if(this.getIsGameRunning()) {
			checkClouds();
			RK.move();
			checkFlockBirds();
		}
		
		
	
	}
	
	public boolean hasReachedDestination() {
		return this.reachedDestination;
	}

	
	
	
	

	/**@author Miguel
	 * -Takes in no arguments, returns nothing
	 * -Using an iterator, iterates through the 'clouds' ArrayList
	 * -Handles any cloud related actions in order to keep it O(n) and
	 * to keep game logic more organized
	 */
	/*NOTE: 
	 * Added collision with clouds and everything works perfectly but
	 * if the Red Knot touches more than one cloud at a time the game
	 * 'overticks' and removes all the birds/removes the player's score a lot
	 */
	public void checkClouds(){
		if((System.currentTimeMillis() - collisionTime) > 400)
			this.RK.setColliding(false);
		addClouds(); //adds the clouds intially and readds clouds
		
		//Keeps track of the Player touching one or 2+ clouds
		Cloud first = null;
		Cloud second = null;
		
		//Added iterator to remove clouds once they reach the end
		ListIterator<Cloud> cloud_iter = clouds.listIterator();
		while(cloud_iter.hasNext()) {
			Cloud c = cloud_iter.next();
			c.move();
			
			this.createCloudTutorial(c);
			
			
			//Detects if the Player is touching two clouds at the same time and if so 
			//stops the code from 'overticking' and removing the player's points
			boolean touching_two_clouds = false;
			if(Utility.GameObjectCollision(RK, c)&& first==null) {
				first = c;
			}
			else if(Utility.GameObjectCollision(RK, c) && first!=null && second==null) {
				second = c;
			}
			if(first!=null && second!=null) {
				touching_two_clouds = true;
			}
			
			
			//If the cloud goes out of bounds (exits the left side of screen)
			//it then gets removed from 'clouds' and a new 'Cloud'
			//instance is created
			if(c.checkIfOutOfBounds(Cloud.LEFT_MOST)){
				//System.out.println("REMOVING");
				try {
					cloud_iter.remove();
				}
				catch(Exception e) {
					//e.printStackTrace();
				}
			}
			
			Iterator<FlockBird> fb_iter = flock.iterator();
			while(fb_iter.hasNext()) {
				FlockBird FB = fb_iter.next();

				//Checking Enemy Clouds
				if(Utility.GameObjectCollision(RK, c) && this.lastCloudTouched !=c && c instanceof EnemyCloud && touching_two_clouds==false) {
					this.lastCloudTouched = c;
					RK.setColliding(true);
					this.collisionTime = System.currentTimeMillis();
					this.incrementScore(TOUCHED_CLOUD_SCORE);
					int random_amount = Utility.randRangeInt(0, this.MAX_AMOUNT_OF_BIRDS_REMOVED);
					this.setBirdsLost(this.flock, random_amount);
						
				}
				
				//Checking for QuestionCloud (in order to initialize a quiz)
				else if(Utility.GameObjectCollision(RK, c) && this.lastCloudTouched !=c && c instanceof QuestionCloud && this.current_quiz==null && touching_two_clouds==false) {
					//Stops the redknot and flockbirds from accelerating
					RK.setFlyState(0);
					for(FlockBird fb:flock) {
						fb.setFlyState(0);
					}
					
					this.lastCloudTouched = c;
					
					//Test QuestionBox (for quiz)
					this.createQuiz(c.getPosition());
					c.setPosition(new Position(0-c.hitBox.width,c.getPosition().getY()));
					RK.setPosition(RK.getPosition());
				}
			
		}
		
	}}
	
	/**@author Miguel
	 * -Randomly checks and adds a QuestionCloud
	 */
	public void addQuestionCloud(int low_chance, int high_chance, int threshold) {
		int chance = Utility.randRangeInt(low_chance,high_chance);
		
		if(chance<threshold && (this.qr.getQuizQuestions().size()>=1)) {
			QuestionCloud qc = new QuestionCloud(Cloud.spawnCloud(GameScreen.PLAY_SCREEN_HEIGHT, 0, GameScreen.PLAY_SCREEN_HEIGHT));
			if(qc instanceof QuestionCloud) {
				System.out.println("QUESTIONCLOUD");
			}
			clouds.add(qc);
		}
	}
	
	/*Created by Derek:
	 * -Takes in no arguments, returns nothing
	 * -Uses MVC design to pass over GameObjects to the Controller
	 * which then passes them to the RedKnotView in order to draw
	 * them onto the screen
	 */
	/* (non-Javadoc)
	 * @see game.GameState#getUpdateableGameObjects()
	 */
	public ArrayList<GameObject> getUpdateableGameObjects(){
//		RK flock clouds
		ArrayList<GameObject> output = new ArrayList<>();
		output.add(RK);//it is important that we insert the redknot first.
		output.addAll(clouds);
		output.addAll(flock);
		
		output = updateGameObjects(RK, output);
		
		return output;
		
	}
	
	/**@author Miguel
	 * @param RK
	 * @param GO_AL
	 * @return ArrayList<GameObject>
	 * -Iterates through every GameObject instance and updates it
	 * -Currently updates every GameObject by moving its position to the left by the RedKnot's velocity (since the redknot is moving towards the 
	 * clouds, flock birds, etc)
	 */
	public ArrayList<GameObject> updateGameObjects(RedKnot RK, ArrayList<GameObject> GO_AL) {
		if(this.getIsGameRunning()) {
			for(GameObject GO:GO_AL) {
				
				//Shifts all of the game objects by the RedKnots velocity
				if(GO!=RK && (GO instanceof FlockBird == false)) {
					//System.out.println("BEFORE POS:"+GO.getPosition());
					int x_speed=  -1*RK.getVelocity().getXSpeed();
					GO.shiftGameObject(new Velocity(x_speed,0));
					//System.out.println("UPDATING VELOCITIES:"+x_speed);
					//System.out.println("AFTER POS:"+GO.getPosition());
				}
			}
		}
		
		return GO_AL;
	}

	/**@author Miguel
	 * -Takes in no arguments, returns nothing
	 * -Adds 'Cloud' instances into 'clouds' ArrayList
	 * -Currently re-adds 'Cloud' instances when they go off-screen
	 */
	public void addClouds() {
//		System.out.println("READDING CLOUDS");
//		System.out.println("CLOUDS SIZE:"+clouds.size());
		
		//Checks if there are not enough clouds and adds the amount needed
		if(clouds.size()<this.AMOUNT_OF_ENEMYCLOUDS) {
			for(int i=0;i<this.AMOUNT_OF_ENEMYCLOUDS-clouds.size();i++) {
				int screen_width = this.controller.getScreen().getX();
				int screen_height = this.controller.getScreen().getY();
				this.clouds.add(new EnemyCloud(Cloud.spawnCloud(GameScreen.PLAY_SCREEN_WIDTH, 0, GameScreen.PLAY_SCREEN_HEIGHT-Cloud.Y_MARGIN)));
			}
		}
		
		addQuestionCloud(this.QC_CHANCE_LOW, this.QC_CHANCE_MAX,this.QC_THRESHOLD);
	}
	

	/**@author Miguel
	 *-Iterates through the 'flock' List of FlockBirds and checks them. If a flockBird goes off screen then it removes it from the 
	 *List of FlockBirds for example. 
	 */
	public void checkFlockBirds() {
		addRandomFlockBirds(this.FB_CHANCE_LOW, this.FB_CHANCE_MAX,this.FB_THRESHOLD);
		
		//Added iterator to remove clouds once they reach the end
		ListIterator<FlockBird> fb_iter = flock.listIterator();
		
		while(fb_iter.hasNext()) {
			FlockBird FB = fb_iter.next();
			FB.move();
			
			detectFlockBirdCollection(this.getRK(),FB,fb_iter);
			
			//If the cloud goes out of bounds (exits the left side of screen)
			//it then gets removed from 'clouds' and a new 'Cloud'
			//instance is created
			if(FB.checkIfOutOfBoundsLeft()){
				//System.out.println("REMOVING");
				fb_iter.remove();
			}
			
			if(FB.checkIfOutOfBoundsBottom()) {
				fb_iter.remove();
			}
		}
	}
	

	/**@author Miguel
	 *-Allows the FlockBirds owned by the Player, to move alongside the player. 
	 *EX: if the player moves up, the flockbirds owned by the player also move up
	 */
	public void setFlyStateAllFlockBirds() {
		ListIterator<FlockBird> fb_iter = flock.listIterator();
		
		while(fb_iter.hasNext()) {
			FlockBird FB = fb_iter.next();
			
			if(FB.getFlyState() == -1) {
				FB.setFlyState(0);
			}
			else {
				FB.setFlyState(FB.getFlyState());
			}
			
			if(FB.getFlyState() == 1) {
				FB.setFlyState(0);
			}
			else {
				FB.setFlyState(FB.getFlyState());
			}
		}
	}
	
	/**@author Miguel
	 * @param list
	 * @param amount
	 * -Given an integer 'amount', it takes that integer and checks 'amount'
	 * many times, a random index of the flock list. 
	 * -If the random index is a bird that is owned by the player, it is 'removed' by the flock
	 * by setting its 'setGotLostInStorm' variable to true which causes
	 * the bird to fall.
	 */
	public void setBirdsLost(List<FlockBird> list, int amount) {
		for(int i=0;i<amount;i++) {
			try {
				int random_index = Utility.randRangeInt(0, list.size());
				FlockBird fb = list.get(random_index);
				
				if(fb.getIsCollected()) {
					fb.setGotLostInStorm(true);
				}
			}
			catch(Exception e) {
				System.out.println("INDEX ERROR");
			}
			
		}
	}
	

	/**@author Miguel
	 *-Allows the FlockBirds owned by the Player, to move alongside the player. 
	 *EX: if the player moves up, the flockbirds owned by the player also move up
	 */
	public void allFlockBirdsFlyUp() {
		ListIterator<FlockBird> fb_iter = flock.listIterator();
		
		while(fb_iter.hasNext()) {
			FlockBird FB = fb_iter.next();
			FB.newFlyUp();
		}
	}
	
	/**@author Miguel
	 *-Allows the FlockBirds owned by the Player, to move alongside the player. 
	 *EX: if the player moves down, the flockbirds owned by the player also move down
	 */
	public void allFlockBirdsFlyDown() {
		ListIterator<FlockBird> fb_iter = flock.listIterator();
		
		while(fb_iter.hasNext()) {
			FlockBird FB = fb_iter.next();
			FB.newFlyDown();
		}
	}
	
	/**@author Miguel
	 * @param RK
	 * @param FB
	 * @param fb_iter
	 * @return boolean (true if collision occurred, false otherwise)
	 * -Checks if the player (Redknot) touched an 'uncollected' flockbird and therefore if they did
	 * then spawns a FlockBird near the player and travels alongside the player
	 */
	public boolean detectFlockBirdCollection(RedKnot RK, FlockBird FB, ListIterator fb_iter) {
		if(Utility.GameObjectCollision(RK, FB) && !FB.getIsCollected()) {
			fb_iter.remove(); //removes the FlockBird
			this.collectBird(fb_iter, FB);
			this.incrementScore(COLLECTED_BIRD_SCORE);
			return true;
		}
		else {
			return false;
		}
	}

	/**@author Miguel
	 * -Adds random FlockBirds (birds that are Not collected by the Player) by the 'FB_CHANCE_LOW' and 'FB_CHANCE_MAX' (chance and randomness)
	 */
	public void addRandomFlockBirds(int low_chance, int high_chance, int threshold) {
		int chance = Utility.randRangeInt(low_chance,high_chance);

		if(chance<threshold) {
			this.flock.add(FlockBird.spawnRandomFlockBird(GameScreen.PLAY_SCREEN_WIDTH+FlockBird.X_MARGIN, 0+FlockBird.TOP_Y_MARGIN, GameScreen.PLAY_SCREEN_HEIGHT-FlockBird.BOTTOM_Y_MARGIN));
		}
	}

	
	/*Getters, Setters----------------------*/
	
	/* (non-Javadoc)
	 * @see game.GameState#addGameObject(game.GameObject)
	 */
	@Override
	public void addGameObject(GameObject o) {
		
	}
	
	/*Getters*/
	
	/**@author Miguel
	 * @return
	 */
	public ArrayList<Cloud> getClouds(){
		return this.clouds;
	}
	
	/**@author Miguel
	 * @return
	 */
	public int countBirds() {
		return flock.size();
	}
	
	/**@author Miguel
	 * @return
	 */
	public RedKnot getRK() {
		return RK;
	}

	/**@author Miguel
	 * @return
	 */
	public ArrayList<FlockBird> getFlock() {
		return flock;
	}
	
	/**@author Miguel
	 * @return debug_mode
	 * -Returns whether or not the game is in debug_mode
	 */
	public boolean getDebuggingMode() {
		return this.debug_mode;
	}
	
	/**@author Miguel
	 * @param b
	 * -sets the debug_mode to inputed boolean b
	 */
	public void setDebuggingMode(boolean b) {
		this.debug_mode = b;
	}
	
	/**@author Miguel
	 * @return
	 */
	public int getScore(){
		return score;
	}
	/**@author Miguel
	 * @param x
	 */
	public void setScore(int x){
		score=x;
	}
	/**@author Miguel
	 * @param x
	 */
	public void incrementScore(int x){
		score+=x;
	}
	
	/**@author Miguel
	 * @param new_flock
	 * -Sets the current ArrayList of flock birds to the new inputted ArrayList of FlockBirds
	 */
	public void setFlock(ArrayList<FlockBird> new_flock) {
		this.flock = new_flock;
	}
	
	/**@author Miguel
	 * @param new_clouds
	 * -Sets the 'clouds' property to the inputted cloud ArrayList
	 */
	public void setClouds(ArrayList<Cloud> new_clouds) {
		this.clouds = new_clouds;
	}

	/**@author Miguel
	 * @return
	 * -retrieves the current_TA property
	 */
	public RKTutorialAction getCurrent_TA() {
		System.out.println("CURRENTTA:"+current_TA);
		return current_TA;
	}

	/**@author Miguel
	 * @param current_TA
	 * -Sets the current RKTutorialAction property
	 */
	public void setCurrent_RKTA(RKTutorialAction current_TA) {
		this.current_TA = current_TA;
	}

	
	
	
	
}

package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;


/*
 * TODO: Take anything that updates the model, and move it out of the view.
 */


/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

/*Class: RedKnotView
 * -class that acts as the View of the RedKnot GameMode
 * -contains methods and control over the drawing of the RedKnot minigame
 */
public class RedKnotView extends GameView {
	/**
	 * 
	 */
	
	/*
	 *  Clouds 
	 *  Bird
	 *  Score
	 * 
	 */
	private final int BACKGROUND_SPEED = 5;
	private ArrayList<Cloud> clouds;
	private RedKnot redKnot;
	private ArrayList<FlockBird> flock;
	private MiniMap map;
	private int score;
	private boolean debug_mode;
	
	int background_x = 5;
	
	//MAP:
	private FlatteningPathIterator iter;
	private Path2D.Double map_curve;
	private ArrayList<Point> points;
	
	private GameTimer test_timer;
	int current_time;
	
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	public RedKnotView(){
		super();
		score=0;
		redKnot= new RedKnot();
		clouds = new ArrayList<>();
		flock = new ArrayList<>();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				
				
				current_time+=1;
				//System.out.println("GAMETIME RAN:"+current_time +" milliseconds");
			}
		};
		test_timer = new GameTimer(1000, task);
		
		//Map: (Curve works for any map size, used ratios to determine curve points etc)
		Size map_size = new Size(150,150);
		map = new MiniMap(new Position(GameScreen.PLAY_SCREEN_WIDTH-map_size.getWidth()-MiniMap.LEFT_MARGIN,GameScreen.PLAY_SCREEN_HEIGHT-map_size.getHeight()-MiniMap.BOTTOM_MARGIN), map_size);
		this.current_time = 0;
		this.map_curve = new Path2D.Double();
		createMapPoints();
		
	
		
		try {
			loadAllImages("/resources/images/redknot");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	/*
	 * NOTE: 
	 * NEVER CHANGE THE MODEL IN HERE EVER. ONLY DRAW WHAT THE MODEL IS.
	 */
	/* (non-Javadoc)
	 * @see game.GameView#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		scrollImage(g, RedKnotAsset.SABACKGROUND, RedKnotAsset.SABACKGROUND);
		g.setColor(Color.RED);
//		birdMovement(RK);
		drawClouds(g);
		drawScore(g);
		drawBird(g);
		g.setColor(Color.BLUE);
		drawFlockBirds(g);
		Utility.drawHitBoxPoint(g, this.redKnot.hitBox, this.debug_mode);	
		
		//MAP:
		drawMap(g);
		drawMapCurve(g);
	}
	
	public void drawMapCurve(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
//		System.out.println(map_size.getWidth());
//		System.out.println(map_size.getHeight());
//		System.out.println("X1Y1:"+x1+','+y1);
//		System.out.println("X3Y3:"+x3+','+y3);
//		System.out.println("X2Y2:"+x2+","+y2);
		g2d.draw(map_curve);
		
		if(points.size()>0) {
			System.out.println(points.size());
			g.setColor(Color.RED);
			int i = this.current_time;
			g.fillOval((int)points.get(i%points.size()).getX(), (int)points.get(i%points.size()).getY(), 5, 5);
		}
		
	}
	
	public void createMapPoints() {
		Size map_size = new Size(map.hitBox.width,map.hitBox.height);
		int x1 = (int) ((320d/500d)*map_size.getWidth())+map.getPosition().getX(); //gives the width ratio of first x1
		int y1 = (int) ((420d/500d)*map_size.getHeight())+map.getPosition().getY(); //gives height ratio
		int x3 = (int) ((260d/500d)*map_size.getWidth())+map.getPosition().getX();
		int y3 = (int) ((140d/500d)*map_size.getHeight())+map.getPosition().getY();
		
		
		int x2 = (x1-x3)+x3;
		int y2 = (y3-y1)+y3+(int)(y1*.075);
		
		map_curve.moveTo(x1, y1);
		map_curve.curveTo(x1, y1, x2, y2, x3, y3);
		
        float[] coords=new float[6];
        this.points = new ArrayList<>();
		this.iter=new FlatteningPathIterator(map_curve.getPathIterator(new AffineTransform()), 0.01);
        while (!this.iter.isDone()) {
            this.iter.currentSegment(coords);
            int x=(int)coords[0];
            int y=(int)coords[1];
            this.points.add(new Point(x,y));
            this.iter.next();
        }
        System.out.println(points.size());
	}
	
	/* (non-Javadoc)
	 * @see game.GameView#update(java.util.ArrayList)
	 */
	public void update(ArrayList<GameObject> gameObjects){
		clouds = new ArrayList<>();
		redKnot = (RedKnot)gameObjects.get(0);
		flock = new ArrayList<>();
		//gameObjects.remove(0);
		for(GameObject go : gameObjects){
			if(go instanceof Cloud){
				clouds.add((Cloud)go);
			}
			else if(go instanceof FlockBird) {
				//System.out.println("ADDED FLOCK BIRD");
				flock.add((FlockBird)go);
			}
		}

	}
	
	/**
	 * @param debug_mode
	 */
	public void updateDebugging(boolean debug_mode) {
		this.debug_mode = debug_mode;
	}
	
	//draw our character, the bird.
	/**
	 * @param g
	 */
	public void drawBird(Graphics g){
		Animation birdAnim = (Animation) objectMap.get(RedKnotAsset.MAINBIRD);
		g.drawImage(birdAnim.currImage(),redKnot.getPosition().getX(),redKnot.getPosition().getY(),redKnot.getSize().getWidth(),redKnot.getSize().getHeight(),null,this);
	}
	
	/**
	 * @param g
	 */
	public void drawScore(Graphics g){
		g.setColor(Color.BLACK);
		g.setFont(new Font("TimesRoman",Font.PLAIN,RedKnotGameState.SCORE_FONT_SIZE));
		FontMetrics fm = g.getFontMetrics();
		//System.out.println(fm.getFont());
		
		//The String being drawn
		String toDrawString = RedKnotGameState.SCORE_TEXT + this.score;
		int string_width = fm.stringWidth(toDrawString);
		
		g.drawString(toDrawString, GameScreen.PLAY_SCREEN_WIDTH-string_width-GameScreen.SCREEN_BORDER_PX, 0+RedKnotGameState.SCORE_FONT_SIZE);
	}
	
	/**
	 * @param FB
	 * @param g
	 */
	public void drawFlockBird(FlockBird FB, Graphics g) {
		Animation FlockBirdAnim = (Animation) objectMap.get(RedKnotAsset.MAINBIRD);
		g.drawImage(FlockBirdAnim.currImage(FB.frameIndex),FB.getPosition().getX(),FB.getPosition().getY(),FB.getSize().getWidth(),FB.getSize().getHeight(),null,this);
		FB.updateCurrImage();
	}
	
	/**
	 * @param g
	 */
	public void drawFlockBirds(Graphics g) {
		for(FlockBird FB: flock) {
			drawFlockBird(FB, g);
			Utility.drawHitBoxPoint(g, FB.hitBox, this.debug_mode);
			if(Utility.GameObjectCollision(this.redKnot, FB) && this.debug_mode) {
				System.out.println("COLLISION!");
			}
		}
	}
	
	//Takes the Clouds ArrayList and draws individual clouds
	/**
	 * @param g
	 */
	public void drawClouds(Graphics g) {
		
		
		for(Cloud c:clouds){
			drawCloud(c,g);
			Utility.drawHitBoxPoint(g, c.hitBox, this.debug_mode);
			if(Utility.GameObjectCollision(this.redKnot, c) && this.debug_mode) {
				System.out.println("COLLISION!");
			}
		}
		/*
		for(Cloud c:clouds) {
			drawCloud(c,g);	
			//the hitbox drawing needs to be restructured, if we want to still use it.
//			Utility.drawHitBoxPoint(g, c.hitBox, this.controller.getRedKnotGS().debug_mode);
		}*/
	}
	/**
	 * @param c
	 * @param g
	 */
	public void drawCloud(Cloud c, Graphics g){
		Position current_pos = c.getPosition();
		g.drawImage((Image) objectMap.get(RedKnotAsset.CLOUD), current_pos.getX(), current_pos.getY(), c.getWidth(),c.getHeight(),null, this);
	}
	
	public void drawMap(Graphics g) {
		g.drawImage((Image) objectMap.get(RedKnotAsset.MAP), map.getPosition().getX(),map.getPosition().getY(),map.hitBox.width,map.hitBox.height,null);
	}
	
	/* collision comment: 
	 * 
	 * //Testing Collision for Clouds and RedKnot (Works -Miguel)
		if(Utility.GameObjectCollision(this.controller.getRedKnotGS().getRK(), c)) {
			System.out.println("COLLISION!");
		}
	 * 
	 * 
	 */
	
	//Moves the background 
	/* (non-Javadoc)
	 * @see game.GameView#scrollImage(java.awt.Graphics, java.lang.Object, java.lang.Object)
	 */
	public void scrollImage(Graphics g, Object background1, Object background2){
		background_x = (this.background_x % GameScreen.PLAY_SCREEN_WIDTH)+redKnot.getVelocity().getXSpeed();//BACKGROUND_SPEED;
		g.drawImage((Image) objectMap.get(background1), background_x*-1, 0, GameScreen.PLAY_SCREEN_WIDTH, GameScreen.PLAY_SCREEN_HEIGHT, null, this);
		g.drawImage((Image) objectMap.get(background2), (background_x*-1)+GameScreen.PLAY_SCREEN_WIDTH, 0, GameScreen.PLAY_SCREEN_WIDTH, GameScreen.PLAY_SCREEN_HEIGHT, null, null);
	}
    
	/* (non-Javadoc)
	 * @see game.GameView#fnameMapCreate()
	 */
	@Override
	public void fnameMapCreate() {
		fnameMap.put("background1.png", RedKnotAsset.BACKGROUND);
		fnameMap.put("forest2.png", RedKnotAsset.FOREST1);
		fnameMap.put("cloudnorain.png",RedKnotAsset.CLOUD);
		fnameMap.put("southamericabackground.jpeg", RedKnotAsset.SABACKGROUND);
		fnameMap.put("sprite-6-redknot.png", RedKnotAsset.MAINBIRD);
		fnameMap.put("sprite-6-flockbird.png", RedKnotAsset.FLOCKBIRD);
		fnameMap.put("NA_SA_MAP.png", RedKnotAsset.MAP);
	}



	/* (non-Javadoc)
	 * @see game.GameView#updateScore(int)
	 */
	@Override
	public void updateScore(int x) {
		this.score = x;
//		System.out.println("VIEW IS UPDATING SCORE");
//		System.out.println(this.score);
	}



	@Override
	public void drawEndGame() {
		// TODO Auto-generated method stub
		
	}
	
}
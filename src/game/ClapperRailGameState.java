package game;

import java.util.ArrayList;

/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

/*Class: ClapperRailGameState
 * -class that acts as the Model of the ClapperRail GameMode
 * -keeps track of the data for the ClapperRail mini-game
 */
public class ClapperRailGameState extends GameState {
	private Bird CR;
	private ArrayList<GameObject> Materials;
	
	public ClapperRailGameState(Controller controller){
		super(controller);
		this.CR = new Bird(100,380,10,0, 80, BirdType.CLAPPERRAIL);
		this.Materials = new ArrayList<>();
		
	}
	
	public void collectMaterial() {
		this.Materials.add(new GameObject(0,0));
	}
	
	public int countMaterials() {
		return this.Materials.size();
	}

	public Bird getCR() {
		return CR;
	}

	public ArrayList<GameObject> getMaterials() {
		return Materials;
	}
	
	
}

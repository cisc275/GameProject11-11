package game;
/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

import java.io.Serializable;

/*Class: Position
 * -class that allows us to keep track of each element's position (x,y).
 */
/**
 * @author MiguelZN
 *
 */
public class Position implements Serializable{
	private int x;
	private int y;
	
	/*Constructor:
	 * -takes in two integers: an x,y in order to provide where the element is located (x,y)
	 */

	/**@author Miguel
	 * @param x
	 * @param y
	 */
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	

	/**@author Miguel
	 * @param v
	 * @return Position
	 * -Generates (returns) a new position by adding the inputed velocity 
	 */
	public Position moveByVelocity(Velocity v) {
		return new Position(this.getX()+v.getXSpeed(), this.getY()+v.getYSpeed());
	}
	

	/**@author Miguel
	 * @param v
	 * -Increments the current position by the given velocity
	 */
	public void Shift(Velocity v) {
		this.x = this.getX()+v.getXSpeed();
		this.y = this.getY()+v.getYSpeed();
	}

	/*Getters, Setters------------------------*/
	/**
	 * @return
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/*toString*/
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Position:("+this.getX()+","+this.getY()+")";
	}
	
}

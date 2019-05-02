package game;
/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;

/*Class: TitleScreenView
 * -class that acts as the View of the TitleScreen 
 * -contains methods and control over the drawing of the TitleScreen
 */
public class TitleScreenView extends GameView{
	JButton RedKnot;
	JButton ClapperRail;
	JButton Instructions;
	
	
	
	
	
	Controller contr;
	
	/*TODO: fix later
	 * This isn't mvc but I'm leaving it for now.
	 */
	public TitleScreenView(Controller c) {
		super();
		contr=c;
		this.RedKnot = new JButton("RED KNOT");
		this.ClapperRail = new JButton("CLAPPER RAIL");
		this.Instructions = new JButton("INSTRUCTIONS");
		this.RedKnot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("button pressed");
				contr.changeView(GameMode.REDKNOT);
			}
		});
		this.ClapperRail.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contr.changeView(GameMode.CLAPPERRAIL);
			}
		});
		this.Instructions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				contr.changeView(GameMode.INSTRUCTIONS);
			}
		});
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.add(ClapperRail, BorderLayout.EAST);
		this.add(Instructions, BorderLayout.PAGE_START);
		this.add(RedKnot, BorderLayout.WEST);
	}

	@Override
	public void fnameMapCreate() {
		//Title currently does not have any images
		
	}
	
	public Object loadImage(File f) {
		return null;
	}


	@Override
	public void scrollImage(Graphics g, Object background1, Object background2) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void update(ArrayList<GameObject> gameObjects) {
		// TODO Auto-generated method stub
		
	}


	//maybe we need to split our views that aren't games from the gameview, but
	// if it is only for the purpose of this single method that they don't share, id be fine just leaving this
	// here since it changes nothing.
	@Override
	public void setScore(int x) {
		// TODO Auto-generated method stub
		
	}
}

package game;
/*Authors: Miguel Zavala, Derek Baum, Matt Benvenuto, Jake Wise
 * 
 */

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
	
	public TitleScreenView(Controller c) {
		super(c);
		this.RedKnot = new JButton("RED KNOT");
		this.RedKnot.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        c.changeView(GameMode.REDKNOT);
		    }
		});
		this.ClapperRail = new JButton("CLAPPER RAIL");
		this.ClapperRail.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        c.changeView(GameMode.CLAPPERRAIL);
		    }
		});
		this.Instructions = new JButton("INSTRUCTIONS");
		this.Instructions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				c.changeView(GameMode.INSTRUCTIONS);
			}
		});
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		this.add(RedKnot, BorderLayout.WEST);
		this.add(ClapperRail, BorderLayout.EAST);
		this.add(Instructions, BorderLayout.PAGE_START);
	}
	

	@Override
	public void fnameMapCreate() {
		//Title currently does not have any images
		
	}
	
	public Object loadImage(File f) {
		return null;
	}
}

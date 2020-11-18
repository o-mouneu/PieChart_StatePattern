package piechart;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

/**
 * A PercentagePieChart acts boths as a MVC View and Controller of a Percentage It maintains a reference to its model in order to
 * update it.
 *
 */
public class PercentagePieChart extends JComponent implements PercentageView {

	// Predefined cursors

	private static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	private static final Cursor CROSS = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	private static final Cursor ARROW = Cursor.getDefaultCursor();

	/**
	 * Hold a reference to the model
	 */
	private final PercentageModel myModel;


        private class AbstractState extends MouseInputAdapter {
        };

        // State Objects
        // 1) Pour chaque état on crée une classe interne anonyme qui étend State
        // 2) On surcharge la ou les méthodes qu'on veut traiter
        //            (ici mousePressed )
        // 3) On crée une instance de cette classe anonyme
        private AbstractState INIT = new AbstractState() {
            @Override
            public void mousePressed(MouseEvent e) {
            }
            public void mouseMoved(MouseEvent e) {
                //System.out.println("Mouse moved");
                /* 
                    Detect when curson is INPIN (zone) 
                */
                if (inPin(e)) {
                    setCursor(HAND);  // Change curson appearance
                    state = INPIN;
                }
            }
        };
        private AbstractState INPIN = new AbstractState() {
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseDragged(MouseEvent e) {
            }
            @Override 
            public void mousePressed(MouseEvent e) {
                state = ADJUSTING;
                setCursor(CROSS); // Change curson appearance
                repaint();  
            }
        };
        private AbstractState ADJUSTING = new AbstractState() {
            public void mouseReleased(MouseEvent e) {
                if (inPin(e)) {
                    state = INPIN;
                    setCursor(CROSS);
                } else { 
                    /* Out of zone
                        Return to INIT STATE
                    */
                    state = INIT;
                    setCursor(ARROW);
                }
                repaint();
            }
            public void mouseDragged(MouseEvent e) {
                myModel.setValue(pointToPercentage(e));
            }
        };  
        
        MouseInputAdapter eventHandler = new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            state.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            state.mouseReleased(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            state.mouseDragged(e);
        }
        @Override
        public void mouseMoved(MouseEvent e) {
            state.mouseMoved(e);
        }

        };

        private AbstractState state = INIT;

        
        
	public PercentagePieChart(PercentageModel model) {
		super();
		myModel = model;

		addMouseListener(eventHandler);
		addMouseMotionListener(eventHandler);
	}

	// "View" behaviour : when the percentage changes, the piechart must be repainted
	public void notify(PercentageModel model) {
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;
		int radius = Math.min(getWidth() - 4, getHeight() - 4) / 2;
		double angle = myModel.getValue() * 2 * Math.PI;
		g.fillOval(centerX - radius, centerY - radius, radius * 2,
			radius * 2);
		g.setColor(Color.yellow);
		g.fillArc(centerX - radius, centerY - radius, radius * 2,
			radius * 2, 0, (int) Math.toDegrees(angle));
		int pinX = centerX + (int) (Math.cos(angle) * radius);
		int pinY = centerY - (int) (Math.sin(angle) * radius);
		g.setColor(Color.gray.brighter());
		g.fill3DRect(pinX - 4, pinY - 4, 8, 8, state != ADJUSTING);
	}

	/**
	 * Test if a mouse event is inside the "Pin" that allows to change the percentage
	 */
	private boolean inPin(MouseEvent ev) {
		int mouseX = ev.getX();
		int mouseY = ev.getY();
		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;
		int radius = Math.min(getWidth() - 4, getHeight() - 4) / 2;
		double angle = myModel.getValue() * 2 * Math.PI;
		int pinX = centerX + (int) (Math.cos(angle) * radius);
		int pinY = centerY - (int) (Math.sin(angle) * radius);

		Rectangle r = new Rectangle();
		r.setBounds(pinX - 4, pinY - 4, 8, 8);
		return r.contains(mouseX, mouseY);
	}

	/**
	 * Converts a mouse position to a Percentage value
	 */
	private float pointToPercentage(MouseEvent e) {
		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;
		int mouseX = e.getX() - centerX;
		int mouseY = e.getY() - centerY;
		double l = Math.sqrt(mouseX * mouseX + mouseY * mouseY);
		double lx = mouseX / l;
		double ly = mouseY / l;
		double theta;
		if (lx > 0) {
			theta = Math.atan(ly / lx);
		} else if (lx < 0) {
			theta = -1 * Math.atan(ly / lx);
		} else {
			theta = 0;
		}

		if ((mouseX > 0) && (mouseY < 0)) {
			theta = -1 * theta;
		} else if (mouseX < 0) {
			theta += Math.PI;
		} else {
			theta = 2 * Math.PI - theta;
		}

		return (float) (theta / (2 * Math.PI));
	}
}

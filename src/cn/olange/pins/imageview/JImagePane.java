package cn.olange.pins.imageview;

import cn.olange.pins.utils.ImageUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JImagePane extends JPanel {

	private static final long serialVersionUID = 7088737542307810259L;

	public static final int ORIN_SIZE = 1;
	public static final int WIND_SIZE = 2;

	public static final float MAX_PERCENT = 1.5F;
	public static final float MIN_PERCENT = 0.02F;
	public static final float WHEEL_ZOOM_OFFSET = 0.02F;
	public static final float CLICK_ZOOM_OFFSET = 0.1F;
	private final JScrollBar v_bar;
	private final JScrollBar h_bar;

	private ImageIcon icon = null;
	private ImageIcon i_bak = null;
	private int theta = 0;
	private float percent = 1.0F;
	private Dimension orin_size = null;

	private int i_width, i_height;
	private int w_width, w_height;
	private Point point;
	private int x_val = -1, y_val = -1;
	private boolean wheel_lock = false;
	public static Object LOCK = new Object();

	public JImagePane(JScrollBar v_bar, JScrollBar h_bar) {
		this.v_bar = v_bar;
		this.h_bar = h_bar;
		DragEventListener listener = new DragEventListener();
		this.addMouseListener( listener );
		this.addMouseMotionListener(listener);
		this.addMouseWheelListener(listener);

	}

	/**
	 * picture drag handler inner class
	 */
	private class DragEventListener extends MouseAdapter
			implements MouseMotionListener,MouseWheelListener {
		@Override
		public void mousePressed(MouseEvent e) {
			if ( icon != null ) {
				point = e.getLocationOnScreen();
				y_val = v_bar.getValue();
				x_val = h_bar.getValue();
				setCursor( new Cursor( Cursor.MOVE_CURSOR ) );
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if ( icon != null ) {
				setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
			}
		}

		@Override
		public synchronized void mouseDragged(MouseEvent e) {
			if ( icon != null && point != null ) {
				Point p = e.getLocationOnScreen();
				int x_offset = p.x - point.x;
				int y_offset = p.y - point.y;

				//check the vertical direction.
				if ( y_offset > 0 ) {				//head down
					if ( v_bar.getValue() > v_bar.getMinimum() ) {
						int v = y_val - y_offset;
						if ( v < v_bar.getMinimum() )
							v = v_bar.getMinimum();
						v_bar.setValue(v);
					}
				} else {							//head up
					if ( v_bar.getValue() < v_bar.getMaximum() ) {
						int v = y_val - y_offset;
						if ( v > v_bar.getMaximum() )
							v = v_bar.getMaximum();
						v_bar.setValue(v);
					}
				}

				//check the horizontal direction
				if ( x_offset > 0 ) {				//head right
					if ( h_bar.getValue() > h_bar.getMinimum() ) {
						int v = x_val - x_offset;
						if ( v < h_bar.getMinimum() )
							v = h_bar.getMinimum();
						h_bar.setValue(v);
					}
				} else {							//head left
					if ( h_bar.getValue() < h_bar.getMaximum() ) {
						int v = x_val - x_offset;
						if ( v > h_bar.getMaximum() )
							v = h_bar.getMaximum();
						h_bar.setValue(v);
					}
				}
			}
		}

		@Override
		public synchronized void mouseWheelMoved( MouseWheelEvent e) {
			if ( wheel_lock == false && icon != null &&
					e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL
					/*&& ( e.isShiftDown() || e.isControlDown() )*/) {
				wheel_lock = true;
				if ( e.getWheelRotation() < 0 ) {
					UIUtil.invokeAndWaitIfNeeded(new Runnable(){
						@Override
						public void run() {
							larger( WHEEL_ZOOM_OFFSET );
							wheel_lock = false;
						}
					});
				} else {
					UIUtil.invokeAndWaitIfNeeded((Runnable) ()->{
						smaller( WHEEL_ZOOM_OFFSET );
						wheel_lock = false;
					});
				}

			}
		}

	}

	@Override
	public void paintComponent( Graphics g ) {
		Graphics2D g2 = ( Graphics2D ) g;
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());

		if ( i_bak != null ) {
			g2.translate(this.getWidth() / 2, this.getHeight() / 2);
			g2.rotate(Math.toRadians(theta));
			g.drawImage(i_bak.getImage(), - i_width / 2, - i_height / 2, i_width, i_height, null);
			JLoadDialog.getInstance().hiddenDialog();
		}
	}

	public void drawPicture( ImageIcon __src ) {
		if (orin_size != null) {
			resetWindSizeFlag(ORIN_SIZE);
		} else {
			orin_size = new Dimension(__src.getIconWidth(), __src.getIconHeight());
			resetWindSizeFlag(WIND_SIZE);
		}
		icon = __src;
		theta = 0;percent = 1.0F;
		re_count();
		selfRepaint();
	}

	public void rotateLeft() {
		if ( icon != null ) {
			JLoadDialog.getInstance().showDialog();
			synchronized ( LOCK ) {
				resetWindSizeFlag( ORIN_SIZE );
				percent = 1.0F;
				theta += -90;
				if ( theta == -360 ) theta = 0;
				re_count();
				selfRepaint();
			}
		}
	}

	public void rotateRight() {
		if ( icon != null ) {
			JLoadDialog.getInstance().showDialog();
			synchronized ( LOCK ) {
				resetWindSizeFlag( ORIN_SIZE );
				percent = 1.0F;
				theta += 90;
				if ( theta == 360 ) theta = 0;
				re_count();
				selfRepaint();
			}
		}
	}

	public void larger( float offset ) {
		if ( icon != null && percent < MAX_PERCENT ) {
			JLoadDialog.getInstance().showDialog();
			synchronized ( LOCK ) {
				percent += offset;
				if ( percent > MAX_PERCENT )
					percent = MAX_PERCENT;
				re_count();
				selfRepaint();
				centerScrollPane();
			}
		}
	}

	public void smaller( float offset ) {
		if ( icon != null && percent > MIN_PERCENT ) {
			JLoadDialog.getInstance().showDialog();
			synchronized ( LOCK ) {
				percent -= offset;
				if ( percent < MIN_PERCENT )
					percent = MIN_PERCENT;
				re_count();
				selfRepaint();
				centerScrollPane();
			}
		}
	}

	public void self() {
		if ( icon != null ) {
			JLoadDialog.getInstance().showDialog();
			synchronized ( LOCK ) {
				resetWindSizeFlag( ORIN_SIZE );
				theta = 0;
				percent = 1.0F;
				re_count();
				selfRepaint();
			}
		}
	}

	public void setPaneSize( Dimension size ) {
		if ( size != null ) {
			UIUtil.invokeAndWaitIfNeeded((Runnable) ()->{
				setPreferredSize( new Dimension( size.width - 25, size.height - 30 ) );
				setSize(size);
			});
		}
	}

	public void selfRepaint() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				repaint();
			}
		});
	}

	public void re_count() {
		if ( icon == null ) return;
		i_width = icon.getIconWidth();
		i_height = icon.getIconHeight();
		float wbit = ( float ) w_width / icon.getIconWidth();
		float hbit = ( float ) w_height / icon.getIconHeight();

		if ( theta < 0 ) theta += 360;
		if ( theta == 90 ||  theta == 270 ) {
			wbit = ( float ) w_width / icon.getIconHeight();
			hbit = ( float ) w_height / icon.getIconWidth();
			//width = icon.getIconHeight();
			//height = icon.getIconWidth();
		}

		if ( percent == 1.0F && ( wbit < 1 || hbit < 1 ) ) {
			percent = Math.min(wbit, hbit);
			orin_size = new Dimension(w_width, w_height);
		}
//		PView.getInstance().setPercent(percent);
		i_width = ( int ) ( i_width *  percent );
		i_height = ( int ) ( i_height * percent );

		//get the scaled image
		i_bak = new ImageIcon(ImageUtil.scaleImage(icon.getImage(),i_width,i_height));
		//update the size of image pane
		if ( i_width > this.getWidth() || i_height > this.getHeight() ) {
			this.setPaneSize(new Dimension(
					Math.max(w_width, i_width),
					Math.max(w_height, i_height)));
		} else {
			this.setPaneSize(orin_size);
		}


	}

	public void resetOrinSize( int w, int h ) {
		orin_size = new Dimension( w, h );
	}

	public void resetWindSizeFlag( int flag ) {
		if ( flag == ORIN_SIZE ) {
			w_width = orin_size.width;
			w_height = orin_size.height;
		} else if ( flag == WIND_SIZE ) {
			w_width = this.getWidth();
			w_height = this.getHeight();
		}
	}

	public void centerScrollPane() {
		v_bar.setValue(v_bar.getMaximum() / 4);
		h_bar.setValue(h_bar.getMaximum() / 4);
	}

}

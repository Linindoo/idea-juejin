package cn.olange.pins.imageview;

import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class JLoadDialog extends JDialog {
	public static final Dimension W_SIZE = new Dimension(150, 40);
	public static final Font W_FONT = new Font("Arial", Font.BOLD, 18);

	private static JLoadDialog __instance = null;

	private String str = "Loading...";

	public static JLoadDialog getInstance() {
		return __instance;
	}

	public static void init(DialogWrapper dialogWrapper) {
		__instance = new JLoadDialog(dialogWrapper);
	}

	private JLoadDialog(DialogWrapper dialogWrapper) {
		super(dialogWrapper.getWindow());
		setUndecorated(true);
		setPreferredSize(W_SIZE);
		setSize(W_SIZE);
	    setLocationRelativeTo(dialogWrapper.getContentPanel());
		setVisible(false);
		setOpacity(0.6f);
		setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
	}

	@Override
	public void paint( Graphics g ) {
		Graphics2D g2 = ( Graphics2D ) g;
		//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6F));
		g2.setColor(new Color(255, 0, 0));
		g2.fill3DRect(0, 0, this.getWidth(), this.getHeight(), true);
		g2.setColor(Color.WHITE);

		if ( str != null ) {
			g2.setFont(W_FONT);
			FontMetrics fm = this.getFontMetrics(W_FONT);
			g2.drawString(str,
					( this.getWidth() - fm.stringWidth(str) ) / 2,
					this.getHeight() / 2 + 5);
		}
	}

	public void hiddenDialog() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				setVisible(false);
			}
		});
	}

	public void showDialog() {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				setVisible(true);
			}
		});
	}

}

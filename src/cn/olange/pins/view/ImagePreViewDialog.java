package cn.olange.pins.view;

import cn.olange.pins.imageview.JImagePane;
import cn.olange.pins.imageview.JLoadDialog;
import com.google.gson.JsonArray;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonShortcuts;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.util.DimensionService;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.PopupBorder;
import com.intellij.ui.WindowMoveListener;
import com.intellij.ui.WindowResizeListener;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ImagePreViewDialog extends DialogWrapper implements Disposable {
	private static final String SERVICE_KEY = "image.preview.dialog";
	private JPanel main;
	private JScrollPane scroll;
	private JButton prebtn;
	private JPanel imageInfo;
	private JButton nextbtn;
	private JLabel zoom_in;
	private JLabel zoom_out;
	private JLabel zoom_reset;
	private JLabel turn_left;
	private JLabel turn_right;
	private JTextPane image_url_text;
	private JPanel toolbar;
	private JsonArray images;
	private int index;
	private JImagePane imagePane = null;
	private LoadingDecorator loadingDecorator ;

	JScrollBar v_bar = null;
	JScrollBar h_bar = null;

	protected ImagePreViewDialog(@Nullable Project project, JsonArray images, int index) {
		super(project,false, IdeModalityType.MODELESS);
		this.setTitle("图片预览");
		this.images = images;
		this.index = index;
		JLoadDialog.init(this);
		v_bar = scroll.getVerticalScrollBar();
		h_bar = scroll.getHorizontalScrollBar();
		imagePane = new JImagePane(v_bar, h_bar);
		imagePane.setPaneSize(new Dimension(700, 500));
		this.main.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension size = new Dimension(main.getWidth() - 8,
						main.getHeight() - toolbar.getHeight());
				scroll.setPreferredSize(size);
				scroll.setSize(size);
				imagePane.resetOrinSize( size.width, size.height );
			}
		});
		this.scroll.setViewportView(imagePane);
		this.init();
		this.setUndecorated(true);
		Disposer.register(project, this::closeImmediately);
		final Window w = this.getPeer().getWindow();
		AnAction escape = ActionManager.getInstance().getAction("EditorEscape");
		JRootPane root = ((RootPaneContainer) w).getRootPane();
		DumbAwareAction.create((e) -> {
			ImagePreViewDialog.this.closeImmediately();
		}).registerCustomShortcutSet(escape == null ? CommonShortcuts.ESCAPE : escape.getShortcutSet(), root, this.myDisposable);
		w.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				w.addWindowFocusListener(new WindowAdapter() {
					public void windowLostFocus(WindowEvent e) {
						Window oppositeWindow = e.getOppositeWindow();
						if (oppositeWindow != w && (oppositeWindow == null || oppositeWindow.getOwner() != w)) {
							if (oppositeWindow != null) {
								ImagePreViewDialog.this.doCancelAction();
							}
						}
					}
				});
			}
		});
		root.setWindowDecorationStyle(0);
		if (SystemInfo.isMac && UIUtil.isUnderDarcula()) {
			root.setBorder(PopupBorder.Factory.createColored(OnePixelDivider.BACKGROUND));
		} else {
			root.setBorder(PopupBorder.Factory.create(true, true));
		}
		Window window = WindowManager.getInstance().suggestParentWindow(project);
		Component parent = UIUtil.findUltimateParent(window);
		RelativePoint showPoint = null;
		Point screenPoint = DimensionService.getInstance().getLocation(SERVICE_KEY);
		if (screenPoint != null) {
			if (parent != null) {
				SwingUtilities.convertPointFromScreen(screenPoint, parent);
				showPoint = new RelativePoint(parent, screenPoint);
			} else {
				showPoint = new RelativePoint(screenPoint);
			}
		}

		if (parent != null && showPoint == null) {
			int height = UISettings.getInstance().getShowNavigationBar() ? 135 : 115;
			if (parent instanceof IdeFrameImpl && ((IdeFrameImpl) parent).isInFullScreen()) {
				height -= 20;
			}

			showPoint = new RelativePoint(parent, new Point((parent.getSize().width - this.getPreferredSize().width) / 2, height));
		}

		WindowMoveListener windowListener = new WindowMoveListener(main);
		main.addMouseListener(windowListener);
		main.addMouseMotionListener(windowListener);
		Dimension panelSize = this.getPreferredSize();
		Dimension prev = DimensionService.getInstance().getSize(SERVICE_KEY);

		panelSize.width += JBUI.scale(24);
		if (prev != null && prev.height < panelSize.height) {
			prev.height = panelSize.height;
		}

		final IdeGlassPane glass = (IdeGlassPane) this.getRootPane().getGlassPane();
		int i = Registry.intValue("ide.popup.resizable.border.sensitivity", 4);
		WindowResizeListener resizeListener = new WindowResizeListener(root, JBUI.insets(i), (Icon) null) {
			private Cursor myCursor;

			protected void setCursor(Component content, Cursor cursor) {
				if (this.myCursor != cursor || this.myCursor != Cursor.getDefaultCursor()) {
					glass.setCursor(cursor, this);
					this.myCursor = cursor;
					if (content instanceof JComponent) {
						JComponent component = (JComponent) content;
						if (component.getClientProperty("SuperCursor") == null) {
							component.putClientProperty("SuperCursor", cursor);
						}
					}
					super.setCursor(content, cursor);
				}

			}
		};
		glass.addMousePreprocessor(resizeListener, this.myDisposable);
		glass.addMouseMotionPreprocessor(resizeListener, this.myDisposable);
		root.setWindowDecorationStyle(0);
		if (SystemInfo.isMac && UIUtil.isUnderDarcula()) {
			root.setBorder(PopupBorder.Factory.createColored(OnePixelDivider.BACKGROUND));
		} else {
			root.setBorder(PopupBorder.Factory.create(true, true));
		}

		w.setBackground(UIUtil.getPanelBackground());
		w.setMinimumSize(panelSize);
		if (prev == null) {
			panelSize.height = (int) ((double) panelSize.height * 0.75D);
			panelSize.width = (int) ((double) panelSize.width * 0.75D);
		}

		w.setSize(prev != null ? prev : panelSize);
		IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
		if (showPoint != null) {
			this.setLocation(showPoint.getScreenPoint());
		} else {
			w.setLocationRelativeTo(parent);
		}
		this.initData();
		String imageUrl = this.images.get(index).getAsString();
		previewImage(imageUrl);
	}

	private void closeImmediately() {
		if (this.isVisible()) {
			this.doCancelAction();
		}
	}

	@NotNull
	@Override
	protected Action[] createActions() {
		return new Action[]{};
	}

	private void previewImage(String imageUrl) {
		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			try {
				URL url = new URL(imageUrl);
				ImageIcon icon = new ImageIcon(url);
				UIUtil.invokeLaterIfNeeded(()->{
					imagePane.drawPicture(icon);
					image_url_text.setText(imageUrl + " \n " + icon.getIconWidth() + " × " + icon.getIconHeight() + " 像素   ");
					validateBtn();
				});
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});
	}
	private void initData() {
		this.prebtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				index--;
				String imageUrl = images.get(index).getAsString();
				previewImage(imageUrl);
			}
		});
		this.nextbtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				index++;
				String imageUrl = images.get(index).getAsString();
				previewImage(imageUrl);
			}
		});
		this.zoom_in.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
					@Override
					public void run() {
						imagePane.larger(JImagePane.CLICK_ZOOM_OFFSET);
					}
				});
			}
		});
		this.zoom_out.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
					@Override
					public void run() {
						imagePane.smaller(JImagePane.CLICK_ZOOM_OFFSET);
					}
				});
			}
		});
		this.zoom_reset.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
					@Override
					public void run() {
						imagePane.self();
					}
				});
			}
		});
		turn_left.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
					@Override
					public void run() {
						imagePane.rotateLeft();
					}
				});
			}
		});
		turn_right.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
					@Override
					public void run() {
						imagePane.rotateRight();
					}
				});
			}
		});
	}


	private void validateBtn() {
		if (this.index == 0) {
			this.prebtn.setEnabled(false);
		} else {
			this.prebtn.setEnabled(true);
		}
		if (this.index == this.images.size() - 1) {
			this.nextbtn.setEnabled(false);
		} else {
			this.nextbtn.setEnabled(true);
		}
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return main;
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}

package cn.olange.pins.view;

import cn.olange.pins.event.ImageClickEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.Alarm;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.EmptyIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class PinContentDialog extends JBPanel<PinContentDialog> {
	private static final KeyStroke ENTER = KeyStroke.getKeyStroke(10, 0);
	private static final KeyStroke ENTER_WITH_MODIFIERS = KeyStroke.getKeyStroke(10, SystemInfo.isMac ? 256 : 128);
	private final Disposable myDisposable;
	private Project project;
	private JsonObject pinInfo;
	private DialogWrapper myDialog;
	private JBLabel myTitleLabel;
	private JPanel myTitlePanel;
	private LoadingDecorator myLoadingDecorator;
	private final AtomicBoolean myCanClose;
	private final AtomicBoolean myIsPinned;
	public static final String SERVICE_KEY = "juejin.pin";
	private boolean showImageDialog = false;


	public PinContentDialog(Project project, JsonObject pinInfo) {
		super();
		this.project = project;
		this.pinInfo = pinInfo;
		this.myDisposable = Disposer.newDisposable();
		this.myCanClose = new AtomicBoolean(true);
		this.myIsPinned = new AtomicBoolean(false);
		initComponents();
	}

	private void initComponents() {
		this.setLayout(new MigLayout("flowx, ins 4, gap 0, fillx, hidemode 3"));
		this.myTitleLabel = new JBLabel("沸点详情", UIUtil.ComponentStyle.REGULAR);
		this.myTitleLabel.setFont(this.myTitleLabel.getFont().deriveFont(1));
		this.myLoadingDecorator = new LoadingDecorator(new JLabel(EmptyIcon.ICON_16), this.getDisposable(), 250, true, new AsyncProcessIcon("FindInPathLoading"));
		this.myLoadingDecorator.setLoadingText("");
		this.myTitlePanel = new JPanel(new MigLayout("flowx, ins 0, gap 0, fillx, filly"));
		this.myTitlePanel.add(this.myTitleLabel);
		this.myTitlePanel.add(this.myLoadingDecorator.getComponent(), "w 24, wmin 24");
		this.myTitlePanel.add(Box.createHorizontalGlue(), "growx, pushx");
		this.add(this.myTitlePanel, "wrap");
		ImageClickEvent imageClickEvent = new ImageClickEvent() {
			@Override
			public void viewImage(JsonArray pictures, int index) {
				ImagePreViewDialog imagePreViewDialog = new ImagePreViewDialog(project, pictures, index);
				showImageDialog = true;
				Disposer.register(imagePreViewDialog.getDisposable(),()->{
					showImageDialog = false;
					PinContentDialog.this.myDialog.getContentPanel().requestFocus(true);
				});
				imagePreViewDialog.show();
			}
		};
		PinContentInfoPanel pinContentInfoPanel = new PinContentInfoPanel(this.pinInfo, project, imageClickEvent);
		this.add(pinContentInfoPanel, "pushx, growx, sx 10, gaptop 4, wrap");
		JTextField textField = new JTextField();
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(textField, BorderLayout.CENTER);
		bottomPanel.add(new JButton("评论"), BorderLayout.EAST);
		this.add(bottomPanel, "pushx, growx, sx 10, gaptop 4, wrap");
		CommentList commentList = new CommentList(project, pinInfo.get("msg_id").getAsString());
		this.add(commentList, "pushx, growx, growy, pushy, sx 10, wrap, pad 4 4 4 4");
//		bottomPanel.add(myOKButton, BorderLayout.EAST);
	}


	public void showUI() {
		if (this.myDialog == null || !this.myDialog.isVisible()) {
			if (this.myDialog != null && !Disposer.isDisposed(this.myDialog.getDisposable())) {
				this.myDialog.doCancelAction();
			}

			if (this.myDialog == null || Disposer.isDisposed(this.myDialog.getDisposable())) {
				myDialog = new DialogWrapper(this.project, true, DialogWrapper.IdeModalityType.MODELESS) {
					{
						init();
						getContentPane().add(new JLabel(), BorderLayout.SOUTH);//remove hardcoded southSection
						getRootPane().setDefaultButton(null);
					}


					@Override
					protected void dispose() {
						super.dispose();
					}

					@NotNull
					@Override
					protected Action[] createLeftSideActions() {
						return new Action[0];
					}

					@NotNull
					@Override
					protected Action[] createActions() {
						return new Action[0];
					}

					@Nullable
					@Override
					protected Border createContentPaneBorder() {
						return null;
					}

					@Override
					protected JComponent createCenterPanel() {
						return PinContentDialog.this;
					}

					@Override
					protected String getDimensionServiceKey() {
						return SERVICE_KEY;
					}
				};
				this.myDialog.setUndecorated(true);
				Disposer.register(project, PinContentDialog.this::closeImmediately);
				Disposer.register(this.myDialog.getDisposable(), this.myDisposable);
				Window window = WindowManager.getInstance().suggestParentWindow(this.project);
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

				WindowMoveListener windowListener = new WindowMoveListener(this);
				this.addMouseListener(windowListener);
				this.addMouseMotionListener(windowListener);
				Dimension panelSize = this.getPreferredSize();
				Dimension prev = DimensionService.getInstance().getSize(SERVICE_KEY);

				panelSize.width += JBUI.scale(24);
				panelSize.height *= 2;
				if (prev != null && prev.height < panelSize.height) {
					prev.height = panelSize.height;
				}

				final Window w = this.myDialog.getPeer().getWindow();
				AnAction escape = ActionManager.getInstance().getAction("EditorEscape");
				JRootPane root = ((RootPaneContainer) w).getRootPane();
				final IdeGlassPane glass = (IdeGlassPane) this.myDialog.getRootPane().getGlassPane();
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
				DumbAwareAction.create((e) -> {
					PinContentDialog.this.closeImmediately();
				}).registerCustomShortcutSet(escape == null ? CommonShortcuts.ESCAPE : escape.getShortcutSet(), root, this.myDisposable);
				root.setWindowDecorationStyle(0);
				if (SystemInfo.isMac && UIUtil.isUnderDarcula()) {
					root.setBorder(PopupBorder.Factory.createColored(OnePixelDivider.BACKGROUND));
				} else {
					root.setBorder(PopupBorder.Factory.create(true, true));
				}

				w.setBackground(UIUtil.getPanelBackground());
				w.setMinimumSize(panelSize);
				if (prev == null) {
					panelSize.height = (int) ((double) panelSize.height * 1.5D);
					panelSize.width = (int) ((double) panelSize.width * 1.15D);
				}

				w.setSize(prev != null ? prev : panelSize);
				IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
				if (showPoint != null) {
					this.myDialog.setLocation(showPoint.getScreenPoint());
				} else {
					w.setLocationRelativeTo(parent);
				}

				this.myDialog.show();
				w.addWindowListener(new WindowAdapter() {
					public void windowOpened(WindowEvent e) {
						w.addWindowFocusListener(new WindowAdapter() {
							public void windowLostFocus(WindowEvent e) {
								Window oppositeWindow = e.getOppositeWindow();
								if (oppositeWindow != w && (oppositeWindow == null || oppositeWindow.getOwner() != w)) {
									if (PinContentDialog.this.canBeClosed()) {
										PinContentDialog.this.myDialog.doCancelAction();
									} else if (!PinContentDialog.this.myIsPinned.get() && oppositeWindow != null) {
										if (!showImageDialog) {
											PinContentDialog.this.myDialog.doCancelAction();
										}
									}

								}
							}
						});
					}
				});
			}
		}
	}

	@NotNull
	public Disposable getDisposable() {
		return this.myDisposable;
	}

	private void closeImmediately() {
		if (this.canBeClosedImmediately() && this.myDialog != null && this.myDialog.isVisible()) {
			this.myIsPinned.set(false);
			this.myDialog.doCancelAction();
		}

	}

	private String convertRule(String rule) {
		if (rule == null) {
			return "";
		}
		return rule;
	}

	private boolean canBeClosedImmediately() {
		boolean state = this.myIsPinned.get();
		this.myIsPinned.set(false);

		boolean var2;
		try {
			var2 = this.myDialog != null && this.canBeClosed();
		} finally {
			this.myIsPinned.set(state);
		}

		return var2;
	}

	protected boolean canBeClosed() {
		if (this.project.isDisposed()) {
			return true;
		} else if (!this.myCanClose.get()) {
			return false;
		} else if (this.myIsPinned.get()) {
			return false;
		} else if (!ApplicationManager.getApplication().isActive()) {
			return false;
		} else if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow() == null) {
			return false;
		} else if (showImageDialog) {
			return false;
		} else {
			return true;
		}
	}

}

package cn.olange.pins.view;

import com.google.gson.JsonObject;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.OnePixelDivider;
import com.intellij.openapi.util.DimensionService;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PinDetailDialog extends DialogWrapper implements Disposable {
	private static final String SERVICE_KEY = "juejin.pin.detail";
	public static final String SPLITTER_SERVICE_KEY = "juejin.comment.splitter";
	JsonObject pinItem;
	private JPanel mainContent;

	protected PinDetailDialog(JsonObject pinItem, Project project) {
		super(project,null,true, IdeModalityType.MODELESS, false);
		this.pinItem = pinItem;
		String pinID = pinItem.get("msg_id").getAsString();
		PinContentInfoPanel pinContentInfoPanel = new PinContentInfoPanel(this.pinItem, project, null);
		this.setCancelButtonText("关闭");
		this.init();
		this.getRootPane().setDefaultButton((JButton)null);
		this.setUndecorated(!Registry.is("ide.find.as.popup.decorated"));
		ApplicationManager.getApplication().getMessageBus().connect(this.getDisposable()).subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
			public void projectClosed(@NotNull Project project) {
				PinDetailDialog.this.doCancelAction();
			}
		});

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

		JBSplitter splitter = new JBSplitter(true, 0.33F);
		splitter.setSplitterProportionKey(SPLITTER_SERVICE_KEY);
		splitter.setDividerWidth(1);
		splitter.getDivider().setBackground(OnePixelDivider.BACKGROUND);
		splitter.setFirstComponent(pinContentInfoPanel);
		CommentList commentList = new CommentList(project, pinID) {
			public Dimension getPreferredSize() {
				return new Dimension(pinContentInfoPanel.getWidth(), this.getHeight());
			}
		};
		splitter.setSecondComponent(commentList);

		this.mainContent.setLayout(new BorderLayout());
		this.mainContent.add(splitter, BorderLayout.CENTER);
		final Window w = this.getPeer().getWindow();
		w.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				w.addWindowFocusListener(new WindowAdapter() {
					public void windowLostFocus(WindowEvent e) {
						Window oppositeWindow = e.getOppositeWindow();
						if (oppositeWindow != w && (oppositeWindow == null || oppositeWindow.getOwner() != w)) {
							if ( oppositeWindow != null) {
								PinDetailDialog.this.doCancelAction();
							}

						}
					}
				});
			}
		});
	}


	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return mainContent;
	}

	@NotNull
	@Override
	protected Action[] createActions() {
		return new Action[]{this.getCancelAction()};
	}


	@Nullable
	protected Border createContentPaneBorder() {
		return null;
	}
	protected String getDimensionServiceKey() {
		return SERVICE_KEY;
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}

package cn.olange.pins.view;

import cn.olange.pins.action.RefreshPinsAction;
import cn.olange.pins.model.PinListCellRender;
import cn.olange.pins.service.PinsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PinsToolWindowPanel extends JPanel implements Disposable{
	public JPanel pins;
	private JScrollPane scrollPanel;
	private JPanel viewport;
	private JPanel toolbar;
	private JButton loadmorebtn;
	private JList pinList;
	private Project project;
	private String topic;
	private JsonArray datas = new JsonArray();
	private LoadingDecorator loadingDecorator;
	private PinsService instance;
	private int pageSize = 20;
	private String endCursor = "";
	private String datafield = "recommendedActivityFeed";


	public PinsToolWindowPanel(Project project, String topic, String type) {
		this.project = project;
		this.topic = topic;
		if ("hot".equalsIgnoreCase(type)) {
			datafield = "popularPinList";
		}
		DefaultActionGroup actionGroup = new DefaultActionGroup("PinsGroup", false);
		RefreshPinsAction refreshPinsAction = new RefreshPinsAction(x -> {
			refresh();
		});
		actionGroup.add(refreshPinsAction);
		JComponent actionToolbar = ActionManager.getInstance().createActionToolbar("pinsToolBar", actionGroup, true).getComponent();
		toolbar.add(actionToolbar, new FlowLayout());
		loadingDecorator = new LoadingDecorator(pins, this, 0);
		loadingDecorator.setLoadingText("加载中");
		scrollPanel.getVerticalScrollBar().setUnitIncrement(100);
		this.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		this.add(loadingDecorator.getComponent(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		instance = PinsService.getInstance(project);
		this.loadmorebtn.setVisible(false);
		loadingDecorator.startLoading(true);
		this.pinList.setCellRenderer(new PinListCellRender(project));
		this.pinList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JsonObject jsonObject = datas.get(pinList.getSelectedIndex()).getAsJsonObject();
				PinDetailDialog pinDetailDialog = new PinDetailDialog(jsonObject, project);
				pinDetailDialog.show();
			}
		});
		instance.getPageInfo(topic, this.endCursor, pageSize, result -> {
			if (result.isSuccess()) {
				JsonObject pinsData = ((JsonObject) result.getResult()).getAsJsonObject("data");
				JsonObject items = pinsData.getAsJsonObject(datafield)
						.getAsJsonObject("items");
				JsonObject pageInfo = items.getAsJsonObject("pageInfo");
				saveEndCorse(pageInfo);
				JsonArray edges = items.getAsJsonArray("edges");
				datas = edges;
				refeshData();
			} else {
				UIUtil.invokeLaterIfNeeded(() -> showNotification(pinList, MessageType.ERROR, "获取数据失败，请稍后重试", Balloon.Position.atRight));
			}
			loadingDecorator.stopLoading();
		});
		this.loadmorebtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				loadingDecorator.startLoading(true);
				instance.getPageInfo(topic, endCursor, pageSize, result -> {
					if (result.isSuccess()) {
						JsonObject data = (JsonObject) result.getResult();
						JsonObject items = data.getAsJsonObject("data").getAsJsonObject(datafield)
								.getAsJsonObject("items");
						JsonObject pageInfo = items.getAsJsonObject("pageInfo");
						saveEndCorse(pageInfo);
						JsonArray edges = items.getAsJsonArray("edges");
						datas.addAll(edges);
						refeshData();
					} else {
						UIUtil.invokeLaterIfNeeded(() -> showNotification(pinList, MessageType.ERROR, "获取数据失败，请稍后重试", Balloon.Position.atRight));
					}
					loadingDecorator.stopLoading();
				});
			}
		});
	}

	public static void showNotification(final JComponent component, final MessageType info, final String message, final Balloon.Position position) {
		UIUtil.invokeLaterIfNeeded(() -> JBPopupFactory.getInstance().createBalloonBuilder(new JLabel(message))
				.setFillColor(info.getPopupBackground())
				.createBalloon()
				.show(new RelativePoint(component, new Point(0, 0)), position));
	}

	private void refeshData() {
		this.pinList.setModel(new AbstractListModel() {
			@Override
			public int getSize() {
				return datas.size();
			}

			@Override
			public Object getElementAt(int index) {
				return datas.get(index);
			}
		});
	}

	@Override
	public void dispose() {

	}

	private void refresh() {
		loadingDecorator.startLoading(true);
		this.datas = new JsonArray();
		this.endCursor = "";
		instance.getPageInfo(this.topic, endCursor, pageSize, result -> {
			if (result.isSuccess()) {
				JsonObject data = (JsonObject) result.getResult();
				JsonObject items = data.getAsJsonObject("data").getAsJsonObject(datafield)
						.getAsJsonObject("items");
				JsonObject pageInfo = items.getAsJsonObject("pageInfo");
				saveEndCorse(pageInfo);
				JsonArray edges = items.getAsJsonArray("edges");
				datas = edges;
				refeshData();
			} else {
				UIUtil.invokeLaterIfNeeded(() -> showNotification(pinList, MessageType.ERROR, "获取数据失败，请稍后重试", Balloon.Position.atRight));
			}
			UIUtil.invokeLaterIfNeeded(() -> loadingDecorator.stopLoading());
		});
	}

	private void saveEndCorse(JsonObject pageInfo) {
		this.endCursor = pageInfo.get("endCursor").getAsString();
		boolean hasNextPage = pageInfo.get("hasNextPage").getAsBoolean();
		if (!hasNextPage) {
			this.loadmorebtn.setVisible(false);
		} else {
			this.loadmorebtn.setVisible(true);
		}
	}
}

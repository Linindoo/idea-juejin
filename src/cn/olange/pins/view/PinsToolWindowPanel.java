package cn.olange.pins.view;

import cn.olange.pins.action.ConfigAction;
import cn.olange.pins.action.RefreshPinsAction;
import cn.olange.pins.action.UserActionGroup;
import cn.olange.pins.model.Config;
import cn.olange.pins.model.NeedMore;
import cn.olange.pins.model.TableCellRender;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.DataKeys;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class PinsToolWindowPanel extends SimpleToolWindowPanel implements Disposable, DataProvider {
	public JPanel pins;
	private JPanel toolbar;
	private JPanel contentPanel;
	private JBTable pinTable;
	private Project project;
	private String topic;
	private JsonArray datas = new JsonArray();
	private PinsService instance;
	private int pageSize = 40;
	private int pageIndex = 0;
	private Map<Integer, String> pageMap = new HashMap<>();
	private String endCursor = "0";
	private Alarm mySearchRescheduleOnCancellationsAlarm;
	private PinContentDialog pinDetailDialog;

	public PinsToolWindowPanel(Project project, String topic) {
		super(Boolean.TRUE, Boolean.TRUE);
		this.project = project;
		this.topic = topic;
		DefaultActionGroup actionGroup = new DefaultActionGroup("PinsGroup", false);
		ActionManager actionManager = ActionManager.getInstance();
		actionGroup.add(actionManager.getAction("juejin.RefreshAction"));
		actionGroup.addSeparator();
		actionGroup.add(new UserActionGroup());
		actionGroup.add(actionManager.getAction("juejin.Setting"));
		actionGroup.add(actionManager.getAction("juejin.HelpAction"));
		actionGroup.addSeparator();
		ActionGroup catalogGroup = (ActionGroup) actionManager.getAction("Juejuin.Category");
		actionGroup.add(catalogGroup);
		ActionToolbar actionToolbar = actionManager.createActionToolbar("pinsToolBar", actionGroup, true);
		actionToolbar.setTargetComponent(this.pinTable);
		toolbar.add(actionToolbar.getComponent(), BorderLayout.WEST);
		this.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		this.add(pins, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		this.instance = PinsService.getInstance(project);
		this.pinTable = new JBTable() {
			public Dimension getPreferredScrollableViewportSize() {
				return new Dimension(this.getWidth(), 1 + this.getRowHeight() * 4);
			}
		};
		mySearchRescheduleOnCancellationsAlarm = new Alarm();
		this.pinTable.setFocusable(true);
		this.pinTable.getEmptyText().setShowAboveCenter(false);
		this.pinTable.setShowColumns(false);
		this.pinTable.setShowGrid(false);
		this.pinTable.setIntercellSpacing(JBUI.emptySize());
		this.pinTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.pinTable.getEmptyText().clear().setText("");
		(new DoubleClickListener() {
			protected boolean onDoubleClick(MouseEvent event) {
				if (event.getSource() != PinsToolWindowPanel.this.pinTable) {
					return false;
				} else {
					int selectedRow = pinTable.getSelectedRow();
					if (selectedRow < 0) {
						return false;
					}
					Object myData = pinTable.getModel().getValueAt(selectedRow, 0);
					if (myData instanceof JsonObject) {
						JsonObject jsonObject = (JsonObject) myData;
						pinDetailDialog = new PinContentDialog(project, jsonObject);
						pinDetailDialog.showUI();
					} else if (myData instanceof NeedMore) {
						scheduleResultsUpdate();
					}
					return true;
				}
			}
		}).installOn(this.pinTable);
		this.pinTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				PinsToolWindowPanel.this.pinTable.transferFocus();
			}
		});
		JBScrollPane scrollPane = new JBScrollPane(this.pinTable) {
			public Dimension getMinimumSize() {
				Dimension size = super.getMinimumSize();
				size.height = PinsToolWindowPanel.this.pinTable.getPreferredScrollableViewportSize().height;
				return size;
			}
		};
		this.contentPanel.add(scrollPane, BorderLayout.CENTER);
		this.endCursor = "0";
		this.scheduleResultsUpdate();
	}



	@Override
	public void dispose() {
		this.mySearchRescheduleOnCancellationsAlarm.dispose();
	}

	public void scheduleResultsUpdate() {
		if (this.mySearchRescheduleOnCancellationsAlarm != null && !this.mySearchRescheduleOnCancellationsAlarm.isDisposed()) {
			this.mySearchRescheduleOnCancellationsAlarm.cancelAllRequests();
			this.mySearchRescheduleOnCancellationsAlarm.addRequest(this::findSettingsChanged, 100);
		}
	}

	private void findSettingsChanged() {
		if (this.isShowing()) {
			ScrollingUtil.ensureSelectionExists(this.pinTable);
		}
		final ModalityState state = ModalityState.current();
		this.mySearchRescheduleOnCancellationsAlarm.cancelAllRequests();
		final DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.addColumn("Usages");
		this.pinTable.setModel(model);
		this.pinTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRender());
		ApplicationManager.getApplication().invokeLater(() -> {
			pinTable.setPaintBusy(true);
			Config config = JuejinPersistentConfig.getInstance().getConfig();
			instance.getPageInfo(config.getCurentCatalog(), endCursor, pageSize, config.isLogined() ? config.getCookieValue() : "", result -> {
				if (result.isSuccess()) {
					SwingUtilities.invokeLater(() -> {
						JsonObject resultObj = (JsonObject) result.getResult();
						boolean hasNextPage = resultObj.get("has_more").getAsBoolean();
						if ("0".equals(endCursor)) {
							datas = resultObj.getAsJsonArray("data");
						} else {
							datas.addAll(resultObj.getAsJsonArray("data"));
						}
						endCursor = resultObj.get("cursor").getAsString();
						pageMap.put(pageIndex, endCursor);
						for (int i = 0; i < datas.size(); i++) {
							JsonObject rule = datas.get(i).getAsJsonObject();
							model.insertRow(i, new Object[]{rule});
						}
						if (hasNextPage) {
							model.insertRow(datas.size(), new Object[]{new NeedMore()});
						}
						pinTable.getEmptyText().setText("");
					});
				} else {
					UIUtil.invokeLaterIfNeeded(() -> {
						pinTable.getEmptyText().setText("获取数据失败，请稍后刷新重试");
					});
				}
				pinTable.setPaintBusy(false);
			});
		}, state);
	}
	public void refrshPage() {
		this.endCursor = "0";
		scheduleResultsUpdate();
	}

	@Nullable
	@Override
	public Object getData(@NotNull String dataId) {
		if (DataKeys.JUEJIN_PROJECTS_PINPANEL.is(dataId)) {
			return this;
		} else if (DataKeys.JUEJIN_PINCONTENTDETAIL_DAILOG.is(dataId)) {
			return pinDetailDialog;
		}
		return null;
	}
}

package cn.olange.pins.view;

import cn.olange.pins.action.*;
import cn.olange.pins.model.Config;
import cn.olange.pins.model.NeedMore;
import cn.olange.pins.model.PageOperation;
import cn.olange.pins.model.TableCellRender;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.DataKeys;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.find.actions.ShowUsagesAction;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ReadTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StatusText;
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
import java.util.concurrent.atomic.AtomicInteger;

public class PinsToolWindowPanel extends SimpleToolWindowPanel implements Disposable, PageOperation, DataProvider {
	public JPanel pins;
	private JPanel toolbar;
	private JPanel contentPanel;
	private JBTable pinTable;
	//	private JList pinList;
	private Project project;
	private String topic;
	private JsonArray datas = new JsonArray();
	private LoadingDecorator loadingDecorator;
	private PinsService instance;
	private int pageSize = 40;
	private int pageIndex = 0;
	private Map<Integer, String> pageMap = new HashMap<>();
	private String endCursor = "0";
	private Alarm mySearchRescheduleOnCancellationsAlarm;
	private volatile ProgressIndicatorBase myResultsPreviewSearchProgress;

	public PinsToolWindowPanel(Project project, String topic) {
		super(Boolean.TRUE, Boolean.TRUE);
		this.project = project;
		this.topic = topic;
		DefaultActionGroup actionGroup = new DefaultActionGroup("PinsGroup", false);
		actionGroup.add(new RefreshPinsAction(this));
		actionGroup.addSeparator();
		actionGroup.add(new UserActionGroup());
		actionGroup.add(new ConfigAction());
		actionGroup.addSeparator();
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup catalogGroup = (ActionGroup) actionManager.getAction("Juejuin.Category");
		actionGroup.add(catalogGroup);
		ActionToolbar actionToolbar = actionManager.createActionToolbar("pinsToolBar", actionGroup, true);
		actionToolbar.setTargetComponent(this.pinTable);
		toolbar.add(actionToolbar.getComponent(), BorderLayout.WEST);

		loadingDecorator = new LoadingDecorator(pins, this, 0);
		loadingDecorator.setLoadingText("加载中");
		this.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		this.add(loadingDecorator.getComponent(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		this.instance = PinsService.getInstance(project);
		loadingDecorator.startLoading(true);
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
						PinContentDialog pinDetailDialog = new PinContentDialog(project, jsonObject);
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

	public static void showNotification(final JComponent component, final MessageType info, final String message, final Balloon.Position position) {
		UIUtil.invokeLaterIfNeeded(() -> JBPopupFactory.getInstance().createBalloonBuilder(new JLabel(message))
				.setFillColor(info.getPopupBackground())
				.createBalloon()
				.show(new RelativePoint(component, new Point(0, 0)), position));
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

	private void finishPreviousPreviewSearch() {
		if (this.myResultsPreviewSearchProgress != null && !this.myResultsPreviewSearchProgress.isCanceled()) {
			this.myResultsPreviewSearchProgress.cancel();
		}

	}

	private void findSettingsChanged() {
		if (this.isShowing()) {
			ScrollingUtil.ensureSelectionExists(this.pinTable);
		}
		final ModalityState state = ModalityState.current();
		this.finishPreviousPreviewSearch();
		this.mySearchRescheduleOnCancellationsAlarm.cancelAllRequests();
		final ProgressIndicatorBase progressIndicatorWhenSearchStarted = new ProgressIndicatorBase() {
			public void stop() {
				super.stop();
				loadingDecorator.stopLoading();
			}
		};
		this.myResultsPreviewSearchProgress = progressIndicatorWhenSearchStarted;
		final DefaultTableModel model = new DefaultTableModel() {
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		model.addColumn("Usages");
		this.pinTable.setModel(model);
		this.pinTable.getColumnModel().getColumn(0).setCellRenderer(new TableCellRender());
		final AtomicInteger resultsCount = new AtomicInteger();
		loadingDecorator.startLoading(false);
		ProgressIndicatorUtils.scheduleWithWriteActionPriority(this.myResultsPreviewSearchProgress, new ReadTask() {
			public ReadTask.Continuation performInReadAction(@NotNull ProgressIndicator indicator) {
				if (this.isCancelled()) {
					loadingDecorator.stopLoading();
				} else {
					ApplicationManager.getApplication().invokeLater(() -> {
						if (this.isCancelled()) {
							loadingDecorator.stopLoading();
						} else {
							Config config = JuejinPersistentConfig.getInstance().getConfig();
							instance.getPageInfo(config.getCurentCatalog(), endCursor, pageSize, result -> {
								if (result.isSuccess()) {
									UIUtil.invokeAndWaitIfNeeded((Runnable) () -> {
										JsonObject resultObj = (JsonObject) result.getResult();
										boolean hasNextPage = resultObj.get("has_more").getAsBoolean();
										if ("0".equals(endCursor)) {
											datas = resultObj.getAsJsonArray("data");
										}else {
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
										showNotification(pinTable, MessageType.ERROR, "获取数据失败，请稍后重试", Balloon.Position.atRight);
									});
								}
								loadingDecorator.stopLoading();
							});
						}
					}, state);
					boolean continueSearch = resultsCount.incrementAndGet() < ShowUsagesAction.getUsagesPageSize();
					if (!continueSearch) {
						loadingDecorator.stopLoading();
					}
				}
				return new Continuation(() -> {
					if (!this.isCancelled() && resultsCount.get() == 0) {
						StatusText emptyText = pinTable.getEmptyText();
						emptyText.clear();
					}
					loadingDecorator.stopLoading();
				}, state);
			}

			boolean isCancelled() {
				return progressIndicatorWhenSearchStarted != PinsToolWindowPanel.this.myResultsPreviewSearchProgress || progressIndicatorWhenSearchStarted.isCanceled();
			}

			public void onCanceled(@NotNull ProgressIndicator indicator) {
				if (PinsToolWindowPanel.this.isShowing() && progressIndicatorWhenSearchStarted == PinsToolWindowPanel.this.myResultsPreviewSearchProgress) {
					PinsToolWindowPanel.this.scheduleResultsUpdate();
				}
			}
		});
	}


	private void saveEndCorse(JsonObject pageInfo) {
		this.endCursor = pageInfo.get("cursor").getAsString();
		this.pageMap.put(pageIndex, this.endCursor);
		boolean hasNextPage = pageInfo.get("has_more").getAsBoolean();
	}

	@Override
	public void nextPage() {
		scheduleResultsUpdate();
		this.pageIndex += 1;
	}

	@Override
	public void previewPage() {
		if (pageIndex - 1 > 0) {
			this.endCursor = pageMap.get(pageIndex - 1);
		}
		scheduleResultsUpdate();
	}

	@Override
	public void refrshPage() {
		this.endCursor = "0";
		scheduleResultsUpdate();
	}

	@Nullable
	@Override
	public Object getData(@NotNull String dataId) {
		if (DataKeys.JUEJIN_PROJECTS_PINPANEL.is(dataId)) {
			return this;
		}
		return null;
	}
}

package cn.olange.pins.view;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.NeedMore;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.DateUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.*;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class MessageInfoPanel extends JBPanel {

    private Project project;

    public MessageInfoPanel(Project project) {
        super();
        this.project = project;
        this.setLayout(new BorderLayout());
        this.initComponent();
    }

    private void initComponent() {
        JBTabbedPane tabbedPane = new JBTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Component component = tabbedPane.getSelectedComponent();
                if (component instanceof MessageList) {
                    MessageList messageList = (MessageList) component;
                    messageList.refresh();
                }
            }
        });
        tabbedPane.addTab("评论", new MessageList(3));
        tabbedPane.addTab("点赞", new MessageList(1));
        tabbedPane.addTab("关注", new MessageList(2));
        tabbedPane.addTab("系统", new MessageList(4));
        ApplicationManager.getApplication().invokeLater(() -> {
            IdeFocusManager.getInstance(this.project).requestFocus(tabbedPane, true);
        });
    }


    class MessageList extends JBScrollPane{
        private JsonArray mesages = new JsonArray();
        private String cursor;
        private int msgType;
        private int pageSize = 20;
        private Alarm alarm;
        private JBList list;

        public MessageList(int msgType) {
            super();
            this.setBorder(JBUI.Borders.empty());
            this.msgType = msgType;
            list = new JBList<>();
            alarm = new Alarm();
            list.setMinimumSize(new Dimension(500,700));
            this.setViewportView(list);
            (new DoubleClickListener() {
                protected boolean onDoubleClick(MouseEvent event) {
                    if (event.getSource() != list) {
                        return false;
                    } else {
                        int selectedRow = MessageList.this.list.getSelectedIndex();
                        if (selectedRow < 0) {
                            return false;
                        }
                        Object myData = list.getModel().getElementAt(selectedRow);
                        if (myData instanceof JsonObject) {
                            JsonObject jsonObject = (JsonObject) myData;
                            JsonObject parent_info = jsonObject.get("parent_info").getAsJsonObject();
                            String item_id = "";
                            if (msgType == 1) {
                                JsonObject dst_info = jsonObject.get("dst_info").getAsJsonObject();
                                item_id = dst_info.get("item_id").getAsString();
                            } else if (msgType == 3) {
                                item_id = parent_info.get("item_id").getAsString();
                            } else if (msgType == 2) {
                                item_id = jsonObject.get("src_info").getAsJsonObject().get("item_id").getAsString();
                                BrowserLauncher.getInstance().browse("https://juejin.cn/user/" + item_id, WebBrowserManager.getInstance().getFirstActiveBrowser());
                                return true;
                            } else {
                                return true;
                            }
                            String finalItem_id = item_id;
                            ApplicationManager.getApplication().executeOnPooledThread(()->{
                                PinsService pinsService = PinsService.getInstance(project);
                                JsonObject pinInfo = pinsService.getPinInfo(finalItem_id);
                                if (pinInfo != null && !pinInfo.get("data").isJsonNull()) {
                                    UIUtil.invokeLaterIfNeeded(() -> {
                                        PinContentDialog pinDetailDialog = new PinContentDialog(project, pinInfo.get("data").getAsJsonObject());
                                        pinDetailDialog.showUI();
                                    });

                                }
                            });

                        } else if (myData instanceof NeedMore) {
                            scheduleGet();
                        }
                        return true;
                    }
                }
            }).installOn(list);
        }

        private void scheduleGet() {
            if (this.alarm != null && !this.alarm.isDisposed()) {
                this.alarm.cancelAllRequests();
                this.alarm.addRequest(this::getMessages, 100);
            }
        }

        private void refresh() {
            this.cursor = "0";
            this.scheduleGet();
        }

        private void getMessages() {
            DefaultListModel model = new DefaultListModel();
            list.setModel(model);
            list.setCellRenderer(new MessageCellRender(msgType));
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                UIUtil.invokeLaterIfNeeded(()->{
                    MessageList.this.list.setPaintBusy(true);
                });
                PinsService pinsService = PinsService.getInstance(project);
                Config config = JuejinPersistentConfig.getInstance().getState();
                JsonObject messageData = pinsService.getMessageList(msgType, pageSize, cursor, config.getCookieValue());
                UIUtil.invokeLaterIfNeeded(() -> {
                    JsonElement jsonElement = messageData.get("data");
                    if (jsonElement != null && !jsonElement.isJsonNull()) {
                        if ("0".equalsIgnoreCase(cursor)) {
                            mesages = jsonElement.getAsJsonArray();
                        } else {
                            mesages.addAll(jsonElement.getAsJsonArray());
                        }
                        cursor = messageData.get("cursor").getAsString();
                        for (int i = 0; i < mesages.size(); i++) {
                            JsonObject message = mesages.get(i).getAsJsonObject();
                            model.add(i, message);
                        }
                        boolean hasNextPage = messageData.get("has_more").getAsBoolean();
                        if (hasNextPage) {
                            model.add(mesages.size(), new NeedMore());
                        }
                        MessageList.this.list.getEmptyText().setText("");
                    } else {
                        MessageList.this.list.getEmptyText().setText("获取失败，请稍后再试");
                    }
                    MessageList.this.list.setPaintBusy(false);
                });
            });

        }
    }

    class MessageCellRender extends ColoredListCellRenderer{
        private int msgType;

        public MessageCellRender(int msgType) {

            this.msgType = msgType;
        }

        @Override
        protected void customizeCellRenderer(@NotNull JList jList, Object value, int i, boolean b, boolean b1) {
            if (value instanceof NeedMore) {
                this.append("...");
            } else if (value instanceof JsonObject) {
                JsonObject item = (JsonObject) value;
                JsonObject message = item.get("message").getAsJsonObject();
                JsonObject src_info = item.get("src_info").getAsJsonObject();
                int dst_type = message.get("dst_type").getAsInt();
                int status = message.get("status").getAsInt();
                SimpleTextAttributes commonText = status == 1 ? new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.listTitleLabelForeground()) : new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.searchFieldGrayForeground());
                this.append(DateUtils.getDistanceDate(message.get("ctime").getAsString()), new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.listTitleLabelForeground()));
                this.append(" ");
                if (msgType == 1) {
                    this.append(src_info.get("name").getAsString(), new SimpleTextAttributes(128, JBColor.BLUE));
                    this.append(" ");
                    this.append("赞了你", commonText);
                    if (dst_type == 5) {
                        this.append("在沸点下的评论", commonText);
                    } else if (4 == dst_type) {
                        this.append("的沸点", commonText);
                    }
                } else if (msgType == 3) {
                    this.append(src_info.get("name").getAsString(), new SimpleTextAttributes(128, JBColor.BLUE));
                    this.append(" ");
                    if (dst_type == 5) {
                        this.append("评论了你的沸点：", commonText);
                    } else if (dst_type == 6) {
                        this.append("回复了你在沸点下的评论：", commonText);
                    }
                    this.append(item.get("dst_info").getAsJsonObject().get("detail").getAsString(), commonText);
                } else if (msgType == 2) {
                    this.append(src_info.get("name").getAsString(), new SimpleTextAttributes(128, JBColor.BLUE));
                    this.append(" ");
                    this.append("关注了你", commonText);
                } else if (msgType == 4) {
                    if (4 == dst_type) { //沸点相关
                        if (message.get("action_type").getAsInt() == 40) {
                            this.append("您的 沸点 已被推荐", commonText);
                        } else if (message.get("action_type").getAsInt() == 41) {
                            this.append("您的 沸点 已被移除", commonText);
                        }
                    } else if (1 == dst_type) { // 等级相关
                        this.append("您在掘金社区已升级至 Lv" + item.get("dst_info").getAsJsonObject().get("detail").getAsString(), commonText);
                    }
                    /** todo
                     * 待完善
                     */
                }
            }
        }
    }
}

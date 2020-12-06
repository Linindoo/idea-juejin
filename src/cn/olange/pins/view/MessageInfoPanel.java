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
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
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
import java.util.Map;

public class MessageInfoPanel extends JBPanel {

    private Project project;
    private JsonObject messageData;
    private Disposable disposable;

    public MessageInfoPanel(Project project, JsonObject messageData, Disposable disposable) {
        super();
        this.project = project;
        this.messageData = messageData;
        this.disposable = disposable;
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
        MessageList commentList = new MessageList(3);
        tabbedPane.addTab("评论", commentList);
        MessageList praiseList = new MessageList(1);
        tabbedPane.addTab("点赞", praiseList);
        MessageList guanzhulist = new MessageList(2);
        tabbedPane.addTab("关注", guanzhulist);
        MessageList systemList = new MessageList(4);
        tabbedPane.addTab("系统", systemList);
        Disposer.register(this.disposable, commentList);
        Disposer.register(this.disposable, praiseList);
        Disposer.register(this.disposable, guanzhulist);
        Disposer.register(this.disposable, systemList);
        ApplicationManager.getApplication().invokeLater(() -> {
            IdeFocusManager.getInstance(this.project).requestFocus(tabbedPane, true);
        });
        if (messageData != null) {
            JsonObject count = messageData.get("count").getAsJsonObject();
            for (Map.Entry<String, JsonElement> messageCount : count.entrySet()) {
                String key = messageCount.getKey();
                int noreadCount = messageCount.getValue().getAsInt();
                if (noreadCount > 0) {
                    if ("3".equalsIgnoreCase(key)) {
                        tabbedPane.setTitleAt(0, "评论(" + noreadCount + ")");
                    } else if ("2".equalsIgnoreCase(key)) {
                        tabbedPane.setTitleAt(2, "关注(" + noreadCount + ")");
                    } else if ("1".equalsIgnoreCase(key)) {
                        tabbedPane.setTitleAt(1, "点赞(" + noreadCount + ")");
                    } else if ("4".equalsIgnoreCase(key)) {
                        tabbedPane.setTitleAt(3, "系统(" + noreadCount + ")");
                    }
                }

            }
        }
    }


    class MessageList extends JBScrollPane implements Disposable{
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
            list.getEmptyText().setText("混水摸鱼");
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
                            updateMessageStatus();
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

        private void updateMessageStatus() {
            if (mesages != null && mesages.size() > 0) {
                JsonObject lastMessage = mesages.get(0).getAsJsonObject().get("message").getAsJsonObject();
                if (lastMessage.get("status").getAsInt() == 0) {
                    String lastMessageID = lastMessage.get("message_id").getAsString();
                    PinsService pinsService = PinsService.getInstance(project);
                    Config config = JuejinPersistentConfig.getInstance().getState();
                    pinsService.setAllRead(msgType, lastMessageID, config.getCookieValue());
                }
            }
        }

        @Override
        public void dispose() {
            if (alarm != null && !alarm.isDisposed()) {
                Disposer.dispose(this.alarm);
            }
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
                SimpleTextAttributes commonText = status == 1 ? new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.listTitleLabelForeground()) : new SimpleTextAttributes(128, JBUI.CurrentTheme.BigPopup.selectedTabTextColor());
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

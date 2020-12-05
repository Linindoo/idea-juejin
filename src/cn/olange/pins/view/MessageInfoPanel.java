package cn.olange.pins.view;

import cn.olange.pins.model.CommentNode;
import cn.olange.pins.model.Config;
import cn.olange.pins.model.NeedMore;
import cn.olange.pins.service.PinsService;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.Alarm;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

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
        tabbedPane.addTab("评论", getScrollPanel(3));
        tabbedPane.addTab("点赞", getScrollPanel(1));
        tabbedPane.addTab("关注", getScrollPanel(2));
        tabbedPane.addTab("系统", getScrollPanel(4));
    }

    private JBScrollPane getScrollPanel(int msgType) {
        JBScrollPane scrollPane = new JBScrollPane(new MessageList(msgType));
        scrollPane.setBorder(JBUI.Borders.empty());
        return scrollPane;
    }

    class MessageList extends JBList{
        private JsonArray mesages = new JsonArray();
        private String cursor;
        private int msgType;
        private int pageSize = 20;
        private Alarm alarm;

        public MessageList(int msgType) {
            this.msgType = msgType;
            alarm = new Alarm();
            (new DoubleClickListener() {
                protected boolean onDoubleClick(MouseEvent event) {
                    if (event.getSource() != MessageList.this) {
                        return false;
                    } else {
                        int selectedRow = MessageList.this.getSelectedIndex();
                        if (selectedRow < 0) {
                            return false;
                        }
                        Object myData = MessageList.this.getModel().getElementAt(selectedRow);
                        if (myData instanceof JsonObject) {
                            JsonObject jsonObject = (JsonObject) myData;
//                            pinDetailDialog = new PinContentDialog(project, jsonObject);
//                            pinDetailDialog.showUI();
                        } else if (myData instanceof NeedMore) {
                            scheduleGet();
                        }
                        return true;
                    }
                }
            }).installOn(this);
            this.scheduleGet();
        }

        private void scheduleGet() {
            if (this.alarm != null && !this.alarm.isDisposed()) {
                this.alarm.cancelAllRequests();
                this.alarm.addRequest(this::getMessages, 100);
            }
        }

        private void getMessages() {
            DefaultListModel model = new DefaultListModel();
            setModel(model);
            setCellRenderer(new MessageCellRender(msgType));
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                UIUtil.invokeLaterIfNeeded(()->{
                    MessageList.this.setPaintBusy(true);
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
                        MessageList.this.getEmptyText().setText("");
                    } else {
                        MessageList.this.getEmptyText().setText("获取失败，请稍后再试");
                    }
                    MessageList.this.setPaintBusy(false);
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
                if (msgType == 1) {
                    this.append(src_info.get("name").getAsString(), new SimpleTextAttributes(128, JBColor.BLUE));
                    this.append("赞了你的");
                    if (dst_type == 5) {
                        this.append("在沸点下的评论");
                    } else if (4 == dst_type) {
                        this.append("沸点");
                    }
                } else if (msgType == 3) {
                    this.append(src_info.get("name").getAsString(), new SimpleTextAttributes(128, JBColor.BLUE));
                    if (dst_type == 5) {
                        this.append("评论了你的沸点：");
                    } else if (dst_type == 6) {
                        this.append("回复了你在沸点下的评论：");
                    }
                    this.append(item.get("dst_info").getAsJsonObject().get("detail").getAsString());
                } else if (msgType == 2) {
                    this.append(src_info.get("name").getAsString(), new SimpleTextAttributes(128, JBColor.BLUE));
                    this.append("关注了你");
                } else if (msgType == 4) {
                    if (4 == dst_type) { //沸点相关
                        if (message.get("action_type").getAsInt() == 40) {
                            this.append("您的 沸点 已被推荐");
                        } else if (message.get("action_type").getAsInt() == 41) {
                            this.append("您的 沸点 已被移除");
                        }
                    } else if (1 == dst_type) { // 等级相关
                        this.append("您在掘金社区已升级至 Lv" + item.get("dst_info").getAsJsonObject().get("detail").getAsString());
                    }
                }
            }
        }
    }
}

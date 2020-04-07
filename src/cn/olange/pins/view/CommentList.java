package cn.olange.pins.view;

import cn.olange.pins.model.MyVFlowLayout;
import cn.olange.pins.service.PinsService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CommentList extends JPanel implements Disposable {
	private final Project project;
	private final String pinID;
	private JPanel main;
	private JButton loadmore;
	private JPanel commentbox;
	private JScrollPane scrollPanel;
	private JsonArray commentData = new JsonArray();
	private int pageSize = 20;
	private int pageNum = 1;
	private LoadingDecorator loadingDecorator;

	public CommentList(Project project, String pinID) {
		this.project = project;
		this.pinID = pinID;
		scrollPanel.getVerticalScrollBar().setUnitIncrement(100);
		loadingDecorator = new LoadingDecorator(main, this, 0);
		this.initData();
	}

	public JComponent getContent() {
		return this.loadingDecorator.getComponent();
	}

	private void initData() {
		PinsService instance = PinsService.getInstance(project);
		this.loadmore.setVisible(false);
		loadingDecorator.startLoading(false);
		instance.getComments(pageNum, pageSize, pinID, result -> {
			if (result.isSuccess()) {
				JsonObject commentResult = (JsonObject) result.getResult();
				JsonObject data = commentResult.getAsJsonObject("d");
				JsonArray comments = data.getAsJsonArray("comments");
				commentData.addAll(comments);
				refreshData();
				validatePage(data);
				loadingDecorator.stopLoading();
			}
		});
		this.loadmore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pageNum++;
				loadingDecorator.startLoading(false);
				instance.getComments(pageNum, pageSize, pinID, result -> {
					if (result.isSuccess()) {
						JsonObject commentResult = (JsonObject) result.getResult();
						JsonObject data = commentResult.getAsJsonObject("d");
						JsonArray comments = data.getAsJsonArray("comments");
						commentData.addAll(comments);
						loadingDecorator.stopLoading();
						refreshData();
						validatePage(data);
					}
				});
			}
		});
	}

	private void refreshData() {
		for (int i = 0; i < this.commentData.size(); i++) {
			JsonObject comment = (JsonObject) this.commentData.get(i);
			CommentItem commentInfo = new CommentItem(comment, project);
			this.commentbox.add(commentInfo);
		}
		this.commentbox.updateUI();
	}

	private void createUIComponents() {
		this.commentbox = new JPanel();
		MyVFlowLayout myVFlowLayout = new MyVFlowLayout(0, 1, 5, 5, 20, 40, true, false);
		this.commentbox.setLayout(myVFlowLayout);
	}

	private void validatePage(JsonObject data) {
		int count = data.get("count").getAsInt();
		if (this.pageNum * this.pageSize > count) {
			this.loadmore.setVisible(false);
		} else {
			this.loadmore.setVisible(true);
		}
	}

	@Override
	public void dispose() {
	}
}

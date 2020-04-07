package cn.olange.pins.view;

import com.google.gson.JsonObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PinDetailDialog extends DialogWrapper implements Disposable {
	private JPanel pincontent;
	private JPanel main;
	private JPanel comment;
	private JScrollPane mainscroll;
	JsonObject pinItem;

	protected PinDetailDialog(JsonObject pinItem, Project project) {
		super(project);
		this.pinItem = pinItem;
		String pinID = pinItem.getAsJsonObject("node").get("id").getAsString();
		mainscroll.getVerticalScrollBar().setUnitIncrement(100);
		this.pincontent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		this.pincontent.add(new PinContentInfoPanel(this.pinItem, project), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		CommentList commentList = new CommentList(project, pinID);
		this.comment.add(commentList.getContent(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		setTitle("沸点详情");
		this.setCancelButtonText("关闭");
		this.init();
	}


	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return mainscroll;
	}

	@NotNull
	@Override
	protected Action[] createActions() {
		return new Action[]{this.getCancelAction()};
	}



	@Override
	public void dispose() {
		super.dispose();
	}
}

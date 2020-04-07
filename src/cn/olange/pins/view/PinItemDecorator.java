package cn.olange.pins.view;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;

public class PinItemDecorator extends JPanel {

	private JPanel container;

	public PinItemDecorator(JsonObject pintem, Project project) {
		PinContentInfoPanel pinInfoPanel = new PinContentInfoPanel(pintem,project,true);
		container.add(pinInfoPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200,-1), null, 0, false));
		this.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
		this.add(container, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

	}
}

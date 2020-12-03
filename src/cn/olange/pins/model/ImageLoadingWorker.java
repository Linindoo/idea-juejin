package cn.olange.pins.model;

import cn.olange.pins.PinsToolWindowFactory;
import cn.olange.pins.utils.DataKeys;
import cn.olange.pins.utils.ImageUtils;
import cn.olange.pins.view.PinContentDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageLoadingWorker extends SwingWorker<List<ImageIcon>, String> {
    private Project project;
    private JTextArea log;
    private JPanel viewer;
    private JsonArray pictures;

    public ImageLoadingWorker(Project project, JTextArea log, JPanel viewer, JsonArray pictures) {
        this.project = project;
        this.log = log;
        this.viewer = viewer;
        this.pictures = pictures;
        int row = pictures.size() % 3 == 0 ? pictures.size() / 3 : pictures.size() / 3 + 1;
        this.viewer.setLayout(new GridLayoutManager(row+1, 3, new Insets(10, 10, 0, 10), -1, 10));
        for (int i = 0; i < pictures.size(); i++) {
            Icon icon = IconLoader.getIcon("/icons/image_loading.png");
            JLabel jl = new JLabel(icon);
            jl.setPreferredSize(new Dimension(115,79));
            int grow = i / 3;
            int gc = i % 3;
            this.viewer.add(jl, new GridConstraints(grow , gc, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        }
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(115,79));
        if (pictures.size() == 1) {
            this.viewer.add(label, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
            this.viewer.add(label, new GridConstraints(0  , 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        } else if (pictures.size() == 2) {
            this.viewer.add(label, new GridConstraints(0  , 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        }
        viewer.updateUI();
    }

    @Override
    protected void done() {
        if (this.isCancelled()) {
            return;
        }
        try {
            viewer.removeAll();
            for (int i = 0; i < get().size(); i++) {
                ImageIcon imageIcon = get().get(i);
                JLabel jl = new JLabel(imageIcon);
                int grow = i / 3;
                int gc = i % 3;
                this.viewer.add(jl, new GridConstraints(grow, gc, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
                viewer.revalidate();
                int finalI = i;
                jl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        PinContentDialog pinContentDialog = PinsToolWindowFactory.getDataContext(project).getData(DataKeys.JUEJIN_PINCONTENTDETAIL_DAILOG);
                        if (pinContentDialog != null) {
                            pinContentDialog.showImageDialog(pictures, finalI);
                        }
                    }

                });
            }
            JLabel label = new JLabel();
            label.setPreferredSize(new Dimension(115,79));
            if (pictures.size() == 1) {
                this.viewer.add(label, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
                this.viewer.add(label, new GridConstraints(0  , 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
            } else if (pictures.size() == 2) {
                this.viewer.add(label, new GridConstraints(0  , 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
            }
            viewer.updateUI();
        } catch (Exception e) {
        }
    }

    @Override
    public List<ImageIcon> doInBackground() {
        List<ImageIcon> images = new ArrayList<ImageIcon>();
        int width = 115;
        int height = 79;
        for (JsonElement jsonElement : pictures) {
            ImageIcon ii = new ImageIcon();
            String filename = jsonElement.getAsString();
            filename = ImageUtils.getThumbnailUrl(filename);
            try {
                ii.setImage(new ImageIcon(new URL(filename)).getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
                images.add(ii);
                publish("Loaded " + filename);
            } catch (IOException ioe) {
                publish("Error loading " + filename);
            }
        }
        return images;
    }
}

package cn.olange.pins.action;

import cn.olange.pins.model.CatalogTag;
import cn.olange.pins.model.Config;
import cn.olange.pins.setting.JuejinPersistentConfig;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CatalogGroup extends ActionGroup {

    public CatalogGroup() {
        super();
        this.setPopup(true);
    }

    @Override
    public boolean displayTextInToolbar() {
        return true;
    }

    @Override
    public void update(AnActionEvent e) {
        List<CatalogTag> tags = getTags();
        if (tags != null && !tags.isEmpty()) {
            for (CatalogTag tag : tags) {
                if(tag.isSelected()){
                    e.getPresentation().setText(tag.getLabel());
                    return;
                }
            }
        }
        e.getPresentation().setIcon(null);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        List<AnAction> anActionList = Lists.newArrayList();
        List<CatalogTag> tags = getTags();
        for (CatalogTag tag : tags) {
            anActionList.add(new CatalogAction(tag));
        }
        AnAction[] anActions = new AnAction[anActionList.size()];
        anActionList.toArray(anActions);
        return anActions;
    }

    List<CatalogTag> getTags() {
        Config config = JuejinPersistentConfig.getInstance().getState();
        List<CatalogTag> tags = new ArrayList<>();
        tags.add(new CatalogTag("推荐", "recommend", "recommend".equals(config.getCurentCatalog())));
        tags.add(new CatalogTag("热门", "hot", "hot".equals(config.getCurentCatalog())));
        return tags;
    }
}

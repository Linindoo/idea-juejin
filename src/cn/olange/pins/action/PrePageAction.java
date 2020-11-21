package cn.olange.pins.action;

import cn.olange.pins.model.PageOperation;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class PrePageAction extends AnAction {
    private PageOperation pageOperation;

    public PrePageAction(PageOperation pageOperation) {
        super(IconLoader.getIcon("/icons/pre.svg"));
        this.pageOperation = pageOperation;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        pageOperation.previewPage();
    }
}

package cn.olange.pins.action;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class HelpAction extends AnAction {
    public HelpAction() {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        BrowserLauncher.getInstance().browse("https://github.com/Linindoo/idea-juejin", WebBrowserManager.getInstance().getFirstActiveBrowser());
    }
}

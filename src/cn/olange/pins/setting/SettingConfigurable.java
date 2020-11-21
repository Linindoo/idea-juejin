package cn.olange.pins.setting;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingConfigurable implements SearchableConfigurable {
    public static final String DISPLAY_NAME = "juejin plugin";

    private SettingUI mainPanel;
    @NotNull
    @Override
    public String getId() {
        return "juejin.pin";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return DISPLAY_NAME;
    }

    public static SettingConfigurable getInstance() {
        return ServiceManager.getService(SettingConfigurable.class);
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new SettingUI(this);
        return mainPanel.getComponent();
    }

    @Override
    public boolean isModified() {
        return mainPanel.isModified(this);
    }

    @Override
    public void apply() throws ConfigurationException {
        mainPanel.apply(this);
    }

    @Override
    public void reset() {
        mainPanel.reset(this);
    }

    @Override
    public void disposeUIResources() {
//        mainPanel.disposeUIResources();
        mainPanel = null;
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }
}

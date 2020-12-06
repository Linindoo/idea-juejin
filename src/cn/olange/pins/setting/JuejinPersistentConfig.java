package cn.olange.pins.setting;

import cn.olange.pins.model.Config;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "JuejinPersistentConfig", storages = {@Storage(value = "juejin-pins-config.xml")})
public class JuejinPersistentConfig implements PersistentStateComponent<Config> {
    private Config config = new Config();

    @Override
    public Config getState() {
        return this.config;
    }

    @Override
    public void loadState(@NotNull Config state) {
        this.config = state;
    }


    @Nullable
    public static JuejinPersistentConfig getInstance() {
        return ServiceManager.getService(JuejinPersistentConfig.class);
    }
}

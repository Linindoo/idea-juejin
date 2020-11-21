package cn.olange.pins.setting;

import cn.olange.pins.model.Config;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@State(name = "JuejinPersistentConfig", storages = {@Storage(value = "juejin-pins-config.xml")})
public class JuejinPersistentConfig implements PersistentStateComponent<JuejinPersistentConfig> {
    private Map<String, Config> initConfig = new HashMap<>();
    private static String INITNAME = "myConfig";
    @Nullable
    @Override
    public JuejinPersistentConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull JuejinPersistentConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }


    @Nullable
    public static JuejinPersistentConfig getInstance() {
        return ServiceManager.getService(JuejinPersistentConfig.class);
    }

    public Config getConfig() {
        Config config = initConfig.get(INITNAME);
        if (config == null) {
            config = new Config();
            initConfig.put(INITNAME, config);
            return config;
        } else {
            return config;
        }
    }
    public void setInitConfig(Config config) {
        initConfig.put(INITNAME, config);
    }
}

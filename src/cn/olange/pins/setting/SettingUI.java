package cn.olange.pins.setting;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.Constant;
import com.google.common.net.HostAndPort;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.PortField;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.util.io.HttpRequests;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.proxy.CommonProxy;
import com.intellij.util.proxy.JavaProxyProperty;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SettingUI implements ConfigurableUi<SettingConfigurable> {
  private JPanel myMainPanel;
  private String cookieType;
  private JRadioButton wechatCode;
  private JRadioButton directInput;
  private JButton checkbtn;
  private JTextField cookieArea;
  private ButtonGroup radioGroup;

  @Override
  public boolean isModified(@NotNull SettingConfigurable settings) {
    Config config = JuejinPersistentConfig.getInstance().getConfig();
    return !Comparing.strEqual(cookieArea.getText().trim(), config.getCookieValue()) || !Comparing.strEqual(cookieType, config.getCookieType());
  }

  public SettingUI(@NotNull final SettingConfigurable settings) {
    radioGroup=new ButtonGroup();
    radioGroup.add(wechatCode);
    radioGroup.add(directInput);
    wechatCode.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (wechatCode.isSelected()) {
          cookieArea.setEnabled(false);
          cookieType = Constant.cookieType.QRCODE.name();
        }
      }
    });
    directInput.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (directInput.isSelected()) {
          cookieArea.setEnabled(true);
          cookieType = Constant.cookieType.DIRECT.name();
        }
      }
    });
  }

  @Override
  public void reset(@NotNull SettingConfigurable settings) {
    Config config = JuejinPersistentConfig.getInstance().getState();
    this.cookieType = config.getCookieType();
    this.cookieArea.setText(config.getCookieValue());
    if (Constant.cookieType.DIRECT.name().equalsIgnoreCase(cookieType)) {
      radioGroup.setSelected(directInput.getModel(), true);
    } else if (Constant.cookieType.QRCODE.name().equalsIgnoreCase(cookieType)) {
      radioGroup.setSelected(wechatCode.getModel(), true);
    }
  }

  @Override
  public void apply(@NotNull SettingConfigurable settings) {
    Config config = JuejinPersistentConfig.getInstance().getConfig();
    if (config == null) {
      config = new Config();
    }
    config.setCookieValue(this.cookieArea.getText().trim());
    config.setCookieType(this.cookieType);
    JuejinPersistentConfig.getInstance().setInitConfig(config);
  }

  @Override
  @NotNull
  public JComponent getComponent() {
    return myMainPanel;
  }
}

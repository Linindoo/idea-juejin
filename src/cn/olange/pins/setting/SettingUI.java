package cn.olange.pins.setting;

import cn.olange.pins.model.Config;
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

  private JTextField myProxyLoginTextField;
  private JPasswordField myProxyPasswordTextField;
  private JCheckBox myProxyAuthCheckBox;
  private JCheckBox myRememberProxyPasswordCheckBox;

  private JButton myClearPasswordsButton;
  private JButton myCheckButton;
  private JLabel myProxyExceptionsLabel;
  private RawCommandLineEditor myProxyExceptions;
  private JRadioButton wechatCode;
  private JRadioButton directInput;
  private JButton checkbtn;
  private JTextField cookieArea;

  @Override
  public boolean isModified(@NotNull SettingConfigurable settings) {
    Config config = JuejinPersistentConfig.getInstance().getConfig();
//    String cookieValue = config.getUserCookie().entrySet().stream().map(x -> x.getKey() + (StringUtil.isNotEmpty(x.getValue()) ? ("=" + x.getValue()) : "")
//    ).collect(Collectors.joining(";"));
    return !Comparing.strEqual(cookieArea.getText().trim(), config.getCookieValue());
  }

  public SettingUI(@NotNull final SettingConfigurable settings) {
    wechatCode.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (wechatCode.isSelected()) {
          cookieArea.setEnabled(false);
          directInput.setSelected(false);
        }
      }
    });
    directInput.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (directInput.isSelected()) {
          cookieArea.setEnabled(true);
          wechatCode.setSelected(false);
        }
      }
    });
    Config config = JuejinPersistentConfig.getInstance().getConfig();
    String cookieValue = config.getUserCookie().entrySet().stream().map(x -> x.getKey() + "=" + x.getValue()).collect(Collectors.joining(";"));
    this.cookieArea.setText(cookieValue);
    configureCheckButton();
  }

  private void configureCheckButton() {
//    if (HttpConfigurable.getInstance() == null) {
//      myCheckButton.setVisible(false);
//      return;
//    }

//    myCheckButton.addActionListener(event -> {
//      String error = isValid();
//      if (error != null) {
//        Messages.showErrorDialog(myMainPanel, error);
//        return;
//      }
//
//      final String title = IdeBundle.message("dialog.title.check.proxy.settings");
//      final String answer =
//        Messages.showInputDialog(myMainPanel,
//                                 IdeBundle.message("message.text.enter.url.to.check.connection"),
//                                 title, Messages.getQuestionIcon(), "http://", null);
//      if (StringUtil.isEmptyOrSpaces(answer)) {
//        return;
//      }
//
//      final SettingConfigurable settings = SettingConfigurable.getInstance();
//      try {
//        apply(settings);
//      }
//      catch (ConfigurationException e) {
//        return;
//      }
//
//      final AtomicReference<IOException> exceptionReference = new AtomicReference<>();
//      ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
//        try {
//          HttpRequests.request(answer).readTimeout(3 * 1000).tryConnect();
//        }
//        catch (IOException e) {
//          exceptionReference.set(e);
//        }
//      }, IdeBundle.message("progress.title.check.connection"), true, null);
//
//      reset(settings);  // since password might have been set
//
//      final IOException exception = exceptionReference.get();
//      if (exception == null) {
//        Messages.showMessageDialog(myMainPanel, IdeBundle.message("message.connection.successful"), title, Messages.getInformationIcon());
//      }
//      else {
//        final String message = exception.getMessage();
////        if (settings.USE_HTTP_PROXY) {
////          settings.LAST_ERROR = message;
////        }
//        Messages.showErrorDialog(myMainPanel, errorText(message));
//      }
//    });
  }

  @Override
  public void reset(@NotNull SettingConfigurable settings) {
    cookieArea.setText("");
//    myAutoDetectProxyRb.setSelected(settings.USE_PROXY_PAC);
//    myPacUrlCheckBox.setSelected(settings.USE_PAC_URL);
//    cookieText.setText(settings.PAC_URL);
//    myUseHTTPProxyRb.setSelected(settings.USE_HTTP_PROXY);
//    myProxyAuthCheckBox.setSelected(settings.PROXY_AUTHENTICATION);
//
//    enableProxy(settings.USE_HTTP_PROXY);
//
//    myProxyLoginTextField.setText(settings.getProxyLogin());
//    myProxyPasswordTextField.setText(settings.getPlainProxyPassword());
//
//    myProxyPortTextField.setNumber(settings.PROXY_PORT);
//    myProxyHostTextField.setText(settings.PROXY_HOST);
//    myProxyExceptions.setText(StringUtil.notNullize(settings.PROXY_EXCEPTIONS));
//
//    myRememberProxyPasswordCheckBox.setSelected(settings.KEEP_PROXY_PASSWORD);
//    mySocks.setSelected(settings.PROXY_TYPE_IS_SOCKS);
//    myHTTP.setSelected(!settings.PROXY_TYPE_IS_SOCKS);
//
//    boolean showError = !StringUtil.isEmptyOrSpaces(settings.LAST_ERROR);
//    myErrorLabel.setVisible(showError);
//    myErrorLabel.setText(showError ? errorText(settings.LAST_ERROR) : null);
  }

  private void createUIComponents() {
    myProxyExceptions = new RawCommandLineEditor(text -> {
      List<String> result = new ArrayList<>();
      for (String token : text.split(",")) {
        String trimmedToken = token.trim();
        if (!trimmedToken.isEmpty()) {
          result.add(trimmedToken);
        }
      }
      return result;
    }, strings -> StringUtil.join(strings, ", "));
  }

  @Nullable
  private @NlsContexts.DialogMessage String isValid() {
    return null;
  }

  @Override
  public void apply(@NotNull SettingConfigurable settings) throws ConfigurationException {
    String error = isValid();
    if (error != null) {
      throw new ConfigurationException(error);
    }
    Config config = JuejinPersistentConfig.getInstance().getConfig();
    if (config == null) {
      config = new Config();
    }
//    Map<String, String> cookieMap = Arrays.stream(this.cookieArea.getText().split(";")).collect(Collectors.toMap(x -> x.split("=")[0], y->{
//      String[] split = y.split("=");
//      if (split.length >= 2) {
//        return split[1];
//      }
//      return "";
//    }));
    config.setCookieValue(this.cookieArea.getText().trim());
    JuejinPersistentConfig.getInstance().setInitConfig(config);
  }

  @Override
  @NotNull
  public JComponent getComponent() {
    return myMainPanel;
  }
}

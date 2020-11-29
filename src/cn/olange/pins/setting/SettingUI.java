package cn.olange.pins.setting;

import cn.olange.pins.model.Config;
import cn.olange.pins.model.Constant;
import cn.olange.pins.utils.HttpUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.util.Comparing;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingUI implements ConfigurableUi<SettingConfigurable> {
  private JPanel mainPanel;
  private String cookieType;
  private JRadioButton wechatCode;
  private JRadioButton directInput;
  private JPanel cookieEditorPanel;
  private JPanel extendPanel;
  private JButton 刷新Button;
  private JLabel qrcodeImage;
  private JLabel resultLabel;
  private ButtonGroup radioGroup;
  private Editor cookieEditor = null;

  @Override
  public boolean isModified(@NotNull SettingConfigurable settings) {
    Config config = JuejinPersistentConfig.getInstance().getConfig();
    return !Comparing.strEqual(cookieEditor.getDocument().getText().trim(), config.getCookieValue());
  }

  public SettingUI(@NotNull final SettingConfigurable settings) {
    radioGroup=new ButtonGroup();
    radioGroup.add(wechatCode);
    radioGroup.add(directInput);
    wechatCode.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (wechatCode.isSelected()) {
          cookieType = Constant.cookieType.QRCODE.name();
          refreshQrCode();
        }
      }
    });
    directInput.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (directInput.isSelected()) {
          cookieType = Constant.cookieType.DIRECT.name();
        }
      }
    });
    cookieEditor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), null, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
    EditorSettings templateHelpEditorSettings = cookieEditor.getSettings();
    templateHelpEditorSettings.setAdditionalLinesCount(0);
    templateHelpEditorSettings.setAdditionalColumnsCount(0);
    templateHelpEditorSettings.setLineMarkerAreaShown(false);
    templateHelpEditorSettings.setLineNumbersShown(false);
    templateHelpEditorSettings.setVirtualSpace(false);
    JBScrollPane jbScrollPane = new JBScrollPane(cookieEditor.getComponent());

    cookieEditorPanel.setLayout(new BorderLayout());
    cookieEditorPanel.add(jbScrollPane, BorderLayout.CENTER);
  }

  private void refreshQrCode() {
    extendPanel.setVisible(true);
    ApplicationManager.getApplication().invokeLater(()->{
      ImageIcon imageIcon = new ImageIcon();
      try {
        String result = HttpUtil.getJson("https://open.weixin.qq.com/connect/qrconnect?appid=wx5059f665cac93f16&redirect_uri=https%3A%2F%2Fjuejin.cn%2Fpassport%2Fauth%2Flogin_success&response_type=code&scope=snsapi_login&state=136023ea3gASoVCgoVPZIDRmNjQzY2IyYThlODJjNzU0OWFlY2E4YzhhN2U0ZjIwoU6-aHR0cHM6Ly9qdWVqaW4uY24vb2F1dGgtcmVzdWx0oVYBoUkAoUQAoUHRCjChTdEKMKFIqWp1ZWppbi5jbqFSBKJQTNEE_aZBQ1RJT06goUyyaHR0cHM6Ly9qdWVqaW4uY24voVTZIGZkN2I5MjUxNzQxMzY5ZGI2ZTZlNmI1MTJhYjEwODJhoVcAoUYAolNBAKFVww%253D%253D");
        Document document = Jsoup.parse(result);
        Elements js_qr_img = document.body().getElementsByClass("js_qr_img");
        if (js_qr_img.size() > 0) {
          String src = js_qr_img.get(0).attr("src");
          String UID = src.substring(src.lastIndexOf("/") +1);
          URL location = new URL("https://open.weixin.qq.com" + src);
          UIUtil.invokeLaterIfNeeded(()->{
            imageIcon.setImage(new ImageIcon(location).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
            JLabel label = new JLabel(imageIcon);
            qrcodeImage.setIcon(imageIcon);
            EdtScheduledExecutorService.getInstance().schedule(() -> {
              checkLogin(UID, "");
            }, 1, TimeUnit.SECONDS);
          });
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    });
  }

  void checkLogin(String ID, String last) {
    try {
      String url = String.format("https://lp.open.weixin.qq.com/connect/l/qrconnect?uuid=%s&_=%s", ID, System.currentTimeMillis());
      if (StringUtils.isNotEmpty(last)) {
        url = url + "&last=" + last;
      }
      System.out.println(url);
      String json = HttpUtil.getLonTimeJson(url);
      System.out.println(json);
      Pattern compile = Pattern.compile("window.wx_errcode=([0-9]*?);window.wx_code='(.*?)';");
      Matcher matcher = compile.matcher(json);
      if (matcher.find()) {
        String nextLast = matcher.group(1);
        switch (nextLast) {
          case "405":
            String h = "https://juejin.cn/passport/auth/login_success";
            h = h.replace("&amp;", "&");
            h += "?" + "code=" + ID + "&state=d1c87c2d3gASoVCgoVPZIDgyZDc4NThkNTViMmEwOWM4Y2ZhOTgzMDhkZTVmOGQ2oU6-aHR0cHM6Ly9qdWVqaW4uY24vb2F1dGgtcmVzdWx0oVYBoUkAoUQAoUHRCjChTdEKMKFIqWp1ZWppbi5jbqFSBKJQTNEE_aZBQ1RJT06goUyyaHR0cHM6Ly9qdWVqaW4uY24voVTZIDQ3OWUwYmZmYmZiZGEyOGE3YzZjMjY0YWIwMGQ4NjQyoVcAoUYAolNBAKFVww%3D%3D";
            System.out.println(h);
            CloseableHttpResponse response = HttpUtil.getResponse(h);
            Header[] allHeaders = response.getAllHeaders();
            System.out.println(allHeaders);

//            var i = c("self_redirect");
//            if (d)
//              if ("true" !== i && "false" !== i)
//                try {
//                  document.domain = "qq.com";
//                  var j = window.top.location.host.toLowerCase();
//                  j && (window.location = h)
//                } catch (k) {
//                  window.top.location = h
//                }
//              else if ("true" === i)
//                try {
//                  window.location = h
//                } catch (k) {
//                  window.top.location = h
//                }
//              else
//                window.top.location = h;
//            else
//              window.location = h;
            break;
          case "404":
            checkLogin(ID,nextLast);
//            jQuery(".js_status").hide(),
//                    jQuery(".js_qr_img").hide(),
//                    jQuery(".js_wx_after_scan").show(),
//                    setTimeout(b, 100, g);
            break;
          case "403":
            checkLogin(ID,nextLast);
//            jQuery(".js_status").hide(),
//                    jQuery(".js_qr_img").hide(),
//                    jQuery(".js_wx_after_cancel").show(),
//                    setTimeout(b, 2e3, g);
            break;
          case "402":
          case "500":
            UIUtil.invokeLaterIfNeeded(()->{
              resultLabel.setText("请刷新重试");
            });
//            window.location.reload();
            break;
          case "408":
            checkLogin(ID,nextLast);
//            setTimeout(b, 2e3)
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void toRefreshLogin() {

  }

  @Override
  public void reset(@NotNull SettingConfigurable settings) {
    Config config = JuejinPersistentConfig.getInstance().getState();
    this.cookieType = config.getCookieType();
    ApplicationManager.getApplication().runWriteAction(() -> {
      cookieEditor.getDocument().setText(config.getCookieValue());
    });
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
    config.setCookieValue(this.cookieEditor.getDocument().getText().trim());
    config.setCookieType(this.cookieType);
    JuejinPersistentConfig.getInstance().setInitConfig(config);
  }

  @Override
  @NotNull
  public JComponent getComponent() {
    return mainPanel;
  }
}

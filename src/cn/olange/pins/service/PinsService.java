package cn.olange.pins.service;

import cn.olange.pins.model.AsyncResult;
import cn.olange.pins.model.Config;
import cn.olange.pins.model.Handler;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.io.IOException;

public class PinsService {
	private final static String COMMENT_URL = "https://hot-topic-comment-wrapper-ms.juejin.im/v1/reply/";
	public static PinsService getInstance(Project project) {
		return ServiceManager.getService(project, PinsService.class);
	}

	public void getPageInfo(String topic, String endCursor, int pageSize, Handler<AsyncResult> handler) {
		JsonObject query = new JsonObject();
		query.addProperty("id_type", 4);
		query.addProperty("limit", pageSize);
		query.addProperty("sort_type", "hot".equals(topic) ? 200 : 300);
		query.addProperty("cursor", endCursor);
		try {
			String result = HttpUtil.postJson("https://apinew.juejin.im/recommend_api/v1/short_msg/" + topic, query.toString());
			JsonObject jObject = JsonParser.parseString(result).getAsJsonObject();
			handler.handle(new AsyncResult(true, jObject));
		} catch (IOException e) {
			e.printStackTrace();
			handler.handle(new AsyncResult(false, e.getMessage()));
		}
	}

	public void getComments(String cursor, int pageSize, String pinId, Handler<AsyncResult> handler) {
		try {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("client_type",2608);
			jsonObject.addProperty("cursor", cursor);
			jsonObject.addProperty("item_id", pinId);
			jsonObject.addProperty("item_type", 4);
			jsonObject.addProperty("limit", pageSize);
			String json = HttpUtil.postJson("https://apinew.juejin.im/interact_api/v1/comment/list", jsonObject.toString());
			JsonObject result = JsonParser.parseString(json).getAsJsonObject();
			handler.handle(new AsyncResult(true, result));
		} catch (IOException e) {
			e.printStackTrace();
			handler.handle(new AsyncResult(false, e.getMessage()));
		}
	}

	public void getCommentReply(int pageNum, int pageSize,String commentID, Handler<AsyncResult> handler) {
		try {
			String json = HttpUtil.getJson(COMMENT_URL + commentID + "?pageNum=" + pageNum + "&pageSize=" + pageSize);
			JsonObject result = JsonParser.parseString(json).getAsJsonObject();
			handler.handle(new AsyncResult(true, result));
		} catch (IOException e) {
			e.printStackTrace();
			handler.handle(new AsyncResult(false,e.getMessage()));
		}
	}

	public JsonObject getUserInfo(String cookieValue) {
		try {
			String json = HttpUtil.getJson("https://api.juejin.cn/user_api/v1/user/get?aid=2608&not_self=0", cookieValue);
			return JsonParser.parseString(json).getAsJsonObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}
}

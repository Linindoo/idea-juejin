package cn.olange.pins.service;

import cn.olange.pins.model.AsyncResult;
import cn.olange.pins.model.Config;
import cn.olange.pins.model.Handler;
import cn.olange.pins.setting.JuejinPersistentConfig;
import cn.olange.pins.utils.HttpUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;

public class PinsService {
	private final static String COMMENT_URL = "https://api.juejin.cn/interact_api/v1/reply/list";
	public static PinsService getInstance(Project project) {
		return ServiceManager.getService(project, PinsService.class);
	}

	public void getPageInfo(String topic, String endCursor, int pageSize, String cookieValue, Handler<AsyncResult> handler) {
		JsonObject query = new JsonObject();
		query.addProperty("id_type", 4);
		query.addProperty("limit", pageSize);
		query.addProperty("sort_type", "hot".equals(topic) ? 200 : 300);
		query.addProperty("cursor", endCursor);
		try {
			String result = HttpUtil.postJson("https://apinew.juejin.im/recommend_api/v1/short_msg/" + topic, query.toString(), cookieValue);
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

	public void getCommentReply(String cursor, int pageSize, String commentID, String pinID, Handler<AsyncResult> handler) {
		try {
			JsonObject params = new JsonObject();
			params.addProperty("client_type", 2608);
			params.addProperty("comment_id", commentID);
			params.addProperty("cursor", cursor);
			params.addProperty("item_id", pinID);
			params.addProperty("item_type", 4);
			params.addProperty("limit", pageSize);
			String json = HttpUtil.postJson(COMMENT_URL, params.toString());
			JsonObject result = JsonParser.parseString(json).getAsJsonObject();
			handler.handle(new AsyncResult(true, result));
		} catch (IOException e) {
			e.printStackTrace();
			handler.handle(new AsyncResult(false, e.getMessage()));
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

	public JsonObject comment(String pinID, String replyContent, String cookieValue) {
		JsonObject params = new JsonObject();
		params.addProperty("client_type", 2608);
		params.addProperty("comment_content", replyContent);
		params.add("comment_pics", new JsonArray());
		params.addProperty("item_id", pinID);
		params.addProperty("item_type", 4);
		try {
			String resp = HttpUtil.postJson("https://api.juejin.cn/interact_api/v1/comment/publish", params.toString(), cookieValue);
			if (StringUtils.isNotEmpty(resp)) {
				return JsonParser.parseString(resp).getAsJsonObject();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}

	public JsonObject replyComment(String pinID, String commentID, String replyID, String replyUserID, String replyContent, String cookieValue) {
		JsonObject params = new JsonObject();
		params.addProperty("client_type", 2608);
		params.addProperty("reply_content", replyContent);
		params.add("reply_pics", new JsonArray());
		params.addProperty("reply_to_comment_id", commentID);
		params.addProperty("reply_to_reply_id", replyID);
		params.addProperty("reply_to_user_id", replyUserID);
		params.addProperty("item_id", pinID);
		params.addProperty("item_type", 4);
		try {
			String resp = HttpUtil.postJson("https://api.juejin.cn/interact_api/v1/reply/publish", params.toString(), cookieValue);
			System.out.println(resp);
			if (StringUtils.isNotEmpty(resp)) {
				return JsonParser.parseString(resp).getAsJsonObject();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new JsonObject();
	}


	public boolean praise(String pinID, String cookieValue) {
		JsonObject params = new JsonObject();
		try {
			params.addProperty("item_id", pinID);
			params.addProperty("item_type", 4);
			String ret = HttpUtil.postJson("https://api.juejin.cn/interact_api/v1/digg/save", params.toString(), cookieValue);
			if (StringUtils.isNotEmpty(ret)) {
				JsonObject result = JsonParser.parseString(ret).getAsJsonObject();
				if (result.get("err_no").getAsInt() == 0) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}

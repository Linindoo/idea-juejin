package cn.olange.pins.service;

import cn.olange.pins.model.AsyncResult;
import cn.olange.pins.model.Handler;
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
		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			@Override
			public void run() {
				JsonObject query = new JsonObject();
				JsonObject pageInfo = new JsonObject();
				pageInfo.addProperty("size", pageSize);
				pageInfo.addProperty("afterPosition", endCursor);
				pageInfo.addProperty("after", endCursor);
				query.add("variables", pageInfo);
				JsonObject extensions = new JsonObject();
				JsonObject exquery = new JsonObject();
				exquery.addProperty("id", topic);
				extensions.add("query", exquery);
				query.add("extensions", extensions);
				try {
					String result = HttpUtil.postJson("https://web-api.juejin.im/query", query.toString());
					JsonObject jObject = new JsonParser().parse(result).getAsJsonObject();
					handler.handle(new AsyncResult(true, jObject));
				} catch (IOException e) {
					e.printStackTrace();
					handler.handle(new AsyncResult(false, e.getMessage()));
				}
			}
		});
	}

	public void getComments(int pageNum, int pageSize, String pinId, Handler<AsyncResult> handler) {
		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			@Override
			public void run() {
				try {
					String json = HttpUtil.getJson("https://hot-topic-comment-wrapper-ms.juejin.im/v1/comments/" + pinId + "?pageNum=" + pageNum + "&pageSize=" + pageSize);
					JsonObject result = new JsonParser().parse(json).getAsJsonObject();
					handler.handle(new AsyncResult(true, result));
				} catch (IOException e) {
					e.printStackTrace();
					handler.handle(new AsyncResult(false, e.getMessage()));
				}
			}
		});
	}

	public void getCommentReply(int pageNum, int pageSize,String commentID, Handler<AsyncResult> handler) {
		try {
			String json = HttpUtil.getJson(COMMENT_URL + commentID + "?pageNum=" + pageNum + "&pageSize=" + pageSize);
			JsonObject result = new JsonParser().parse(json).getAsJsonObject();
			handler.handle(new AsyncResult(true, result));
		} catch (IOException e) {
			e.printStackTrace();
			handler.handle(new AsyncResult(false,e.getMessage()));
		}
	}
}

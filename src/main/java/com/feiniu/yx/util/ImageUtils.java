package com.feiniu.yx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.feiniu.yx.config.SystemEnv;

/**
 * @author tongwenhuan
 * 2017年2月20日 下午3:18:38
 */
public class ImageUtils {
	
	private static List<String> newImageHosts;

	private static List<String> oldScImageHosts;

	private static List<String> oldZyImageHosts;

	private static String scOldMatcher = "http://.+?/group\\d+/.+\\.\\w+";

	private static String newMatcher = "(http://.+?)?/pic/.+\\.\\w+";

	private static String zyOldMatcher = "(http://.+?)?/C/.+\\.\\w+";

	private static String thumbMatcher = ".+?_\\d+x\\d+\\.\\w+";
	
	static {
		String env = SystemEnv.getProperty("fn.env");
		String newImageDownloadHostExp = SystemEnv.getProperty("fn.newimage." + env + ".download.host");
		String oldScImageDownloadHostExp = SystemEnv.getProperty("fn.oldscimage." + env + ".download.host");
		String oldZyImageDownloadHostExp = SystemEnv.getProperty("fn.oldzyimage." + env + ".download.host");

		newImageHosts = splitHostExpress(newImageDownloadHostExp);
		oldScImageHosts = splitHostExpress(oldScImageDownloadHostExp);
		oldZyImageHosts = splitHostExpress(oldZyImageDownloadHostExp);
		
//		newImageHosts = splitHostExpress("http://img[17,18].fn-mart.com");
//		oldScImageHosts = splitHostExpress("http://img[17,18].fn-mart.com");
//		oldZyImageHosts = splitHostExpress("http://img[01,02].fn-mart.com");
	}

	public static String getImageUrl(String path) {
		Object[] imageInfo = getImageInfo(path);
		return ((String) imageInfo[0]).replace("_org.", ".");
	}

	public static String getThumbUrl(String path, int width, int height) {
		Object[] imageInfo = getImageInfo(path);
		String imageUrl = (String) imageInfo[0];
		ImageType imageType = (ImageType) imageInfo[1];
		String thumbUrl = imageUrl;
		if (!ImageType.UNKNOWN.equals(imageType) && !path.matches(thumbMatcher)) {
			thumbUrl = imageUrl.replaceFirst("\\.\\w+$", "_" + width + "x" + height + "$0");
		}
		return removeORG(thumbUrl, width, height);
	}

	//去掉org
	private static String removeORG(String thumbUrl,int width,int height){
		String url = thumbUrl.replaceFirst("_org_"+width+"x"+height, "_" + width + "x" + height);
		return url;
	}
	
	private static Object[] getImageInfo(String path) {
		String url = path;
		ImageType imageType = path.matches(newMatcher) ? ImageType.NEW : path.matches(scOldMatcher) ? ImageType.SCOLD
				: path.matches(zyOldMatcher) ? ImageType.ZYOLD : ImageType.UNKNOWN;
		if (!ImageType.UNKNOWN.equals(imageType)) {
			String host = getHost(imageType);
			boolean isAbsolutePath = path.startsWith("http://");
			if (isAbsolutePath) {
				url = path.replaceFirst("http://.+?(?=/)", host);
			} else {
				if (path.startsWith("/")) {
					url = host + path;
				} else {
					url = host + "/" + path;
				}
			}
		}
		return new Object[] { url, imageType };
	}

	private static List<String> splitHostExpress(String hostExpress) {
		List<String> hostList = new ArrayList<String>();
		String regex = "\\[([\\w,]+)\\]";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(hostExpress);
		if (matcher.find()) {
			String strHashValues = matcher.group(1);
			String[] hashValues = strHashValues.split(",");
			for (String hashValue : hashValues) {
				String imageDownloadHost = hostExpress.replaceFirst(regex, hashValue);
				hostList.add(imageDownloadHost);
			}
		} else {
			hostList.add(hostExpress);
		}
		return hostList;
	}

	private static String getHost(ImageType imageType) {
		String host = "";
		List<String> list = null;
		if (ImageType.NEW.equals(imageType)) {
			list = newImageHosts;
		} else if (ImageType.SCOLD.equals(imageType)) {
			list = oldScImageHosts;
		} else if (ImageType.ZYOLD.equals(imageType)) {
			list = oldZyImageHosts;
		}
		if (list != null) {
			int index = 0;
			if (list.size() > 1) {
				index = (int) Math.floor(Math.random() * list.size());
			}
			host = list.get(index);
			if (host.endsWith("/")) {
				host = host.substring(0, host.length() - 1);
			}
		}
		return host;
	}

	private static enum ImageType {
		NEW, SCOLD, ZYOLD, UNKNOWN
	}

//	public static void main(String[] args) {
//		String s = "/C/item/2015/0319/201503C190006191/_944887285_org.jpg";
//		System.out.println(getThumbUrl(s, 400, 400));
//	}
}

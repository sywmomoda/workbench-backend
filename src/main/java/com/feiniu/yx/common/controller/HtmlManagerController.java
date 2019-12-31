package com.feiniu.yx.common.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.entity.FileList;
import com.feiniu.yx.util.FileComparator;

@Controller
@RequestMapping(value = "/htmlManager")
public class HtmlManagerController {

	@RequestMapping(value = "/htmlList")
	public ModelAndView docList(HttpServletRequest request, HttpServletResponse response) {
		String path = "";
		String curPage = "1";
		String pageRows = "10";
		String topPath = YXConstant.STATICPATH;
		try {
			if (request.getParameter("fileName") != null)
				path = new String(request.getParameter("fileName").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			if (request.getParameter("curPage") != null)
				curPage = new String(request.getParameter("curPage").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			if (request.getParameter("pageRows") != null)
				pageRows = new String(request.getParameter("pageRows").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (path == null || "".equals(path)
				|| (!(path.replaceAll("\\\\", "/").startsWith(topPath)) && !path.startsWith(topPath)))
			path = topPath;
		File floder = new File(path);
		File[] files = floder.listFiles();
		FileList fList = new FileList();
		List<FileList> fileList = new ArrayList<FileList>();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				// System.out.println("Name is "+f.getName());
				FileList fl = new FileList();
				fl.setFileName(f.getName());
				fl.setFilePath(f.getPath());
				if (f.isDirectory()) {
					fl.setFileType("文件夹");
				} else {
					fl.setFileType(f.getName().substring(f.getName().lastIndexOf(".") + 1).toUpperCase());
				}
				if (f.isFile()) {
					long size = f.length();
					if (size > 1024 * 1024)
						fl.setFileSize(size / 1024 / 1024 + "MB");
					else if (size > 1024 && size <= 1024 * 1024)
						fl.setFileSize(size / 1024 + "KB");
					else
						fl.setFileSize(size + "byte");

				}
				Date date = new Date(f.lastModified());
				fl.setLastEditDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
				fileList.add(fl);
			}
		}
		FileComparator comparator = new FileComparator();
		Collections.sort(fileList, comparator);
		fList.setTotalRows(fileList.size());
		int fromIndex = (Integer.parseInt(curPage) - 1) * Integer.parseInt(pageRows);
		int toIndex = (Integer.parseInt(curPage)) * Integer.parseInt(pageRows);
		if (fileList.size() < toIndex)
			toIndex = fileList.size();
		fList.setCurPage(Integer.parseInt(curPage));
		fList.setPageRows(Integer.parseInt(pageRows));
		fileList = fileList.subList(fromIndex, toIndex);
		fList.setFileLists(fileList);

		fList.setParentPath(floder.getParent());
		fList.setFilePath(path.replace("\\", "/"));

		ModelAndView mv = new ModelAndView("/uploadFile/htmlList");
		mv.addObject("topPath", topPath);
		mv.addObject("fileList", fileList);
		mv.addObject("fList", fList);
		return mv;

	}

	@RequestMapping(value = "/newFolder")
	public ModelAndView newFolder(HttpServletRequest request, HttpServletResponse response) {
		String curPage = "1";
		String pageRows = "10";
		String path = "";
		String topPath = YXConstant.STATICPATH;
		try {
			if (request.getParameter("filePath") != null)
				path = new String(request.getParameter("filePath").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			if (request.getParameter("curPage") != null)
				curPage = new String(request.getParameter("curPage").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			if (request.getParameter("pageRows") != null)
				pageRows = new String(request.getParameter("pageRows").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String newName = "";
		try {
			if (request.getParameter("fileName") != null)
				newName = new String(request.getParameter("fileName").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		File newFloder = new File(path + File.separator + newName);
		newFloder.mkdir();
		if (path == null || "".equals(path)
				|| (!(path.replaceAll("\\\\", "/").startsWith(topPath)) && !path.startsWith(topPath)))
			path = topPath;
		File floder = new File(path);
		File[] files = floder.listFiles();
		FileList fList = new FileList();
		List<FileList> fileList = new ArrayList<FileList>();
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				// System.out.println("Name is "+f.getName());
				FileList fl = new FileList();
				fl.setFileName(f.getName());
				fl.setFilePath(f.getPath());
				if (f.isDirectory()) {
					fl.setFileType("文件夹");
				}
				if (f.isFile()) {
					long size = f.length();
					if (size > 1024 * 1024)
						fl.setFileSize(size / 1024 / 1024 + "MB");
					else if (size > 1024 && size <= 1024 * 1024)
						fl.setFileSize(size / 1024 + "KB");
					else
						fl.setFileSize(size + "byte");

				}
				Date date = new Date(f.lastModified());
				fl.setLastEditDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
				fileList.add(fl);
			}
		}
		FileComparator comparator = new FileComparator();
		Collections.sort(fileList, comparator);
		fList.setTotalRows(fileList.size());
		int fromIndex = (Integer.parseInt(curPage) - 1) * Integer.parseInt(pageRows);
		int toIndex = (Integer.parseInt(curPage)) * Integer.parseInt(pageRows);
		if (fileList.size() < toIndex)
			toIndex = fileList.size();
		fList.setCurPage(Integer.parseInt(curPage));
		fileList = fileList.subList(fromIndex, toIndex);
		fList.setFileLists(fileList);
		fList.setParentPath(floder.getParent());
		fList.setFilePath(path);

		ModelAndView mv = new ModelAndView("/uploadFile/htmlList");
		mv.addObject("topPath", topPath);
		mv.addObject("fileList", fileList);
		mv.addObject("fList", fList);
		return mv;
	}

	@RequestMapping(value = "/delFile")
	// @LogTrace(msgFomort="删掉文件（{delName}）")
	public ModelAndView delFile(HttpServletRequest request, HttpServletResponse response) {
		String path = "";
		String curPage = "1";
		String pageRows = "10";
		try {
			if (request.getParameter("curPage") != null)
				curPage = new String(request.getParameter("curPage").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			if (request.getParameter("pageRows") != null)
				pageRows = new String(request.getParameter("pageRows").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String topPath = YXConstant.STATICPATH;
		String fileName = request.getParameter("delName");
		try {
			if (request.getParameter("path") != null)
				path = new String(request.getParameter("path").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(fileName)) {
			File delFile = new File(path + File.separator + fileName);
			if (delFile.exists()) {
				delFile.delete();
			}
		}
		if (path == null || "".equals(path)
				|| (!(path.replaceAll("\\\\", "/").startsWith(topPath)) && !path.startsWith(topPath)))
			path = topPath;
		File floder = new File(path);
		File[] files = floder.listFiles();
		FileList fList = new FileList();
		List<FileList> fileList = new ArrayList<FileList>();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			// System.out.println("Name is "+f.getName());
			FileList fl = new FileList();
			fl.setFileName(f.getName());
			fl.setFilePath(f.getPath());
			if (f.isDirectory()) {
				fl.setFileType("文件夹");
			} else {
				fl.setFileType(f.getName().substring(f.getName().lastIndexOf(".") + 1).toUpperCase());
			}
			if (f.isFile()) {
				long size = f.length();
				if (size > 1024 * 1024)
					fl.setFileSize(size / 1024 / 1024 + "MB");
				else if (size > 1024 && size <= 1024 * 1024)
					fl.setFileSize(size / 1024 + "KB");
				else
					fl.setFileSize(size + "byte");

			}
			Date date = new Date(f.lastModified());
			fl.setLastEditDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
			fileList.add(fl);
		}
		FileComparator comparator = new FileComparator();
		Collections.sort(fileList, comparator);
		fList.setTotalRows(fileList.size());
		int fromIndex = (Integer.parseInt(curPage) - 1) * Integer.parseInt(pageRows);
		int toIndex = (Integer.parseInt(curPage)) * Integer.parseInt(pageRows);
		if (fileList.size() < toIndex)
			toIndex = fileList.size();
		fList.setCurPage(Integer.parseInt(curPage));
		fileList = fileList.subList(fromIndex, toIndex);
		fList.setFileLists(fileList);
		fList.setParentPath(floder.getParent());
		fList.setFilePath(path);

		ModelAndView mv = new ModelAndView("/uploadFile/htmlList");
		mv.addObject("topPath", topPath);
		mv.addObject("fList", fList);
		return mv;
	}

	@RequestMapping(value = "/gotoUpload")
	public ModelAndView gotoUpload(HttpServletRequest request, HttpServletResponse response) {
		String path = "";
		String topPath = YXConstant.STATICPATH;
		try {
			if (request.getParameter("fileName") != null)
				path = new String(request.getParameter("fileName").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (path == null || "".equals(path)
				|| (!(path.replaceAll("\\\\", "/").startsWith(topPath)) && !path.startsWith(topPath)))
			path = topPath;
		ModelAndView mv = new ModelAndView("/uploadFile/activityPageUpload");
		mv.addObject("path", path);
		return mv;
	}

	@RequestMapping(value = "/fileUpload")
	// @LogTrace(msgFomort="上传html文件")
	public ModelAndView fileUpload(HttpServletRequest request, HttpServletResponse response) {
		String savepath = request.getParameter("path");
		MultipartHttpServletRequest mulRequest = (MultipartHttpServletRequest) request;
		@SuppressWarnings("rawtypes")
		List myFiles = mulRequest.getFiles("file");
		File tempFile = null;
		String fileStr = "";
		for (int i = 0; i < myFiles.size(); i++) {
			MultipartFile file = (MultipartFile) myFiles.get(i);
			// FormFile file = uploadFile.getFile();

			if (file == null || "".equals(file.getOriginalFilename())) {
				fileStr = "没找到上传的文件！请指定正确的路径！";
			} else {
				InputStream ins;
				try {
					ins = file.getInputStream();
					tempFile = new File(savepath + File.separator + file.getOriginalFilename());
					if (tempFile.exists())
						tempFile.delete();
					tempFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(tempFile);
					fos.write(file.getBytes());
					fos.flush();
					fos.close();
					ins.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// 能运行到这里，就可以使用单个文件上传的方法进行上传了。循环而已
				fileStr += "文件名:" + file.getOriginalFilename() + "&nbsp;&nbsp;&nbsp;&nbsp; 文件大小:" + file.getSize()
						+ "Byte<br/>";
			}
		}
		// 返回文本
		String msg = "上传成功：'" + fileStr + "<a href=\"" + request.getContextPath()
				+ "/htmlManager/htmlList.form?fileName=" + savepath + "\">点此返回查看</a>";
		ModelAndView mv = new ModelAndView("/uploadFile/activityPageUpload");
		mv.addObject("path", savepath);
		mv.addObject("msg", msg);
		return mv;

	}

	@RequestMapping(value = "/download")
	public void download(String path, HttpServletResponse response) {
		try {
			if (StringUtils.isNotBlank(path)) {
				path = new String(path.getBytes("ISO8859-1"), "UTF8");
			}
			File file = new File(path);
			// 取得文件名。
			String filename = file.getName();
			// 以流的形式下载文件。
			InputStream fis = new BufferedInputStream(new FileInputStream(path));
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
			// 清空response
			response.reset();
			// 设置response的Header
			response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
			response.addHeader("Content-Length", "" + file.length());
			OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
			response.setContentType("application/octet-stream");
			toClient.write(buffer);
			toClient.flush();
			toClient.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@RequestMapping(value = "/deleteBatchFile")
	// @LogTrace(msgFomort="批量删掉文件（{deleteBatchFile}）")
	public ModelAndView deleteBatchFile(HttpServletRequest request, HttpServletResponse response) {
		String path = "";
		String curPage = "1";
		String pageRows = "10";
		try {
			if (request.getParameter("curPage") != null)
				curPage = new String(request.getParameter("curPage").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			if (request.getParameter("pageRows") != null)
				pageRows = new String(request.getParameter("pageRows").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String topPath = YXConstant.STATICPATH;
		String filePaths = request.getParameter("filePaths");
		try {
			if (request.getParameter("path") != null)
				path = new String(request.getParameter("path").getBytes("ISO8859-1"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(filePaths)) {
			String[] fileNames = filePaths.split(",");
			if (fileNames != null && fileNames.length > 0) {
				for (String fileName : fileNames) {
					File delFile = new File(path + File.separator + fileName);
					if (delFile.exists()) {
						delFile.delete();
					}
				}
			}
		}
		if (path == null || "".equals(path)
				|| (!(path.replaceAll("\\\\", "/").startsWith(topPath)) && !path.startsWith(topPath)))
			path = topPath;
		File floder = new File(path);
		File[] files = floder.listFiles();
		FileList fList = new FileList();
		List<FileList> fileList = new ArrayList<FileList>();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			// System.out.println("Name is "+f.getName());
			FileList fl = new FileList();
			fl.setFileName(f.getName());
			fl.setFilePath(f.getPath());
			if (f.isDirectory()) {
				fl.setFileType("文件夹");
			} else {
				fl.setFileType(f.getName().substring(f.getName().lastIndexOf(".") + 1).toUpperCase());
			}
			if (f.isFile()) {
				long size = f.length();
				if (size > 1024 * 1024)
					fl.setFileSize(size / 1024 / 1024 + "MB");
				else if (size > 1024 && size <= 1024 * 1024)
					fl.setFileSize(size / 1024 + "KB");
				else
					fl.setFileSize(size + "byte");

			}
			Date date = new Date(f.lastModified());
			fl.setLastEditDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
			fileList.add(fl);
		}
		FileComparator comparator = new FileComparator();
		Collections.sort(fileList, comparator);
		fList.setTotalRows(fileList.size());
		int fromIndex = (Integer.parseInt(curPage) - 1) * Integer.parseInt(pageRows);
		int toIndex = (Integer.parseInt(curPage)) * Integer.parseInt(pageRows);
		if (fileList.size() < toIndex)
			toIndex = fileList.size();
		fList.setCurPage(Integer.parseInt(curPage));
		fileList = fileList.subList(fromIndex, toIndex);
		fList.setFileLists(fileList);
		fList.setParentPath(floder.getParent());
		fList.setFilePath(path);

		ModelAndView mv = new ModelAndView("/uploadFile/htmlList");
		mv.addObject("topPath", topPath);
		mv.addObject("fList", fList);
		return mv;
	}

}

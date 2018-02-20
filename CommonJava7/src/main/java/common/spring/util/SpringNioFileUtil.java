package common.spring.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import common.util.ResponseUtil;
import common.util.file.FileUtil;
import common.util.file.NioFileTypeUtil;
import common.util.file.NioFileUtil;

public class SpringNioFileUtil {

	private SpringNioFileUtil() {
		super();
	}

	private static final Logger logger = LoggerFactory.getLogger(SpringNioFileUtil.class);

	private static String getRandomString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	/**
	 * <pre>
	 * Spring 전용 파일 업로드
	 *  - Java 7 NIO API
	 *  - 확장자 체크 및 MIME Type 체크 선행 후 진행 권장
	 * </pre>
	 * @param multipartFile
	 * @param strDestFilePath
	 * @return
	 */
	public static Map<String, Object> uploadFileSpring(MultipartFile multipartFile, String strDestFilePath) {
		Map<String, Object> fileInfoMap = new HashMap<>();
		Path destFilePath = Paths.get(strDestFilePath);

		if ( !destFilePath.toFile().exists() ) {
			try {
				Files.createDirectories(destFilePath);
				
			} catch (IOException e) {
				logger.error("uploadFileSpringNio Exception", e);
			}
		}
		
		String saveFileName = getRandomString();
		String fileExtension = NioFileUtil.getFileExtension(multipartFile.getOriginalFilename());

		try {
			byte[] bytes = multipartFile.getBytes();
            Path path = Paths.get(strDestFilePath + File.separator + saveFileName + "." + fileExtension);
            Files.write(path, bytes);
            
		} catch (Exception e) {
			logger.error("uploadFileSpringNio Exception", e);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(saveFileName).append(".").append(fileExtension);

		String fileSize = FileUtil.readableFileSize(multipartFile.getSize());

		fileInfoMap.put("originalFilename", multipartFile.getOriginalFilename());
		fileInfoMap.put("saveFileName", sb.toString());
		fileInfoMap.put("fileSize", fileSize);

		return fileInfoMap;
	}
	
	/**
	 * Spring 전용 파일 다운로드
	 * @param filePath
	 * @param originalFilename
	 * @param saveFileName
	 * @param request
	 * @param response
	 */
	public static void downloadFileSpring(String filePath, String originalFilename, String saveFileName,
			HttpServletRequest request, HttpServletResponse response) {

		String originFileName = "";
		if ( StringUtils.isEmpty(originalFilename) ) {
			originFileName = ResponseUtil.contentDisposition(request, saveFileName);
		} else {
			originFileName = ResponseUtil.contentDisposition(request, originalFilename);
		}

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + originFileName + "\";");

		Path source = Paths.get(filePath + File.separator + saveFileName);

		try ( OutputStream os = response.getOutputStream() ) {
			Files.copy(source, os);

		} catch (Exception e) {
			logger.error("downloadFileNio Exception", e);
		}
	}
	
	/**
	 * Spring MultipartFile -> Java File 변환
	 * @param multipart
	 * @return
	 */
	public static File multipartToFile(MultipartFile multipart) {
		File convFile = new File(multipart.getOriginalFilename());
		
		try {
			multipart.transferTo(convFile);
			
		} catch (IllegalStateException | IOException e) {
			logger.error("multipartToFile IllegalStateException | IOException", e);
		}
		
        return convFile;
	}
	
	public static boolean isDocImageFile(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		
		String sExtension = NioFileUtil.getFileExtension(fileName);
		String sMimeType = file.getContentType();

		return NioFileTypeUtil.isDocFile(sExtension, sMimeType) || NioFileTypeUtil.isImgFile(sExtension, sMimeType);
	}

	public static boolean isDocFile(MultipartFile file) {
		String fileName = file.getName();
		
		String sExtension = NioFileUtil.getFileExtension(fileName);
		String sMimeType = file.getContentType();

		return NioFileTypeUtil.isDocFile(sExtension, sMimeType);
	}

	public static boolean isImageFile(MultipartFile file) {
		String fileName = file.getName();
		
		String sExtension = NioFileUtil.getFileExtension(fileName);
		String sMimeType = file.getContentType();

		return NioFileTypeUtil.isImgFile(sExtension, sMimeType);
	}
	
}

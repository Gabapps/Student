package com.gabapps.student;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.*;
import org.xml.sax.InputSource;

import android.util.Log;

interface OnDownloadingListener {
	public void onDownloadStarted();
	public void onDownloadProgressed(int progress); //Not working
	public void onDownloadFinished();
}

public class FTP {
	private static FTPClient client = new FTPClient();
	private static String server = "localhost";
	private static int port = 21;
	private static String login = "anonymous", password = "";
	private static boolean wasConnected = false;
	private static boolean isConnected = false;
	private static boolean hasDisconnected = false; //not working
	private static boolean isDownloading = false;
	private static OnDownloadingListener downloadingListener = null;


	private static void showServerReply() {
		String[] replies = client.getReplyStrings();
		if (replies != null && replies.length > 0) {
			for (String aReply : replies) {
				Log.i("FTPReply", "SERVER: " + aReply);
			}
		}
	}
	
	public static void setOnDownloadingListener(OnDownloadingListener value) {
		downloadingListener = value;
	}

	public static boolean isConnected()	 {
		isConnected = client.isConnected();

		if(wasConnected&&!isConnected&&!hasDisconnected) {
			hasDisconnected=true;
		}
		wasConnected = isConnected;
		return isConnected;
	}
	
	public static boolean isDownloading() {
		return isDownloading;
	}

	public static boolean hasDisconnected() {
		isConnected();
		boolean temp = hasDisconnected;
		hasDisconnected = false;
		return temp;
	}

	public static void setServer(String server, int port) {
		FTP.server = server;
		FTP.port = port;
	}

	public static void setUser(String username, String password) {
		FTP.login = username;
		FTP.password = password;
	}

	public static void connect() {

		try {
			client.connect(server, port);
			showServerReply();
			int replyCode = client.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				Log.e("FTPConnect", "Operation failed. Server reply code: " + replyCode);
			}
			else {
				Log.d("FTPConnect", "Connection done !");
			}
		} catch (IOException ex) {
			Log.e("FTPConnect", "Oops! Something wrong happened");
			ex.printStackTrace();
		}
	}

	public static void login() {
		try {
			boolean success = client.login(login, password);
			showServerReply();
			if (!success) {
				Log.d("FTPLogin", "Could not login to the server");
			} else {
				Log.d("FTPLogin", "LOGGED IN SERVER");
			}
		} catch (IOException ex) {
			Log.e("FTPLogin", "Oops! Something wrong happened");
			ex.printStackTrace();
		}
	}

	public static void disconnect() {
		try {
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("FTPDisconnect", "Oops! Something wrong happened");
		}
	}
	public static void logout() {
		try {
			client.logout();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("FTPLogout", "Oops! Something wrong happened");
		}
	}

	public static FTPFile[] listFTPFiles(String path) {
		FTPFile[] files;
		try {
			files = client.listFiles(path);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("FTPList", "Can't list files in the current folder");
			return null;
		}
		return files;
	}

	public static boolean downloadSingleFile(
			String remoteFilePath, String savePath) throws IOException {
		File downloadFile = new File(savePath);

		File parentDir = downloadFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdir();
		}

		OutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(downloadFile));
		InputStream inputStream = null;
		try {
			if(downloadingListener != null) downloadingListener.onDownloadStarted();

			FTPFile file = client.mlistFile(remoteFilePath);
			long totallen = file.getSize();
			
			client.setFileType(2); //BINARY FILE TYPE
			inputStream = client.retrieveFileStream(remoteFilePath);
			
			byte buf[] = new byte[4096];
			long len;
			
			long cumulatelen = 0;
			int dlpercent = 0;
			int lastdlpercent = 0;
			isDownloading = true;
			while((len=inputStream.read(buf))>0) {
				outputStream.write(buf,0,(int)len);

				
				cumulatelen+=len;
				dlpercent = (int)(100*cumulatelen/totallen);
				if(dlpercent-lastdlpercent > 2) {
					if(downloadingListener != null) downloadingListener.onDownloadProgressed(dlpercent);
					lastdlpercent=dlpercent;
				}
			}
			if(!client.completePendingCommand()) {
				client.logout();
				client.disconnect();
				Log.e("FTPDownload", "File transfer failed.");
				return false;
			}
			
			return true;
		} catch (IOException ex) {
			throw ex;
		} finally {
			isDownloading = false;
			if (outputStream != null) outputStream.close();
			if (inputStream != null) inputStream.close();
			if(downloadingListener != null) downloadingListener.onDownloadFinished();
		}
	}
	public static void downloadDirectory(FTPClient ftpClient, String parentDir,
			String currentDir, String saveDir) throws IOException {
		String dirToList = parentDir;
		if (!currentDir.equals("")) {
			dirToList += "/" + currentDir;
		}

		FTPFile[] subFiles = ftpClient.listFiles(dirToList);

		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = parentDir + "/" + currentDir + "/"
						+ currentFileName;
				if (currentDir.equals("")) {
					filePath = parentDir + "/" + currentFileName;
				}

				String newDirPath = saveDir + parentDir + File.separator
						+ currentDir + File.separator + currentFileName;
				if (currentDir.equals("")) {
					newDirPath = saveDir + parentDir + File.separator
							+ currentFileName;
				}

				if (aFile.isDirectory()) {
					// create the directory in saveDir
					File newDir = new File(newDirPath);
					boolean created = newDir.mkdirs();
					if (created) {
						Log.d("FTP", "CREATED the directory: " + newDirPath);
					} else {
						Log.d("FTP", "COULD NOT create the directory: " + newDirPath);
					}

					// download the sub directory
					downloadDirectory(ftpClient, dirToList, currentFileName,
							saveDir);
				} else {
					// download the file
					boolean success = downloadSingleFile(filePath,
							newDirPath);
					if (success) {
						Log.d("FTP", "DOWNLOADED the file: " + filePath);
					} else {
						Log.d("FTP", "COULD NOT download the file: "
								+ filePath);
					}
				}
			}
		}
	}
}

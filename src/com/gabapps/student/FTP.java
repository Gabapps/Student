package com.gabapps.student;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.net.ftp.*;

import android.util.Log;

public class FTP {
	private static FTPClient client = new FTPClient();
	private static String server = "localhost";
	private static int port = 21;
	public static boolean logged=false;
	private static String login = "anonymous", password = "";
	
	private static void showServerReply() {
        String[] replies = client.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
            	Log.d("FTP", "SERVER: " + aReply);
            }
        }
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
            	Log.d("FTP", "Operation failed. Server reply code: " + replyCode);
            }
            else {
            	Log.d("FTP", "Connection done !");
            }
        } catch (IOException ex) {
        	Log.d("FTP", "Oops! Something wrong happened");
            ex.printStackTrace();
        }
	}
	
	public static void login() {
    	try {
            boolean success = client.login(login, password);
            showServerReply();
            if (!success) {
            	Log.d("FTP", "Could not login to the server");
            } else {
            	logged=true;
            	Log.d("FTP", "LOGGED IN SERVER");
            }
        } catch (IOException ex) {
        	Log.d("FTP", "Oops! Something wrong happened");
            ex.printStackTrace();
        }
	}
	
	public static void disconnect() {
		new Thread(new Runnable() {
	        public void run() {
		        try {
					client.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("FTP", "Oops! Something wrong happened");
				}
	        }
		}).start();
		
	}
	public static void logout() {
		new Thread(new Runnable() {
	        public void run() {
	        	try {
	    			logged=false; // A revoir
	    			client.logout();
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    			Log.d("FTP", "Oops! Something wrong happened");
	    		}
	        }
		}).start();
		
	}
	
	public static FTPFile[] listFTPFiles(String path) {
		FTPFile[] files;
		try {
			files = client.listFiles(path);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("FTP", "Can't list files in the current folder");
			return null;
		}
		return files;
	}
	
	public static boolean downloadSingleFile(FTPClient ftpClient,
	        String remoteFilePath, String savePath) throws IOException {
	    File downloadFile = new File(savePath);
	     
	    File parentDir = downloadFile.getParentFile();
	    if (!parentDir.exists()) {
	        parentDir.mkdir();
	    }
	         
	    OutputStream outputStream = new BufferedOutputStream(
	            new FileOutputStream(downloadFile));
	    try {
	        ftpClient.setFileType(2); //BINARY FILE TYPE
	        return ftpClient.retrieveFile(remoteFilePath, outputStream);
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (outputStream != null) {
	            outputStream.close();
	        }
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
	                boolean success = downloadSingleFile(ftpClient, filePath,
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

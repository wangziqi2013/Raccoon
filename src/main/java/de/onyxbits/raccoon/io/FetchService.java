package de.onyxbits.raccoon.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.akdeniz.googleplaycrawler.DownloadData;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.App;

/**
 * Download an APK.
 * 
 * @author patrick
 * 
 */
public class FetchService implements Runnable {

	private int versionCode;
	private String appId;
	private Archive archive;
	private GooglePlayAPI service;
	private int offerType;
	private FetchListener callback;
	private boolean paid;
	private long received = 0;
	private boolean ignore_obb_flag = false;

	/**
	 * Create a new downloader
	 * 
	 * @param archive
	 *          archive to download to
	 * @param appId
	 *          app packagename
	 * @param versionCode
	 *          app version code
	 * @param offerType
	 *          app offertype
	 * @param paid
	 *          true if this is a paid application.
	 * @param callback
	 *          optional callback to notify about progress (may be null).
	 * @param ignore_obb_flag
	 *          Whether to ignore OBB files
	 */
	public FetchService(Archive archive, String appId, int versionCode, int offerType, boolean paid,
			FetchListener callback,
			boolean ignore_obb_flag) {
		if (callback == null) {
			throw new NullPointerException("A Callback is required!");
		}
		this.appId = appId;
		this.versionCode = versionCode;
		this.archive = archive;
		this.offerType = offerType;
		this.callback = callback;
		this.paid = paid;

		// If this flag is set we just ignore OBB files even if there are them
		this.ignore_obb_flag = ignore_obb_flag;

		return;
	}

	/**
	 * Create a new downloader without the ignore OBB flag
	 * 
	 * @param archive
	 *          archive to download to
	 * @param appId
	 *          app packagename
	 * @param versionCode
	 *          app version code
	 * @param offerType
	 *          app offertype
	 * @param paid
	 *          true if this is a paid application.
	 * @param callback
	 *          optional callback to notify about progress (may be null).
	 */
	public FetchService(Archive archive, String appId, int versionCode, int offerType, boolean paid,
			FetchListener callback) {
		if (callback == null) {
			throw new NullPointerException("A Callback is required!");
		}
		this.appId = appId;
		this.versionCode = versionCode;
		this.archive = archive;
		this.offerType = offerType;
		this.callback = callback;
		this.paid = paid;

		return;
	}

	public void run() {
		File appFile = null;
		File mainFile = null;
		File patchFile = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			service = App.createConnection(archive);
			DownloadData data = null;
			if (paid) {
				data = service.delivery(appId, versionCode, offerType);
			}
			else {
				data = service.download(appId, versionCode, offerType);
			}
			appFile = archive.fileUnder(appId, versionCode);
			mainFile = archive.fileExpansionUnder("main", appId, data.getMainFileVersion());
			patchFile = archive.fileExpansionUnder("patch", appId, data.getPatchFileVersion());

			appFile.getParentFile().mkdirs();
			out = new FileOutputStream(appFile);
			in = data.openApp();
			callback.onBeginFile(this,appFile);
			boolean keepApp = transfer(in, out);
			callback.onFinishFile(this,appFile);
			in.close();
			out.close();
			if (!keepApp) {
				appFile.delete();
				callback.onAborted(this);
				return;
			}

			// Inform the user that it's happening
			if(ignore_obb_flag == true) {
				System.out.println("\nIgnore OBB files");
			}

			in = data.openMainExpansion();
			// Download main expansion file - Note that we do not invoke
			// the function if ignore OBB flag is turned on
			if (in != null && 
			    !mainFile.exists() && 
					ignore_obb_flag == false) {
				out = new FileOutputStream(mainFile);
				callback.onBeginFile(this,mainFile);
				boolean keepMain = transfer(in, out);
				callback.onFinishFile(this,mainFile);
				in.close();
				out.close();
				if (!keepMain) {
					appFile.delete();
					mainFile.delete();
					callback.onAborted(this);
					return;
				}
			}

			// Do the same for patch OBB file. Also do not download if the 
			// ignore OBB flag is turned on
			in = data.openPatchExpansion();
			if (in != null && 
			    !patchFile.exists() && 
					ignore_obb_flag == false) {
				out = new FileOutputStream(patchFile);
				callback.onBeginFile(this,patchFile);
				boolean keepPatch = transfer(in, out);
				callback.onFinishFile(this,patchFile);
				in.close();
				out.close();
				if (!keepPatch) {
					appFile.delete();
					mainFile.delete();
					patchFile.delete();
					callback.onAborted(this);
					return;
				}
			}
			callback.onComplete(this);
		}
		catch (Exception e) {
			if (appFile != null) {
				appFile.delete();
			}
			if (mainFile != null) {
				mainFile.delete();
			}
			if (patchFile != null) {
				patchFile.delete();
			}
			callback.onFailure(this, e);
		}
	}

	/**
	 * Download from Google Play to file
	 * 
	 * @param in
	 *          source
	 * @param out
	 *          destination
	 * @return true to keep the file, false if the user has cancelled the download
	 * @throws Exception
	 *           if something goes wrong.
	 */
	private boolean transfer(InputStream in, OutputStream out) throws Exception {
		byte[] buffer = new byte[1024 * 16];
		int length;
		callback.onChunk(this, 0);
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
			received += length;
			if (callback.onChunk(this, received)) {
				return false;
			}
		}
		return true;
	}
}

package io.github.sergkhram.data.service;

import com.vaadin.flow.server.StreamResource;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import io.github.sergkhram.views.list.forms.DeviceForm;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;

@Service
public class DownloadService {
    AdbManager adbManager;
    IdbManager idbManager;
    private static final String ZIP_EXTENSION = ".zip";

    public DownloadService(AdbManager adbManager, IdbManager idbManager) {
        this.adbManager = adbManager;
        this.idbManager = idbManager;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class DownloadData {
        public File file;
        public StreamResource resource;
        public String error;
    }

    public DownloadData downloadFile(DeviceForm.DownloadFileEvent downloadFileEvent) throws FileNotFoundException {
        File file = downloadFileEvent.getDevice().getDeviceType().equals(DeviceType.ANDROID)
            ? adbManager.downloadFile(
                downloadFileEvent.getDevice(),
                downloadFileEvent.getDeviceDirectoryElement(),
                "./target"
            )
            : idbManager.downloadFile(
                downloadFileEvent.getDevice(),
                downloadFileEvent.getDeviceDirectoryElement(),
                downloadFileEvent.getIosPackageType(),
            "./target"
            );
        InputStream inputStream = new FileInputStream(
            file);
        StreamResource resource = new StreamResource(file.getName(), () -> inputStream);
        return new DownloadData(file, resource, null);
    }

    public DownloadData downloadFolder(DeviceForm.DownloadFileEvent downloadFileEvent) throws IOException {
        DownloadData downloadData = new DownloadData();
        File directory = downloadFileEvent.getDevice().getDeviceType().equals(DeviceType.ANDROID)
            ? adbManager.downloadFolder(
                downloadFileEvent.getDevice(),
                downloadFileEvent.getDeviceDirectoryElement(),
                "./target"
            )
            : idbManager.downloadFolder(
                downloadFileEvent.getDevice(),
                downloadFileEvent.getDeviceDirectoryElement(),
                downloadFileEvent.getIosPackageType(),
                "./target"
            );
        if(directory.exists()) {
            File zipFile = new File(directory + ZIP_EXTENSION);
            ZipUtil.pack(directory, zipFile);
            InputStream inputStream = new FileInputStream(
                zipFile);
            downloadData.resource = new StreamResource(zipFile.getName(), () -> inputStream);
            FileUtils.deleteDirectory(directory);
            downloadData.file = zipFile;
        } else {
            downloadData.error = "Empty directory!";
        }
        return downloadData;
    }
}
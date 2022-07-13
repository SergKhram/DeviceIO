package io.github.sergkhram.data.service;

import com.vaadin.flow.server.StreamResource;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import lombok.*;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.NoSuchElementException;

import static io.github.sergkhram.utils.Const.DEFAULT_DOWNLOAD_PATH;

@Service
public class DownloadService {
    AdbManager adbManager;
    IdbManager idbManager;
    private static final String ZIP_EXTENSION = ".zip";

    public DownloadService(AdbManager adbManager, IdbManager idbManager) {
        this.adbManager = adbManager;
        this.idbManager = idbManager;
    }

    @Builder
    @Getter
    public static class DownloadResponseData {
        private final File file;
        private final StreamResource resource;
    }

    @Builder
    @Getter
    public static class DownloadRequestData {
        private final Device device;
        private final DeviceDirectoryElement deviceDirectoryElement;
        private final IOSPackageType iosPackageType;
    }

    public DownloadResponseData downloadFile(DownloadRequestData downloadRequestData) throws FileNotFoundException {
        File file = downloadRequestData.getDevice().getOsType().equals(OsType.ANDROID)
            ? adbManager.downloadFile(
            downloadRequestData.getDevice(),
            downloadRequestData.getDeviceDirectoryElement(),
            DEFAULT_DOWNLOAD_PATH
        )
            : idbManager.downloadFile(
            downloadRequestData.getDevice(),
            downloadRequestData.getDeviceDirectoryElement(),
            downloadRequestData.getIosPackageType(),
            DEFAULT_DOWNLOAD_PATH
        );
        InputStream inputStream = new FileInputStream(
            file);
        StreamResource resource = new StreamResource(file.getName(), () -> inputStream);
        return DownloadResponseData
            .builder()
            .file(file)
            .resource(resource)
            .build();
    }

    public DownloadResponseData downloadFolder(DownloadRequestData downloadRequestData)
        throws IOException, NoSuchElementException
    {
        File directory = downloadRequestData.getDevice().getOsType().equals(OsType.ANDROID)
            ? adbManager.downloadFolder(
            downloadRequestData.getDevice(),
            downloadRequestData.getDeviceDirectoryElement(),
            DEFAULT_DOWNLOAD_PATH
        )
            : idbManager.downloadFolder(
            downloadRequestData.getDevice(),
            downloadRequestData.getDeviceDirectoryElement(),
            downloadRequestData.getIosPackageType(),
            DEFAULT_DOWNLOAD_PATH
        );
        if (directory.exists()) {
            File zipFile = new File(directory + ZIP_EXTENSION);
            ZipUtil.pack(directory, zipFile);
            InputStream inputStream = new FileInputStream(
                zipFile);
            FileUtils.deleteDirectory(directory);
            return DownloadResponseData
                .builder()
                .file(zipFile)
                .resource(new StreamResource(zipFile.getName(), () -> inputStream))
                .build();
        } else {
            throw new NoSuchElementException("Empty directory!");
        }
    }
}

package io.github.sergkhram.managers.idb

import io.github.sergkhram.data.entity.AppDescription
import io.github.sergkhram.data.entity.Device
import io.github.sergkhram.data.entity.DeviceDirectoryElement
import io.github.sergkhram.data.entity.Host
import io.github.sergkhram.data.enums.DeviceType
import io.github.sergkhram.data.enums.IOSPackageType
import io.github.sergkhram.data.enums.OsType
import io.github.sergkhram.idbClient.IOSDebugBridgeClient
import io.github.sergkhram.idbClient.entities.address.DomainSocketAddress
import io.github.sergkhram.idbClient.entities.address.TcpAddress
import io.github.sergkhram.idbClient.entities.requestsBody.files.ContainerKind
import io.github.sergkhram.idbClient.entities.requestsBody.files.FileContainer
import io.github.sergkhram.idbClient.entities.requestsBody.files.LsRequestBody
import io.github.sergkhram.idbClient.entities.response.DescribeKtResponse
import io.github.sergkhram.idbClient.requests.app.ListAppsRequest
import io.github.sergkhram.idbClient.requests.files.LsRequest
import io.github.sergkhram.idbClient.requests.files.PullRequest
import io.github.sergkhram.idbClient.requests.media.ScreenshotRequest
import io.github.sergkhram.idbClient.util.exportFile
import io.github.sergkhram.managers.Manager
import io.github.sergkhram.managers.adb.Logger
import kotlinx.coroutines.*
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import java.io.File
import java.util.stream.Collectors

@Service
@Slf4j
open class IdbKtManager: Manager {
    val idb: IOSDebugBridgeClient = IOSDebugBridgeClient()

    companion object : Logger()

    override fun getListOfDevices(host: Host?): List<Device> {
        return runBlocking {
            val response = idb.getTargetsList()
            return@runBlocking if(host == null) convert(response, null) ?: emptyList()
            else convert(
                response.filter {
                    if(it.address is TcpAddress) {
                        (it.address as TcpAddress).host.contains(host.address)
                    } else {
                        (it.address as DomainSocketAddress).path.contains(host.address)
                    }
                },
                host
            ) ?: emptyList()
        }
    }

    override fun connectToHost(host: String?, port: Int?) {
        runBlocking {
            host?.let {
                idb.connectToCompanion(
                    TcpAddress(host, port ?: 0)
                )
            }
        }
    }

    override fun disconnectHost(host: String?, port: Int?) {
        if(host!=null && port!=null) {
            idb.disconnectCompanion(
                TcpAddress(host, port)
            )
        }
    }

    override fun getDevicesStates(): Map<String, String> {
        val devicesMap: HashMap<String, String> = HashMap()
        val devices = getListOfDevices(null)
        devicesMap.putAll(devices.map { Pair(it.serial, it.state) })
        return devicesMap
    }

    override fun rebootDevice(device: Device?) {
        TODO("Not yet implemented")
    }

    override fun makeScreenshot(device: Device?, filePath: String): File {
        device?.let {
            runBlocking {
                idb.execute(
                    ScreenshotRequest(),
                    it.serial
                ).exportFile(filePath)
            }
        }
        return File(filePath)
    }

    override fun getAppsList(device: Device?): List<AppDescription> {
        return device?.let {
            runBlocking {
                idb.execute(
                    ListAppsRequest(),
                    it.serial
                ).appsList.map {
                    AppDescription(
                        it.bundleId,
                        it.name,
                        "",
                        it.processState.name,
                        it.processState.name.contains("Running")
                    )
                }
            }
        } ?: emptyList()
    }

    private fun convert(iosDevices: List<DescribeKtResponse>, host: Host?): List<Device>? {
        return iosDevices.parallelStream().map {
            val device = Device()
            device.serial = it.targetDescription.udid
            device.state = it.targetDescription.state
            device.isActive = it.targetDescription.state != "Shutdown"
            device.host = host
            device.osType = OsType.IOS
            device.name = it.targetDescription.name
            device.deviceType = DeviceType.valueOf(it.targetDescription.targetType.uppercase())
            device.osVersion = it.targetDescription.osVersion
            device
        }.collect(Collectors.toList())
    }

    open fun downloadFile(
        device: Device,
        deviceDirectoryElement: DeviceDirectoryElement,
        iosPackageType: IOSPackageType,
        destination: String
    ): File {
        val file = File(destination + File.separator + deviceDirectoryElement.name)
        return runBlocking {
            idb.execute(
                PullRequest(
                    deviceDirectoryElement.path + "/" + deviceDirectoryElement.name,
                    FileContainer(
                        kind = ContainerKind.valueOf(iosPackageType.name)
                    )
                ),
                device.serial
            ).exportFile(file.absolutePath + ".gz")
        }
    }

    open fun downloadFolder(
        device: Device,
        deviceDirectoryElement: DeviceDirectoryElement,
        iosPackageType: IOSPackageType,
        destination: String
    ): File {
        val file = File(destination + File.separator + deviceDirectoryElement.name)
        return runBlocking {
            idb.execute(
                PullRequest(
                    deviceDirectoryElement.path + "/" + deviceDirectoryElement.name,
                    FileContainer(
                        kind = ContainerKind.valueOf(iosPackageType.name)
                    )
                ),
                device.serial
            ).exportFile(file.absolutePath + ".gz")
        }
    }

    open fun getListFiles(device: Device,
                     path: String,
                     iosPackageType: IOSPackageType
    ): List<DeviceDirectoryElement> {
        return runBlocking {
            val response = idb.execute(
                LsRequest(
                    LsRequestBody.SingleLsRequestBody(
                        path,
                        FileContainer(kind = ContainerKind.valueOf(iosPackageType.name))
                    )
                ),
                device.serial
            )
            response.filesList.map {
                file -> DeviceDirectoryElement().apply {
                    name = file.path
                    this.path = path
                }
            }
        }
    }
}
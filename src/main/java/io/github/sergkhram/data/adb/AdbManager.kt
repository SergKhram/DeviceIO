package io.github.sergkhram.data.adb

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.interactor.StopAdbInteractor
import com.malinskiy.adam.request.Feature
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.framebuffer.RawImageScreenCaptureAdapter
import com.malinskiy.adam.request.framebuffer.ScreenCaptureRequest
import com.malinskiy.adam.request.misc.ConnectDeviceRequest
import com.malinskiy.adam.request.misc.DisconnectDeviceRequest
import com.malinskiy.adam.request.misc.RebootRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandResult
import com.malinskiy.adam.request.sync.PullRequest
import com.malinskiy.adam.request.sync.v1.ListFileRequest
import com.malinskiy.adam.request.sync.v1.PullFileRequest
import io.github.sergkhram.data.entity.DeviceDirectoryElement
import io.github.sergkhram.data.entity.Host
import io.github.sergkhram.utils.Const
import io.github.sergkhram.utils.Const.LOCAL_HOST
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import org.springframework.stereotype.Service
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.IOException
import javax.annotation.PreDestroy
import javax.imageio.ImageIO
import io.github.sergkhram.data.entity.Device as DeviceEntity

@Service
class AdbManager {
    private var adb: AndroidDebugBridgeClient? = null
    private val DEFAULT_PORT: Int = 5555
    private val supportedFeatures = listOf(Feature.STAT_V2, Feature.LS_V2, Feature.SENDRECV_V2)

    init {
        initAdbClient()
        startAdb()
    }

    @PreDestroy
    fun stopAdb() {
        runBlocking {
            println("Stopping adb")
            StopAdbInteractor().execute()
        }
    }

    fun startAdb(androidHomePath: String? = null) {
        runBlocking {
            println("Starting adb")
            androidHomePath?.let {
                StartAdbInteractor().execute(androidHome = File(androidHomePath))
            } ?: StartAdbInteractor().execute()
        }
    }

    fun initAdbClient() {
        adb = AndroidDebugBridgeClientFactory().build()
    }

    fun reinitializeAdb(adbPath: String) {
        stopAdb()
        startAdb(adbPath)
    }

    fun connectToDevice(address: String, port: Integer?) {
        runBlocking {
            if(address != LOCAL_HOST) {
                withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                    adb?.execute(ConnectDeviceRequest(address, port?.toInt() ?: DEFAULT_PORT))
                }
            }
        }
    }

    fun disconnectDevice(address: String, port: Integer?) {
        runBlocking {
            if(address != LOCAL_HOST) {
                withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                    adb?.execute(DisconnectDeviceRequest(address, port?.toInt() ?: DEFAULT_PORT))
                }
            }
        }
    }

    fun getListOfDevices(host: Host? = null): List<DeviceEntity> {
        lateinit var listOfDevices: List<Device>
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                val devices: List<Device>? = adb?.execute(request = ListDevicesRequest())
                listOfDevices = host?.let { host ->
                    if (!host.address.equals(LOCAL_HOST)) {
                        devices?.filter {
                            it.serial.contains(host.address)
                        }
                    } else {
                        devices?.filter {
                            !it.serial.contains(":")
                        }
                    }
                } ?: devices ?: emptyList()
            }
        }
        return listOfDevices.convert(host)
    }

    private fun List<Device>.convert(host: Host? = null): List<DeviceEntity> {
        return this.map {
            val finalDevice = DeviceEntity()
            finalDevice.host = host
            finalDevice.isActive = true
            finalDevice.name = it.serial
            finalDevice.state = it.state.name
            return@map finalDevice
        }
    }

    fun rebootDevice(device: DeviceEntity) {
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                adb?.execute(request = RebootRequest(), serial = device.name)
            }
        }
    }

    fun executeShell(device: DeviceEntity, cmd: String): String {
        var response: ShellCommandResult?
        runBlocking {
            response = withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                 adb?.execute(
                    request = ShellCommandRequest(cmd),
                    serial = device.name
                )
            }
        }
        if(response==null) return ""
        return if(response!!.exitCode == 0) response!!.stdout else response!!.stderr
    }

    fun getDeviceMonitoringChannel(): ReceiveChannel<List<Device>>? {
        return adb?.execute(
            request = AsyncDeviceMonitorRequest(),
            scope = GlobalScope
        )
    }

    fun getDevicesStates(): Map<String, String> {
        val devicesMap = mutableMapOf<String, String>()
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                val channel = getDeviceMonitoringChannel()
                val devices = channel?.receive()
                devices?.forEach {
                    devicesMap[it.serial] = it.state.name
                }
                channel?.cancel()
            }
        }
        return devicesMap
    }

    fun makeScreenshot(serial: String, fileName: String): File {
        return runBlocking {
            val adapter = RawImageScreenCaptureAdapter()
            val image = adb?.execute(
                request = ScreenCaptureRequest(adapter),
                serial = serial
            )?.toBufferedImage()

            val imageFile = File("/target/$fileName.png");
            if (!ImageIO.write(image, "png", imageFile)) {
                throw IOException("Failed to find png writer")
            }
            return@runBlocking imageFile
        }
    }

    fun getListFiles(device: DeviceEntity, path: String): List<DeviceDirectoryElement> {
        val list: MutableList<DeviceDirectoryElement> = mutableListOf()
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                list.addAll(
                    adb?.execute(
                        ListFileRequest(path),
                        device.name
                    )?.filter {
                        it.name != "." && it.name != ".."
                    }?.map {
                        DeviceDirectoryElement().apply {
                            this.isDirectory = it.isDirectory()
                            this.name = it.name
                            this.path = path
                            this.size = it.size.toString()
                        }
                    } ?: emptyList()
                )
            }
        }
        return list
    }

    fun downloadFile(device: DeviceEntity, deviceDirectoryElement: DeviceDirectoryElement, destination: String): File {
        var file = File(destination + File.separator + deviceDirectoryElement.name)
        runBlocking {
            withTimeoutOrNull(30000) {
                val pullDevicesRequest = PullFileRequest(
                    deviceDirectoryElement.path + "/" + deviceDirectoryElement.name,
                    file,
                    coroutineContext = this.coroutineContext)
                val channel = adb!!.execute(
                    pullDevicesRequest,
                    this,
                    device.name
                )

                for (percentageDouble in channel) {
                    println("Downloading ${file.name}==========${percentageDouble * 100}")
                }
            }
        }
        return file
    }

    fun downloadFolder(device: DeviceEntity, deviceDirectoryElement: DeviceDirectoryElement, destination: String): File {
        var file = File(destination + File.separator + deviceDirectoryElement.name)
        runBlocking {
            withTimeoutOrNull(30000) {
                val pullRequest = PullRequest(
                    deviceDirectoryElement.path + "/" + deviceDirectoryElement.name,
                    file,
                    coroutineContext = this.coroutineContext,
                    supportedFeatures = listOf()
                )
                adb?.let {
                    pullRequest.execute(it, device.name)
                }
            }
        }
        return file
    }
}
package io.github.sergkhram.managers.adb

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
import com.malinskiy.adam.request.prop.GetPropRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandRequest
import com.malinskiy.adam.request.shell.v2.ShellCommandResult
import com.malinskiy.adam.request.sync.PullRequest
import com.malinskiy.adam.request.sync.v1.ListFileRequest
import com.malinskiy.adam.request.sync.v1.PullFileRequest
import io.github.sergkhram.data.entity.DeviceDirectoryElement
import io.github.sergkhram.data.entity.Host
import io.github.sergkhram.data.enums.OsType
import io.github.sergkhram.managers.Manager
import io.github.sergkhram.utils.Const
import io.github.sergkhram.utils.Const.LOCAL_HOST
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.PreDestroy
import javax.imageio.ImageIO
import io.github.sergkhram.data.entity.Device as DeviceEntity

@Service
@Slf4j
class AdbManager: Manager {
    private var adb: AndroidDebugBridgeClient? = null
    private val DEFAULT_PORT: Int = 5555
    private val supportedFeatures = listOf(Feature.STAT_V2, Feature.LS_V2, Feature.SENDRECV_V2)
    companion object : Logger()

    init {
        initAdbClient()
        startAdb()
    }

    @PreDestroy
    fun stopAdb() {
        runBlocking {
            log.info("Stopping adb")
            StopAdbInteractor().execute()
        }
    }

    fun startAdb(androidHomePath: String? = null) {
        runBlocking {
            log.info("Starting adb")
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

    override fun connectToHost(host: String?, port: Int?) {
        host?.let {
            runBlocking {
                if(host != LOCAL_HOST) {
                    withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                        val processUuid = UUID.randomUUID()
                        log.info("[$processUuid] Connecting to host ${host + port} process started")
                        adb?.execute(ConnectDeviceRequest(host, port ?: DEFAULT_PORT))
                        log.info("[$processUuid] Connecting to host ${host + port} process finished")
                    }
                }
            }
        }
    }

    override fun disconnectHost(host: String?, port: Int?) {
        host?.let {
            runBlocking {
                if(host != LOCAL_HOST) {
                    withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                        val processUuid = UUID.randomUUID()
                        log.info("[$processUuid] Disconnecting host ${host + port} process started")
                        adb?.execute(DisconnectDeviceRequest(host, port ?: DEFAULT_PORT))
                        log.info("[$processUuid] Disconnecting host ${host + port} process finished")
                    }
                }
            }
        }
    }

    override fun getListOfDevices(host: Host?): List<DeviceEntity> {
        lateinit var listOfDevices: List<Device>
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                val processUuid = UUID.randomUUID()
                log.info("[$processUuid] Get list of devices process started")
                val devices: List<Device>? = adb?.execute(request = ListDevicesRequest())
                log.info("[$processUuid] Get list of devices process finished")

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
        val list = this
        return runBlocking(Dispatchers.Default) {
            list.pmap {
                return@pmap DeviceEntity().apply {
                    this.host = host
                    this.isActive = true
                    this.serial = it.serial
                    this.state = it.state.name
                    this.osType = OsType.ANDROID
                    var name: String? = null
                    log.info("Receiving ${it.serial} device info started")
                    adb?.execute(
                        request = GetPropRequest(),
                        serial = it.serial
                    )?.let {
                        val keys = it.keys
                        if (keys.contains("ro.config.marketing_name")) {
                            name = it["ro.config.marketing_name"]
                        } else if (keys.contains("ro.kernel.qemu.avd_name")) {
                            name = it["ro.kernel.qemu.avd_name"]
                        }
                    }
                    log.info("Receiving ${it.serial} device info finished")
                    this.name = name.orEmpty()
                }
            }
        }
    }

    override fun rebootDevice(device: DeviceEntity) {
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                val processUuid = UUID.randomUUID()
                log.info("[$processUuid] Reboot/boot device ${device.serial} process started")
                adb?.execute(request = RebootRequest(), serial = device.serial)
                log.info("[$processUuid] Reboot/boot device ${device.serial} process finished")
            }
        }
    }

    fun executeShell(device: DeviceEntity, cmd: String): String {
        var response: ShellCommandResult?
        runBlocking {
            response = withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                val processUuid = UUID.randomUUID()
                log.info("[$processUuid] Executing shell request '$cmd' for device ${device.serial}")
                return@withTimeoutOrNull adb?.execute(
                    request = ShellCommandRequest(cmd),
                    serial = device.serial
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

    override fun getDevicesStates(): Map<String, String> {
        val devicesMap = mutableMapOf<String, String>()
        runBlocking {
            withTimeoutOrNull(Const.TIMEOUT.toLong()) {
                val processUuid = UUID.randomUUID()
                log.info("[$processUuid] Get devices states process started")
                val channel = getDeviceMonitoringChannel()
                val devices = channel?.receive()
                devices?.forEach {
                    devicesMap[it.serial] = it.state.name
                }
                channel?.cancel()
                log.info("[$processUuid] Get devices states process finished")
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
                val processUuid = UUID.randomUUID()
                log.debug("[$processUuid] Get list of files process for path '$path' of device ${device.serial} started")
                list.addAll(
                    adb?.execute(
                        ListFileRequest(path),
                        device.serial
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
                log.debug("[$processUuid] Get list of files process for path '$path' of device ${device.serial} finished")
            }
        }
        return list
    }

    fun downloadFile(device: DeviceEntity, deviceDirectoryElement: DeviceDirectoryElement, destination: String): File {
        val file = File(destination + File.separator + deviceDirectoryElement.name)
        runBlocking {
            withTimeoutOrNull(30000) {
                val processUuid = UUID.randomUUID()
                val parentPath = if(deviceDirectoryElement.path.equals("/")) "/" else deviceDirectoryElement.path + "/"
                log.info(
                    "[$processUuid] Download '${parentPath + deviceDirectoryElement.name }' file process for " +
                            "device ${device.serial} started"
                )
                val pullDevicesRequest = PullFileRequest(
                    parentPath + deviceDirectoryElement.name,
                    file,
                    coroutineContext = this.coroutineContext)
                val channel = adb!!.execute(
                    pullDevicesRequest,
                    this,
                    device.serial
                )

                for (percentageDouble in channel) {
                    log.info("[$processUuid] Downloading ${file.name}==========${percentageDouble * 100}")
                }
                log.info(
                    "[$processUuid] Download '${parentPath + deviceDirectoryElement.name }' file process for " +
                            "device ${device.serial} finished"
                )
            }
        }
        return file
    }

    fun downloadFolder(device: DeviceEntity, deviceDirectoryElement: DeviceDirectoryElement, destination: String): File {
        val file = File(destination + File.separator + deviceDirectoryElement.name)
        runBlocking {
            withTimeoutOrNull(30000) {
                val processUuid = UUID.randomUUID()
                val parentPath = if(deviceDirectoryElement.path.equals("/")) "/" else deviceDirectoryElement.path + "/"
                log.info(
                    "[$processUuid] Download '${parentPath + deviceDirectoryElement.name }' folder process for " +
                            "device ${device.serial} started"
                )
                val pullRequest = PullRequest(
                    parentPath + deviceDirectoryElement.name,
                    file,
                    coroutineContext = this.coroutineContext,
                    supportedFeatures = listOf()
                )
                adb?.let {
                    pullRequest.execute(it, device.serial)
                }
                log.info(
                    "[$processUuid] Download '${parentPath + deviceDirectoryElement.name }' folder process for " +
                            "device ${device.serial} finished"
                )
            }
        }
        return file
    }

    suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
        map { async { f(it) } }.awaitAll()
    }
}
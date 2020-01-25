package com.futuretech.nfmovies.Utils

import android.util.Log
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.ProtocolInfo
import org.fourthline.cling.support.model.Res
import org.fourthline.cling.support.model.item.VideoItem
import org.seamless.util.MimeType

import java.text.SimpleDateFormat
import java.util.*

object DLNAUtil {

    private const val DIDL_LITE_FOOTER = "</DIDL-Lite>"
    private const val DIDL_LITE_HEADER = "<?xml version=\"1.0\"?>" +
            "<DIDL-Lite " + "xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" " +
            "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">"


    class DeviceDisplay(device: Device<*, *, *>) {

        var device: Device<*, *, *>
            internal set

        init {
            this.device = device
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as DeviceDisplay?
            return device == that!!.device
        }

        override fun hashCode(): Int {
            return device.hashCode()
        }

        override fun toString(): String {
            val name = if (device.details != null && device.details.friendlyName != null)
                device.details.friendlyName
            else
                device.displayString
            // Display a little star while the device is being loaded (see performance optimization earlier)
            return if (device.isFullyHydrated) name else "$name *"
        }
    }

    private fun createItemMetadata(item: DIDLObject): String {
        val metadata = StringBuilder()
        metadata.append(DIDL_LITE_HEADER)

        metadata.append(String.format("<item id=\"%s\" parentID=\"%s\" restricted=\"%s\">", item.id, item.parentID, if (item.isRestricted) "1" else "0"))

        metadata.append(String.format("<dc:title>%s</dc:title>", item.title))
        var creator: String? = item.creator
        if (creator != null) {
            creator = creator.replace("<".toRegex(), "_")
            creator = creator.replace(">".toRegex(), "_")
        }
        metadata.append(String.format("<upnp:artist>%s</upnp:artist>", creator))
        metadata.append(String.format("<upnp:class>%s</upnp:class>", item.clazz.value))

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val now = Date()
        val time = sdf.format(now)
        metadata.append(String.format("<dc:date>%s</dc:date>", time))

        val res = item.firstResource
        if (res != null) {
            // protocol info
            var protocolinfo = ""
            val pi = res.protocolInfo
            if (pi != null) {
                protocolinfo = String.format("protocolInfo=\"%s:%s:%s:%s\"", pi.protocol, pi.network, pi.contentFormatMimeType, pi
                        .additionalInfo)
            }
            Log.e("TAG", "protocolinfo: $protocolinfo")

            // resolution, extra info, not adding yet
            var resolution = ""
            if (res.resolution != null && res.resolution.isNotEmpty()) {
                resolution = String.format("resolution=\"%s\"", res.resolution)
            }

            // duration
            var duration = ""
            if (res.duration != null && res.duration.isNotEmpty()) {
                duration = String.format("duration=\"%s\"", res.duration)
            }

            // res begin
            //            metadata.append(String.format("<res %s>", protocolinfo)); // no resolution & duration yet
            metadata.append(String.format("<res %s %s %s>", protocolinfo, resolution, duration))

            // url
            val url = res.value
            metadata.append(url)

            // res end
            metadata.append("</res>")
        }
        metadata.append("</item>")

        metadata.append(DIDL_LITE_FOOTER)

        return metadata.toString()
    }

    fun pushMediaToRender(url: String, id: String, name: String, duration: String): String {
        val size: Long = 0
        val bitrate: Long = 0
        val res = Res(MimeType(ProtocolInfo.WILDCARD, ProtocolInfo.WILDCARD), size, url)

        val creator = "unknown"
        val resolution = "unknown"
        var metadata: String? = null

        val videoItem = VideoItem(id, "0", name, creator, res)
        metadata = createItemMetadata(videoItem)
        Log.i("tag", metadata.toString())
        Log.e("TAG", "metadata: $metadata")
        return metadata
    }
}


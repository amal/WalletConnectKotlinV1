package org.walletconnect.impls

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.*
import org.walletconnect.Session
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class OkHttpTransport(
    private val client: OkHttpClient,
    private val serverUrl: String,
    private val statusHandler: (Session.Transport.Status) -> Unit,
    private val messageHandler: (Session.Transport.Message) -> Unit,
    moshi: Moshi
) : Session.Transport, WebSocketListener() {

    private val adapter = moshi.adapter<Map<String, Any>>(
        Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Any::class.java
        )
    )

    private val socketLock = Any()
    private var socket: WebSocket? = null
    private var connected: Boolean = false
    private val queue: Queue<Session.Transport.Message> = ConcurrentLinkedQueue()

    override fun isConnected(): Boolean = connected

    override fun connect(): Boolean {
        synchronized(socketLock) {
            socket ?: run {
                connected = false
                val bridgeWS = serverUrl.replace("https://", "wss://").replace("http://", "ws://")
                return tryExec { socket = client.newWebSocket(Request.Builder().url(bridgeWS).build(), this) }
            }
        }
        return false
    }

    override fun send(message: Session.Transport.Message) {
        queue.offer(message)
        drainQueue()
    }

    private fun drainQueue() {
        if (connected) {
            socket?.let { s ->
                queue.poll()?.let {
                    tryExec {
                        s.send(adapter.toJson(it.toMap()))
                    }
                    drainQueue() // continue draining until there are no more messages
                }
            }
        } else {
            connect()
        }
    }

    private fun Session.Transport.Message.toMap() =
        mapOf(
            "topic" to topic,
            "type" to type,
            "payload" to payload,
            "silent" to true
        )

    override fun close() {
        tryExec { socket?.close(1000, null) }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        connected = true
        drainQueue()
        statusHandler(Session.Transport.Status.Connected)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        tryExec {
            adapter.fromJson(text)?.toMessage()?.let { messageHandler(it) }
        }
    }

    private fun Map<String, Any>.toMessage(): Session.Transport.Message? {
        val topic = get("topic")?.toString() ?: return null
        val type = get("type")?.toString() ?: return null
        val payload = get("payload")?.toString() ?: return null
        return Session.Transport.Message(topic, type, payload)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        socket = null
        connected = false
        statusHandler(Session.Transport.Status.ConnectionFailed(t, response?.code))
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        socket = null
        connected = false
        statusHandler(Session.Transport.Status.ConnectionClosed(Throwable(reason), code))
    }

    private fun tryExec(block: () -> Unit): Boolean {
        return try {
            block()
            true
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            statusHandler(Session.Transport.Status.Error(e))
            false
        }
    }

    class Builder(private val client: OkHttpClient, private val moshi: Moshi) :
        Session.Transport.Builder {
        override fun build(
            url: String,
            statusHandler: (Session.Transport.Status) -> Unit,
            messageHandler: (Session.Transport.Message) -> Unit,
        ): Session.Transport =
            OkHttpTransport(client, url, statusHandler, messageHandler, moshi)

    }

}

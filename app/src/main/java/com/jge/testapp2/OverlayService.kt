package com.jge.testapp2
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.app.*
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.allViews
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue
import kotlin.system.exitProcess
import android.os.Looper;


class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var NOTI_ID = 1

    private lateinit var mediaProjection: MediaProjection
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var imageReader: ImageReader
    private lateinit var handler: Handler
    private val toastHandler = Handler(Looper.getMainLooper())

    private lateinit var tagLinearLayout: LinearLayout
    private lateinit var activeTagLinearLayout: LinearLayout

    private var selectedTag = 0

    private var isRecognizing: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun createNotification(): Notification {
        // 알림 표시
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                "default",
                "기본 채널",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val notificationIntent = Intent(this, ExitActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.mipmap.appicon)
            .setContentTitle("공채계산기 작동 중")
            .setContentText("알림 누르거나 결과 화면에서 X 버튼 누를 경우 앱 종료")
            .setContentIntent(pendingIntent) // 알림 클릭 시 이동
            .setOngoing(true)

        val notification = builder.build()
        notification.flags = Notification.FLAG_ONGOING_EVENT

        return notification
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_main_view, null)

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.TOP or Gravity.START

        addEvents(params)

        addButton()

        Loader.serv = this

        windowManager.addView(overlayView, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(NOTI_ID, createNotification(), FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_OK) ?: Activity.RESULT_OK
        val data = intent?.getParcelableExtra<Intent>("data")
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data!!)

        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                // MediaProjection이 중지되었을 때 리소스 해제
                stopSelf()
            }
        }, null)

        setupMediaProjection()


        return START_NOT_STICKY
    }

    private fun setupMediaProjection() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)

        val handlerThread = HandlerThread("ScreenCapture")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "OverlayService",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            object : VirtualDisplay.Callback() {
                override fun onPaused() {
                    // Handle pause
                }

                override fun onResumed() {
                    // Handle resume
                }

                override fun onStopped() {
                    // Handle stop
                }
            },
            handler
        )
    }

    /* 인식 결과로 한번에 여러 개의 태그 데이터 들어올 경우 */
    private fun onTagClick(selectedTextView: List<TextView>, isError: Boolean = false) {
        selectedTextView.forEach {
            onTagClick(it, isError)
        }

        val calculatror = Calculatror()

        makeList(calculatror.getResultData(selectedTag))
    }

    /* 태그 하나씩 누를 경우 */
    private fun onTagClick(selectedTextViwe: TextView, isError: Boolean = false) {
        val tag = Loader.tagToInt(selectedTextViwe.text.toString())
        val isActive = selectedTag and tag == tag

        if (isActive) {
            getTextView(selectedTextViwe.text.toString(), true, isError)
            selectedTag -= tag
        } else {
            getTextView(selectedTextViwe.text.toString(), false, isError)
            selectedTag += tag
        }
    }

    /* 태그 눌렀을 때 숨김 처리 */
    private fun getTextView(text: String, isActive: Boolean, isError: Boolean = false) {
        var view: TextView?
        var activeView: TextView?

        when (text) {
            "신입" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activetlsdlq)
                view = tagLinearLayout.findViewById(R.id.tlsdlq)
            }

            "특별채용" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activexmrco)
                view = tagLinearLayout.findViewById(R.id.xmrco)
            }

            "고급특별채용" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activerhxmrco)
                view = tagLinearLayout.findViewById(R.id.rhxmrco)
            }

            "근거리" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activermsrjfl)
                view = tagLinearLayout.findViewById(R.id.rmsrjfl)
            }

            "원거리" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activednjsrjfl)
                view = tagLinearLayout.findViewById(R.id.dnjsrjfl)
            }

            "가드" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activerkem)
                view = tagLinearLayout.findViewById(R.id.rkem)
            }

            "디펜더" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeelvpsej)
                view = tagLinearLayout.findViewById(R.id.elvpsej)
            }

            "메딕" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeapelr)
                view = tagLinearLayout.findViewById(R.id.apelr)
            }

            "뱅가드" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeqodrkem)
                view = tagLinearLayout.findViewById(R.id.qodrkem)
            }

            "서포터" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activetjvhxj)
                view = tagLinearLayout.findViewById(R.id.tjvhxj)
            }

            "스나이퍼" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activetmskdlvj)
                view = tagLinearLayout.findViewById(R.id.tmskdlvj)
            }

            "스페셜리스트" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activetmvptuffltmxm)
                view = tagLinearLayout.findViewById(R.id.tmvptuffltmxm)
            }

            "캐스터" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activezotmxj)
                view = tagLinearLayout.findViewById(R.id.zotmxj)
            }

            "감속" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activerkathr)
                view = tagLinearLayout.findViewById(R.id.rkathr)
            }

            "강제이동" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activerkdwpdlehd)
                view = tagLinearLayout.findViewById(R.id.rkdwpdlehd)
            }

            "누커" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activesnzj)
                view = tagLinearLayout.findViewById(R.id.snzj)
            }

            "디버프" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeelqjvm)
                view = tagLinearLayout.findViewById(R.id.elqjvm)
            }

            "딜러" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeelffj)
                view = tagLinearLayout.findViewById(R.id.elffj)
            }

            "로봇" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activefhqht)
                view = tagLinearLayout.findViewById(R.id.fhqht)
            }

            "방어형" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeqkddjgud)
                view = tagLinearLayout.findViewById(R.id.qkddjgud)
            }

            "범위공격" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeqjadnlrhdrur)
                view = tagLinearLayout.findViewById(R.id.qjadnlrhdrur)
            }

            "생존형" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activetodwhs)
                view = tagLinearLayout.findViewById(R.id.todwhs)
            }

            "소환" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activethghks)
                view = tagLinearLayout.findViewById(R.id.thghks)
            }

            "제어형" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activewpdjgud)
                view = tagLinearLayout.findViewById(R.id.wpdjgud)
            }

            "지원" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activewldnjs)
                view = tagLinearLayout.findViewById(R.id.wldnjs)
            }

            "코스트+" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activezhtmxm)
                view = tagLinearLayout.findViewById(R.id.zhtmxm)
            }

            "쾌속부활" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activezhothrqnghkf)
                view = tagLinearLayout.findViewById(R.id.zhothrqnghkf)
            }

            "힐링" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activeglffld)
                view = tagLinearLayout.findViewById(R.id.glffld)
            }

            "원소" -> {
                activeView = activeTagLinearLayout.findViewById(R.id.activednjsth)
                view = tagLinearLayout.findViewById(R.id.dnjsth)
            }
            else -> {
                activeView = null
                view = null
            }
        }

        if (isActive) {
            activeView?.visibility = View.GONE
            view?.visibility = View.VISIBLE
        } else {
            activeView?.visibility = View.VISIBLE
            view?.visibility = View.GONE

            if(Loader.showFailedHighlightState) {
                if (isError) {
                    activeView?.setBackgroundResource(R.drawable.active_tag_error)
                } else {
                    activeView?.setBackgroundResource(R.drawable.active_tag)
                }
            }
        }
    }

    /* 이벤트 달기 */
    fun addButton() {
        tagLinearLayout = overlayView.findViewById<LinearLayout>(R.id.tagLinearLayout)
        activeTagLinearLayout = overlayView.findViewById<LinearLayout>(R.id.activeTagLinearLayout)

        for (i in 0 until tagLinearLayout.childCount) {
            val textView = tagLinearLayout.getChildAt(i) as TextView

            textView.setOnClickListener {
                onTagClick(listOf( it as TextView))
            }
        }
        for (i in 0 until activeTagLinearLayout.childCount) {
            val textView = activeTagLinearLayout.getChildAt(i) as TextView

            textView.setOnClickListener {
                onTagClick(listOf(it as TextView))
            }
        }

    }

    /* 최소화할 때 데이터 날리기 */
    fun clearList() {
        val itemLinearLayout = overlayView.findViewById<LinearLayout>(R.id.itemLinearLayout)

        itemLinearLayout.removeAllViews()
    }

    /* 결과 데이터로 목록 만들기 */
    fun makeList(items: Map<Int, Set<Item>>) {
        clearList()

        val comparator = compareBy<Pair<Int, Set<Item>>> { entry ->
            val first = entry.first

            when {
                first and 2048 == 2048 -> 0     // 고특채
                first and 16384 == 16384 -> 1   // 특채
                else -> 2
            }
        }.thenByDescending { entry ->
            entry.second.maxByOrNull {
                when (val rarity = it.rarity.toInt()) {
                    6 -> 0
                    else -> rarity
                }
            }?.rarity?.toInt() ?: 0 // 결과 데이터 최고치로 정렬
        }.thenByDescending { entry ->
            entry.second.minByOrNull {
                when (val rarity = it.rarity.toInt()) {
                    6 -> 7
                    else -> rarity
                }
            }?.rarity?.toInt() ?: 0
        }

        val sortedMap = items.toList().sortedWith(comparator).toMap()

        val itemLinearLayout = overlayView.findViewById<LinearLayout>(R.id.itemLinearLayout)

        sortedMap.forEach {

            // 3성과 2성이 포함된 데이터는 제외
            val tmp = it.value.filter { it_ ->
                it_.rarity == "3" || it_.rarity == "2"
            }

            if (tmp.isEmpty()) {
                val tags = Loader.tagToArray(it.key)

                if (tags.isNotEmpty()) {
                    val key = it.key

                    val filteredList = it.value.toList()
                        .filter { !(key and 2048 != 2048 && it.rarity == "6") }
                        .sortedByDescending { it.rarity }

                    if (filteredList.isEmpty()) {
                        return@forEach
                    }

                    val linearLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }

                    val horizontalScrollView = HorizontalScrollView(this)
                    horizontalScrollView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 6
                    }

                    val drawable = ContextCompat.getDrawable(this, R.drawable.item_tag)

                    tags.forEach {

                        val tv = TextView(this).apply {
                            text = it
                            background = drawable
                            setPadding(12, 4, 12, 4)
                            textSize = 12.0f
                            setTextColor(Color.BLACK)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                marginEnd = 8
                            }
                        }

                        linearLayout.addView(tv)
                    }

                    horizontalScrollView.addView(linearLayout)

                    itemLinearLayout.addView(horizontalScrollView)

                    val listView = RecyclerView(this).apply {
                        overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        val padding = resources.displayMetrics.density.toInt() * 20
                        setPadding(0, 0, 0, padding)
                    }

                    listView.layoutManager = FlexboxLayoutManager(this).apply {
                        flexDirection = FlexDirection.ROW
                        flexWrap = FlexWrap.WRAP
                    }

                    listView.adapter = ItemAdapter(filteredList)
                    itemLinearLayout.addView(listView)
                }
            }
        }
    }


    fun setLoading(isLoading: Boolean) {
        val overlayView1: View = overlayView.findViewById(R.id.overlayView)
        val overlayButton: Button = overlayView1.findViewById(R.id.overlayButton)

        isRecognizing = isLoading
        overlayButton.text = if (isLoading) { "•••" } else { "인식" }
    }

    fun addEvents(params:  WindowManager.LayoutParams) {

        val overlayView1: View = overlayView.findViewById(R.id.overlayView)
        val overlayView2: View = overlayView.findViewById(R.id.overlayResultView)

        val overlayButton: Button = overlayView1.findViewById(R.id.overlayButton)
        overlayButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Remember the initial position.
                    initialX = params.x
                    initialY = params.y

                    // Get the touch location
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new X and Y coordinates of the view.
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()

                    // Update the layout with new X & Y coordinate
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = (event.rawX - initialTouchX).toInt().absoluteValue
                    val diffY = (event.rawY - initialTouchY).toInt().absoluteValue
                    if (diffX < 10 && diffY < 10 && !isRecognizing) {

                        setLoading(true)
                        captureScreenAndRecognizeText()
                    }

                    true
                }
                else -> false
            }
        }

        val dragView: View = overlayView2.findViewById(R.id.dragView)
        dragView.setOnTouchListener{ _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Remember the initial position.
                    initialX = params.x
                    initialY = params.y

                    // Get the touch location
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new X and Y coordinates of the view.
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()

                    // Update the layout with new X & Y coordinate

                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }


        val overlayCloseButton: Button = overlayView2.findViewById(R.id.overlayCloseButton)
        val appCloseButton: Button = overlayView2.findViewById(R.id.appCloseButton)
        val retryButton: Button = overlayView2.findViewById(R.id.retryButton)

        overlayCloseButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Remember the initial position.
                    initialX = params.x
                    initialY = params.y

                    // Get the touch location
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new X and Y coordinates of the view.
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()

                    // Update the layout with new X & Y coordinate
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {

                    val diffX = (event.rawX - initialTouchX).toInt().absoluteValue
                    val diffY = (event.rawY - initialTouchY).toInt().absoluteValue
                    if (diffX < 10 && diffY < 10) {
                        activeTagLinearLayout.allViews.forEach {
                            if (it is TextView) {
                                it.visibility = View.GONE
                            }
                        }
                        tagLinearLayout.allViews.forEach {
                            it.visibility = View.VISIBLE
                        }

                        selectedTag = 0

                        clearList()

                        overlayView1.visibility = View.VISIBLE
                        overlayView2.visibility = View.GONE
                    }
                    true
                }
                else -> false
            }
        }

        retryButton.setOnTouchListener {_, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Remember the initial position.
                    initialX = params.x
                    initialY = params.y

                    // Get the touch location
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new X and Y coordinates of the view.
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()

                    // Update the layout with new X & Y coordinate
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = (event.rawX - initialTouchX).toInt().absoluteValue
                    val diffY = (event.rawY - initialTouchY).toInt().absoluteValue
                    if (diffX < 10 && diffY < 10) {
                        activeTagLinearLayout.allViews.forEach {
                            if (it is TextView) {
                                it.visibility = View.GONE
                            }
                        }
                        tagLinearLayout.allViews.forEach {
                            it.visibility = View.VISIBLE
                        }

                        selectedTag = 0

                        clearList()

                        CoroutineScope(Dispatchers.Main).launch {
                            overlayView1.visibility = View.VISIBLE
                            overlayView2.visibility = View.GONE

                            delay(100L)

                            setLoading(true)
                            captureScreenAndRecognizeText()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        appCloseButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Remember the initial position.
                    initialX = params.x
                    initialY = params.y

                    // Get the touch location
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the new X and Y coordinates of the view.
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()

                    // Update the layout with new X & Y coordinate
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = (event.rawX - initialTouchX).toInt().absoluteValue
                    val diffY = (event.rawY - initialTouchY).toInt().absoluteValue
                    if (diffX < 10 && diffY < 10) {
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(ExitActivity.NOTI_ID)

                        if (Loader.serv != null) {
                            Loader.serv!!.stopForeground(true)
                        }

//                        finishAffinity()
                        exitProcess(0)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun captureScreenAndRecognizeText() {
        val image = imageReader.acquireLatestImage()
        if (image != null) {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * image.width

            val bitmap = Bitmap.createBitmap(
                image.width + rowPadding / pixelStride,
                image.height, Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            image.close()

            CoroutineScope(Dispatchers.Main).launch {
                recognizeTextFromImage(bitmap)
            }
        }
    }

    private suspend fun recognizeImage(image: InputImage, recognizer: TextRecognizer, isLastTry: Boolean): Boolean {
        return suspendCoroutine { continuation ->
            val resultView = overlayView.findViewById<View>(R.id.overlayResultView)
            val linearLayout = resultView.findViewById<LinearLayout>(R.id.tagLinearLayout)

            var hitCnt = 0
            var recognitionSuccess = false

            val result: Task<Text> = recognizer.process(image)
            result.addOnSuccessListener { visionText ->
                var resultViews: MutableList<TextView> = mutableListOf()

                for (block in visionText.textBlocks) {
                    val blockText = block.text.replace(" ", "")

                    var resultTextView: TextView? = null
                    when (blockText) {
                        "신입" -> resultTextView = linearLayout.findViewById(R.id.tlsdlq)
                        "특별채용" -> resultTextView = linearLayout.findViewById(R.id.xmrco)
                        "고급특별채용" -> resultTextView = linearLayout.findViewById(R.id.rhxmrco)
                        "근거리" -> resultTextView = linearLayout.findViewById(R.id.rmsrjfl)
                        "원거리" -> resultTextView = linearLayout.findViewById(R.id.dnjsrjfl)
                        "가드" -> resultTextView = linearLayout.findViewById(R.id.rkem)
                        "디펜더" -> resultTextView = linearLayout.findViewById(R.id.elvpsej)
                        "메딕" -> resultTextView = linearLayout.findViewById(R.id.apelr)
                        "뱅가드" -> resultTextView = linearLayout.findViewById(R.id.qodrkem)
                        "서포터" -> resultTextView = linearLayout.findViewById(R.id.tjvhxj)
                        "스나이퍼" -> resultTextView = linearLayout.findViewById(R.id.tmskdlvj)
                        "스페셜리스트" -> resultTextView = linearLayout.findViewById(R.id.tmvptuffltmxm)
                        "캐스터" -> resultTextView = linearLayout.findViewById(R.id.zotmxj)
                        "감속" -> resultTextView = linearLayout.findViewById(R.id.rkathr)
                        "강제이동" -> resultTextView = linearLayout.findViewById(R.id.rkdwpdlehd)
                        "누커" -> resultTextView = linearLayout.findViewById(R.id.snzj)
                        "디버프" -> resultTextView = linearLayout.findViewById(R.id.elqjvm)
                        "딜러" -> resultTextView = linearLayout.findViewById(R.id.elffj)
                        "로봇" -> resultTextView = linearLayout.findViewById(R.id.fhqht)
                        "방어형" -> resultTextView = linearLayout.findViewById(R.id.qkddjgud)
                        "범위공격" -> resultTextView = linearLayout.findViewById(R.id.qjadnlrhdrur)
                        "생존형" -> resultTextView = linearLayout.findViewById(R.id.todwhs)
                        "소환" -> resultTextView = linearLayout.findViewById(R.id.thghks)
                        "제어형" -> resultTextView = linearLayout.findViewById(R.id.wpdjgud)
                        "지원" -> resultTextView = linearLayout.findViewById(R.id.wldnjs)
                        "코스트+" -> resultTextView = linearLayout.findViewById(R.id.zhtmxm)
                        "쾌속부활" -> resultTextView = linearLayout.findViewById(R.id.zhothrqnghkf)
                        "힐링" -> resultTextView = linearLayout.findViewById(R.id.glffld)
                        "원소" -> resultTextView = linearLayout.findViewById(R.id.dnjsth)
                    }

                    if (resultTextView != null) {
                        resultViews.add(resultTextView)
                        hitCnt += 1
                    } else {
                        if (blockText.contains("고급")) {
                            resultTextView = linearLayout.findViewById(R.id.rhxmrco) /* 고특채 보정 */

                            resultViews.add(resultTextView)

                            hitCnt += 1
                        } else if (blockText.contains("가드") && blockText.length > 2) { /* 뱅가드 보정 */
                            resultTextView = linearLayout.findViewById((R.id.qodrkem))

                            resultViews.add(resultTextView)

                            hitCnt += 1
                        } else if (blockText.contains("신임") || blockText.contains("신업") || blockText.contains(
                                "신엄"
                            )
                        ) { /* 신입 보정 */
                            resultTextView = linearLayout.findViewById((R.id.tlsdlq))

                            resultViews.add(resultTextView)

                            hitCnt += 1
                        } else if (blockText.contains("속") && blockText.length > 3) { /* 쾌부 보정 */
                            resultTextView = linearLayout.findViewById((R.id.zhothrqnghkf))

                            resultViews.add(resultTextView)

                            hitCnt += 1
                        } else if (blockText.contains("캐스티")) { /* 캐스터 보정 */
                            resultTextView = linearLayout.findViewById(R.id.zotmxj)

                            resultViews.add(resultTextView)

                            hitCnt += 1
                        }
                    }
                }

                if (hitCnt == 5 || isLastTry) {
                    onTagClick(resultViews, isLastTry && hitCnt < 5 && Loader.showFailedHighlightState)

                    setLoading(false)
                    val overlayView1: View = overlayView.findViewById(R.id.overlayView)
                    overlayView1.visibility = View.GONE
                    resultView.visibility = View.VISIBLE

                    recognitionSuccess = true
                    if (isLastTry && hitCnt < 5) {
                        if (Loader.showFailedToastState) {
                            toastHandler.post {
                                Toast.makeText(applicationContext, "감지된 태그가 5개 미만입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                continuation.resume(recognitionSuccess)
            }.addOnFailureListener { e ->
                Log.e("OverlayService", "Text recognition failed", e)
                continuation.resume(false)
            }
        }
    }

    private suspend fun recognizeTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        val resultView = overlayView.findViewById<View>(R.id.overlayResultView)

        var loopCnt = 0
        val loopLimit = Loader.retryLimit

        resultView.background = Drawer.setBorder(Color.parseColor("#FAFAFA"), 12.0f, Color.BLACK, 2)

        while (loopCnt < loopLimit) {
            val result = recognizeImage(image, recognizer, loopCnt + 1 == loopLimit)

            if(result) { break }
            else { loopCnt++ }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (overlayView.isAttachedToWindow) {
            windowManager.removeView(overlayView)
        }

        if (::mediaProjection.isInitialized) {
            mediaProjection.stop()
        }
    }
}
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
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue
import kotlin.system.exitProcess
import android.os.Looper;
import kotlin.reflect.KMutableProperty0


class OverlayService : Service() {
    private val TAG_VIEW_MAP = mapOf(
        // Tag ID to Pair(Active View ID, Normal View ID)
        1 to Pair(R.id.activerkem, R.id.rkem),               // 가드 (Guard)
        2 to Pair(R.id.activetmskdlvj, R.id.tmskdlvj),       // 스나이퍼 (Sniper)
        3 to Pair(R.id.activeelvpsej, R.id.elvpsej),         // 디펜더 (Defender)
        4 to Pair(R.id.activeapelr, R.id.apelr),             // 메딕 (Medic)
        5 to Pair(R.id.activetjvhxj, R.id.tjvhxj),           // 서포터 (Supporter)
        6 to Pair(R.id.activezotmxj, R.id.zotmxj),           // 캐스터 (Caster)
        7 to Pair(R.id.activetmvptuffltmxm, R.id.tmvptuffltmxm), // 스페셜리스트 (Specialist)
        8 to Pair(R.id.activeqodrkem, R.id.qodrkem),         // 뱅가드 (Vanguard)
        9 to Pair(R.id.activermsrjfl, R.id.rmsrjfl),         // 근거리 (Melee)
        10 to Pair(R.id.activednjsrjfl, R.id.dnjsrjfl),       // 원거리 (Ranged)
        11 to Pair(R.id.activerhxmrco, R.id.rhxmrco),         // 고급특별채용 (Top Operator)
        12 to Pair(R.id.activewpdjgud, R.id.wpdjgud),         // 제어형 (Crowd-Control)
        13 to Pair(R.id.activesnzj, R.id.snzj),             // 누커 (Nuker)
        14 to Pair(R.id.activexmrco, R.id.xmrco),           // 특별채용 (Senior Operator)
        15 to Pair(R.id.activeglffld, R.id.glffld),         // 힐링 (Healing)
        16 to Pair(R.id.activewldnjs, R.id.wldnjs),         // 지원 (Support)
        17 to Pair(R.id.activetlsdlq, R.id.tlsdlq),         // 신입 (Starter)
        18 to Pair(R.id.activezhtmxm, R.id.zhtmxm),         // 코스트+ (DP-Recovery)
        19 to Pair(R.id.activeelffj, R.id.elffj),           // 딜러 (DPS)
        20 to Pair(R.id.activetodwhs, R.id.todwhs),         // 생존형 (Survival)
        21 to Pair(R.id.activeqjadnlrhdrur, R.id.qjadnlrhdrur), // 범위공격 (AoE)
        22 to Pair(R.id.activeqkddjgud, R.id.qkddjgud),       // 방어형 (Defense)
        23 to Pair(R.id.activerkathr, R.id.rkathr),         // 감속 (Slow)
        24 to Pair(R.id.activeelqjvm, R.id.elqjvm),         // 디버프 (Debuff)
        25 to Pair(R.id.activezhothrqnghkf, R.id.zhothrqnghkf), // 쾌속부활 (Fast-Redeploy)
        26 to Pair(R.id.activerkdwpdlehd, R.id.rkdwpdlehd),   // 강제이동 (Shift)
        27 to Pair(R.id.activethghks, R.id.thghks),         // 소환 (Summon)
        28 to Pair(R.id.activefhqht, R.id.fhqht),           // 로봇 (Robot)
        29 to Pair(R.id.activednjsth, R.id.dnjsth)          // 원소 (Elemental)
    )

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    private var NOTI_ID = 1

    private lateinit var mediaProjection: MediaProjection
    private var virtualDisplay: VirtualDisplay? = null
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

    private fun updateTagTexts() {
        // TagStrings.kt에 정의된 객체는 import 되어 있다고 가정합니다.
        // Loader 객체에 LanguageType이 저장되어 있다고 가정합니다.
        val currentLang = Loader.language

        Log.w("ㅓㅎㄷ", currentLang.key)
        Log.w("ㅓㅎㄷ", currentLang.name)
        // 여기에 TextView ID와 Tag ID 매핑 코드를 넣습니다.
        val tagViewIdMap = mapOf(
            // ID 1~8: 클래스 (Guard, Sniper, Defender, Medic, Supporter, Caster, Specialist, Vanguard)
            R.id.rkem to 1,         // 가드
            R.id.tmskdlvj to 2,     // 스나이퍼
            R.id.elvpsej to 3,      // 디펜더
            R.id.apelr to 4,        // 메딕
            R.id.tjvhxj to 5,       // 서포터
            R.id.zotmxj to 6,       // 캐스터
            R.id.tmvptuffltmxm to 7,// 스페셜리스트
            R.id.qodrkem to 8,      // 뱅가드

            // ID 9~10: 위치 (Melee, Ranged)
            R.id.rmsrjfl to 9,      // 근거리
            R.id.dnjsrjfl to 10,    // 원거리

            // ID 11~14: 등급/제어 (Top Op, Crowd Control, Nuker, Senior Op)
            R.id.rhxmrco to 11,     // 고급 특별 채용 (고급특별채용)
            R.id.wpdjgud to 12,     // 제어형
            R.id.snzj to 13,        // 누커
            R.id.xmrco to 14,       // 특별 채용 (특별채용)

            // ID 15~29: 특성
            R.id.glffld to 15,      // 힐링
            R.id.wldnjs to 16,      // 지원
            R.id.tlsdlq to 17,      // 신입
            R.id.zhtmxm to 18,      // 코스트+
            R.id.elffj to 19,       // 딜러
            R.id.todwhs to 20,      // 생존형
            R.id.qjadnlrhdrur to 21,// 범위공격
            R.id.qkddjgud to 22,    // 방어형
            R.id.rkathr to 23,      // 감속
            R.id.elqjvm to 24,      // 디버프
            R.id.zhothrqnghkf to 25,// 쾌속부활
            R.id.rkdwpdlehd to 26,  // 강제이동
            R.id.thghks to 27,      // 소환
            R.id.fhqht to 28,       // 로봇
            R.id.dnjsth to 29       // 원소
        )
        val tagViewIdMapActive = mapOf(
            // ID 1~8: 클래스 (Guard, Sniper, Defender, Medic, Supporter, Caster, Specialist, Vanguard)
            R.id.activerkem to 1,         // 가드
            R.id.activetmskdlvj to 2,     // 스나이퍼
            R.id.activeelvpsej to 3,      // 디펜더
            R.id.activeapelr to 4,        // 메딕
            R.id.activetjvhxj to 5,       // 서포터
            R.id.activezotmxj to 6,       // 캐스터
            R.id.activetmvptuffltmxm to 7,// 스페셜리스트
            R.id.activeqodrkem to 8,      // 뱅가드

            // ID 9~10: 위치 (Melee, Ranged)
            R.id.activermsrjfl to 9,      // 근거리
            R.id.activednjsrjfl to 10,    // 원거리

            // ID 11~14: 등급/제어 (Top Op, Crowd Control, Nuker, Senior Op)
            R.id.activerhxmrco to 11,     // 고급 특별 채용 (고급특별채용)
            R.id.activewpdjgud to 12,     // 제어형
            R.id.activesnzj to 13,        // 누커
            R.id.activexmrco to 14,       // 특별 채용 (특별채용)

            // ID 15~29: 특성
            R.id.activeglffld to 15,      // 힐링
            R.id.activewldnjs to 16,      // 지원
            R.id.activetlsdlq to 17,      // 신입
            R.id.activezhtmxm to 18,      // 코스트+
            R.id.activeelffj to 19,       // 딜러
            R.id.activetodwhs to 20,      // 생존형
            R.id.activeqjadnlrhdrur to 21,// 범위공격
            R.id.activeqkddjgud to 22,    // 방어형
            R.id.activerkathr to 23,      // 감속
            R.id.activeelqjvm to 24,      // 디버프
            R.id.activezhothrqnghkf to 25,// 쾌속부활
            R.id.activerkdwpdlehd to 26,  // 강제이동
            R.id.activethghks to 27,      // 소환
            R.id.activefhqht to 28,       // 로봇
            R.id.activednjsth to 29       // 원소
        )
        tagViewIdMap.forEach { (viewId, tagId) ->
            val textView = overlayView.findViewById<TextView>(viewId)

            val localizedText = TagStrings.getString(tagId, currentLang)
            var finalTagText = localizedText

            // 2. 현재 언어가 KOREAN이 아닐 경우에만 괄호와 한국어 텍스트를 추가합니다.
            if (currentLang != LanguageType.KOREAN) {
                // 3. 한국어 텍스트를 가져옵니다. (예: "가드")
                val koreanText = TagStrings.getString(tagId, LanguageType.KOREAN)

                // 4. 최종 문자열을 만듭니다. (예: "Guard(가드)")
                finalTagText = "$localizedText($koreanText)"
            }

            Log.w("ㅓㅎㄷ", finalTagText)
            textView.text = finalTagText

        }
        tagViewIdMapActive.forEach { (viewId, tagId) ->
            val textView = overlayView.findViewById<TextView>(viewId)

            val localizedText = TagStrings.getString(tagId, currentLang)
            var finalTagText = localizedText

            // 2. 현재 언어가 KOREAN이 아닐 경우에만 괄호와 한국어 텍스트를 추가합니다.
            if (currentLang != LanguageType.KOREAN) {
                // 3. 한국어 텍스트를 가져옵니다. (예: "가드")
                val koreanText = TagStrings.getString(tagId, LanguageType.KOREAN)

                // 4. 최종 문자열을 만듭니다. (예: "Guard(가드)")
                finalTagText = "$localizedText($koreanText)"
            }

            Log.w("ㅓㅎㄷ", finalTagText + "ACTIVE")
            textView.text = finalTagText
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(NOTI_ID, createNotification(), FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_OK) ?: Activity.RESULT_OK
        val data = intent?.getParcelableExtra<Intent>("data")
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val projection = data?.let { projectionManager.getMediaProjection(resultCode, it) }
        if (projection == null) {
            Toast.makeText(this, "화면 캡처 권한을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            stopSelf()
            return START_NOT_STICKY
        }

        mediaProjection = projection

        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                // MediaProjection이 중지되었을 때 리소스 해제
                stopSelf()
            }
        }, null)

        updateTagTexts()

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

        if (virtualDisplay == null) {
            Toast.makeText(this, "VirtualDisplay 생성 실패", Toast.LENGTH_SHORT).show()
        }
    }

    /* 인식 결과로 한번에 여러 개의 태그 데이터 들어올 경우 */
    private fun onTagClick(selectedTextView: List<TextView>, isError: Boolean = false) {
        selectedTextView.forEach {
            onTagClick(it, isError)
        }

        val calculatror = Calculatror()

        makeList(calculatror.getResultData(selectedTag))
    }

//    /* 태그 하나씩 누를 경우 */
//    private fun onTagClick(selectedTextViwe: TextView, isError: Boolean = false) {
//        val tag = Loader.tagToInt(selectedTextViwe.text.toString())
//        val isActive = selectedTag and tag == tag
//
//        if (isActive) {
//            getTextView(selectedTextViwe.text.toString(), true, isError)
//            selectedTag -= tag
//        } else {
//            getTextView(selectedTextViwe.text.toString(), false, isError)
//            selectedTag += tag
//        }
//    }
    /* 태그 하나씩 누를 경우 */
    private fun onTagClick(selectedTextViwe: TextView, isError: Boolean = false) {
        // 1. TextView의 tag 속성에 저장된 태그 ID (String)를 가져와 Int로 변환
        val tagIdString = selectedTextViwe.tag.toString()
        Log.w("ㅓㅎㄷ", selectedTextViwe.text.toString()) // 디버깅용이므로 유지
        Log.w("ㅓㅎㄷ", tagIdString) // 디버깅용이므로 유지

        val tagId = tagIdString.toIntOrNull() ?: return // ID가 없거나 유효하지 않으면 종료

        // 2. TagStrings 객체의 함수를 사용하여 해당 ID의 비트 값을 가져옵니다.
        val tagBitValue = TagStrings.getBitValue(tagId)

        if (tagBitValue == -1 || tagBitValue == 0) return

        // 3. 기존 로직 유지 (비트 연산)
        val isActive = selectedTag and tagBitValue == tagBitValue

        if (isActive) {
            // 수정된 부분: 텍스트 대신 tagId를 getTextView에 전달
            getTextView(tagId, true, isError)
            selectedTag -= tagBitValue
        } else {
            // 수정된 부분: 텍스트 대신 tagId를 getTextView에 전달
            getTextView(tagId, false, isError)
            selectedTag += tagBitValue
        }
    }
    /* 태그 눌렀을 때 숨김 처리 */
//    private fun getTextView(text: String, isActive: Boolean, isError: Boolean = false) {
//        var view: TextView?
//        var activeView: TextView?
//
//        // Loader 객체에 LanguageType이 저장되어 있다고 가정
//        val currentLang = Loader.language
//
//        // 입력된 텍스트와 현재 언어를 사용하여 태그 ID를 찾습니다.
//        // text에 공백이 포함되어 있다면 먼저 제거해야 합니다.
//        val tagText = text.replace(" ", "")
//        val tagId = TagStrings.getId(tagText, currentLang)
//
//        when (tagId) {
//            17 -> { // 신입 (Starter)
//                activeView = activeTagLinearLayout.findViewById(R.id.activetlsdlq)
//                view = tagLinearLayout.findViewById(R.id.tlsdlq)
//            }
//            14 -> { // 특별채용 (Senior Operator)
//                activeView = activeTagLinearLayout.findViewById(R.id.activexmrco)
//                view = tagLinearLayout.findViewById(R.id.xmrco)
//            }
//            11 -> { // 고급특별채용 (Top Operator)
//                activeView = activeTagLinearLayout.findViewById(R.id.activerhxmrco)
//                view = tagLinearLayout.findViewById(R.id.rhxmrco)
//            }
//            9 -> { // 근거리 (Melee)
//                activeView = activeTagLinearLayout.findViewById(R.id.activermsrjfl)
//                view = tagLinearLayout.findViewById(R.id.rmsrjfl)
//            }
//            10 -> { // 원거리 (Ranged)
//                activeView = activeTagLinearLayout.findViewById(R.id.activednjsrjfl)
//                view = tagLinearLayout.findViewById(R.id.dnjsrjfl)
//            }
//            1 -> { // 가드 (Guard)
//                activeView = activeTagLinearLayout.findViewById(R.id.activerkem)
//                view = tagLinearLayout.findViewById(R.id.rkem)
//            }
//            3 -> { // 디펜더 (Defender)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeelvpsej)
//                view = tagLinearLayout.findViewById(R.id.elvpsej)
//            }
//            4 -> { // 메딕 (Medic)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeapelr)
//                view = tagLinearLayout.findViewById(R.id.apelr)
//            }
//            8 -> { // 뱅가드 (Vanguard)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeqodrkem)
//                view = tagLinearLayout.findViewById(R.id.qodrkem)
//            }
//            5 -> { // 서포터 (Supporter)
//                activeView = activeTagLinearLayout.findViewById(R.id.activetjvhxj)
//                view = tagLinearLayout.findViewById(R.id.tjvhxj)
//            }
//            2 -> { // 스나이퍼 (Sniper)
//                activeView = activeTagLinearLayout.findViewById(R.id.activetmskdlvj)
//                view = tagLinearLayout.findViewById(R.id.tmskdlvj)
//            }
//            7 -> { // 스페셜리스트 (Specialist)
//                activeView = activeTagLinearLayout.findViewById(R.id.activetmvptuffltmxm)
//                view = tagLinearLayout.findViewById(R.id.tmvptuffltmxm)
//            }
//            6 -> { // 캐스터 (Caster)
//                activeView = activeTagLinearLayout.findViewById(R.id.activezotmxj)
//                view = tagLinearLayout.findViewById(R.id.zotmxj)
//            }
//            23 -> { // 감속 (Slow)
//                activeView = activeTagLinearLayout.findViewById(R.id.activerkathr)
//                view = tagLinearLayout.findViewById(R.id.rkathr)
//            }
//            26 -> { // 강제이동 (Shift)
//                activeView = activeTagLinearLayout.findViewById(R.id.activerkdwpdlehd)
//                view = tagLinearLayout.findViewById(R.id.rkdwpdlehd)
//            }
//            13 -> { // 누커 (Nuker)
//                activeView = activeTagLinearLayout.findViewById(R.id.activesnzj)
//                view = tagLinearLayout.findViewById(R.id.snzj)
//            }
//            24 -> { // 디버프 (Debuff)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeelqjvm)
//                view = tagLinearLayout.findViewById(R.id.elqjvm)
//            }
//            19 -> { // 딜러 (DPS)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeelffj)
//                view = tagLinearLayout.findViewById(R.id.elffj)
//            }
//            28 -> { // 로봇 (Robot)
//                activeView = activeTagLinearLayout.findViewById(R.id.activefhqht)
//                view = tagLinearLayout.findViewById(R.id.fhqht)
//            }
//            22 -> { // 방어형 (Defense)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeqkddjgud)
//                view = tagLinearLayout.findViewById(R.id.qkddjgud)
//            }
//            21 -> { // 범위공격 (AoE)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeqjadnlrhdrur)
//                view = tagLinearLayout.findViewById(R.id.qjadnlrhdrur)
//            }
//            20 -> { // 생존형 (Survival)
//                activeView = activeTagLinearLayout.findViewById(R.id.activetodwhs)
//                view = tagLinearLayout.findViewById(R.id.todwhs)
//            }
//            27 -> { // 소환 (Summon)
//                activeView = activeTagLinearLayout.findViewById(R.id.activethghks)
//                view = tagLinearLayout.findViewById(R.id.thghks)
//            }
//            12 -> { // 제어형 (Crowd-Control)
//                activeView = activeTagLinearLayout.findViewById(R.id.activewpdjgud)
//                view = tagLinearLayout.findViewById(R.id.wpdjgud)
//            }
//            16 -> { // 지원 (Support)
//                activeView = activeTagLinearLayout.findViewById(R.id.activewldnjs)
//                view = tagLinearLayout.findViewById(R.id.wldnjs)
//            }
//            18 -> { // 코스트+ (DP-Recovery)
//                activeView = activeTagLinearLayout.findViewById(R.id.activezhtmxm)
//                view = tagLinearLayout.findViewById(R.id.zhtmxm)
//            }
//            25 -> { // 쾌속부활 (Fast-Redeploy)
//                activeView = activeTagLinearLayout.findViewById(R.id.activezhothrqnghkf)
//                view = tagLinearLayout.findViewById(R.id.zhothrqnghkf)
//            }
//            15 -> { // 힐링 (Healing)
//                activeView = activeTagLinearLayout.findViewById(R.id.activeglffld)
//                view = tagLinearLayout.findViewById(R.id.glffld)
//            }
//            29 -> { // 원소 (Elemental)
//                activeView = activeTagLinearLayout.findViewById(R.id.activednjsth)
//                view = tagLinearLayout.findViewById(R.id.dnjsth)
//            }
//            else -> {
//                activeView = null
//                view = null
//            }
//        }
//
//        if (isActive) {
//            activeView?.visibility = View.GONE
//            view?.visibility = View.VISIBLE
//        } else {
//            activeView?.visibility = View.VISIBLE
//            view?.visibility = View.GONE
//
//            if(Loader.showFailedHighlightState) {
//                if (isError) {
//                    activeView?.setBackgroundResource(R.drawable.active_tag_error)
//                } else {
//                    activeView?.setBackgroundResource(R.drawable.active_tag)
//                }
//            }
//        }
//    }
    private fun getTextView(tagId: Int, isActive: Boolean, isError: Boolean = false) {
        // 1. tagId를 사용하여 Map에서 뷰 ID 쌍을 찾습니다.
        val viewIds = TAG_VIEW_MAP[tagId] ?: return

        // 뷰 ID를 사용하여 실제 뷰를 찾습니다. (activeTagLinearLayout, tagLinearLayout은 멤버 변수)
        val activeView = activeTagLinearLayout.findViewById<TextView>(viewIds.first)
        val view = tagLinearLayout.findViewById<TextView>(viewIds.second)

        // 뷰를 찾지 못했으면 안전하게 종료
        if (activeView == null || view == null) return

        // 3. 상태에 따른 뷰 가시성 및 스타일 변경 (기존 로직 유지)
        if (isActive) {
            // 활성화 해제: activeView 숨기고, view 보이기
            activeView.visibility = View.GONE
            view.visibility = View.VISIBLE
        } else {
            // 활성화: activeView 보이고, view 숨기기
            activeView.visibility = View.VISIBLE
            view.visibility = View.GONE

            if(Loader.showFailedHighlightState) {
                if (isError) {
                    activeView.setBackgroundResource(R.drawable.active_tag_error)
                } else {
                    activeView.setBackgroundResource(R.drawable.active_tag)
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
        val resultViews: MutableList<TextView> = mutableListOf()
        val currentLang = Loader.language

        val result: Task<Text> = recognizer.process(image)
        result.addOnSuccessListener { visionText ->

            // 모든 TextBlock을 순회
            for (block in visionText.textBlocks) {

                // TextBlock 전체 텍스트를 확인하고 hitCnt 업데이트
                hitCnt = processTextForTag(block.text, linearLayout, currentLang, resultViews, hitCnt)

                // Line(줄)을 순회하며 태그 탐색 강화
                for (line in block.lines) {

                    // Line 전체 텍스트를 확인하고 hitCnt 업데이트
                    hitCnt = processTextForTag(line.text, linearLayout, currentLang, resultViews, hitCnt)

                    // Element(단어)를 순회하며 텍스트를 확인하고 hitCnt 업데이트
                    for (element in line.elements) {
                        hitCnt = processTextForTag(element.text, linearLayout, currentLang, resultViews, hitCnt)
                    }
                }
            }

            // ... (후처리 로직은 동일)

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

    /**
     * 주어진 텍스트가 유효한 태그를 포함하는지 확인하고, 뷰를 찾아 resultViews에 추가합니다.
     * @param rawText TextBlock, Line, 또는 Element에서 추출된 텍스트
     * @param linearLayout 태그 뷰가 포함된 레이아웃
     * @param currentLang 현재 설정된 언어
     * @param resultViews 찾은 TextView 목록
     * @param hitCnt 찾은 태그 개수 (Call-by-reference 역할을 위해 KProperty0 사용)
     */
    private fun processTextForTag(
        rawText: String,
        linearLayout: LinearLayout,
        currentLang: LanguageType,
        resultViews: MutableList<TextView>,
        currentHitCnt: Int
    ): Int {
        if (rawText.isBlank()) return currentHitCnt

        var newHitCnt = currentHitCnt
        val blockText = rawText.replace(" ", "")
        var resultTextView: TextView? = null

        // 1. Tag ID 직접 매칭 (정확 일치)
        var tagId = TagStrings.getId(blockText, currentLang)

        // 2. Tag 문자열 기반 매칭 (포함)
        if (tagId == null) {
            for (id in 1..29) {
                val tagText = TagStrings.getString(id, currentLang).replace(" ", "")
                if (blockText.contains(tagText, ignoreCase = true) && TAG_VIEW_MAP.containsKey(id)) {
                    tagId = id
                    break
                }
            }
        }

        // 3. 데이터 기반 보정
        if (tagId == null) {
            tagId = CorrectionService.applyCorrection(blockText, currentLang)
        }

        // 4. 결과 처리
        if (tagId != null) {
            val normalViewId = TAG_VIEW_MAP[tagId]?.second
            if (normalViewId != null) {
                resultTextView = linearLayout.findViewById(normalViewId)
            }
        }

        resultTextView?.let { view ->
            if (resultViews.none { it.id == view.id }) {
                resultViews.add(view)
                newHitCnt++
            }
        }

        return newHitCnt
    }

    private suspend fun recognizeTextFromImage(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = getTextRecognizer(Loader.language)

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

    /**
     * 선택된 LanguageType에 따라 적절한 TextRecognizer 클라이언트를 생성하여 반환합니다.
     *
     * @param languageType 인식에 사용할 언어 타입 (Korean, Chinese, Japanese, English/Latin)
     * @return 초기화된 TextRecognizer 인스턴스
     */
    private fun getTextRecognizer(languageType: LanguageType): TextRecognizer {
        val options = when (languageType) {
            LanguageType.KOREAN -> {
                // 한국어 인식을 위한 옵션
                KoreanTextRecognizerOptions.Builder().build()
            }
            LanguageType.CHINESE -> {
                // 중국어 인식을 위한 옵션
                ChineseTextRecognizerOptions.Builder().build()
            }
            LanguageType.JAPANESE -> {
                // 일본어 인식을 위한 옵션
                JapaneseTextRecognizerOptions.Builder().build()
            }
            LanguageType.ENGLISH -> {
                // 라틴 기반 언어(영어 포함) 인식을 위한 기본 옵션
                TextRecognizerOptions.DEFAULT_OPTIONS
            }
        }

        // 설정된 옵션으로 TextRecognizer 클라이언트를 초기화합니다.
        return TextRecognition.getClient(options)
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
package com.tn233.hamster

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.room.Room
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tn233.hamster.ui.theme.HamsterNewsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import kotlin.random.Random

data class News(var id: String, var title: String, var content: String, var source: String, var timestamp: Long, var cover_image: String, var category: String, var url: String)

fun news_from_json(json: JSONObject): News {
    // 这里的timestamp没有实现，因为后端给了个非标准的timestamp实现
    val timestamp_str = json.getString("timestamp")
    // 定义时间格式
    val formatter = DateTimeFormatter.ISO_INSTANT
    // 解析时间字符串为 Instant 后转时间戳
    val news_timestamp = Instant.from(formatter.parse(timestamp_str)).toEpochMilli()
    return News(json.getString("id"),json.getString("title"),json.getString("content"),json.getString("source"),news_timestamp,json.getString("cover_image"),json.getString("category"), json.getString("url"))
}

fun timestamp_to_display(timestamp: Long): String{
    // 使用 DateTimeFormatter 解析时间字符串为 Instant
//    val formatter = DateTimeFormatter.ISO_INSTANT
    val pastInstant = Instant.ofEpochMilli(timestamp)
    // 获取当前时间
    val currentInstant = Instant.now()
    // 计算时间差
    val duration = Duration.between(pastInstant, currentInstant)
    // 根据时间差输出描述
    return when {
        duration.toMillis() < 60000 -> "刚刚" // 小于 1 分钟
        duration.toMinutes() < 60 -> "${duration.toMinutes()} 分钟前" // 小于 1 小时
        duration.toHours() < 24 -> "${duration.toHours()} 小时前" // 小于 1 天
        duration.toDays() < 7 -> "${duration.toDays()} 天前" // 小于 1 周
        else -> DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(pastInstant.atZone(java.time.ZoneId.systemDefault())) // 超过 1 周，展示友好日期
    }
}

val ChineseParagraphLineBreak = LineBreak(
    strategy = LineBreak.Strategy.HighQuality,
    strictness = LineBreak.Strictness.Strict,
    wordBreak = LineBreak.WordBreak.Phrase
)

class NewsActivity : ComponentActivity() {

    var isShare = mutableStateOf(false)
    var isLike = mutableStateOf(false)
    var isClick = mutableStateOf(true)
    var readTime = mutableIntStateOf(0)
    var slideLength = mutableDoubleStateOf(0.0)
    var slideCount = mutableIntStateOf(0)
    var NewsInfo = mutableStateOf(News("", "","","",0L,"", "", ""))
//    var clickCount = mutableIntStateOf(0)

    val coroutineScope = CoroutineScope(Dispatchers.Default) // Coroutine

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val news_id = intent.getStringExtra("NewsId")
        if (news_id != null) {
            Log.i("NewsId", news_id)
            coroutineScope.launch {
                val client = OkHttpClient()
                val request = Request.Builder().url("http://113.45.206.216:3000/api/news/${news_id}").build();
                try {
                    val response: Response = client.newCall(request).execute()
                    val res_obj = JSONObject(response.body?.string())
                    val news = res_obj.getJSONObject("data")
                    // 这里timestamp的类型转换没有写 - news.getString("timestamp")
                    NewsInfo.value = news_from_json(news)
                }catch (e: Exception){
                    Log.e("GET_NEWS_ERR", e.toString());
                }
            }
        }

        setContent {
            HamsterNewsTheme {
                val pagerState_Home = rememberPagerState(pageCount = { 4 })
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                    rememberTopAppBarState()
                )

                var display_flag by remember { mutableStateOf(false) }

                // 新闻阅览时间Timer
                LaunchedEffect(readTime) {
                    while(true){
                        readTime.intValue += 1
                        Log.i("readTime", readTime.intValue.toString())
                        delay(1000)
                    }
                }

                LaunchedEffect(scrollBehavior.state.contentOffset) {
                    slideLength.doubleValue += scrollBehavior.state.contentOffset.absoluteValue // 新闻阅览滑动轨迹的长度(一定是绝对值，向下滑的轨迹是负数)
                    slideCount.intValue += 1
                    Log.i("slideLength", slideLength.doubleValue.toString())
                    Log.i("SlideFrac", (slideLength.doubleValue / 1000).toString()) // slideCount.intValue

                    if(scrollBehavior.state.overlappedFraction.absoluteValue >= 1) display_flag = true
                    else display_flag = false
//                    Log.i("offset",scrollBehavior.state.contentOffset.toString())
//                    Log.i("collapsedFraction",scrollBehavior.state.collapsedFraction.toString())
//                    Log.i("overlappedFraction",scrollBehavior.state.overlappedFraction.toString())
//                    Log.i("offset_frac", display_flag.toString())
                }

                Box{
                    Scaffold(containerColor = Color.Transparent, modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(Color.Transparent, Color.White, if(display_flag) Color.Black else Color.White, Color.Black, if(display_flag) Color.Black else Color.White ),
                                title = {
                                    if(display_flag){
                                        Text(
                                            text = NewsInfo.value.title,
                                            color = Color.Black,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = SourceHanSerif,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "返回按钮"
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        isShare.value = true
                                        val share = Intent.createChooser(Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, NewsInfo.value.url)
                                            // (Optional) Here you're setting the title of the content
                                            putExtra(Intent.EXTRA_TITLE, NewsInfo.value.title)

                                            // (Optional) Here you're passing a content URI to an image to be displayed
//                                            data = R.drawable.logo_hamster
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }, null)
                                        startActivity(share)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "分享按钮"
                                        )
                                    }
                                },
                                scrollBehavior = scrollBehavior
//                        colors = TODO()
                            )
                        },
                        bottomBar = {}
                    ) { innerPadding ->
                        DetailPage(modifier = Modifier.fillMaxSize().padding(innerPadding.calculateStartPadding(
                            LayoutDirection.Ltr),
                            0.dp,
                            innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            innerPadding.calculateBottomPadding()),
                            news = NewsInfo.value
                        )
                    }
                }

            }
        }
    }

    override fun finish() {
        coroutineScope.launch {
            val db = Room.databaseBuilder(
                applicationContext,
                HistoryDatabase::class.java, "hamster_history"
            ).build()

            val history_dao = db.historyDao()
            // 获取当前时间的时间戳（毫秒）
            val current_timestamp = Instant.now().toEpochMilli()
            // 计算时间差
            val time_diff = current_timestamp - NewsInfo.value.timestamp
            // 30%的几率抽取样本为喜欢
            val randomValue = Random.nextInt(100)
            if(!isLike.value && randomValue < 30) isLike.value = true
            // 如果分享代表用户在意该新闻，也视为喜欢
            if(isShare.value) isLike.value = true
            // 插入记录
            history_dao.insertAll(History(null, NewsInfo.value.id, NewsInfo.value.category, isClick.value, isShare.value, slideLength.value, slideCount.value, readTime.value, time_diff,0.2993, isLike.value))
        }
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,R.anim.slide_in_left_exit,R.anim.slide_out_right_exit)
        } else {
            overridePendingTransition(R.anim.slide_in_left_exit,R.anim.slide_out_right_exit)
        }
    }

    @Composable
//@Preview(showBackground = true)
    fun DetailPage(modifier: Modifier,news: News){ //content: String
        HamsterNewsTheme {
            val scrollState = rememberScrollState()
            Column(modifier = modifier.verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally){
                News_Detail_Header(news.title, news.source, news.timestamp, news.cover_image) // News.getLong("timestamp")
                Spacer(modifier = Modifier.height(20.dp))
                MarkdownRenderer(news.content)
                Spacer(modifier = Modifier.height(20.dp))
                Text("全文完", fontFamily = SourceHanSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, color = Color(0xBFBDB6AD), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(20.dp))
                Row{
                    TextButton(onClick = {
                        isLike.value = !isLike.value
                    }){
                        Row(verticalAlignment = Alignment.CenterVertically){
                            if(isLike.value){
                                Icon(painterResource(R.drawable.like_fill),"喜欢", modifier = Modifier.size(28.dp))
                            }else{
                                Icon(painterResource(R.drawable.like),"喜欢", modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.padding(3.dp))
                            Text("喜欢该文章")
                        }
                    }
//                IconButton(onClick = {}) {
//                    Icon(painterResource(R.drawable.like),"喜欢", modifier = Modifier.size(28.dp))
//                }
//                IconButton(onClick = {}) {
//                    Icon(
//                        imageVector = Icons.Filled.Share,
//                        contentDescription = "Localized description"
//                    )
//                }
//                IconButton(onClick = {}) {
//                    Icon(
//                        imageVector = Icons.Filled.Star,
//                        contentDescription = "Localized description"
//                    )
//                }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    @Composable
    fun News_Detail_Header(title: String, source: String, timestamp: Long, cover_image: String){
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(270.dp) // 使用Box去做罩层的时候需要规定高度，因为罩层的endY = Float.POSITIVE_INFINITY
        ){
            // 渐变层遮罩 + image_cover
            AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cover_image)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image"
                , onError = {
                    Log.e("img_err",it.toString())
                }, onSuccess = {
                    Log.i("img_success","SUCCESS!!!!")
                },
                contentScale = ContentScale.Crop
//                modifier = Modifier.fillMaxSize().zIndex(1f)
            )
            // 添加灰色遮罩
            // 渐变层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // 文字
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ){
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = SourceHanSerif,
                    lineHeight = 1.5.em,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "${source} · 1分钟前",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }

}


@Composable
fun MarkdownRenderer(markdown: String) {
    // 简单的解析规则
    // 图片: ![alt](url)
    // 粗体: **text**
    // 斜体: *text*

    val markdown_process = markdown.split("\n\n").map {
        it.trimIndent().replace("","")
    }.filter {
        it.isNotBlank()
    }.joinToString(separator = "\n\n")

//    Log.i("text", markdown_process)

    val parts = parseMarkdown(markdown_process)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        parts.forEach { part ->
            Log.i("PartsRenderer", part.toString())
            when (part) {
                is MarkdownPart.Text -> RenderText(part)
                is MarkdownPart.Image -> RenderImage(part.url, part.alt)
                is MarkdownPart.Link -> {}
            }
        }
    }
}

@Composable
private fun RenderText(text_block: MarkdownPart.Text) {
    SelectionContainer {
        Text(text = buildAnnotatedString {
            for(block in text_block.iter()){
                Log.i("Renderer", block.toString())
                when(block){
                    is MarkdownTextBlock.Bold -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Normal)){
                            append(block.content)
                        }
                    }
                    is MarkdownTextBlock.Italic -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Normal, fontStyle = FontStyle.Italic)){
                            append(block.content)
                        }
                    }
                    is MarkdownTextBlock.Normal -> {
                        append(block.content)
                    }
                }
            }
        }, fontSize = 21.sp, color = Color.Black, // , modifier = Modifier.padding(5.dp,5.dp)
            style = TextStyle.Default.copy(
                lineBreak = ChineseParagraphLineBreak,
                lineHeight = 1.8.em,  // 添加适当的行高
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false  // 移除默认的字体内边距
                )
            )
            , textAlign = TextAlign.Start
            , modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp) // 添加内边距, vertical = 14.dp
            , fontFamily = SourceHanSerif)
    }
}

@Composable
private fun RenderImage(url: String, alt: String) {
    Log.i("img_url",url)
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = alt,
        modifier = Modifier
            .fillMaxWidth(0.8f)
//            .height(200.dp)
        , onError = {
            Log.e("img_err",it.toString())
        }, onSuccess = {
            Log.i("img_success","SUCCESS!!!!")
        }
    )
//    Image(
//        painter = rememberImagePainter(url),
//        contentDescription = alt,
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//    )
}

// Markdown解析部分的数据类
sealed class MarkdownPart {
    data class Text(private val block_arr: List<MarkdownTextBlock>) : MarkdownPart(){
        fun iter(): Iterator<MarkdownTextBlock> {
            return this.block_arr.iterator()
        }
    }
    data class Image(val url: String, val alt: String) : MarkdownPart()
//    data class Bold(val content: String) : MarkdownPart()
//    data class Italic(val content: String) : MarkdownPart()
    data class Link(val url: String, val alt: String) : MarkdownPart()

}

// Markdown文字解析部分的数据类
sealed class MarkdownTextBlock {
    data class Normal(val content: String) : MarkdownTextBlock()
    data class Bold(val content: String) : MarkdownTextBlock()
    data class Italic(val content: String) : MarkdownTextBlock()

    override fun toString(): String {
        return when{
            this is Normal -> this.content.trim()
            this is Bold -> this.content.trim()
            this is Italic -> this.content.trim()
            else -> "[ERR]"
        }
    }
}

// 简单的解析函数
fun parseMarkdown(markdown: String): List<MarkdownPart> {
    var parts = mutableListOf<MarkdownPart>()
    var currentText = StringBuilder()
    var currentTextBlock = mutableListOf<MarkdownTextBlock>()

    var index = 0
    while(index < markdown.length){
        val char = markdown[index]
        when(char){
            '[' -> {
                val endAlt = markdown.indexOf("](", index)
                val endUrl = markdown.indexOf(")", endAlt)
                val prefix_is_img = (markdown[index - 1] == '!')

                if (endAlt > -1 && endUrl > -1) {
                    if (currentText.isNotEmpty()) {
//                        Log.i("renderer-type", "normal(" + currentText.toString() + ")")
                        if(prefix_is_img){ // 这里是判断`[`而不是`[`前面的`!`，所以需要把当成正常文字的`!`给删掉
                            currentText.deleteAt(currentText.lastIndex)
                        }
                        currentTextBlock.add(MarkdownTextBlock.Normal(currentText.toString()))
                        currentText.clear()
                    }

                    if (currentTextBlock.isNotEmpty()) {
//                        Log.i("renderer-type", "Text(" + currentTextBlock.joinToString { "," } + ")")
                        parts.add(MarkdownPart.Text(currentTextBlock.toList()))
                        currentTextBlock.clear()
                    }

                    val url = markdown.substring(endAlt + 2, endUrl)
                    val alt = markdown.substring(index + 1, endAlt)
                    val block = if(prefix_is_img) MarkdownPart.Image(url, alt) else MarkdownPart.Link(url, alt)
                    parts.add(block)
                    index = endUrl + 1
                }
            }
            '*' -> {
                val suffix = markdown.indexOf("*", index + 2) // 因为不知道index + 1是不是星号（也就是粗体标记）
                val prefix_is_bold = (markdown[index + 1] == '*')

                if(suffix > -1){

                    if (currentText.isNotEmpty()) {
//                        Log.i("renderer-type", "normal(" + currentText.toString() + ")")
                        currentTextBlock.add(MarkdownTextBlock.Normal(currentText.toString()))
                        currentText.clear()
                    }

                    val block = if(prefix_is_bold){
//                        Log.i("renderer-type", "bold(" + markdown.substring(index + 2, suffix) + ")")
                        MarkdownTextBlock.Bold(markdown.substring(index + 2, suffix))
                    }else{
//                        Log.i("renderer-type", "italic(" + markdown.substring(index + 1, suffix) + ")")
                        MarkdownTextBlock.Italic(markdown.substring(index + 1, suffix))
                    }

                    currentTextBlock.add(block)
                    index = if(prefix_is_bold) (suffix + 3) else (suffix + 2)
                }
            }
            else -> {
                currentText.append(markdown[index++])
            }
        }
    }

    if (currentText.isNotEmpty()) {
        val normal_block = MarkdownTextBlock.Normal(currentText.toString())
        currentTextBlock.add(normal_block)
        currentText.clear()
//        Log.i("renderer-type", normal_block.toString())
    }

    if (currentTextBlock.isNotEmpty()) {
        parts.add(MarkdownPart.Text(currentTextBlock.toList())) // 默认是浅拷贝，那么下面的一旦清除，所有的都会被清除，所以用toList做深拷贝的作用
        currentTextBlock.clear()
    }

//    Log.i("renderer",parts.joinToString { "," })

    return parts
}
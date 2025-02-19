package com.tn233.hamster

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.room.Room
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.tn233.hamster.ui.theme.HamsterNewsTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext
import kotlin.math.absoluteValue


val news_category = arrayOf("推荐","时政&国际","财经","科技","体育")
//val selectedCategory = mutableIntStateOf(0)

private val SourceHanSerif_regular = Font(R.font.source_han_serif_sc_regular, FontWeight.W400)
private val SourceHanSerif_medium = Font(R.font.source_han_serif_sc_medium, FontWeight.W500)
private val SourceHanSerif_semibold = Font(R.font.source_han_serif_sc_semibold, FontWeight.W600)
private val SourceHanSerif_bold = Font(R.font.source_han_serif_sc_bold, FontWeight.W700)
val SourceHanSerif = FontFamily(SourceHanSerif_regular, SourceHanSerif_medium, SourceHanSerif_semibold, SourceHanSerif_bold)

//val page_flag = mutableIntStateOf(0) // 0 主页， 1 新闻详情

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    val client = OkHttpClient()

    val topic_list = mutableStateListOf<News>() // 取推送给到的前三个
    val recommend_list = mutableStateListOf<News>() // 这里compose的状态切换要用compose给的mutable系列API

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 主页流加载
        CoroutineScope(Dispatchers.Default).launch {
            recommend_list.clear()
            topic_list.clear()
            val request = Request.Builder().url("http://113.45.206.216:3000/api/news?page=1&limit=5").build();
            try {
                val response: Response = client.newCall(request).execute()
                val res_obj = JSONObject(response.body?.string())
                val news_array_obj = res_obj.getJSONArray("data")
                for(i in 0..2){ // 推荐召回的前三个新闻用于topic
                    val news = news_array_obj.getJSONObject(i)
                    topic_list.add(news_from_json(news))
                }
                for (i in 3..<news_array_obj.length()) { // 后面走常规的推荐流
                    val news = news_array_obj.getJSONObject(i)
                    recommend_list.add(news_from_json(news))
                }
                Log.i("fetch", recommend_list.size.toString())
            } catch (e: Exception) {
                Log.e("GET_NEWS_ERR", e.toString());
            }
        }

        setContent {
            HamsterNewsTheme {
                val pagerState_Home = rememberPagerState(pageCount = { 5 })
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

                var display_flag by remember { mutableStateOf(false) }

                LaunchedEffect(scrollBehavior.state.contentOffset) {
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
//                            colors = TopAppBarDefaults.topAppBarColors(Color.Transparent, Color.White, if(display_flag) Color.Black else Color.White, Color.Black, if(display_flag) Color.Black else Color.White ),
                            title = {
                                News_Header(pagerState_Home)
                            },
                            navigationIcon = {

                            },
                            actions = {

                            },
                            scrollBehavior = scrollBehavior
//                        colors = TODO()
                        )
                    },
                    bottomBar = {
                        BottomAppBar( // MaterialTheme.colorScheme.primaryContainer
                            containerColor = Color.Transparent,
//                        contentColor = MaterialTheme.colorScheme.primary,
                        ) {
//                            val bottomBarIcon = intArrayOf(R.drawable.home, R.drawable.search, R.drawable.star, R.drawable.me)
                            val bottomBarIcon = intArrayOf(R.drawable.home, R.drawable.search, R.drawable.me)
                            Row{
                                bottomBarIcon.forEachIndexed { i, resourceId ->
                                    IconButton(
                                        onClick = {
                                            when(i){
                                                2 -> {
                                                    val intent = Intent(baseContext, AuthActivity::class.java)
//                                                    intent.putExtra("NewsId","123330")
                                                    startActivity(intent)
                                                }
                                                else -> {}
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(1f / (3 - i))
//                                enabled = TODO(),
//                                colors = TODO(),
//                                interactionSource = TODO()
                                    ) {
                                        Icon(painterResource(resourceId),"主页", modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                        }
                    }
                    ) { innerPadding ->
                        HomePage(pagerState_Home, modifier = Modifier.padding(innerPadding).fillMaxHeight())
                    }
                }

            }
        }
    }


    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun TopicCard(news_list: SnapshotStateList<News>) {
        Spacer(modifier = Modifier.height(5.dp))

        val pagerState = rememberPagerState(pageCount = {
            3
        })
        val time_display = news_list.map { timestamp_to_display(it.timestamp) }

        Card(colors = CardDefaults.cardColors(Color.Gray,Color.Gray,Color.Gray,Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 0.dp)
                .height(400.dp)
                .clip(RoundedCornerShape(30.dp))
        ){

//            var topic_news = arrayOf(arrayOf("习近平抵达巴西利亚开始对巴西进行国事访问","中国新闻网 · 1分钟前"), arrayOf("英国副首相承认已经为乌克兰培训了约 2.2 万人的武装力量","xx新闻 · 2分钟前"), arrayOf("英国副首相承认已经为乌克兰培训了约 2.2 万人的武装力量","xx新闻 · 3分钟前"))
            HorizontalPager(state = pagerState){ page ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth().clickable {
                            val intent = Intent(baseContext, NewsActivity::class.java)
                            intent.putExtra("NewsId",news_list[page].id)
                            startActivity(intent)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.slide_in_left,R.anim.slide_out_right)
                            } else {
//                    android.R.anim.slide_in_left
                                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                            }
                        }
                ) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(news_list[page].cover_image)
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
                            text = news_list[page].title,
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SourceHanSerif,
                            lineHeight = 1.35.em
                        )
//                            Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "${news_list[page].source} · ${time_display[page]}",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NewsCard(news: News) { // name: String, modifier: Modifier = Modifier type: String,
        val time_display = timestamp_to_display(news.timestamp)
        val have_cover_image = news.cover_image.isNotBlank()
        Card(colors = CardDefaults.cardColors(Color.Transparent,Color.Transparent,Color.Transparent,Color.Transparent),
            onClick = {
                val intent = Intent(this, NewsActivity::class.java)
                intent.putExtra("NewsId",news.id)
                startActivity(intent)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.slide_in_left,R.anim.slide_out_right)
                } else {
//                    android.R.anim.slide_in_left
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
                }
            }){ // type = news_image, news
            Row(modifier = Modifier.fillMaxWidth()
                .padding(20.dp, 7.dp)
                .height(110.dp)
                , verticalAlignment = Alignment.CenterVertically
            ){

                val fraction = if(have_cover_image) 0.7f else 1f
                Column(modifier = Modifier.fillMaxWidth(fraction), verticalArrangement = Arrangement.Center){
                    Text(news.title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Color.Black, modifier = Modifier.padding(5.dp,5.dp)
                        , style = TextStyle.Default.copy(
                            lineBreak = LineBreak.Paragraph
                        )
                        , overflow = TextOverflow.Ellipsis
                        , maxLines = 2
                    )
                    Text("${news.source} · ${time_display}", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(5.dp,5.dp))
                }
                if(have_cover_image){
                    Surface(shape = RoundedCornerShape(10.dp), color = Color.DarkGray, modifier = Modifier.fillMaxSize().padding(10.dp)) {
                        // 放图片的地方
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(news.cover_image)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Image"
//                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)) // .padding(10.dp)
//                                .fillMaxWidth(0.8f)
//            .height(200.dp)
                            , onError = {
                                Log.e("img_err",it.toString())
                            }, onSuccess = {
                                Log.i("img_success","SUCCESS!!!!")
                            },
                            contentScale = ContentScale.Crop
                        )
                    }

//                    Card(colors = CardDefaults.cardColors(Color.Gray,Color.Gray,Color.Gray,Color.Gray),
//                        modifier = Modifier
////                .padding(20.dp, 7.dp)
//                            .fillMaxSize()
//                            .padding(10.dp)
////                    .clip(RoundedCornerShape(30.dp))
//                    ){
//
//                    }
                }
            }
            Divider(thickness = 0.7.dp, color = Color(0x106c6b6a), modifier = Modifier.padding(0.dp, 0.dp)) // 分割线
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun CategoryTab(pagerState: PagerState) { // name: String, modifier: Modifier = Modifier
        val scrollCoroutineScope = rememberCoroutineScope() // 为什么这里不能用常规的Coroutine！？而非要使用remember Coroutine，不然就会报
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 20.dp, 0.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            news_category.forEachIndexed { index, category_name ->
                TextButton(
                    onClick = {
                        scrollCoroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (pagerState.currentPage == index)
                            Color(0xAFBDB6AD) else Color.Transparent,
                        contentColor = if (pagerState.currentPage == index)
                            Color.Black else Color(0xFF6C6B6A)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(category_name, modifier = Modifier.padding(5.dp, 2.dp), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
//@Preview(showBackground = true)
    @Composable
    fun HomePage(pagerState_Home: PagerState, modifier: Modifier) {
        HamsterNewsTheme {
            HorizontalPager(state = pagerState_Home, modifier = modifier) { page ->
                Log.i("???page", pagerState_Home.currentPage.toString())
                // 双向绑定
//            LaunchedEffect(pagerState_Home.currentPage) {
//                selectedCategory.intValue = pagerState_Home.currentPage
//            }
//            LaunchedEffect(selectedCategory.intValue){
//                pagerState_Home.scrollToPage(selectedCategory.intValue)
//            }

                LazyColumn(modifier = Modifier.fillMaxSize()){
                    when(page){
                        0 -> {
                            item{
                                TopicCard(topic_list)
                            }

                            items(recommend_list){ news ->
                                NewsCard(news)
                            }
                        }
                        1 -> {
                            repeat(5){
                                item {
                                    NewsCard(News("1","标题测试","hahahah","新闻来源",233L, "", "", ""))
                                }
                            }
                        }
                        2 -> {
//                            item{
//                                TopicCard()
//                            }
                            repeat(5){
                                item {
                                    NewsCard(News("1", "标题测试","hahahah","新闻来源",233L, "", "", ""))
                                }
                            }
                        }
                        3 -> {
                            repeat(5){
                                item {
                                    NewsCard(News("1","标题测试","hahahah","新闻来源",233L, "", "", ""))
                                }
                            }
                        }
                        4 -> {
                            repeat(5){
                                item {
                                    NewsCard(News("1","标题测试","hahahah","新闻来源",233L, "", "", ""))
                                }
                            }
                        }
                        else -> {

                        }
                    }
                }
            }

        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun News_Header(pagerState: PagerState){
        Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(10.dp,0.dp)){
            Image(painterResource(R.drawable.logo_hamster),"仓鼠新闻logo", modifier = Modifier.size(50.dp, 50.dp))
            Spacer(modifier = Modifier.width(10.dp))
            CategoryTab(pagerState)
//                                Text("仓鼠新闻", fontWeight = FontWeight.SemiBold, fontSize = 25.sp)
        }
    }

}


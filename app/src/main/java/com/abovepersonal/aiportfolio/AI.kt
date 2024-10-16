package com.abovepersonal.aiportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.abovepersonal.aiportfolio.network.ChatGPTapi
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCancellationBehavior
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.resetToBeginning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AI : ComponentActivity() {

    private var isNetworkConnected = MutableStateFlow(false)

    val coroutineScope = CoroutineScope(Dispatchers.Default)

    val chatList = MutableStateFlow<SnapshotStateList<MessageInfo>>(mutableStateListOf())
    val userRequestText = MutableStateFlow("")
    val responseOfAIText = MutableStateFlow("")

    private val openAIClass = ChatGPTapi()

    private var widthDisplay = 0f
    private var heightDisplay = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        realizeValues()

        checkInternetConnection()


        setContent {
            MainPreview()
        }
    }

    private fun realizeValues(){

        val displayMetrics = resources.displayMetrics

        widthDisplay = displayMetrics.widthPixels.toFloat()
        heightDisplay = displayMetrics.heightPixels.toFloat()

        val onBackCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /** TODO: */
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            onBackCallback
        )
    }

    private fun sendText(text: String){
        checkInternetConnection()

        if (isNetworkConnected.value) {
            // add message
            chatList.value.add(MessageInfo(text, getString(R.string.you), true))
            userRequestText.value = ""

            coroutineScope.launch(Dispatchers.IO) {
                responseOfAIText.value = makeChatGPTRequest(text)

                // add message
                chatList.value.add(MessageInfo(responseOfAIText.value, getString(R.string.ai), false))
            }
        } else {
            Utils.message(
                getString(R.string.problem),
                getString(R.string.no_internet_connection),
                getString(R.string.ok)
            )
        }
    }

    private suspend fun makeChatGPTRequest(text: String): String {
        return openAIClass.makeGPTRequest(text)
    }

    private fun checkInternetConnection(){
        val isConnected = Utils.isNetworkConnected(this)

        isNetworkConnected.value = isConnected
    }

    @Composable
    private fun MainPreview() {
        val isNetworkConnectedState by isNetworkConnected.collectAsState()
        val isMessageVisibleState by isMessageVisible.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.whiteDB39))
        ){
            animationsClass.Appear(isNetworkConnectedState) {
                AIChat()
            }

            animationsClass.Appear(!isNetworkConnectedState) {
                InternetConnectionError()
            }

            // Message
            animationsClass.Appear(
                isVisible = isMessageVisibleState,
                duration = 1000
            ){
                Message(isMessageVisibleState)
            }
        }
    }

    @Preview
    @Composable
    private fun Message(isVisible: Boolean = true) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.whiteF2B20).copy(alpha = 0.5f ))
                .clickable(enabled = false){}
        ){
            Box(
                modifier = Modifier
                    .heightIn(100.dp, Dp.Unspecified)
                    .widthIn(150.dp, 350.dp)
                    .align(Alignment.Center)
                    .padding(20.dp)
            ){
                Column {
                    val title by messageTitle.collectAsState()
                    val description by messageDescription.collectAsState()
                    val textOfButton by messageButtonText.collectAsState()
                    val onClickButton by messageOnClick.collectAsState()

                    LottieReverseAnimationComposable(
                        modifier = Modifier
                            .heightIn(0.dp, 70.dp)
                            .aspectRatio(185f / 47f)
                            .align(Alignment.Start)
                            .offset(y = 30.dp),
                        resId = R.raw.top_message,
                        isPlaying = isVisible
                    ) {
                        Text(
                            modifier = Modifier
                                .widthIn(50.dp, Dp.Unspecified)
                                .padding(20.dp, 15.dp)
                                .padding(bottom = 20.dp, start = 10.dp)
                                .align(Alignment.CenterStart),
                            text = title,
                            fontFamily = base_semibold,
                            fontSize = 16.sp,
                            color = colorResource(R.color.whiteF2B15),
                            textAlign = TextAlign.Center
                        )
                    }

                    Box(
                        Modifier
                            .heightIn(100.dp, Dp.Unspecified)
                            .fillMaxWidth()
                            .background(
                                colorResource(R.color.whiteF2B20),
                                RoundedCornerShape(20.dp)
                            )
                            .zIndex(1f)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center).padding(20.dp),
                            text = description,
                            fontFamily = base_regular,
                            fontSize = 14.sp,
                            color = colorResource(R.color.black15WF2)
                        )
                    }

                    LottieReverseAnimationComposable(
                        modifier = Modifier
                            .heightIn(0.dp, 70.dp)
                            .aspectRatio(163f / 49f)
                            .align(Alignment.End)
                            .offset(y = (-30).dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onClickButton()

                                isMessageVisible.value = false
                            },
                        resId = R.raw.bottom_message,
                        isPlaying = isVisible
                    ){
                        Text(
                            modifier = Modifier
                                .widthIn(100.dp, Dp.Unspecified)
                                .padding(20.dp, 15.dp)
                                .padding(top = 20.dp, end = 10.dp)
                                .align(Alignment.CenterEnd),
                            text = textOfButton,
                            fontFamily = base_semibold,
                            fontSize = 16.sp,
                            color = colorResource(R.color.whiteF2B15),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun AIChat() {
        Box {
            AnimatedFullBackground()

            Box {

                val chatListState by chatList.collectAsState()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    item { Spacer(Modifier.height(40.dp)) }

                    items(chatListState) { messageInfo ->
                        if (!messageInfo.isPresented) {
                            if (messageInfo.fromMe) {
                                animationsClass.SelfFromRightAppear {
                                    ChatMessage(info = messageInfo)
                                }
                            } else {
                                animationsClass.SelfFromLeftAppear {
                                    ChatMessage(info = messageInfo)
                                }
                            }

                            messageInfo.isPresented = true
                        } else {
                            ChatMessage(info = messageInfo)
                        }
                    }

                    item { Spacer(Modifier.height(150.dp)) }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(colorResource(R.color.whiteF2B20).copy(alpha = 0.5f))
                        .align(Alignment.BottomCenter)
                ){
                    LottieInfinityAnimationComposable(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(448f / 111)
                            .align(Alignment.Center)
                            .padding(horizontal = 10.dp),
                        resId = R.raw.ai_search_bar_animation,
                        isPlaying = true
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(448f / 111)
                            .align(Alignment.Center)
                            .padding(20.dp)
                            .background(colorResource(R.color.whiteF2B20), CircleShape)
                    ){
                        Spacer(
                            Modifier
                                .height(1.3.dp)
                                .widthIn(50.dp, 200.dp)
                                .fillMaxWidth()
                                .padding(start = 30.dp)
                                .background(colorResource(R.color.black15WF2), CircleShape)
                                .align(Alignment.BottomStart)
                        )

                        Row (
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            val userText by userRequestText.collectAsState()


                            BasicTextField(
                                modifier = Modifier
                                    .heightIn(min = 0.dp, max = 40.dp)
                                    .weight(1f)
                                    .padding(horizontal = 30.dp)
                                    .focusRequester(focusRequester),
                                value = userText,
                                onValueChange = { text ->
                                    userRequestText.value = text
                                },
                                textStyle = TextStyle(
                                    fontFamily = base_semibold,
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.black15WF2)
                                ),
                                cursorBrush = SolidColor(colorResource(R.color.black15WF2)),
                                decorationBox = { textField ->
                                    if (userText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.type_something),
                                            fontFamily = base_semibold,
                                            fontSize = 14.sp,
                                            color = colorResource(R.color.black15WF2)
                                        )
                                    }

                                    textField()
                                }
                            )


                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .border(1.3.dp, colorResource(R.color.black15WF2), CircleShape)
                            ){
                                Text(
                                    modifier = Modifier
                                        .padding(horizontal = 25.dp)
                                        .align(Alignment.Center)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                focusManager.clearFocus()

                                                checkInternetConnection()

                                                if (userText.isNotEmpty()) {
                                                    sendText(userText)
                                                } else {
                                                    Utils.message(
                                                        getString(R.string.problem),
                                                        getString(R.string.field_is_empty_error),
                                                        getString(R.string.ok)
                                                    )
                                                }
                                            }
                                        ),
                                    text = stringResource(R.string.send),
                                    fontFamily = base_semibold,
                                    fontSize = 14.sp,
                                    color = colorResource(R.color.black15WF2),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            var isCopyrightVisible by remember { mutableStateOf(false) }

            LottieInfinityAnimationComposable(
                modifier = Modifier
                    .width(40.dp)
                    .align(Alignment.TopEnd)
                    .aspectRatio(40f / 37f)
                    .offset(y = (35).dp, x = (-15).dp)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                        isCopyrightVisible = true
                    },
                resId = R.raw.info_icon,
                isPlaying = true,
                speed = 0.5f
            )

            animationsClass.Appear(isCopyrightVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(R.color.whiteF2B20).copy(alpha = 0.5f))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            isCopyrightVisible = false
                        }
                ){
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Created by Orest Tretiak©",
                            fontFamily = base_regular,
                            fontSize = 16.sp,
                            color = colorResource(R.color.black15WF2),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "12.10.2024",
                            fontFamily = base_regular,
                            fontSize = 16.sp,
                            color = colorResource(R.color.black15WF2),
                            textAlign = TextAlign.Center
                        )
                    }

                }
            }

        }
    }

    @Composable
    private fun InternetConnectionError() {
        Box (
            modifier = Modifier.clickable(enabled = false) {}
        ){
            AnimatedFullBackground()


            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){

                Spacer(Modifier.weight(2.5f))

                Image(
                    modifier = Modifier
                        .aspectRatio(218f / 180f)
                        .widthIn(200.dp, 400.dp)
                        .padding(horizontal = 40.dp),
                    painter = painterResource(R.drawable.internet_icon),
                    contentDescription = null,
                )

                Spacer(Modifier.height(40.dp))

                Text(
                    text = stringResource(R.string.no_internet_connection),
                    fontFamily = base_bold,
                    fontSize = 16.sp,
                    color = colorResource(R.color.black15WF2)
                )

                Spacer(Modifier.height(20.dp))
                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .background(colorResource(R.color.black20WF2), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                checkInternetConnection()
                            }
                        )
                ){
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 30.dp, vertical = 10.dp),
                        text = stringResource(R.string.try_again),
                        fontFamily = base_bold,
                        fontSize = 16.sp,
                        color = colorResource(R.color.whiteF2B15)
                    )
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }

    @Composable
    private fun AnimatedFullBackground() {
        val infiniteTransition = rememberInfiniteTransition()

        val xOffset1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = widthDisplay,
            animationSpec = infiniteRepeatable(
                tween(12_000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val yOffset1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 500f,
            animationSpec = infiniteRepeatable(
                tween(10_000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val xOffset2 by infiniteTransition.animateFloat(
            initialValue = widthDisplay,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                tween(15_000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val yOffset2 by infiniteTransition.animateFloat(
            initialValue = heightDisplay,
            targetValue = heightDisplay - 400f,
            animationSpec = infiniteRepeatable(
                tween(12_000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val brush1 = Brush.radialGradient(
            colors = listOf(colorResource(R.color.animatedBackground1).copy(alpha = 0.6f), Color.Transparent),
            center = Offset(xOffset1, yOffset1),
            radius = 800f,
        )

        val brush2 = Brush.radialGradient(
            colors = listOf(colorResource(R.color.animatedBackground2).copy(alpha = 0.6f), Color.Transparent),
            center = Offset(xOffset2, yOffset2),
            radius = 800f,
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.whiteDB39))
        ){
            drawRect(
                brush1
            )

            drawRect(
                brush2
            )
        }
    }

    @Composable
    private fun ChatMessage(modifier: Modifier = Modifier, info: MessageInfo) {
        Box(
            Modifier.fillMaxWidth()
        ){
            Column(
                modifier = modifier
                    .align(if (info.fromMe) Alignment.CenterEnd else Alignment.CenterStart)
                    .widthIn(100.dp, 300.dp),
                horizontalAlignment = if (info.fromMe) Alignment.End else Alignment.Start
            ){
                Text(
                    modifier = Modifier
                        .widthIn(50.dp, Dp.Unspecified)
                        .background(colorResource(R.color.black20WF2) , CircleShape)
                        .padding(horizontal = 15.dp, vertical = 10.dp),
                    text = info.from,
                    fontFamily = base_regular,
                    fontSize = 14.sp,
                    color = colorResource(R.color.whiteF2B15),
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier
                        .background(colorResource(R.color.whiteF2B20).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 15.dp, vertical = 15.dp),
                    text = info.text,
                    fontFamily = base_regular,
                    fontSize = 14.sp,
                    color = colorResource(R.color.black15WF2)
                )
            }
        }

    }
}

val base_bold = FontFamily(
    Font(R.font.jost_bold)
)

val base_semibold = FontFamily(
    Font(R.font.jost_semibold)
)

val base_regular = FontFamily(
    Font(R.font.jost_regular)
)

val animationsClass = Animations()

var isMessageVisible = MutableStateFlow(false)
var messageDescription = MutableStateFlow("")
var messageTitle = MutableStateFlow("")
var messageButtonText = MutableStateFlow("")
var messageOnClick = MutableStateFlow {}

@Composable
fun LottieInfinityAnimationComposable(
    modifier: Modifier = Modifier,
    resId: Int,
    isPlaying: Boolean,
    speed: Float = 1f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier){
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))

        // to control the animation
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = isPlaying,
            restartOnPlay = false,
            speed = speed
        )

        LottieAnimation(
            modifier = Modifier.fillMaxSize(),
            composition = composition,
            progress = progress
        )

        content()
    }
}


@Composable
fun LottieReverseAnimationComposable(
    modifier: Modifier = Modifier,
    resId: Int,
    isPlaying: Boolean,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier){
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))

        // to control the animation
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = 1,
            speed = if (isPlaying) 1f else -1.5f,
            restartOnPlay = false
        )


        LottieAnimation(
            modifier = Modifier.fillMaxSize(),
            composition = composition,
            progress = progress
        )

        content()
    }
}
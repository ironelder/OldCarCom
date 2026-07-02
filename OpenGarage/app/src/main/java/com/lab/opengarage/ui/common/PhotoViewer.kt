package com.lab.opengarage.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage

/**
 * 게시글 사진 가로 페이저. 잘림 없이(Fit) 표시, 로딩 스피너·실패 안내 포함, 탭하면 전체화면.
 * SVG 도해도 렌더링됨(앱 ImageLoader 에 SvgDecoder 등록).
 */
@Composable
fun PhotoPager(urls: List<String>, modifier: Modifier = Modifier) {
    if (urls.isEmpty()) return
    var viewerStart by remember { mutableStateOf(-1) }
    val pagerState = rememberPagerState(pageCount = { urls.size })

    Box(modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) { page ->
            SubcomposeAsyncImage(
                model = urls[page],
                contentDescription = null,
                contentScale = ContentScale.Fit,
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "이미지를 불러올 수 없어요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewerStart = page },
            )
        }
        if (urls.size > 1) {
            Text(
                text = "${pagerState.currentPage + 1} / ${urls.size}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }

    if (viewerStart >= 0) {
        FullscreenPhotoViewer(urls = urls, startIndex = viewerStart, onDismiss = { viewerStart = -1 })
    }
}

/** 전체화면 사진 뷰어. 좌우 스와이프 + 핀치 줌. */
@Composable
fun FullscreenPhotoViewer(urls: List<String>, startIndex: Int, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { urls.size })
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                SubcomposeAsyncImage(
                    model = urls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("이미지를 불러올 수 없어요", color = Color.White)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset = if (scale > 1f) offset + pan else Offset.Zero
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                Text("닫기", color = Color.White)
            }
            if (urls.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${urls.size}",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                )
            }
        }
    }
}

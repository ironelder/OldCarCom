package com.lab.opengarage.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * 접히는 상단 헤더: 타이틀은 항상 고정, [expandedContent] 는 리스트 스크롤에 따라 접힘/펼침.
 * 리스트 최상단 → 검색 영역 완전 노출 / 아래로 스크롤 → 접히고 타이틀만 / 위로 스크롤 → 다시 펼침.
 *
 * exitUntilCollapsedScrollBehavior 의 heightOffset(0~limit) 을 그대로 배치에 반영한다.
 * Layout 안에서 확장 콘텐츠를 항상 원본 높이로 측정하므로 축소-재측정 피드백 루프가 없다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingHeader(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit,
) {
    val fraction = scrollBehavior.state.collapsedFraction
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = (3f * fraction).dp,
    ) {
        Layout(
            modifier = Modifier.clipToBounds(),
            content = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = 1f - fraction },
                ) { expandedContent() }
            },
        ) { measurables, constraints ->
            val loose = constraints.copy(minHeight = 0)
            val titleP = measurables[0].measure(loose)
            val expandedP = measurables[1].measure(loose)

            val limit = -expandedP.height.toFloat()
            if (scrollBehavior.state.heightOffsetLimit != limit) {
                scrollBehavior.state.heightOffsetLimit = limit
            }
            val offset = scrollBehavior.state.heightOffset.coerceIn(limit, 0f).roundToInt()
            val expandedVisible = (expandedP.height + offset).coerceAtLeast(0)
            val totalH = titleP.height + expandedVisible

            layout(constraints.maxWidth, totalH) {
                titleP.place(0, 0)
                expandedP.place(0, titleP.height + offset)
            }
        }
    }
}

/** 스크롤이 [threshold] 인덱스 이상 내려갔을 때 나타나는 '맨 위로' 버튼. */
@Composable
fun ScrollToTopButton(
    listState: LazyListState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    threshold: Int = 3,
) {
    val visible by remember {
        derivedStateOf { listState.firstVisibleItemIndex > threshold }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "맨 위로")
        }
    }
}

package com.example.janken

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.janken.ui.theme.JankenTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JankenTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Janken()
                }
            }
        }
    }
}

/* じゃんけんゲームのコンポーネント
ユーザはボタンを押して手を選択する
コンピュータはランダムに手を選択する
リセットボタンを押すとリセットされる
ユーザが選択した手とコンピュータが選択した手を表示する */
@Composable
fun Janken() {
    // 勝敗数を状態として保存する
    var userScore by remember { mutableStateOf(0) }
    var computerScore by remember { mutableStateOf(0) }
    var drawScore by remember { mutableStateOf(0) }
    // ユーザの手とコンピュータの手を状態として保存する
    var userHand by remember { mutableStateOf(100) }
    var computerHand by remember { mutableStateOf(100) }
    // 勝敗を状態として保存する
    var result by remember { mutableStateOf(100) }
    // 対戦数を状態として保存する
    var battleCount by remember { mutableStateOf(1) }

    // じゃんけんゲームのUIを表示する
    Column {
        // じゃんけんゲームのタイトルを表示する、中央揃え、サイズ大きめ
        Title()
        // 縦方向にSpacerを入れる
        Spacer(modifier = Modifier.height(16.dp))
        // 戦績を表示する
        Score(userScore, computerScore, drawScore)
        // 何回戦かを表示する
        BattleCount(battleCount)
        // じゃんけんの選択肢を表示する
        // ボタンがクリックされたらbattleCountをインクリメントする
        // クリックされたボタンに応じて勝敗数をアップグレードする
        // ボタンがクリックされたらユーザの手を保存する
        // ボタンがクリックされたらコンピュータの手を保存する
        // ボタンがクリックされたら勝敗を判定する
        // Q. itがunresolved referenceになるのを直してください
        // A. onClickの引数をitからhandに変更してください

        JankenButtons {
            battleCount++
            userHand = it
            computerHand = decideComputerHand()
            result = judge(userHand, computerHand)
            when (result) {
                0 -> computerScore++
                1 -> userScore++
                else -> drawScore++
            }
        }
        // 縦方向にSpacerを入れる
        Spacer(modifier = Modifier.height(16.dp))
        // じゃんけんの結果を表示する
        JankenResult(userHand, computerHand, result)
        // リセットボタンを表示する
        // ボタンがクリックされたらbattleCount、userScore、computerScore、drawScore、userHand、computerHandをリセットする
        ResetButton{
            battleCount = 1
            userScore = 0
            computerScore = 0
            drawScore = 0
            userHand = 100
            computerHand = 100
        }
    }
}

// タイトル
@Composable
fun Title() {
    Text(
        text = stringResource(R.string.title),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineLarge
    )
}

// 引数はbattleCount
// デフォルトでは「Game: n\n Ready...」、
// 日本語では「n回戦目\n じゃんけん…」と表示する
// Q. デフォルト言語と日本語で表示を変えることは可能ですか？
// A. 可能です。stringResourceの引数には、R.string.titleのようにリソースIDを指定する代わりに、
// R.string.title_jaのように、リソースIDの後ろに言語コードを指定することで、
// その言語のリソースを取得することができます。
// 例えば、R.string.title_jaの場合は、strings.xmlのtitle_jaのリソースを取得します。
@Composable
fun BattleCount(battleCount: Int) {
    Text(
        text = if (isEnglish()) {
            stringResource(R.string.game) + ":" + battleCount.toString()
        } else {
            battleCount.toString() + stringResource(R.string.game)
        },
        textAlign = TextAlign.Center
    )
    Text(text = stringResource(R.string.ready),
        textAlign = TextAlign.Center
    )
}

// じゃんけんの選択肢をボタンとして表示する
// 引数はボタンのテキストとボタンを押した時の処理
@Composable
fun JankenButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(100.dp)
    ) {
        Text(text = text)
    }
}

// JankenButtonを横に3つ並べるコンポーネント
@Composable
fun JankenButtons(onClick: (Int) -> Unit) {
    Row {
        JankenButton(text = stringResource(R.string.rock)) { onClick(0) }
        Spacer(modifier = Modifier.width(16.dp))
        JankenButton(text = stringResource(R.string.scissors)) { onClick(1) }
        Spacer(modifier = Modifier.width(16.dp))
        JankenButton(text = stringResource(R.string.paper)) { onClick(2) }
    }
}

// じゃんけんの勝敗を決定する
// 引数はユーザの手とコンピュータの手
// 戻り値は0~2の整数
// 0: ユーザの勝ち, 1: コンピュータの勝ち, 2: 引き分け, 100: エラー
// 例: ユーザがグー、コンピュータがチョキの場合は0を返す
@VisibleForTesting
internal fun judge(userHand: Int, computerHand: Int): Int {
    var result = 100
    when (userHand) {
        0 -> {
            when (computerHand) {
                0 -> result = 2
                1 -> result = 0
                2 -> result = 1
            }
        }
        1 -> {
            when (computerHand) {
                0 -> result = 1
                1 -> result = 2
                2 -> result = 0
            }
        }
        2 -> {
            when (computerHand) {
                0 -> result = 0
                1 -> result = 1
                2 -> result = 2
            }
        }
    }
    return result
}

// コンピュータの手をランダムに決定する
fun decideComputerHand(): Int {
    // 0, 1, 2のいずれかをランダムに選択する
    return (0..2).random()
}

// Fetch user language
fun isEnglish(): Boolean {
    return Locale.getDefault().language == "en"
}

// 引数を受け取ってじゃんけんの結果を表示する
@Composable
fun JankenResult(userHand: Int, computerHand: Int, result: Int) {
    // ユーザの手を表示する
    Text(text = stringResource(R.string.your_hand) + ": "
            + when(userHand) {
                0 -> stringResource(R.string.rock)
                1 -> stringResource(R.string.scissors)
                2 -> stringResource(R.string.paper)
              else -> ""
    })
    // コンピュータの手を表示する
    Text(text = stringResource(R.string.cpu_hand) + ": "
            + when(computerHand) {
                0 -> stringResource(R.string.rock)
                1 -> stringResource(R.string.scissors)
                2 -> stringResource(R.string.paper)
              else -> ""
    })
    // 勝敗を表示する
    Text(text = when (result) {
        0 -> stringResource(R.string.you_win)
        1 -> stringResource(R.string.cpu_win)
        2 -> stringResource(R.string.you_draw)
        else -> ""
    })
}

// リセットボタンを表示する
@Composable
fun ResetButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = stringResource(R.string.reset))
    }
}

// 勝敗数を表示する（形式は◯勝◯敗◯分）
@Composable
fun Score(userScore: Int, computerScore: Int, drawScore: Int) {
    Text(text = userScore.toString() + stringResource(R.string.win) + " "
              + computerScore.toString() + stringResource(R.string.lose) + " "
              + drawScore.toString() + stringResource(R.string.draw))
}

// じゃんけんゲームUIのプレビュー
@Preview(showBackground = true)
@Composable
fun JankenPreview() {
    JankenTheme {
        Janken()
    }
}

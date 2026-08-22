package ua.syt0r.kanji.presentation.common.resources.string

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import ua.syt0r.kanji.presentation.common.CommonDateTimeFormat
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.withClickableUrl
import kotlin.time.Duration

object JapaneseStrings : Strings {

    override val appName: String = "Kaiteyo (書いてよ)"

    override val hiragana: String = "ひらがな"
    override val katakana: String = "カタカナ"

    override val kunyomi: String = "訓読み"
    override val onyomi: String = "音読み"

    override val loading: String = "読み込み中"

    override val letterPracticeTypeWriting: String = "書く事"
    override val letterPracticeTypeReading: String = "読む事"
    override val vocabPracticeTypeFlashcard: String = "フラッシュカード"
    override val vocabPracticeTypeReadingPicker: String = "読み取りピッカー"
    override val vocabPracticeTypeWriting: String = "書く事"

    override val reviewStateDone: String = "完了"
    override val reviewStateDue: String = "復習"
    override val reviewStateNew: String = "未習"

    override val home: HomeStrings = JapaneseHomeStrings
    override val commonDashboard = JapaneseCommonDashboardStrings
    override val dailyLimit = JapaneseDailyLimitStrings
    override val tutorialDialog: TutorialDialogStrings = JapaneseTutorialDialogStrings
    override val stats: StatsStrings = JapaneseStatsStrings
    override val search: SearchStrings = JapaneseSearchStrings
    override val alternativeDialog: AlternativeDialogStrings = JapaneseAlternativeDialogStrings

    override val settings: SettingsStrings = JapaneseSettingsStrings
    override val reminderDialog: ReminderDialogStrings = JapaneseReminderDialogStrings
    override val about: AboutStrings = JapaneseAboutStrings
    override val backup: BackupStrings = JapaneseBackupStrings
    override val feedback: FeedbackStrings = JapaneseFeedbackStrings
    override val sponsor: SponsorStrings = JapaneseSponsorStrings
    override val account: AccountScreenStrings = JapaneseAccountScreenStrings
    override val sync: SyncScreenStrings = JapaneseSyncScreenStrings
    override val syncDialog: SyncDialogStrings = JapaneseSyncDialogStrings
    override val syncSnackbar: SyncSnackbarStrings = JapaneseSyncSnackbarStrings

    override val deckPicker: DeckPickerStrings = JapaneseDeckPickerStrings
    override val deckEdit: DeckEditStrings = JapaneseDeckEditStrings
    override val deckDetails: DeckDetailsStrings = JapaneseDeckDetailsStrings
    override val commonPractice: CommonPracticeStrings = JapaneseCommonPracticeStrings
    override val letterPractice: LetterPracticeStrings = JapaneseLetterPracticeStrings
    override val vocabPractice: VocabPracticeStrings = JapaneseVocabPracticeStrings
    override val info: InfoScreenStrings = JapaneseInfoScreenStrings

    override val urlPickerMessage: String = "開く"
    override val urlPickerErrorMessage: String = "ブラウザーが見つかりません"

    override val reminderNotification: ReminderNotificationStrings =
        JapaneseReminderNotificationStrings

    override val nav: NavStrings = JapaneseNavStrings
    override val commandPalette: CommandPaletteStrings = JapaneseCommandPaletteStrings
    override val kanjiBrowser: KanjiBrowserStrings = JapaneseKanjiBrowserStrings
    override val mindMap: MindMapStrings = JapaneseMindMapStrings
    override val collections: CollectionsStrings = JapaneseCollectionsStrings
    override val library: LibraryStrings = JapaneseLibraryStrings

}

object JapaneseKanjiBrowserStrings : KanjiBrowserStrings {
    override val title: String = "漢字ブラウザー"
    override val detail: KanjiDetailStrings = JapaneseKanjiDetailStrings
    override val navigateUpDescription: String = "戻る"
    override val selectionModeDescription: String = "選択モードを有効にする"
    override val selectionModeActiveDescription: String = "選択モードを無効にする"
    override val showFiltersDescription: String = "絞り込みを表示"
    override val hideFiltersDescription: String = "絞り込みを非表示"
    override val showRadicalsDescription: String = "部首を表示"
    override val hideRadicalsDescription: String = "部首を非表示"
    override val switchToListDescription: String = "リスト表示に切り替える"
    override val switchToGridDescription: String = "グリッド表示に切り替える"
    override val searchLabel: String = "漢字を検索"
    override val clearSearchDescription: String = "漢字検索をクリア"
    override val filtersTitle: String = "絞り込み"
    override val resetFilters: String = "すべての絞り込みをリセット"
    override val selectedCount: (Int) -> String = { "$it 個選択中" }
    override val flagAction: String = "フラグ"
    override val tagAction: String = "タグ"
    override val favoriteAction: String = "お気に入り"
    override val resetProgressAction: String = "進捗をリセット"
        override val clearSelection: String = "選択を解除"
    override val loadingMessage: String = "漢字を読み込んでいます…"
    override val noKanjiFound: String = "漢字が見つかりません"
    override val adjustFiltersMessage: String = "絞り込みを調整するか、解除してください"
    override val searchPrompt: String = "漢字・読み・意味を検索"
    override val clearFilters: String = "絞り込みを解除"
    override val radicalSearchTitle: String = "部首検索"
    override val radicalSelectedCount: (Int) -> String = { "$it 個選択中" }
    override val clearRadicals: String = "部首を解除"
    override val allStrokes: String = "すべての画数"
    override val jlptFilter: String = "JLPT"
    override val gradeFilter: String = "学年"
    override val statusFilter: String = "状態"
    override val flagsFilter: String = "フラグ"
    override val strokesFilter: String = "画数"
    override val frequencyFilter: String = "出現頻度"
    override val sortFilter: String = "並べ替え"
    override val difficultySort: String = "難易度"
    override val lastReviewedSort: String = "最終復習"
    override val kanjiSort: String = "漢字"
    override val minLabel: String = "最小"
    override val maxLabel: String = "最大"
    override val anyValue: String = "指定なし"
    override val decreaseValueDescription: String = "値を減らす"
    override val increaseValueDescription: String = "値を増やす"
    override val noMeaning: String = "意味がありません"
    override val setFlagTitle: String = "フラグを設定"
    override val noFlag: String = "フラグなし"
    override val tagsTitle: String = "タグ"
    override val newTagNamePlaceholder: String = "新しいタグ名"
    override val createButton: String = "作成"
    override val cancelButton: String = "キャンセル"
    override val strokeCount: (Int) -> String = { "${it}画" }
    override val difficultyWarning: String = "難しい"
}
object JapaneseKanjiDetailStrings : KanjiDetailStrings {
    override val headerLabel: String = "漢字の詳細"
    override val loadingMessage: String = "漢字データを読み込んでいます…"
    override val loadErrorMessage: String = "漢字の詳細を読み込めませんでした"
    override val notFoundMessage: String = "アプリのデータパックに漢字がありません"
    override val retryButton: String = "再試行"
    override val meaningTitle: String = "意味"
    override val noMeaningMessage: String = "標準データソースに意味がありません。"
    override val onYomiTitle: String = "音読み"
    override val onYomiSubtitle: String = "主な読み方を先に表示。すべての読み方を展開できます"
    override val kunYomiTitle: String = "訓読み"
    override val kunYomiSubtitle: String = "主な読み方を先に表示。すべての読み方を展開できます"
    override val noOnYomiMessage: String = "音読みの記録がありません。"
    override val noKunYomiMessage: String = "訓読みの記録がありません。"
    override val stopReadingDescription: (String) -> String = { "読み上げを停止: $it" }
    override val playOnReadingDescription: (String) -> String = { "音読みを再生: $it" }
    override val collapseReadings: String = "読み方を折りたたむ"
    override val showAllReadings: (Int) -> String = { "$it 件の読み方をすべて表示" }
    override val writingTitle: String = "書き方"
    override val writingSubtitle: String = "書き順と書字練習"
    override val strokeCount: (Int) -> String = { "${it}画" }
    override val frequencyLabel: (Int) -> String = { "出現頻度 #$it" }
    override val practiceWritingButton: String = "書き取りを練習"
    override val writingExplanation: String = "書字キャンバスと書き順判定は既存の文字練習フローで開きます。"
    override val componentsTitle: String = "部首と構成要素"
    override val noComponentsMessage: String = "構成要素の記録がありません。"
    override val componentStrokeCount: (Int) -> String = { "${it}画目" }
    override val whiteboardTitle: String = "つながるホワイトボード"
    override val whiteboardSubtitle: String = "インタラクティブなキャンバスで標準の関係を探索"
    override val whiteboardExplanation: String = "全グラフをパン・ズームできるホワイトボードに表示し、つながりを空間的に確認できます。"
    override val openWhiteboardButton: String = "ホワイトボードを開く"
    override val vocabularyTitle: String = "単語"
    override val vocabularySubtitle: String = "完全な語彙データベースからの用例"
    override val noVocabularyMessage: String = "この漢字の用例がありません。"
    override val learningStatusTitle: String = "学習状況"
    override val inWritingReview: String = "書字練習で学習済み"
    override val notReviewed: String = "未復習"
    override val difficultLabel: String = "難しい"
}

object JapaneseLibraryStrings : LibraryStrings {
    override val title: String = "ライブラリ"
    override val availableCards: (Int) -> String = { "利用可能な漢字 ${it}字" }
    override val hubDescription: String = "学習ハブ — すべてを一か所で管理"
    override val offlineIndexReady: String = "JMdict全文 · オフライン索引の準備完了"
    override val preparingTitle: String = "オフライン辞書を準備しています…"
    override val preparingMessage: String = "初回セットアップには少し時間がかかる場合があります。JMdictの全データは保持されます。"
    override val errorTitle: String = "オフライン辞書を利用できません"
    override val errorMessage: String = "データは削除されていません。接続を確認してJMdictのセットアップを再試行してください。"
    override val retryButton: String = "再試行"
    override val studySection: String = "学習"
    override val smartListsSection: String = "スマートリスト"
    override val kanjiTitle: String = "漢字"
    override val kanjiSubtitle: String = "すべての漢字を検索・絞り込み・復習"
    override val kanjiDecksTitle: String = "漢字デッキ"
    override val kanjiDecksSubtitle: String = "文字デッキと間隔反復"
    override val vocabularyTitle: String = "語彙"
    override val vocabularySubtitle: String = "単語・用語・語彙デッキ"
    override val grammarTitle: String = "文法"
    override val grammarSubtitle: String = "規則・活用・会話"
    override val radicalsTitle: String = "部首"
    override val radicalsSubtitle: String = "部首構成から文字を検索"
    override val radicalMapTitle: String = "部首マップ"
    override val radicalMapSubtitle: String = "すべての部首と関連漢字を探索"
    override val componentMapTitle: String = "漢字構成要素マップ"
    override val componentMapSubtitle: String = "漢字グラフの構成要素ノードを探索"
    override val customCollectionsTitle: String = "カスタムコレクション"
    override val customCollectionsSubtitle: String = "自分で作成した学習リスト"
    override val favoritesTitle: String = "お気に入り"
    override val favoritesSubtitle: String = "お気に入りの漢字"
    override val pinnedTitle: String = "ピン留め"
    override val pinnedSubtitle: String = "ピン留めした項目へすばやくアクセス"
    override val recentlyLearnedTitle: String = "最近学習"
    override val recentlyLearnedSubtitle: String = "過去7日間に学習した漢字"
    override val allSmartListsTitle: String = "すべてのスマートリスト"
    override val allSmartListsSubtitle: String = "自動生成される動的コレクション"
    override val kanjiStatLabel: String = "漢字"
    override val favoritesStatLabel: String = "お気に入り"
    override val reviewsStatLabel: String = "復習"
    override val tagsStatLabel: String = "タグ"
}

object JapaneseCollectionsStrings : CollectionsStrings {
    override val title: String = "コレクション"
    override val availableCards: (Int) -> String = { "利用可能な漢字 ${it}字" }
    override val smartSectionTitle: String = "スマートコレクション"
    override val tagSectionTitle: String = "タグ別"
    override val customSectionTitle: String = "カスタムコレクション"
    override val noTagsMessage: String = "タグはまだありません。ブラウザーから漢字にタグを付けてコレクションを作成できます。"
    override val noCustomMessage: String = "カスタムコレクションはまだありません。"
    override val autoGeneratedLabel: String = "自動生成"
    override val customLabel: String = "カスタム"
    override val tagLabel: String = "タグ"
    override val flagLabel: String = "フラグ"
    override val backDescription: String = "戻る"
    override val openInBrowser: String = "ブラウザーで開く"
    override val emptyMessage: String = "このコレクションは空です"
    override val cardCount: (Int) -> String = { "漢字 ${it}字" }
    override val smartName: (String) -> String = { key ->
        when (key.removePrefix("smart-")) {
            "recently-learned" -> "最近学習"
            "needs-review" -> "復習が必要"
            "frequently-failed" -> "間違いが多い"
            "not-studied-30-days" -> "30日間未学習"
            "flagged" -> "フラグ付き"
            "favorites" -> "お気に入り"
            else -> key
        }
    }
}

object JapaneseMindMapStrings : MindMapStrings {
    override val radicalsTitle: String = "部首マップ"
    override val componentsTitle: String = "漢字構成要素マップ"
    override val radicalsLabel: String = "部首"
    override val componentsLabel: String = "構成要素"
    override val backDescription: String = "戻る"
    override val openCatalogDescription: String = "マップ一覧を開く"
    override val clearGraphDescription: String = "グラフを閉じる"
    override val selectedGraphTitle: (String) -> String = { "ホワイトボード · $it" }
    override val canonicalGraphDescription: String = "標準の学習つながりグラフ"
    override val canvasDescription: String = "インタラクティブなマインドマップ。ドラッグで移動し、ピンチで拡大縮小できます。"
    override val chooseItemTitle: (String) -> String = { "${it}を選択してホワイトボードを開く" }
    override val chooseItemMessage: String = "パン、ピンチズーム、ノード選択に対応しています。安定したAndroid動作のためグラフの表示数を制限しています。"
    override val openCatalogButton: String = "一覧を開く"
    override val catalogTitle: (String) -> String = { "${it}一覧" }
    override val catalogSubtitle: String = "グラフを表示するルートを選択"
    override val clearSearchDescription: String = "検索をクリア"
    override val searchPlaceholder: (String) -> String = { "${it}を検索" }
    override val loadingCatalog: String = "一覧を読み込んでいます…"
    override val totalItems: (Int, String) -> String = { count, label -> "$count $label" }
    override val noMatches: (String) -> String = { "検索に一致する${it}がありません。" }
    override val loadMore: String = "さらに読み込む"
    override val graphNodeCount: (Int) -> String = { "ノード ${it}個" }
    override val selectNodeTitle: String = "ノードを選択"
    override val selectNodeMessage: String = "ノードをタップすると標準データの詳細を確認できます"
    override val connectedNodeSummary: (String, String, Int) -> String = { kind, depth, connections -> "$kind · 深さ $depth · 接続 ${connections}件" }
    override val connectedKanjiCount: (Int) -> String = { "関連漢字 ${it}字" }
    override val strokeCount: (Int) -> String = { "${it}画" }
    override val nodeDescription: (String, String) -> String = { label, kind -> "$label、${kind}ノード" }
    override val detailsButton: String = "詳細"
    override val componentsButton: String = "構成要素"
    override val zoomOutDescription: String = "縮小"
    override val zoomInDescription: String = "拡大"
    override val fitGraphDescription: String = "グラフを全体表示"
    override val chooseRoot: (String) -> String = { "ルートを選択 · $it" }
}

object JapaneseCommandPaletteStrings : CommandPaletteStrings {
    override val emptyTitle: String = "一致する項目がありません"
    override val emptyMessage: String = "別の検索語を試してください"
    override val escapeHint: String = "Esc"
    override val upHint: String = "上"
    override val downHint: String = "下"
    override val enterHint: String = "決定"
    override val openShortcutHint: String = "Ctrl+Kで開く"
    override val clearQueryDescription: String = "検索をクリア"
    override val searchLabel: String = "コマンドを検索"
    override val dismissDescription: String = "コマンドパレットを閉じる"
    override val actions: CommandPaletteActionStrings = JapaneseCommandPaletteActionStrings
}

object JapaneseCommandPaletteActionStrings : CommandPaletteActionStrings {
    override val kanjiBrowser = CommandPaletteActionCopy(
        title = "漢字ブラウザー",
        subtitle = "漢字を検索・絞り込み・一覧表示",
        keywords = "kanji browse search jlpt radical 漢字 検索 検索画面 部首",
        category = "移動",
    )
    override val radicalMindMap = CommandPaletteActionCopy(
        title = "部首マップ",
        subtitle = "すべての部首と関連する漢字を探索",
        keywords = "radical component map graph explorer 部首 構成 マップ グラフ",
        category = "移動",
    )
    override val kanjiComponentMap = CommandPaletteActionCopy(
        title = "漢字構成マップ",
        subtitle = "漢字グラフの構成ノードを探索",
        keywords = "kanji component map graph explorer 漢字 構成 マップ グラフ",
        category = "移動",
    )
    override val collections = CommandPaletteActionCopy(
        title = "コレクション",
        subtitle = "スマートコレクション・タグ・フラグ",
        keywords = "collections tags flags favorites コレクション タグ フラグ お気に入り",
        category = "移動",
    )
    override val connectedLearning = CommandPaletteActionCopy(
        title = "つながる学習",
        subtitle = "漢字から用例へつながる習得パスを進む",
        keywords = "connected learning kanji map mastery lesson graph 連結 学習 習得 レッスン",
        category = "移動",
    )
    override val favorites = CommandPaletteActionCopy(
        title = "お気に入り",
        subtitle = "お気に入りにした漢字だけを表示",
        keywords = "favorites star starred お気に入り 星",
        category = "絞り込み",
    )
    override val flaggedKanji = CommandPaletteActionCopy(
        title = "フラグ付き漢字",
        subtitle = "フラグが設定された漢字",
        keywords = "flagged flags color フラグ 色",
        category = "絞り込み",
    )
    override val difficultKanji = CommandPaletteActionCopy(
        title = "難しい漢字",
        subtitle = "難易度のしきい値を超えた漢字",
        keywords = "difficult hard problems 難しい 難易度 問題",
        category = "絞り込み",
    )
    override val frequentlyFailed = CommandPaletteActionCopy(
        title = "よく間違える漢字",
        subtitle = "3回以上ラプスした漢字",
        keywords = "failed lapses mistakes 間違い ラプス ミス",
        category = "絞り込み",
    )
    override val cardManager = CommandPaletteActionCopy(
        title = "カード管理",
        subtitle = "旧デッキのカードブラウザー",
        keywords = "decks cards manager anki デッキ カード 管理",
        category = "移動",
    )
    override val statistics = CommandPaletteActionCopy(
        title = "統計",
        subtitle = "ダッシュボードと復習統計",
        keywords = "stats statistics dashboard heatmap 統計 復習 グラフ",
        category = "移動",
    )
    override val closePalette = CommandPaletteActionCopy(
        title = "パレットを閉じる",
        subtitle = "このメニューを閉じる",
        keywords = "close exit dismiss esc 閉じる 終了",
        category = "アプリ",
    )
}

object JapaneseNavStrings : NavStrings {
    override val homeSection: String = "ホーム"
    override val featuresSection: String = "機能"
    override val systemSection: String = "システム"
    override val collapseTooltip: String = "折りたたむ"
    override val expandTooltip: String = "展開"
    override val decksLabel: String = "デッキ"
    override val textAnalysisLabel: String = "テキスト解析"
    override val appearanceLabel: String = "外観"
    override val sponsorLabel: String = "スポンサー"
    override val aboutLabel: String = JapaneseAboutStrings.title
    override val backupLabel: String = JapaneseBackupStrings.title
    override val syncLabel: String = JapaneseSyncScreenStrings.title
}

object JapaneseHomeStrings : HomeStrings {
    override val screenTitle: String = JapaneseStrings.appName
    override val generalDashboardTabLabel: String = "ホーム"
    override val lettersDashboardTabLabel: String = "文字"
    override val vocabDashboardTabLabel: String = "単語"
    override val libraryTabLabel: String = "ライブラリ"
    override val statsTabLabel: String = "統計"
    override val searchTabLabel: String = "検索"
    override val settingsTabLabel: String = "設定"
}

object JapaneseCommonDashboardStrings : CommonDashboardStrings {
    override val emptyScreenMessage: (inlineIconId: String) -> AnnotatedString = {
        buildAnnotatedString {
            append("アプリを使うにはデッキが必要です。")
            appendInlineContent(it)
            append("　ボタンを押して、デッキを作成してください。")
        }
    }

    override val mergeButton: String = "統合"
    override val mergeCancelButton: String = "キャンセル"
    override val mergeAcceptButton: String = "統合"
    override val mergeTitle: String = "複数のデッキを1つに統合"
    override val mergeTitleHint: String = "タイトルを入力"
    override val mergeSelectedCount: (Int) -> String = { "$it 個選択中" }
    override val mergeClearSelectionButton: String = "クリア"

    override val mergeDialogTitle: String = "統合の確認"
    override val mergeDialogMessage: (String, List<String>) -> String = { newTitle, mergedTitles ->
        "以下の${mergedTitles.size}個のデッキが新しいデッキ「$newTitle」に統合されます: ${mergedTitles.joinToString()}"
    }
    override val mergeDialogCancelButton: String = "キャンセル"
    override val mergeDialogAcceptButton: String = "統合"

    override val sortButton: String = "並べ替え"
    override val sortCancelButton: String = "キャンセル"
    override val sortAcceptButton: String = "適用"
    override val sortTitle: String = "デッキの順序を変更"
    override val sortByTimeTitle: String = "最終練習時間で並べ替える"

    override val itemTimeMessage: (Duration?) -> String = {
        "最終練習日: " + when {
            it == null -> "なし"
            it.inWholeDays > 0 -> "${it.inWholeDays}日前"
            else -> "1日以内"
        }
    }

    override val itemTotal: String = "合計"
    override val itemDone: String = "完了"
    override val itemReview: String = "復習"
    override val itemNew: String = "未習"
    override val dailyPracticeTitle: String = "クイック練習"
    override val dailyPracticeNew: (Int) -> String = { "新しく学習 ($it)" }
    override val dailyPracticeDue: (Int) -> String = { "復習 ($it)" }
    override val itemGraphProgressTitle: String = "完了率"

    override val selectedPracticeTypeTemplate: (practiceType: String) -> String =
        { "練習タイプ: $it" }

}

object JapaneseDailyLimitStrings : DailyLimitStrings {
    override val enableSwitchTitle: String = "有効"
    override val enableSwitchDescription: String =
        "アプリによって促される毎日の練習の数を制限するには有効にします"
    override val lettersSectionTitle: String = "文字"
    override val vocabSectionTitle: String = "単語"
    override val combinedLimitSwitchTitle: String = "共通の上限"
    override val combinedLimitSwitchDescription: String = "すべての練習タイプで共通の上限を設定"
    override val newLabel: String = "新しい"
    override val dueLabel: String = "期限"
    override val noteMessage: String = "注意: 書く練習と読み練習は、制限に別々に加増されます"
    override val button: String = "保存"
    override val changesSavedMessage: String = "完了"
}


object JapaneseTutorialDialogStrings : TutorialDialogStrings {
    override val title: String = "チュートリアル"

    override val page1: String = """
        • このアプリは、復習のタイミングを最適化するSRS（間隔反復システム）を使っています
        • 復習を行うと、SRSがあなたの記憶力に応じて次の復習を予定します
        • 簡単に思い出せたら、復習の間隔は長くなり、難しかったら短くなります
    """.trimIndent()

    override val page2Top: String = """
        • あなたの記憶力を評価するために、復習後にいくつかの評価オプションが表示されます
    """.trimIndent()

    override val page2Bottom: String = """
        • 自分の記憶力に最も合ったオプションを選んでください
        • 自己評価することで、アプリがあなたに合ったペースで学習を調整できます
    """.trimIndent()

    override val page3Top: String = """
        • 毎日、復習した項目のステータスが更新されます
        • アプリは、新しい項目と復習の期限が過ぎた項目を復習させてくれます
    """.trimIndent()

    override val page3Bottom: String = """
        • 負担を抑えるために、1日の制限を設定できます
    """.trimIndent()

    override val page4Top: String = """
        • アプリを使い始めるには、デッキを作成してください
        • デッキは、マスターしたい項目を整理するために使用します。アプリには文字デッキと単語デッキがあります
    """.trimIndent()

    override val page4Bottom: String = """
        • 自分でデッキを作成するか、いくつかの事前に作られたデッキを選ぶことができます
    """.trimIndent()

    override val page5: String = """
        • デッキが作成されたら、復習を始められます
        • いくつかの練習モードがあるので、全部試してみてください
        • 一貫性が大切です。毎日少しずつ練習することが進歩の鍵です。無理しないように、1日の制限を下げることも検討してください
        • 日本語マスターへの道、がんばってください！\(^_^)/ 
    """.trimIndent()
}


private fun formatDuration(duration: Duration): String = when {
    duration.inWholeHours > 0 -> "${duration.inWholeHours}時 ${duration.inWholeMinutes % 60}分"
    duration.inWholeMinutes > 0 -> "${duration.inWholeMinutes}分 ${duration.inWholeSeconds % 60}秒"
    else -> "${duration.inWholeSeconds}秒"
}

object JapaneseStatsStrings : StatsStrings {
    override val todayTitle: String = "今日"
    override val monthTitle: String = "今月"
    override val monthLabel: (day: LocalDate) -> String =
        { "${it.year}年${it.monthNumber}月" }
    override val yearTitle: String = "今年"
    override val yearDaysPracticedLabel = { practicedDays: Int, daysInYear: Int ->
        "練習日数: $practicedDays/$daysInYear"
    }
    override val totalTitle: String = "合計"
    override val timeSpentTitle: String = "練習時間"
    override val reviewsCountTitle: String = "練習回数"
    override val formattedDuration: (Duration) -> String = { formatDuration(it) }
    override val uniqueLettersReviewed: String = "練習した異なる文字の数"
    override val uniqueWordsReviewed: String = "練習した異なる単語の数"
}


object JapaneseSearchStrings : SearchStrings {
    override val inputHint: String = "文字・単語・ローマ字・#k #c #name"
    override val clearInputDescription: String = "検索をクリア"
    override val radicalsSearchDescription: String = "部首で検索"
    override val charactersTitle: (count: Int) -> String = { "文字 ($it)" }
    override val namesTitle: (count: Int) -> String = { "名前 ($it)" }
    override val wordsTitle: (count: Int) -> String = { "単語 ($it)" }
    override val radicalsSheetTitle: String = "部首で検索"
    override val radicalsFoundCharacters: String = "見つかった文字"
    override val radicalsEmptyFoundCharacters: String = "何も見つかりませんでした"
    override val radicalSheetRadicalsSectionTitle: String = "部首"
}

object JapaneseAlternativeDialogStrings : AlternativeDialogStrings {
    override val title: String = "別の単語"
    override val readingsTitle: String = "読み方"
    override val meaningsTitle: String = "意味"
    override val reportButton: String = "報告"
    override val closeButton: String = "閉じる"
}

object JapaneseSettingsStrings : SettingsStrings {
    override val preferencesSection: String = "設定"
    override val dataSyncSection: String = "データと同期"
    override val moreSection: String = "その他"
    override val analyticsTitle: String = "分析レポート"
    override val analyticsMessage: String = "アプリを向上させるために匿名データの送信を許可する"
    override val themeTitle: String = "テーマ"
    override val themeSystem: String = "システムに従う"
    override val themeLight: String = "ライト"
    override val themeDark: String = "ダーク"
    override val themeAmoled: String = "AMOLED（純黒）"
    override val reminderTitle: String = "リマインダー通知"
    override val reminderEnabled: String = "有効"
    override val reminderDisabled: String = "無効"
    override val defaultTab: String = "デフォルトのタブ"
    override val feedbackTitle: String = "フィードバック"
    override val account: String = "アカウント"
    override val sync: String = "同期（プレビュー）"
    override val backupTitle: String = "バックアップと復元"
    override val aboutTitle: String = "このアプリについて"
    override val pickerDialogCancel: String = "キャンセル"
    override val pickerDialogApply: String = "適用"
}

object JapaneseReminderDialogStrings : ReminderDialogStrings {
    override val title: String = "リマインダー通知"
    override val noPermissionLabel: String = "通知の権限がありません"
    override val noPermissionButton: String = "許可"
    override val enabledLabel: String = "通知"
    override val timeLabel: String = "時間"
    override val cancelButton: String = "キャンセル"
    override val applyButton: String = "適用"
}

object JapaneseAboutStrings : AboutStrings {
    override val title: String = "このアプリについて"
    override val version: (versionName: String) -> String = { "バージョン: $it" }
    override val githubTitle: String = "プロジェクトのGitHubページ"
    override val versionChangesTitle: String = "変更履歴"
    override val versionChangesDescription: String = "アプリの変更履歴"
    override val versionChangesButton: String = "閉じる"
    override val githubDescription: String = "ソースコード、バグ報告、議論"
    override val creditsTitle: String = "クレジット"
    override val creditsDescription: String = "使用されるライブラリとデータソース"
}

object JapaneseBackupStrings : BackupStrings {
    override val title: String = "バックアップ"
    override val backupButton: String = "バックアップ作成"
    override val restoreButton: String = "バックアップからリストア"
    override val unknownError: String = "不明なエラー"
    override val restoreVersionMessage: (Long, Long) -> String = { backupVersion, currentVersion ->
        "データベースバージョン：$backupVersion（現在のバージョン：$currentVersion）"
    }
    override val restoreTimeMessage: (LocalDateTime) -> String = {
        "作成時間：${it.format(CommonDateTimeFormat)}"
    }
    override val restoreNote: String =
        "注意！すべての現在の進捗は、選択したバックアップからの進捗で置き換えられます"
    override val restoreApplyButton: String = "リストア"
    override val completeMessage: String = "完了"
}

object JapaneseFeedbackStrings : FeedbackStrings by EnglishFeedbackStrings {
    override val title: String = "フィードバック"
    override val topicTitle: String = "トピック"
    override val messageLabel: String = "ここにフィードバックを入力してください"
    override val button: String = "送信"
    override val successMessage: String = "フィードバックを送信しました"
    override val errorMessage: (String?) -> String = { "エラー: $it" }
}

object JapaneseSponsorStrings : SponsorStrings by EnglishSponsorStrings

object JapaneseDeckPickerStrings : DeckPickerStrings {

    override val title: String = "選択"

    override val customDeckButton: String = "空のデッキを作る"
    override val kanaTitle: String = "かな"

    override val kanaDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("かなは、いちばんやさしい日本語の文字です。かなはふたつに分けることができます。\n")
            append("・平仮名（ひらがな）─ 日本語のことばや音をつたえるときにつかいます。\n")
            append("・片仮名（かたかな）─ 外国のことばなどを書くときにつかいます。")
            withClickableUrl(
                url = "https://ja.wikibooks.org/wiki/%E3%81%B2%E3%82%89%E3%81%8C%E3%81%AA%E3%83%BB%E3%82%AB%E3%82%BF%E3%82%AB%E3%83%8A",
                color = urlColor
            ) {
                append("もっと知る")
            }
        }
    }
    override val hiragana: String = JapaneseStrings.hiragana
    override val katakana: String = JapaneseStrings.katakana

    override val jltpTitle: String = "日本語能力試験"
    override val jlptDescription: StringResolveScope<AnnotatedString> = {
        buildAnnotatedString {
            append("日本語能力試験 (JLPT) は、日本語を母語としない人のための日本語の試験です。N5からN1までの難しさがあります。")
            withClickableUrl(
                url = "https://ja.wikipedia.org/wiki/%E6%97%A5%E6%9C%AC%E8%AA%9E%E8%83%BD%E5%8A%9B%E8%A9%A6%E9%A8%93",
                color = MaterialTheme.extraColorScheme.link
            ) {
                append("詳細情報")
            }
        }
    }
    override val jlptItem: (level: Int) -> String = { "JLPT N$it" }

    override val gradeTitle: String = "常用漢字"
    override val gradeDescription = { urlColor: Color ->
        buildAnnotatedString {
            withClickableUrl(
                url = "https://ja.wikipedia.org/wiki/%E5%B8%B8%E7%94%A8%E6%BC%A2%E5%AD%97",
                color = urlColor
            ) {
                append("常用漢字")
            }
            append("は、2,136字から成る、よく使われる漢字の表です。内容は以下の通りです。\n")
            append("・最初の1,026字は小学校1年から6年までに学習（")
            withClickableUrl(
                url = "https://ja.wikipedia.org/wiki/%E6%95%99%E8%82%B2%E6%BC%A2%E5%AD%97",
                color = urlColor
            ) {
                append("教育漢字")
            }
            append("）。\n")
            append("・以降の1,110字は中学校以降に学習。")
        }
    }
    override val gradeItemNumbered: (Int) -> String = { "小学校${it}年" }
    override val gradeItemSecondary: String = "中学校以降"
    override val gradeItemNames: String = "人名用漢字(一)"
    override val gradeItemNamesVariants: String = "人名用漢字(二)（常用漢字の異体字）"

    override val wanikaniTitle: String = EnglishDeckPickerStrings.wanikaniTitle
    override val wanikaniDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Tofuguが運営するWaniKaniに準拠したレベル別の漢字の一覧です。")
            withClickableUrl("https://www.wanikani.com/kanji?difficulty=pleasant", urlColor) {
                append("詳細情報")
            }
        }
    }
    override val wanikaniItem: (Int) -> String = { "WaniKani レベル$it" }

    override val vocabOtherTitle: String = "その他"
    override val vocabOtherDescription: AnnotatedString =
        AnnotatedString("よく使うテーマの小さな単語デッキの集まり。言葉を学び始めるのに役立ちます。")

    override val vocabDeckItemWordsCountLabel: (words: Int) -> String = { "${it}語" }
    override val vocabDeckTitleTime: String = "時間"
    override val vocabDeckTitleWeek: String = "曜日"
    override val vocabDeckTitleCommonVerbs: String = "よく使う動詞"
    override val vocabDeckTitleColors: String = "色"
    override val vocabDeckTitleRegularFood: String = "普段の食べ物"
    override val vocabDeckTitleJapaneseFood: String = "日本の食べ物"
    override val vocabDeckTitleGrammarTerms: String = "文法用語"
    override val vocabDeckTitleAnimals: String = "動物"
    override val vocabDeckTitleBody: String = "体"
    override val vocabDeckTitleCommonPlaces: String = "よく行く場所"
    override val vocabDeckTitleCities: String = "都市"
    override val vocabDeckTitleTransport: String = "交通"

}

object JapaneseDeckEditStrings : DeckEditStrings {
    override val createTitle: String = "デッキの作成"
    override val ediTitle: String = "デッキの編集"
    override val searchHint: String = "文字を入力"
    override val editingModeSearchTitle: String = "検索"
    override val editingModeDetailsTitle: String = "詳細"
    override val editingModeRemovalTitle: String = "削除"
    override val vocabDetailsEmptyMessage: (inlineIconId: String) -> AnnotatedString = {
        buildAnnotatedString {
            append("カードなし。新しいカードを追加するには、このデッキを保存して、")
            appendInlineContent(it)
            append(" アイコンを検索画面や書き取り練習中、アプリ内の他の場所などで使用してください。")
        }
    }
    override val completeMessage: String = "完了"
    override val saveTitle: String = "変更の保存"
    override val saveInputHint: String = "名前"
    override val saveButtonDefault: String = "保存"
    override val saveButtonCompleted: String = "完了"
    override val archiveTitle: String = "デッキをアーカイブ"
    override val archiveHint: String = "メインのデッキ一覧からは隠れ、アーカイブを解除すると戻ります"
    override val deleteTitle: String = "削除の確認"
    override val deleteMessage: (practiceTitle: String) -> String = {
        "デッキ「$it」を削除してもよろしいですか？"
    }
    override val deleteButtonDefault: String = "削除"
    override val deleteButtonCompleted: String = "完了"
    override val unknownTitle: String = "不明な文字"
    override val unknownMessage: (characters: List<String>) -> String = {
        "${it.joinToString()} のデータが見つかりませんでした"
    }
    override val unknownButton: String = "OK"

    override val leaveConfirmationTitle: String = "編集をやめますか？"
    override val leaveConfirmationMessage: String = "現在の変化は失われます"
    override val leaveConfirmationCancel: String = "キャンセル"
    override val leaveConfirmationAccept: String = "やめる"

}

object JapaneseDeckDetailsStrings : DeckDetailsStrings {
    override val emptyListMessage: String = "何もありません"
    override val detailsGroupTitle: (index: Int) -> String = { "グループ $it" }
    override val firstTimeReviewMessage: (LocalDateTime?) -> String = {
        "初めて練習した時間: " + when (it) {
            null -> "なし"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val lastTimeReviewMessage: (LocalDateTime?) -> String = {
        "最後に練習した時間: " + when (it) {
            null -> "なし"
            else -> groupDetailsDateTimeFormatter(it)
        }
    }
    override val groupDetailsButton: String = "練習を開始"
    override val expectedReviewDate: (LocalDate?) -> String = {
        "予定の復習日: ${it ?: "-"}"
    }
    override val lastReviewDate: (LocalDateTime?) -> String = {
        "最後の復習日: ${it?.date ?: "-"}"
    }
    override val repetitions: (Int) -> String = { "連続正解回数: $it" }
    override val lapses: (Int) -> String = { "忘却回数: $it" }

    override val dialogCommon: LetterDeckDetailDialogCommonStrings =
        JapaneseLetterDeckDetailDialogCommonStrings
    override val filterDialog: FilterDialogStrings = JapaneseFilterDialogStrings
    override val sortDialog: SortDialogStrings = JapaneseSortDialogStrings
    override val layoutDialog: PracticePreviewLayoutDialogStrings =
        JapanesePracticePreviewLayoutDialogStrings

    override val multiselectTitle: (selectedCount: Int) -> String = { "$it 個選択中" }
    override val multiselectDataNotLoaded: String = "しばらくお待ちください…"
    override val multiselectNoSelected: String = "少なくとも1つ選んでください"

    override val filterAllLabel: String = "すべて"
    override val filterNoneLabel: String = "何も"
    override val kanaGroupsModeActivatedLabel: String = "仮名グループモード"

    override val shareLetterDeckClipboardMessage: String =
        "デッキからの文字がクリップボードにコピーされました"
}

object JapaneseLetterDeckDetailDialogCommonStrings : LetterDeckDetailDialogCommonStrings {
    override val buttonCancel: String = "キャンセル"
    override val buttonApply: String = "適用"
}

object JapaneseFilterDialogStrings : FilterDialogStrings {
    override val title: String = "表示する文字"
}

object JapaneseSortDialogStrings : SortDialogStrings {
    override val title: String = "順序"
    override val sortOptionAddOrder: String = "追加順"
    override val sortOptionAddOrderHint: String = "↑ 新しい文字が最後\n↓ 新しい文字が最初"
    override val sortOptionFrequency: String = "頻出順"
    override val sortOptionFrequencyHint: String =
        "新聞で使われる頻度\n↑ 頻度が高い文字が最初\n↓ 頻度が高い文字が最後"
    override val sortOptionName: String = "符号順"
    override val sortOptionNameHint: String = "↑ 小さい文字が最初\n↓ 小さい文字が最後"
    override val sortOptionReviewTime: String = "予想復習時間"
    override val sortOptionReviewTimeHint: String =
        "↑ 一度も復習していないカードが最初\n↓ 予定が最も遠いカードが最初"
}

object JapanesePracticePreviewLayoutDialogStrings : PracticePreviewLayoutDialogStrings {
    override val title: String = "レイアウト"
    override val singleCharacterOptionLabel: String = "リスト"
    override val groupsOptionLabel: String = "グループ"
    override val kanaGroupsTitle: String = "仮名グループ"
    override val kanaGroupsSubtitle: String =
        "すべての仮名が含まれている場合、五十音に従ってグループのサイズを設定します"
}

object JapaneseCommonPracticeStrings : CommonPracticeStrings {

    override val configurationTitle: String = "練習の設定"
    override val configurationSelectedItemsLabel: String = "練習の数:"
    override val configurationCharactersPreview: String = "文字のプレビュー"
    override val shuffleConfigurationTitle: String = "順序のシャッフル"
    override val shuffleConfigurationMessage: String = "復習順をランダムにする"
    override val configurationCompleteButton: String = "開始"
    override val kanjiInsightsTitle: String = "漢字の詳細"
    override val kanjiInsightsMessage: String = "詳細を開く"

    override val additionalKanaReadingsNote: (List<String>) -> String = {
        "注：${it.joinToString { "「$it」" }}と書くこともあります"
    }

    override val formattedSrsInterval: (Duration) -> String = {
        val duration = formattedSrsDuration(
            duration = it,
            dayLabel = "日",
            hourLabel = "時",
            minuteLabel = "分",
            secondLabel = "秒",
            separator = ""
        )
        "${duration}後"
    }
    override val flashcardRevealButton: String = "答えを見る"
    override val againButton: String = "もう一度"
    override val hardButton: String = "難しい"
    override val goodButton: String = "正解"
    override val easyButton: String = "簡単"
    override val reviewSaveError: String = "復習を保存できませんでした。回答はこのまま残っています。"
    override val reviewSaveRetry: String = "再試行"

    override val summaryTimeSpentValue: (Duration) -> String = { formatDuration(it) }

    override val earlyFinishDialogTitle: String = "練習を終了しますか？"
    override val earlyFinishDialogMessage: String =
        "まとめに移動します。現在の進捗はすでに保存されています"
    override val earlyFinishDialogCancelButton: String = "キャンセル"
    override val earlyFinishDialogAcceptButton: String = "終了"

}

object JapaneseLetterPracticeStrings : LetterPracticeStrings {
    override val configurationTitle: (practiceType: String) -> String = { "文字練習・$it" }
    override val hintStrokesTitle: String = "字画のヒント表示"
    override val hintStrokesMessage: String = "ヒントを表示する条件を設定する"
    override val hintStrokeNewOnlyMode: String = "新規のみ"
    override val hintStrokeAllMode: String = "常時"
    override val hintStrokeNoneMode: String = "しない"
    override val inputModeTitle: String = "入力モード"
    override val inputModeMessage: String = "字画ごとに検証するか、文字全体を検証するかを選択する"
    override val inputModeStroke: String = "字画"
    override val inputModeCharacter: String = "文字"
    override val kanaRomajiTitle: String = "ローマ字を表示"
    override val kanaRomajiMessage: String =
        "かなを練習するときは、かなの代わりにローマ字単語を表示する"
    override val noTranslationLayoutTitle: String = "翻訳の非表示"
    override val noTranslationLayoutMessage: String = "書く練習で字義の翻訳を隠す"
    override val leftHandedModeTitle: String = "左手モード"
    override val leftHandedModeMessage: String = "書く練習で横画面の場合、書く場所を左に移す"

    override val headerWordsMessage: (count: Int) -> String = { "単語 ($it)" }
    override val studyFinishedButton: String = "復習"
    override val altStrokeEvaluatorTitle: String = "代替字画認識"
    override val altStrokeEvaluatorMessage: String =
        "オリジナルの字画認識の代わりに代替のアルゴリズムを使う"
    override val noKanjiTranslationsLabel: String = "[翻訳なし]"

    override val variantsTitle: String = "異体字"
    override val variantsHint: String = "クリックして表示"
    override val unicodeTitle: (String) -> String = EnglishLetterPracticeStrings.unicodeTitle
    override val strokeCountTitle: (count: Int) -> String = { "${it}画" }
}

object JapaneseVocabPracticeStrings : VocabPracticeStrings {
    override val configurationTitle: (String) -> String = { "単語練習・$it" }
    override val readingMeaningConfigurationTitle: String = "常に意味の表示"
    override val readingMeaningConfigurationMessage: String =
        "回答が選択されていない場合の意味の表示を選択してください"
    override val translationInFrontConfigurationTitle: String = "表に翻訳を置く"
    override val translationInFrontConfigurationMessage: String =
        "フラッシュカードが隠れているときに単語の代わりに翻訳を表示する"
    override val writingKanaReadingConfigurationTitle: String = "かなの読み方の表示"
    override val writingKanaReadingConfigurationMessage: String = "漢字の上にかなの読み方を表示します。"
    override val writingKanjiOnlyConfigurationTitle: String = "漢字のみを書く"
    override val writingKanjiOnlyConfigurationMessage: String = "練習中にかな文字を書くのを自動的にスキップします。"
    override val detailsButton: String = "詳細"
}

object JapaneseInfoScreenStrings : InfoScreenStrings {
    override val strokesMessage: (count: Int) -> AnnotatedString = {
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("${it}画") }
        }
    }
    override val clipboardCopyMessage: String = "コピーしました"
    override val radicalsSectionTitle: (count: Int) -> String = { "部首 ($it)" }
    override val noRadicalsMessage: String = "部首なし"
    override val wordsSectionTitle: (count: Int) -> String = { "単語 ($it)" }
    override val relatedReadingsSectionTitle: String = "関連する読み方"
    override val readingInfoMessage: (info: List<String>) -> String = {
        "読みの情報: ${it.joinToString()}"
    }
    override val romajiMessage: (romaji: List<String>) -> String = {
        "ローマ字: ${it.joinToString { "「$it」" }}"
    }
    override val gradeMessage: (grade: Int) -> String = {
        when {
            it <= 6 -> "常用漢字，小学校${it}年で学習"
            it == 8 -> "常用漢字，中学校以降で学習"
            it >= 9 -> "人名用漢字"
            else -> throw IllegalStateException("Unknown grade $it")
        }
    }
    override val jlptMessage: (level: Int) -> String = { "JLPT レベル$it" }
    override val frequencyMessage: (frequency: Int) -> String = {
        "新聞頻出漢字の2500中${it}番目"
    }

}

object JapaneseReminderNotificationStrings : ReminderNotificationStrings {
    override val channelName: String = "リマインダー通知"
    override val title: String = "勉強の時間です！"
    override val noDetailsMessage: String = "日本語の学習を続ける"
    override val newOnlyMessage: (Int) -> String = {
        "今日は未習うカードが${it}枚あります"
    }
    override val dueOnlyMessage: (Int) -> String = {
        "今日は復習するカードが${it}枚あります"
    }
    override val message: (Int, Int) -> String = { new, due ->
        "今日は未習うカードが${new}枚、復習するカードが${due}枚あります"
    }
}

object JapaneseAccountScreenStrings : AccountScreenStrings {
    override val title = "アカウント"
    override val loggedOutMessage = "ログインしていません"
    override val signInButton = "ログイン"
    override val signOutButton = "ログアウト"
    override val emailTitle = "メールアドレス"
    override val subscriptionTitle = "サブスクリプション"
    override val subscriptionStatusActive = "アクティブ"
    override val subscriptionStatusExpired = "期限切れ"
    override val subscriptionStatusInactive = "非アクティブ"
    override val subscriptionValidUntilTemplate = "有効期限: %s"
    override val issueNoConnectionTitle = "接続なし"
    override val issueNoConnectionMessage = "キャッシュされたデータを表示中"
    override val issueSessionExpiredTitle = "セッションの有効期限が切れました"
    override val issueSessionExpiredMessage = "再ログインするにはクリックしてください"
    override val issueSubscriptionOutdatedTitle = "サブスクリプションの状態が古いです"
    override val issueSubscriptionOutdatedMessage = "更新するにはクリックしてください"
    override val issueOtherTitle = "エラー"
    override val issueOtherMessageFallback = "不明なエラー"
}

object JapaneseSyncScreenStrings : SyncScreenStrings {
    override val title = "同期（プレビュー）"
    override val guideTitle: String = "進捗をデバイス間で同期する"
    override val guideMessage =
        "データを自動的にクラウドにアップロードし、バックアップとして保存し、すべてのデバイス間で同期を保つ"
    override val guideStepAccountTitle = "アカウントを作成してログインする"
    override val guideStepAccountMessage: String = "アカウント画面に移動する"
    override val guideStepSubscriptionTitle =
        "サブスクリプションを購入する（プレビュー期間中は無料）"
    override val guideStepSubscriptionMessage: String =
        "更新情報はDiscordで確認してください"
    override val accountErrorMessage: String = "アカウントに問題があります"
    override val syncButton = "今すぐ同期"
    override val statusTitle = "ステータス"
    override val statusMessageLoading = "サーバーの更新を確認中..."
    override val statusMessageDataDiffer = "ローカルデータとリモートデータが異なります"
    override val statusMessageLocalNewer = "更新されたデータをアップロードできます"
    override val statusMessageUpToDate = "サーバーと同期済み"
    override val statusMessageError = "エラー"
    override val statusMessageUploading = "アップロード中"
    override val statusMessageDownloading = "ダウンロード中"
    override val statusMessageCanceled =
        "キャンセルされました。再開するには同期ボタンをクリックしてください"
    override val localDataTitle = "ローカルデータ"
    override val localDataIdTemplate = "ID: %s"
    override val localDataTimestampTemplate = "タイムスタンプ: %s"

    override val errorNoConnectionTitle = "接続なし"
    override val errorNoConnectionMessage = "サーバーにアクセスできませんでした"
    override val errorSessionExpiredTitle = "セッションの有効期限が切れました"
    override val errorSessionExpiredMessage = "再ログインするにはクリックしてください"
    override val errorNoSubscriptionTitle = "サブスクリプションの状態が古いです"
    override val errorNoSubscriptionMessage = "アカウント画面でサブスクリプションを更新してください"
    override val errorOtherTitle = "エラー"
    override val errorOtherMessageFallback = "不明なエラー"
}

object JapaneseSyncDialogStrings : SyncDialogStrings {
    override val title = "同期"
    override val buttonCancel = "キャンセル"
    override val buttonUpload = "アップロード"
    override val buttonDownload = "ダウンロード"
    override val buttonAccount = "アカウント"
    override val uploadingMessage = "アップロード中..."
    override val downloadingMessage = "ダウンロード中..."
    override val conflictRemoteNewerTitle = "新しいデータが見つかりました"
    override val conflictRemoteNewerMessage = "サーバー上のデータはローカルコピーより新しいです"
    override val conflictIncompatibleTitle = "データの競合"
    override val conflictIncompatibleMessage =
        "リモートとローカルの両方のデータが変更されました。結果をマージできません"
    override val errorNoNetworkTitle = "ネットワークなし"
    override val errorNoNetworkMessage = "ネットワーク接続を確立できませんでした"
    override val errorNoSubscriptionTitle = "サブスクリプションの期限切れ"
    override val errorNoSubscriptionMessage =
        "サブスクリプションの期限が切れました。同期は無効になります。アカウント画面でサブスクリプションの状態を更新してください"
    override val errorNotAuthenticatedTitle = "セッションの有効期限が切れました"
    override val errorNotAuthenticatedMessage = "アカウントに再ログインしてください"
    override val errorUnexpectedErrorTitle = "予期しないエラー"
    override val errorUnexpectedErrorMessage = "不明な問題が発生しました"
    override val errorUnsupportedDataTitle = "サーバー上のデータはサポートされていません"
    override val errorUnsupportedDataMessage =
        "サーバー上のデータはアプリの新しいバージョンで作成されました。現在インストールされているバージョンと互換性がありません。データを取得するにはアプリを更新するか、ローカルデータをサーバーにアップロードしてください"
}

object JapaneseSyncSnackbarStrings : SyncSnackbarStrings {
    override val errorNoConnection = "接続なし"
    override val errorNoSubscription = "サブスクリプションの期限切れ"
    override val errorNotAuthenticated = "ログインデータの有効期限切れ"
    override val errorDataNotSupported = "リモートデータがサポートされていません"
    override val errorMessageTemplate = "同期エラー: %s"
    override val errorMessageNoReason = "同期エラー"
    override val actionButton = "詳細"
}

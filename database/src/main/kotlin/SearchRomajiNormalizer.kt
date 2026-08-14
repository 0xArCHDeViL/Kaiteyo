package export.db

import dev.esnault.wanakana.core.Wanakana

internal fun String.searchRomaji(): String = Wanakana.toRomaji(this)
    .lowercase()
    .trim()

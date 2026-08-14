package export.db

import java.util.Locale

/** Maps EDRDG's human-readable JMdict POS labels to compact search tags. */
object EdrdgSearchTagMapper {

    fun canonicalPartOfSpeechTags(value: String): Set<String> {
        val normalized = value.trim().lowercase(Locale.ROOT)
        return buildSet {
            when {
                normalized == "transitive verb" -> add("vt")
                normalized == "intransitive verb" -> add("vi")
                normalized == "ichidan verb" || normalized.startsWith("ichidan verb -") -> add("v1")
                normalized.startsWith("godan verb with 'ru' ending") -> add("v5r")
                normalized.startsWith("godan verb with 'su' ending") -> add("v5s")
                normalized.startsWith("godan verb with 'ku' ending") -> add("v5k")
                normalized.startsWith("godan verb with 'mu' ending") -> add("v5m")
                normalized.startsWith("godan verb with 'u' ending") -> add("v5u")
                normalized.startsWith("godan verb with 'tsu' ending") -> add("v5t")
                normalized.startsWith("godan verb with 'gu' ending") -> add("v5g")
                normalized.startsWith("godan verb with 'bu' ending") -> add("v5b")
                normalized.startsWith("godan verb - iku/yuku special class") -> add("v5k-s")
                normalized.startsWith("godan verb - -aru special class") -> add("v5aru")
                normalized.startsWith("kuru verb") -> add("vk")
                normalized.startsWith("suru verb") ||
                    normalized.startsWith("noun or participle which takes the aux. verb suru") -> add("vs")
            }
        }
    }
}

package com.jge.testapp2

/**
 * 태그 ID, 다국어 문자열, 그리고 기존 tagToInt 함수에서 사용되던 비트 값(2^tagId)을
 * 모두 매핑하여 관리하는 객체입니다.
 */
object TagStrings {

    // 1. 태그 ID와 다국어 문자열 매핑
    // key: tagId (Int), value: 언어별 문자열을 담은 Map (LanguageType -> String)
    private val tagMap: Map<Int, Map<LanguageType, String>> = mapOf(
        // 1: 가드/Guard/前衛タイプ/近卫干员
        1 to mapOf(
            LanguageType.KOREAN to "가드",
            LanguageType.ENGLISH to "Guard",
            LanguageType.JAPANESE to "前衛タイプ",
            LanguageType.CHINESE to "近卫干员"
        ),
        // 2: 스나이퍼/Sniper/狙撃タイプ/狙击干员
        2 to mapOf(
            LanguageType.KOREAN to "스나이퍼",
            LanguageType.ENGLISH to "Sniper",
            LanguageType.JAPANESE to "狙撃タイプ",
            LanguageType.CHINESE to "狙击干员"
        ),
        // 3: 디펜더/Defender/重装タイプ/重装干员
        3 to mapOf(
            LanguageType.KOREAN to "디펜더",
            LanguageType.ENGLISH to "Defender",
            LanguageType.JAPANESE to "重装タイプ",
            LanguageType.CHINESE to "重装干员"
        ),
        // 4: 메딕/Medic/医療タイプ/医疗干员
        4 to mapOf(
            LanguageType.KOREAN to "메딕",
            LanguageType.ENGLISH to "Medic",
            LanguageType.JAPANESE to "医療タイプ",
            LanguageType.CHINESE to "医疗干员"
        ),
        // 5: 서포터/Supporter/補助タイプ/辅助干员
        5 to mapOf(
            LanguageType.KOREAN to "서포터",
            LanguageType.ENGLISH to "Supporter",
            LanguageType.JAPANESE to "補助タイプ",
            LanguageType.CHINESE to "辅助干员"
        ),
        // 6: 캐스터/Caster/術師タイプ/术师干员
        6 to mapOf(
            LanguageType.KOREAN to "캐스터",
            LanguageType.ENGLISH to "Caster",
            LanguageType.JAPANESE to "術師タイプ",
            LanguageType.CHINESE to "术师干员"
        ),
        // 7: 스페셜리스트/Specialist/特殊タイプ/特种干员
        7 to mapOf(
            LanguageType.KOREAN to "스페셜리스트",
            LanguageType.ENGLISH to "Specialist",
            LanguageType.JAPANESE to "特殊タイプ",
            LanguageType.CHINESE to "特种干员"
        ),
        // 8: 뱅가드/Vanguard/先鋒タイプ/先锋干员
        8 to mapOf(
            LanguageType.KOREAN to "뱅가드",
            LanguageType.ENGLISH to "Vanguard",
            LanguageType.JAPANESE to "先鋒タイプ",
            LanguageType.CHINESE to "先锋干员"
        ),
        // 9: 근거리/Melee/近距離/近战位
        9 to mapOf(
            LanguageType.KOREAN to "근거리",
            LanguageType.ENGLISH to "Melee",
            LanguageType.JAPANESE to "近距離",
            LanguageType.CHINESE to "近战位"
        ),
        // 10: 원거리/Ranged/遠距離/远程位
        10 to mapOf(
            LanguageType.KOREAN to "원거리",
            LanguageType.ENGLISH to "Ranged",
            LanguageType.JAPANESE to "遠距離",
            LanguageType.CHINESE to "远程位"
        ),
        // 11: 고급 특별 채용/Top Operator/上級エリート/高级资深干员
        11 to mapOf(
            LanguageType.KOREAN to "고급특별채용",
            LanguageType.ENGLISH to "TopOperator",
            LanguageType.JAPANESE to "上級エリート",
            LanguageType.CHINESE to "高级资深干员"
        ),
        // 12: 제어형/Crowd-Control/牽制/控场
        12 to mapOf(
            LanguageType.KOREAN to "제어형",
            LanguageType.ENGLISH to "Crowd-Control",
            LanguageType.JAPANESE to "牽制",
            LanguageType.CHINESE to "控场"
        ),
        // 13: 누커/Nuker/爆発力/爆发
        13 to mapOf(
            LanguageType.KOREAN to "누커",
            LanguageType.ENGLISH to "Nuker",
            LanguageType.JAPANESE to "爆発力",
            LanguageType.CHINESE to "爆发"
        ),
        // 14: 특별 채용/Senior Operator/エリート/资深干员
        14 to mapOf(
            LanguageType.KOREAN to "특별채용",
            LanguageType.ENGLISH to "SeniorOperator",
            LanguageType.JAPANESE to "エリート",
            LanguageType.CHINESE to "资深干员"
        ),
        // 15: 힐링/Healing/治療/治疗
        15 to mapOf(
            LanguageType.KOREAN to "힐링",
            LanguageType.ENGLISH to "Healing",
            LanguageType.JAPANESE to "治療",
            LanguageType.CHINESE to "治疗"
        ),
        // 16: 지원/Support/支援/支援
        16 to mapOf(
            LanguageType.KOREAN to "지원",
            LanguageType.ENGLISH to "Support",
            LanguageType.JAPANESE to "支援",
            LanguageType.CHINESE to "支援"
        ),
        // 17: 신입/Starter/初期/新手
        17 to mapOf(
            LanguageType.KOREAN to "신입",
            LanguageType.ENGLISH to "Starter",
            LanguageType.JAPANESE to "初期",
            LanguageType.CHINESE to "新手"
        ),
        // 18: 코스트+/DP-Recovery/COST回復/费用回复
        18 to mapOf(
            LanguageType.KOREAN to "코스트+",
            LanguageType.ENGLISH to "DP-Recovery",
            LanguageType.JAPANESE to "COST回復",
            LanguageType.CHINESE to "费用回复"
        ),
        // 19: 딜러/DPS/火力/输出
        19 to mapOf(
            LanguageType.KOREAN to "딜러",
            LanguageType.ENGLISH to "DPS",
            LanguageType.JAPANESE to "火力",
            LanguageType.CHINESE to "输出"
        ),
        // 20: 생존형/Survival/生存/生存
        20 to mapOf(
            LanguageType.KOREAN to "생존형",
            LanguageType.ENGLISH to "Survival",
            LanguageType.JAPANESE to "生存",
            LanguageType.CHINESE to "生存"
        ),
        // 21: 범위공격/AoE/範囲攻撃/群攻
        21 to mapOf(
            LanguageType.KOREAN to "범위공격",
            LanguageType.ENGLISH to "AoE",
            LanguageType.JAPANESE to "範囲攻撃",
            LanguageType.CHINESE to "群攻"
        ),
        // 22: 방어형/Defense/防御/防护
        22 to mapOf(
            LanguageType.KOREAN to "방어형",
            LanguageType.ENGLISH to "Defense",
            LanguageType.JAPANESE to "防御",
            LanguageType.CHINESE to "防护"
        ),
        // 23: 감속/Slow/減速/减速
        23 to mapOf(
            LanguageType.KOREAN to "감속",
            LanguageType.ENGLISH to "Slow",
            LanguageType.JAPANESE to "減速",
            LanguageType.CHINESE to "减速"
        ),
        // 24: 디버프/Debuff/弱化/削弱
        24 to mapOf(
            LanguageType.KOREAN to "디버프",
            LanguageType.ENGLISH to "Debuff",
            LanguageType.JAPANESE to "弱化",
            LanguageType.CHINESE to "削弱"
        ),
        // 25: 쾌속부활/Fast-Redeploy/高速再配置/快速复活
        25 to mapOf(
            LanguageType.KOREAN to "쾌속부활",
            LanguageType.ENGLISH to "Fast-Redeploy",
            LanguageType.JAPANESE to "高速再配置",
            LanguageType.CHINESE to "快速复活"
        ),
        // 26: 강제이동/Shift/強制移動/位移
        26 to mapOf(
            LanguageType.KOREAN to "강제이동",
            LanguageType.ENGLISH to "Shift",
            LanguageType.JAPANESE to "強制移動",
            LanguageType.CHINESE to "位移"
        ),
        // 27: 소환/Summon/召喚/召唤
        27 to mapOf(
            LanguageType.KOREAN to "소환",
            LanguageType.ENGLISH to "Summon",
            LanguageType.JAPANESE to "召喚",
            LanguageType.CHINESE to "召唤"
        ),
        // 28: 로봇/Robot/ロボット/支援机械
        28 to mapOf(
            LanguageType.KOREAN to "로봇",
            LanguageType.ENGLISH to "Robot",
            LanguageType.JAPANESE to "ロボット",
            LanguageType.CHINESE to "支援机械"
        ),
        // 29: 원소/Elemental/元素/元素
        29 to mapOf(
            LanguageType.KOREAN to "원소",
            LanguageType.ENGLISH to "Elemental",
            LanguageType.JAPANESE to "元素",
            LanguageType.CHINESE to "元素"
        ),
        // 1012: 남성/Male/男性/男性干员
        1012 to mapOf(
            LanguageType.KOREAN to "남성",
            LanguageType.ENGLISH to "Male",
            LanguageType.JAPANESE to "男性",
            LanguageType.CHINESE to "男性干员"
        ),
        // 1013: 여성/Female/女性/女性干员
        1013 to mapOf(
            LanguageType.KOREAN to "여성",
            LanguageType.ENGLISH to "Female",
            LanguageType.JAPANESE to "女性",
            LanguageType.CHINESE to "女性干员"
        )
    )
    private val bitValueMap: Map<Int, Int> = (1..29).associateWith { 1 shl it } +
            mapOf(1012 to 0, 1013 to 0)

    // ✅ 추가: 언어별 역매핑 생성
    private val reverseMap: Map<LanguageType, Map<String, Int>> =
        LanguageType.values().associateWith { lang ->
            tagMap.mapNotNull { (id, langMap) ->
                langMap[lang]?.let { tagString -> tagString to id }
            }.toMap()
        }

    fun getString(tagId: Int, lang: LanguageType): String {
        return tagMap[tagId]?.get(lang) ?: "Unknown"
    }

    // ✅ 개선된 O(1) 접근
    fun getId(tagString: String, lang: LanguageType): Int {
        return reverseMap[lang]?.get(tagString) ?: -1
    }

    fun getBitValue(tagId: Int): Int {
        return bitValueMap[tagId] ?: -1
    }

    fun tagToInt(tag: String, lang: LanguageType): Int {
        val tagId = getId(tag, lang)
        return getBitValue(tagId)
    }
}
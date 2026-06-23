package com.lab.opengarage.model

/**
 * 차량 제조사 정규 목록.
 *
 * 자유 입력 시 "현대" vs "Hyundai" 처럼 표기가 갈려 [Car.modelKey] 가 분산되는 문제를 막기 위해,
 * 사용자는 이 고정 목록에서 **선택만** 한다(브랜드당 단일 정규명).
 * 국산 브랜드는 한글, 해외 브랜드는 영문 정규명을 사용한다.
 * [carBrands] 는 가나다(한글) → ABC(영문) 순으로 정렬되어 제공된다.
 */
object CarBrands {

    /** 사용자가 목록에 없는 브랜드를 고를 수 있는 예외 항목. */
    const val OTHER = "기타"

    private val raw: List<String> = listOf(
        // 국산 (한글 정규명)
        "현대", "기아", "제네시스", "쌍용", "KG모빌리티", "르노코리아", "쉐보레", "대우",
        // 해외 (영문 정규명)
        "Abarth", "Acura", "Alfa Romeo", "Alpine", "Aston Martin", "Audi",
        "Bentley", "BMW", "Bugatti", "BYD",
        "Cadillac", "Chery", "Chevrolet", "Chrysler", "Citroën", "Cupra",
        "Dacia", "Daihatsu", "Dodge", "DS",
        "Ferrari", "Fiat", "Fisker", "Ford",
        "Geely", "GMC", "GWM (Great Wall)",
        "Haval", "Honda", "Hummer",
        "Infiniti", "Isuzu",
        "Jaguar", "Jeep",
        "Koenigsegg",
        "Lada", "Lamborghini", "Lancia", "Land Rover", "Lexus", "Li Auto",
        "Lincoln", "Lotus", "Lucid",
        "Maserati", "Maybach", "Mazda", "McLaren", "Mercedes-Benz", "MG",
        "Mini", "Mitsubishi",
        "NIO", "Nissan",
        "Opel",
        "Pagani", "Peugeot", "Polestar", "Pontiac", "Porsche",
        "RAM", "Renault", "Rimac", "Rivian", "Rolls-Royce",
        "Saab", "Seat", "Škoda", "Smart", "Subaru", "Suzuki",
        "Tata", "Tesla", "Toyota",
        "Vauxhall", "Volkswagen", "Volvo",
        "Wuling",
        "XPeng",
        "Zeekr",
    )

    private fun isHangul(c: Char): Boolean = c in '가'..'힣'

    /** 한글 브랜드(가나다) 먼저, 그다음 영문(대소문자 무시 ABC). [OTHER] 는 항상 맨 끝. */
    val carBrands: List<String> = run {
        val comparator = Comparator<String> { a, b ->
            val ah = a.isNotEmpty() && isHangul(a[0])
            val bh = b.isNotEmpty() && isHangul(b[0])
            when {
                ah != bh -> if (ah) -1 else 1
                ah -> a.compareTo(b)
                else -> a.compareTo(b, ignoreCase = true)
            }
        }
        raw.sortedWith(comparator) + OTHER
    }
}

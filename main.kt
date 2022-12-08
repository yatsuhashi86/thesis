fun main(){
    for (i in 1 until 11){ // バス台数
        val ans = mutableListOf<Int>()
        for (l in 10 until 61 step 5){ //基準値
            val notReserved = mutableListOf<Int>()
            for (j in 0 until 1000) { //試行回数
                val a = Demand(i, 60)
                a.busDepot()
                var time = 0
                for (k in 0 until 100) {
                    val x = a.demand() //関数の呼び出し
                    val usingOtherTransportation = manhattan(x.departure, x.destination) / 50 //利用者が他の交通手段で目的地に向かうか(自家用車が/50、路線バスが/20、徒歩が/8、都市内交通が/100)
                    time += (1..15).random() //何分に一回予約が入るか
                    a.travelingSalesman(time, x, usingOtherTransportation + l)
                }
                notReserved.add(a.cancelReservation)
            }
            ans.add(notReserved.sum()!!)
        }
        println(ans)
    }
}






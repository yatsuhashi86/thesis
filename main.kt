fun main(){



    var numberOfBus = 1
    for (k in 0 until 10){
        var standardValue = 0
        val ans = mutableListOf<Int>()
        for (l in 0 until 60){
            val notReserved = mutableListOf<Int>()
            for (j in 0 until 1000) {
                val a = Demand(numberOfBus, 60)
                a.busDepot()
                var time = 0
                for (i in 0 until 50) {
                    val x = a.demand()
                    val usingOtherTransportation = manhattan(x.departure, x.destination) / 40
                    time += (1..30).random()
                    a.travelingSalesman(time, x, usingOtherTransportation + standardValue)

                }
                notReserved.add(a.cancelReservation)
            }
            ans.add(notReserved.sum()!!)
            standardValue++
        }
        println(ans)
        numberOfBus++
    }


}



fun main(){
    val notReserved = mutableListOf<Int>()
    for (j in 0 until 1) {
        val a = Demand(10, 60, 30)
        a.busDepot()
        var time = 0
        for (i in 0 until 50) {
            val x = a.demand()
            time += (1..30).random()
            a.travelingSalesman(time, x)
            println(a.busInfoOfTime)
            println(a.busInfoOfPosition)

        }
        notReserved.add(a.cancelReservation)
    }
    println(notReserved)
    println(notReserved.sum())

}



fun main(){
    val a = Demand(5)
    var time = 0
    for (i in 0 until 50){
        val x = a.demand()
        println(x)
        time += (1..30).random()
        a.travelingSalesman(time, x)
        println(a.busInfoOfPosition)
        println(a.busInfoOfTime)
        println(a.beforeTime)
    }

}



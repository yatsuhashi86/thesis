import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Position(val departure: Pair<Int, Int>, val destination: Pair<Int, Int>)

class Demand(private val numberOfBus: Int, private val standardValue: Int){

    private val busInfoOfPosition = MutableList(numberOfBus){ MutableList(10){ -1 } }
    //[現在地のｘ座標、現在地のｙ座標、一人目のｘ座標、一人目のｙ座標・・・]
    //なぜpositionクラスを使わなかったのか・・・非常に悔やまれるし絶対書き換えた方がいい
    private val busInfoOfTime = MutableList(numberOfBus){ MutableList(5){ -1 } }
    //[一人目到着時間、・・・、総達成時間]
    var beforeTime = 0
    var cancelReservation = 0

    fun busDepot(){
        for (i in 0 until numberOfBus){
            busInfoOfPosition[i][0] = (1..1024).random()
            busInfoOfPosition[i][1] = (1..1024).random()
        }
    }
    
    fun demand(): Position {
        val range = (1..1024)
        val departure = Pair(range.random(), range.random())
        val destination = Pair(range.random(), range.random())
        return Position(departure, destination)
    }

    fun travelingSalesman(currentTime: Int, departureAndDestination: Position, timeToBePatient: Int){
        val departure = departureAndDestination.departure
        val destination = departureAndDestination.destination

        val costList = MutableList(numberOfBus){ 0 } //バスごとの新規予約を引き受けた時の総達成時間から引き受けなかった時の総達成時間を引いたもの
        val busOrder = MutableList(numberOfBus){ MutableList(5){-1} } // 予約達成順位を保存する配列
        val fullHouse = mutableListOf<Int>()
        val timeCourse = currentTime - beforeTime //現在時刻から以前の予約の時刻を引いて何分たったか
        for (i in 0 until numberOfBus){

            val raiding = arrayListOf<Int>()
            //バスに人が乗ってるかどうかの判断（予約が入った時に時間が進む）と現在位置の更新
            //ここで到着予定時刻も更新しなければならない→busOrderを使う
            var minOfDistance = Int.MAX_VALUE
            var moved = timeCourse
            var nextMoved = -1
            var beforeBusStop = Pair(busInfoOfPosition[i][0], busInfoOfPosition[i][1])
            for (j in 0 until 4){ //予約から予約までですでに運び終わった人がいるかどうか
                if (busInfoOfTime[i][j] <= moved && busInfoOfTime[i][j] != -1){
                    moved -= busInfoOfTime[i][j]
                    beforeBusStop = Pair(busInfoOfPosition[i][j*2+2], busInfoOfPosition[i][j*2+3])
                    busInfoOfTime[i][j] = -1
                    busInfoOfPosition[i][0] = busInfoOfPosition[i][j*2+2]
                    busInfoOfPosition[i][1] = busInfoOfPosition[i][j*2+3]
                    busInfoOfPosition[i][j*2+2] = -1
                    busInfoOfPosition[i][j*2+3] = -1

                }
                if (busInfoOfTime[i][j] != -1){
                    if (minOfDistance > busInfoOfTime[i][j]){
                        minOfDistance = busInfoOfTime[i][j]
                        nextMoved = j
                    }
                    raiding.add(j)
                    busInfoOfTime[i][j] -= timeCourse
                }
            }
            val x = abs(beforeBusStop.first - busInfoOfPosition[i][nextMoved*2+2])
            val canMove = moved * 40
            //↑バス停一個あたり15.625ｍなので、かける40することで分速625ｍ、つまり時速37.5ｋｍを想定
            if (x > canMove && beforeBusStop.first > busInfoOfPosition[i][nextMoved*2+2]){ //
                if (beforeBusStop.first - canMove < 0) println(beforeBusStop.first - canMove)
                busInfoOfPosition[i][0] = beforeBusStop.first - canMove
                busInfoOfPosition[i][1] = beforeBusStop.second
            } else if (x > canMove && beforeBusStop.first < busInfoOfPosition[i][nextMoved*2+2]){
                busInfoOfPosition[i][0] = beforeBusStop.first + canMove
                busInfoOfPosition[i][1] = beforeBusStop.second
            } else if (x <= canMove && beforeBusStop.second > busInfoOfPosition[i][nextMoved*2+3]){
                if (beforeBusStop.second - (canMove - x) < 0) {

                }
                busInfoOfPosition[i][0] = busInfoOfPosition[i][nextMoved*2+2]
                busInfoOfPosition[i][1] = beforeBusStop.second - (canMove - x)
            } else if (x <= canMove && beforeBusStop.second < busInfoOfPosition[i][nextMoved*2+3]){
                busInfoOfPosition[i][0] = busInfoOfPosition[i][nextMoved*2+2]
                busInfoOfPosition[i][1] = beforeBusStop.second + (canMove - x)
            }


            val passengers = raiding.size
            if (passengers >= 4) {
                fullHouse.add(i)
                continue
            }
            val distance = MutableList(passengers+3){ MutableList(passengers+3){ Int.MAX_VALUE } }

            //バスに人が乗ってる場合、その人の目的地と新しく追加される人の出発地と目的地を合わせた全ての点間の距離を求める
            //バスの現在地0、新規の予約の出発地1、目的地2、既に乗ってる人一人目の目的地3、二人目4、、、
            for (j in 0 until passengers+3){
                val distanceOne = when (j) {
                    0 -> (busInfoOfPosition[i][0] to busInfoOfPosition[i][1])
                    1 -> departure
                    2 -> destination
                    else -> (busInfoOfPosition[i][j*2-4] to busInfoOfPosition[i][j*2-3])
                }
                for (k in j+1 until passengers+3){
                    val distanceTwo = when (k) {
                        1 -> departure
                        2 -> destination
                        else -> (busInfoOfPosition[i][k*2-4] to busInfoOfPosition[i][k*2-3])
                    }
                    distance[j][k] = manhattan(distanceOne, distanceTwo)
                    distance[k][j] = manhattan(distanceOne, distanceTwo)
                }
            }

            //巡回セールスマン問題という名の全探索。本当に汚いコード。書き直し推奨。
            var cost = Int.MAX_VALUE
            if (passengers == 3){
                for (j in 2 until passengers+3){ //jが一番最初に向かう目的地。1は新規予約の目的地、0は現在地なのでだめ
                    for (k in 1 until passengers+3){
                        if (k == j) break
                        for (l in 1 until passengers+3){
                            if (l == k || l == j) break
                            for (m in 1 until passengers+3){
                                if (m == j || m == k || m == l) break
                                for (n in 1 until passengers+3){
                                    if (n == j || n == k || n == l || n == m) break
                                    val nowCost = distance[j][k] + distance[k][l] + distance[l][m] + distance[m][n] + distance[0][j]
                                    if (nowCost < cost){
                                        busOrder[i][0] = j-1
                                        busOrder[i][1] = k-1
                                        busOrder[i][2] = l-1
                                        busOrder[i][3] = m-1
                                        busOrder[i][4] = n-1
                                    }
                                    cost = minOf(cost, nowCost) //これでi番目のバスが予約を受け持つときの一番早い行き方にかかる時間がわかった

                                }
                            }
                        }
                    }
                }
            } else if (passengers == 2){
                for (j in 2 until passengers+3){ //jが一番最初に向かう目的地。0は新規予約の目的地なのでだめ
                    for (k in 1 until passengers+3){
                        if (k == j) break
                        for (l in 1 until passengers+3){
                            if (l == k || l == j) break
                            for (m in 0 until passengers+3){
                                if (m == j || m == k || m == l) break

                                val nowCost = distance[j][k] + distance[k][l] + distance[l][m] + distance[0][j]
                                if (nowCost < cost){
                                    busOrder[i][0] = j-1
                                    busOrder[i][1] = k-1
                                    busOrder[i][2] = l-1
                                    busOrder[i][3] = m-1
                                }
                                cost = minOf(cost, nowCost) //これでi番目のバスが予約を受け持つときの一番早い行き方にかかる時間がわかった
                            }
                        }
                    }
                }
            } else if (passengers == 1){
                for (j in 2 until passengers+3){ //jが一番最初に向かう目的地。0は新規予約の目的地なのでだめ
                    for (k in 1 until passengers+3){
                        if (k == j) break
                        for (l in 1 until passengers+3){
                            if (l == k || l == j) break
                            val nowCost = distance[j][k] + distance[k][l] + distance[0][j]
                            if (nowCost < cost){
                                busOrder[i][0] = j-1
                                busOrder[i][1] = k-1
                                busOrder[i][2] = l-1
                            }
                            cost = minOf(cost, nowCost) //これでi番目のバスが予約を受け持つときの一番早い行き方にかかる時間がわかった
                        }
                    }
                }
            } else {
                cost = manhattan(departure, destination) + manhattan((busInfoOfPosition[i][0] to busInfoOfPosition[i][1]), departure)
                busOrder[i][0] = 1
                busOrder[i][1] = 0
            }
            if (busInfoOfTime[i][4] - timeCourse >= 0 && busInfoOfTime[i][4] != -1){
                costList[i] = (cost / 40) - (busInfoOfTime[i][4] - timeCourse)
            } else {
                costList[i] = (cost / 40)
            }

            busInfoOfTime[i][4] = -1
            busInfoOfTime[i][4] = busInfoOfTime[i].max()!!
        }
        var minOfTime = Int.MAX_VALUE
        var indexOfMin = 0
        //以下にcostListの中で一番小さいものを選択してそのバスの情報を更新するのが必要
        for (i in 0 until numberOfBus){
            if (fullHouse.contains(i)){
                continue
            }
            if (costList[i] < minOfTime){
                indexOfMin = i //indexOfMinが選択されるバス
                minOfTime = costList[i]
            }
        }
        //もし一番速いバスでも基準値より時間がかかる場合予約を拒否
        if (minOfTime < standardValue) {
            val trayTime = busInfoOfTime[indexOfMin]

            //以下に予想達成時間を入力していく
            var time = 0 //距離での管理
            //バスの現在地からの移動
            var x = when {
                busOrder[indexOfMin][0] == 0 -> destination
                busOrder[indexOfMin][0] == 1 -> departure
                else -> Pair(
                    busInfoOfPosition[indexOfMin][(busOrder[indexOfMin][0])*2-2],
                    busInfoOfPosition[indexOfMin][(busOrder[indexOfMin][0])*2-1]
                )
            }
            time += manhattan(busInfoOfPosition[indexOfMin][0] to busInfoOfPosition[indexOfMin][1], x)
            for (i in 0 until 4) {
                if (busOrder[indexOfMin][i+1] == -1) continue
                x = when {
                    busOrder[indexOfMin][i] == 0 -> destination
                    busOrder[indexOfMin][i] == 1 -> departure
                    else -> Pair(
                        busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i] * 2 - 2],
                        busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i] * 2 - 1]
                    )
                }

                val y = when {
                    busOrder[indexOfMin][i + 1] == 0 -> destination
                    busOrder[indexOfMin][i + 1] == 1 -> departure
                    else -> Pair(
                        busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i + 1] * 2 - 2],
                        busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i + 1] * 2 - 1]
                    )
                }
                time += manhattan(x, y)
                if (busOrder[indexOfMin][i+1] >= 2){
                    busInfoOfTime[indexOfMin][busOrder[indexOfMin][i]-2] = time / 40
                } else if (busOrder[indexOfMin][i+1] == 0){
                    //同じバスに乗る場合ここに入れてない
                    for (j in 0 until 4) {
                        if (busInfoOfTime[indexOfMin][j] == -1) {
                            if (time / 40 > timeToBePatient){ //ここで基準値より時間かかってるか判断
                                cancelReservation++
                                busInfoOfTime[indexOfMin] = trayTime
                            } else {
                                busInfoOfTime[indexOfMin][j] = time / 40 //新規予約の達成時間
                                busInfoOfPosition[indexOfMin][j * 2 + 2] = destination.first
                                busInfoOfPosition[indexOfMin][j * 2 + 3] = destination.second
                            }
                            break
                        }
                    }
                }




            }
            busInfoOfTime[indexOfMin][4] = -1
            busInfoOfTime[indexOfMin][4] = busInfoOfTime[indexOfMin].max()!!
        } else {
            cancelReservation++
        }

        beforeTime = currentTime


    }


}

fun manhattan(xy1: Pair<Int, Int>, xy2: Pair<Int, Int>): Int{
    val x = abs(xy1.first - xy2.first)
    val y = abs(xy1.second - xy2.second)
    return x + y
}


import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Position(val departure: Pair<Int, Int>, val destination: Pair<Int, Int>)

class Demand(private val numberOfBus: Int){

    val busInfoOfPosition = MutableList(numberOfBus){ MutableList(10){ -1 } }
    //[現在地のｘ座標、現在地のｙ座標、一人目のｘ座標、一人目のｙ座標・・・]
    //なぜpositionクラスを使わなかったのか・・・非常に悔やまれるし絶対書き換えた方がいい
    val busInfoOfTime = MutableList(numberOfBus){ MutableList(5){ -1 } }
    //[一人目到着時間、・・・、総達成時間]
    var beforeTime = 0
    
    fun demand(): Position {
        val range = (1..1024)
        val departure = Pair(range.random(), range.random())
        val destination = Pair(range.random(), range.random())
        return Position(departure, destination)
    }

    fun travelingSalesman(currentTime: Int, departureAndDestination: Position){
        val departure = departureAndDestination.departure
        val destination = departureAndDestination.destination

        val costList = MutableList(numberOfBus){ 0 } //バスごとの新規予約を引き受けた時の総達成時間から引き受けなかった時の総達成時間を引いたもの
        val busOrder = MutableList(numberOfBus){ MutableList(5){-1} } // 予約達成順位を保存する配列

        val timeCourse = currentTime - beforeTime //現在時刻から以前の予約の時刻を引いて何分たったか
        for (i in 0 until numberOfBus){

            //↑6*6の、一行目と一列目が一人目に関する時間、二行目と二列目が二人目に関する。。。ってなる二次元配列5と6は新規の出発地、目的地
            var raiding = arrayListOf<Int>()
            //バスに人が乗ってるかどうかの判断（予約が入った時に時間が進む）と現在位置の更新
            //ここで到着予定時刻も更新しなければならない→busOrderを使う
            var minOfDistance = 999999
            var moved = 0
            var nextMoved = -1
            var beforeBusStop = Pair(-1, -1)
            for (j in 0 until 4){
                if (busInfoOfTime[i][j] <= timeCourse && busInfoOfTime[i][j] != -1){
                    if (moved < busInfoOfTime[i][j]){
                        moved = busInfoOfTime[i][j]
                        beforeBusStop = Pair(busInfoOfPosition[i][j*2+2], busInfoOfPosition[i][j*2+3])
                    }
                    busInfoOfTime[i][j] = -1
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
            val canMove = timeCourse * 40
            //↑バス停一個あたり15.625ｍなので、かける40することで分速625ｍ、つまり時速37.5ｋｍを想定
            if (x > canMove && beforeBusStop.first > busInfoOfPosition[i][nextMoved*2+2]){
                busInfoOfPosition[i][0] = beforeBusStop.first - canMove
                busInfoOfPosition[i][1] = beforeBusStop.second
            } else if (x > canMove && beforeBusStop.first < busInfoOfPosition[i][nextMoved*2+2]){
                busInfoOfPosition[i][0] = beforeBusStop.first + canMove
                busInfoOfPosition[i][1] = beforeBusStop.second
            } else if (x <= canMove && beforeBusStop.second > busInfoOfPosition[i][nextMoved*2+3]){
                busInfoOfPosition[i][0] = busInfoOfPosition[i][nextMoved*2+2]
                busInfoOfPosition[i][1] = beforeBusStop.second - (canMove + x)
            } else if (x <= canMove && beforeBusStop.second < busInfoOfPosition[i][nextMoved*2+3]){
                busInfoOfPosition[i][0] = busInfoOfPosition[i][nextMoved*2+2]
                busInfoOfPosition[i][1] = beforeBusStop.second + (canMove + x)
            }


            val passengers = raiding.size
            val distance = MutableList(passengers+2){ MutableList(passengers+2){ Int.MAX_VALUE } }

            //バスに人が乗ってる場合、その人の目的地と新しく追加される人の出発地と目的地を合わせた全ての点間の距離を求める
            for (j in 1 until passengers+1){
                for (k in j+1 until passengers+3){
                    if (k < 5 && busInfoOfPosition[i][j*2] != -1 && busInfoOfPosition[i][k*2] != -1){
                        distance[j-1][k-1] = (manhattan(busInfoOfPosition[i][j*2] to busInfoOfPosition[i][j*2+1], busInfoOfPosition[i][k*2] to busInfoOfPosition[i][k*2+1]))
                        distance[k-1][j-1] = (manhattan(busInfoOfPosition[i][j*2] to busInfoOfPosition[i][j*2+1], busInfoOfPosition[i][k*2] to busInfoOfPosition[i][k*2+1]))
                    } else if (k == 5 && busInfoOfPosition[i][j*2] != -1){
                        distance[j-1][k-1] = (manhattan(busInfoOfPosition[i][j*2] to busInfoOfPosition[i][j*2+1], departure))
                        distance[k-1][j-1] = (manhattan(busInfoOfPosition[i][j*2] to busInfoOfPosition[i][j*2+1], departure))
                    } else if (k == 6 && busInfoOfPosition[i][j*2] != -1){
                        distance[j-1][k-1] = (manhattan(busInfoOfPosition[i][j*2] to busInfoOfPosition[i][j*2+1], destination))
                        distance[k-1][j-1] = (manhattan(busInfoOfPosition[i][j*2] to busInfoOfPosition[i][j*2+1], destination))
                    }
                }
            }

            //乗客人数は変わるけど出発地と目的地だけは変わらへんねんからそっちを0と1にしなあかんやろがい
            //出発地0、目的地1、一人目2、二人目3、、、
            for (j in 0 until passengers+2){
                for (k in j+1 until passengers+2){
                    if (j == 0 && k == 1){
                        distance[j][k] = manhattan(departure, destination)
                        distance[k][j] = manhattan(destination, departure)
                    } else if (j == 0){
                        distance[j][k] = (manhattan(departure, busInfoOfPosition[i][k*2-2] to busInfoOfPosition[i][k*2-1]))
                        distance[k][j] = (manhattan(busInfoOfPosition[i][k*2-2] to busInfoOfPosition[i][k*2-1], departure))
                    } else if (j == 1){
                        distance[j][k] = (manhattan(destination, busInfoOfPosition[i][k*2-2] to busInfoOfPosition[i][k*2-1]))
                        distance[k][j] = (manhattan(busInfoOfPosition[i][k*2-2] to busInfoOfPosition[i][k*2-1], destination))
                    } else {
                        distance[j][k] = (manhattan(busInfoOfPosition[i][j*2-2] to busInfoOfPosition[i][j*2-1], busInfoOfPosition[i][k*2-2] to busInfoOfPosition[i][k*2-1]))
                        distance[k][j] = (manhattan(busInfoOfPosition[i][k*2-2] to busInfoOfPosition[i][k*2-1], busInfoOfPosition[i][j*2-2] to busInfoOfPosition[i][j*2-1]))
                    }
                }
            }

            //巡回セールスマン問題という名の全探索。本当に汚いコード。書き直し推奨。
            var cost = Int.MAX_VALUE
            if (passengers == 3){
                for (j in 1 until passengers+2){ //jが一番最初に向かう目的地。0は新規予約の目的地なのでだめ
                    for (k in 0 until passengers+2){
                        if (k == j) break
                        for (l in 0 until passengers+2){
                            if (l == k || l == j) break
                            for (m in 0 until passengers+2){
                                if (m == j || m == k || m == l) break
                                for (n in 0 until passengers+2){
                                    if (n == j || n == k || n == l || n == m) break
                                    val nowCost = distance[j][k] + distance[k][l] + distance[l][m] + distance[m][n]
                                    if (nowCost < cost){
                                        busOrder[i][0] = j
                                        busOrder[i][1] = k
                                        busOrder[i][2] = l
                                        busOrder[i][3] = m
                                        busOrder[i][4] = n
                                    }
                                    cost = minOf(cost, nowCost) //これでi番目のバスが予約を受け持つときの一番早い行き方にかかる時間がわかった

                                }
                            }
                        }
                    }
                }
            } else if (passengers == 2){
                for (j in 1 until passengers+2){ //jが一番最初に向かう目的地。0は新規予約の目的地なのでだめ
                    for (k in 0 until passengers+2){
                        if (k == j) break
                        for (l in 0 until passengers+2){
                            if (l == k || l == j) break
                            for (m in 0 until passengers+2){
                                if (m == j || m == k || m == l) break

                                val nowCost = distance[j][k] + distance[k][l] + distance[l][m]
                                if (nowCost < cost){
                                    busOrder[i][0] = j
                                    busOrder[i][1] = k
                                    busOrder[i][2] = l
                                    busOrder[i][3] = m
                                }
                                cost = minOf(cost, nowCost) //これでi番目のバスが予約を受け持つときの一番早い行き方にかかる時間がわかった
                            }
                        }
                    }
                }
            } else if (passengers == 1){
                for (j in 1 until passengers+2){ //jが一番最初に向かう目的地。0は新規予約の目的地なのでだめ
                    for (k in 0 until passengers+2){
                        if (k == j) break
                        for (l in 0 until passengers+2){
                            if (l == k || l == j) break
                            val nowCost = distance[j][k] + distance[k][l]
                            if (nowCost < cost){
                                busOrder[i][0] = j
                                busOrder[i][1] = k
                                busOrder[i][2] = l
                            }
                            cost = minOf(cost, nowCost) //これでi番目のバスが予約を受け持つときの一番早い行き方にかかる時間がわかった
                        }
                    }
                }
            } else {
                cost = manhattan(departure, destination)
                busOrder[i][0] = 1
                busOrder[i][1] = 0
            }
            costList[i] = cost - busInfoOfTime[i][4]

        }
        var minOfTime = Int.MAX_VALUE
        var indexOfMin = 0
        //以下にcostListの中で一番小さいものを選択してそのバスの情報を更新するのが必要
        for (i in 0 until numberOfBus){
            if (costList[i] < minOfTime){
                indexOfMin = i
                minOfTime = costList[i]
            }
        }
        //以下に予想達成時間を入力していく
        var time = 0
        //バスの現在地からの移動
        var x = when {
            busOrder[indexOfMin][0] == 0 -> destination
            busOrder[indexOfMin][0] == 1 -> departure
            else -> Pair(busInfoOfPosition[indexOfMin][busOrder[indexOfMin][0]*2-2], busInfoOfPosition[indexOfMin][busOrder[indexOfMin][0]*2-1])
        }
        time += manhattan(busInfoOfPosition[indexOfMin][0] to busInfoOfPosition[indexOfMin][1], x)

        for (i in 0 until 4){
            if (busOrder[indexOfMin][i+1] == -1) break
            x = when {
                busOrder[indexOfMin][i] == 0 -> departure
                busOrder[indexOfMin][i] == 1 -> destination
                else -> Pair(busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i]*2-2], busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i]*2-1])
            }

            val y = when {
                busOrder[indexOfMin][i+1] == 0 -> departure
                busOrder[indexOfMin][i+1] == 1 -> destination
                else -> Pair(busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i+1]*2-2], busInfoOfPosition[indexOfMin][busOrder[indexOfMin][i+1]*2-1])
            }
            time += manhattan(x, y)

            if (busOrder[indexOfMin][i+1] == 1) continue
            else if (busOrder[indexOfMin][i+1] == 0){
                for (j in 0 until 4){
                    if (busInfoOfTime[indexOfMin][j] == -1) {
                        busInfoOfTime[indexOfMin][j] = time / 40
                        busInfoOfPosition[indexOfMin][j*2+2] = destination.first
                        busInfoOfPosition[indexOfMin][j*2+3] = destination.second


                        break
                    }
                }
            }else{
                busInfoOfTime[indexOfMin][busOrder[indexOfMin][i]-2] = time / 40
            }
        }

        beforeTime = currentTime

    }


}

fun manhattan(xy1: Pair<Int, Int>, xy2: Pair<Int, Int>): Int{
    val x = abs(xy1.first - xy2.first)
    val y = abs(xy1.second - xy2.second)
    return x + y
}


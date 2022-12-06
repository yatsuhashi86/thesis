import java.lang.Math.pow
import kotlin.math.round

//始点と終点をひとまとめにするクラスがほしかった。辺の方向に動かすこともできる。
data class P(val i: Int, val j: Int) {
    fun around(): Array<P> {
        return arrayOf(up(), left(), down(), right())
    }

    fun up(): P {
        return P(i, j + 1)
    }

    fun down(): P {
        return P(i, j - 1)
    }

    fun left(): P {
        return P(i - 1, j)
    }

    fun right(): P {
        return P(i + 1, j)
    }

    override fun toString(): String {
        return "($i, $j)"
    }
}

class City(){
    val grid = MutableList(1025){ MutableList(1025){0} }

    //バス停が存在している格子点を1にする関数。xには1以上5以下の数を入れる。
    fun busstop(x: Int){
        var flag = -1
        var check = 1
        val count = round(pow(2.0,x.toDouble())).toInt()
        for (i in 1 until 6){
            check *= 4
            if (count == check) flag = i
        }

        val n = 10 - flag
        check = 1
        for (i in 0 until n) check*=2
        //何個おきにバス停が存在しているか。
        val everyOtherBusstop = 1024 / check

        if (flag != -1){
            for (i in 0 until 1025 step everyOtherBusstop){
                for (j in 0 until 1025 step everyOtherBusstop){
                    grid[i][j] = 1
                }
            }
        } else {
            println("busstop is error")
        }
    }


}

fun reservation(): P{
    val range = (1..1024)
    val x = range.random()
    val y = range.random()
    return P(x, y)
}
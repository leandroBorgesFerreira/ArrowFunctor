
import arrow.Kind
import arrow.data.ForListK
import arrow.data.ListK
import arrow.data.fix
import arrow.data.k
import arrow.effects.*
import arrow.effects.instances.io.functor.functor
import arrow.effects.observablek.functor.functor
import arrow.instances.listk.functor.functor
import arrow.syntax.function.compose
import arrow.typeclasses.Functor
import arrow.typeclasses.compose
import arrow.typeclasses.nest
import arrow.typeclasses.unnest
import io.reactivex.Observable

fun main(args: Array<String>) {
    val data1 : ListK<Int> = listOf(1,2,3,4,5).k()
    val newData1: Kind<ForListK, Int> = applyOurLogicWithLift(data1, ListK.functor())

    println("Caso 1!! - Eu só quero usar Collections")
    newData1.fix().forEachIndexed { i, num ->
        println("Resultado $i: $num")
    }

    val data2 : Observable<Int> = Observable.just(1,2,3,4,5)
    val newData2: Kind<ForObservableK, Int> = applyOurLogicWithLift(data2.k(), ObservableK.functor())
    println("Caso 2 - Eu quero usar RxJava")
    newData2.fix()
        .observable
        .subscribe { num ->
            println("Resultado: $num")
        }

    val data3: IO<ListK<Int>> = IO.just(listOf(1,2,3,4,5).k())
    val functor = IO.functor().compose(ListK.functor())
    val result = applyOurLogicWithLift(data3.nest(), functor)
    println("Caso 3!! - Usando Coroutines (e IO) agora")
    result.unnest()
        .fix()
        .unsafeRunSync()
        .fix()
        .forEachIndexed { i, num ->
            println("Resultado $i: $num")
        }
}

val sum10 : (Int) -> Int = { num : Int -> num + 10 }
val multBy5 : (Int) -> Int = { num : Int -> num * 5 }
val sum10MultBy5 : (Int) -> Int = multBy5 compose sum10

fun <F> applyOurLogicWithLift(data: Kind<F, Int>, functor: Functor<F>): Kind<F, Int> =
    functor.lift(sum10MultBy5).invoke(data)

fun <F> applyOurLogicWithExtensions(data: Kind<F, Int>, functor: Functor<F>) {
    functor.run {
        data.map { num ->
            sum10MultBy5(num)
        }
    }
}
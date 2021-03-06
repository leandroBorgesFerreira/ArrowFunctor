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

val sum10: (Int) -> Int = { num: Int -> num + 10 }
val multBy5: (Int) -> Int = { num: Int -> num * 5 }
val sum10MultBy5: (Int) -> Int = multBy5 compose sum10

fun main(args: Array<String>) {
    testCase1()
    testCase2()
    testCase3()
}


fun testCase1() {
    val data1: ListK<Int> = listOf(1, 2, 3, 4, 5).k()
    val newData1: Kind<ForListK, Int> = applyOurLogicWithExtensions(data1, ListK.functor(), sum10MultBy5)

    println("Caso 1!! - Eu só quero usar Collections")
    newData1.fix()
        .forEachIndexed { i, num ->
            println("Resultado $i: $num")
        }
}

fun testCase2() {
    val data2: Observable<Int> = Observable.just(1, 2, 3, 4, 5)
    val newData2: Kind<ForObservableK, Int> = applyOurLogicWithLift(data2.k(), ObservableK.functor(), sum10MultBy5)
    println("Caso 2 - Eu quero usar RxJava")
    newData2.fix()
        .observable
        .subscribe { num ->
            println("Resultado: $num")
        }
}

fun testCase3() {
    val data3: IO<ListK<Int>> = IO.just(listOf(1, 2, 3, 4, 5).k())
    val functor = IO.functor().compose(ListK.functor())
    val result = applyOurLogicWithLift(data3.nest(), functor, sum10MultBy5)
    println("Caso 3!! - Usando Coroutines (e IO) agora")
    result.unnest()
        .fix()
        .unsafeRunSync()
        .fix()
        .forEachIndexed { i, num ->
            println("Resultado $i: $num")
        }
}

fun <F> applyOurLogicWithLift(data: Kind<F, Int>, functor: Functor<F>, function: (Int) -> Int): Kind<F, Int> =
    functor.lift(function).invoke(data)

fun <F> applyOurLogicWithExtensions(data: Kind<F, Int>, functor: Functor<F>, function: (Int) -> Int): Kind<F, Int> =
    functor.run { data.map(function) }

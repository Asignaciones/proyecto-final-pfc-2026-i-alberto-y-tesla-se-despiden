package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._
import AsignacionAulasPar._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasParTest extends AnyFunSuite {

  val c1: Cursos     = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas      = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos       = (1000, 100, 1, 2)


  val c2: Cursos     = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
  val a2: Aulas      = Vector(("S201", 45), ("S202", 30))
  val d2: Distancias = Vector(Vector(0, 5), Vector(5, 0))

  // ─────────────────────────────────────────────────────────────
  // choquesPar
  // ─────────────────────────────────────────────────────────────

  test("choquesPar: asignacion [0,0,1] tiene 1 choque") {
    assert(choquesPar(c1, Vector(0, 0, 1)) == 1)
  }

  test("choquesPar: asignacion [0,1,0] no tiene choques") {
    assert(choquesPar(c1, Vector(0, 1, 0)) == 0)
  }

  test("choquesPar: resultado igual al secuencial con asignacion [0,0,0]") {
    assert(choquesPar(c1, Vector(0, 0, 0)) == choques(c1, Vector(0, 0, 0)))
  }

  test("choquesPar: asignacion vacia no tiene choques") {
    val cursos = Vector(("A", 0, 4, 10), ("B", 4, 8, 10))
    assert(choquesPar(cursos, Vector(-1, -1)) == 0)
  }

  test("choquesPar: ejemplo 2 asignacion [0,1,0,1] no tiene choques") {
    assert(choquesPar(c2, Vector(0, 1, 0, 1)) == 0)
  }

  test("choquesPar: dos cursos solapados en misma aula produce 1 choque") {
    val cursos = Vector(("A", 0, 6, 10), ("B", 4, 8, 10))
    assert(choquesPar(cursos, Vector(0, 0)) == 1)
  }

  // ─────────────────────────────────────────────────────────────
  // desperdicioPar
  // ─────────────────────────────────────────────────────────────

  test("desperdicioPar: asignacion [0,0,1] tiene desperdicio 25") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("desperdicioPar: asignacion [0,1,0] tiene desperdicio 25") {
    assert(desperdicioPar(c1, a1, Vector(0, 1, 0)) == 25)
  }

  test("desperdicioPar: resultado igual al secuencial") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 1)) == desperdicio(c1, a1, Vector(0, 0, 1)))
  }

  test("desperdicioPar: curso no asignado aporta 0") {
    val cursos = Vector(("A", 0, 4, 20))
    val aulas  = Vector(("E0", 30))
    assert(desperdicioPar(cursos, aulas, Vector(-1)) == 0)
  }

  test("desperdicioPar: capacidad exacta aporta 0") {
    val cursos = Vector(("A", 0, 4, 30))
    val aulas  = Vector(("E0", 30))
    assert(desperdicioPar(cursos, aulas, Vector(0)) == 0)
  }

  test("desperdicioPar: ejemplo 2 asignacion [0,1,0,1] tiene desperdicio 25") {
    assert(desperdicioPar(c2, a2, Vector(0, 1, 0, 1)) == 25)
  }

  // ─────────────────────────────────────────────────────────────
  // movilidadPar
  // ─────────────────────────────────────────────────────────────

  test("movilidadPar: asignacion [0,0,1] tiene movilidad 3") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("movilidadPar: asignacion [0,1,0] tiene movilidad 6") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 1, 0)) == 6)
  }

  test("movilidadPar: resultado igual al secuencial") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 1, 0)) == movilidad(c1, a1, d1, Vector(0, 1, 0)))
  }

  test("movilidadPar: todos en la misma aula tiene movilidad 0") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  test("movilidadPar: ejemplo 2 asignacion [0,1,0,1] tiene movilidad 15") {
    assert(movilidadPar(c2, a2, d2, Vector(0, 1, 0, 1)) == 15)
  }

  test("movilidadPar: un solo curso tiene movilidad 0") {
    val cursos = Vector(("A", 0, 4, 10))
    val aulas  = Vector(("E0", 20))
    val dist   = Vector(Vector(0))
    assert(movilidadPar(cursos, aulas, dist, Vector(0)) == 0)
  }

  // ─────────────────────────────────────────────────────────────
  // generarAsignacionesPar
  // ─────────────────────────────────────────────────────────────

  test("generarAsignacionesPar: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignacionesPar(2, 2).length == 4)
  }

  test("generarAsignacionesPar: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignacionesPar(3, 3).length == 27)
  }

  test("generarAsignacionesPar: 0 cursos produce 1 asignacion vacia") {
    assert(generarAsignacionesPar(0, 2).length == 1)
  }

  test("generarAsignacionesPar: 4 cursos y 2 aulas produce 16 asignaciones") {
    assert(generarAsignacionesPar(4, 2).length == 16)
  }

  test("generarAsignacionesPar: todas las asignaciones tienen longitud n") {
    val asignaciones = generarAsignacionesPar(3, 2)
    assert(asignaciones.forall(a => a.length == 3))
  }

  test("generarAsignacionesPar: mismo numero de asignaciones que la version secuencial") {
    assert(generarAsignacionesPar(3, 3).length == generarAsignaciones(3, 3).length)
  }

  // ─────────────────────────────────────────────────────────────
  // asignacionOptimaPar
  // ─────────────────────────────────────────────────────────────

  test("asignacionOptimaPar: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costo <= 37)
  }

  test("asignacionOptimaPar: resultado igual al secuencial") {
    val (_, costoSeq) = asignacionOptima(c1, a1, d1, w)
    val (_, costoPar) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costoSeq == costoPar)
  }

  test("asignacionOptimaPar: la asignacion optima tiene longitud igual a n") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  test("asignacionOptimaPar: el costo optimo del ejemplo 2 no supera 155") {
    val (_, costo) = asignacionOptimaPar(c2, a2, d2, w)
    assert(costo <= 155)
  }

  test("asignacionOptimaPar: todos los cursos quedan asignados") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0))
  }

  test("asignacionOptimaPar: los indices de aula son validos") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0 && j < a1.length))
  }
}
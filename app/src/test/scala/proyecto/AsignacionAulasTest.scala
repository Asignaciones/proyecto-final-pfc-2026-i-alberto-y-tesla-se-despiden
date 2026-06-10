package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasTest extends AnyFunSuite {

  // Ejemplo 1 del enunciado
  val c1: Cursos     = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas      = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos       = (1000, 100, 1, 2)

  // Ejemplo 2 del enunciado
  val c2: Cursos     = Vector(("F01", 0, 4, 40), ("F02", 4, 8, 25), ("F03", 8, 12, 50), ("F04", 12, 16, 15))
  val a2: Aulas      = Vector(("S201", 45), ("S202", 30))
  val d2: Distancias = Vector(Vector(0, 5), Vector(5, 0))

  // ─────────────────────────────────────────────────────────────
  // solapan
  // ─────────────────────────────────────────────────────────────

  test("solapan: M01[4,8) y M02[6,10) se solapan") {
    assert(solapan(("M01", 4, 8, 25), ("M02", 6, 10, 30)))
  }

  test("solapan: M01[4,8) y M03[12,16) no se solapan") {
    assert(!solapan(("M01", 4, 8, 25), ("M03", 12, 16, 20)))
  }

  test("solapan: cursos adyacentes [0,4) y [4,8) no se solapan") {
    assert(!solapan(("A", 0, 4, 10), ("B", 4, 8, 10)))
  }

  test("solapan: curso contenido dentro de otro se solapa") {
    assert(solapan(("A", 2, 8, 10), ("B", 4, 6, 10)))
  }

  test("solapan: cursos identicos se solapan") {
    assert(solapan(("A", 4, 8, 10), ("B", 4, 8, 10)))
  }

  test("solapan: solo se toca el inicio con el fin no se solapa") {
    assert(!solapan(("A", 0, 4, 10), ("B", 4, 8, 10)))
  }

  // ─────────────────────────────────────────────────────────────
  // choques
  // ─────────────────────────────────────────────────────────────

  test("choques: asignacion [0,0,1] tiene 1 choque (M01 y M02 en E101)") {
    assert(choques(c1, Vector(0, 0, 1)) == 1)
  }

  test("choques: asignacion [0,1,0] no tiene choques") {
    assert(choques(c1, Vector(0, 1, 0)) == 0)
  }

  test("choques: todos en la misma aula con solapamientos produce 1 choque") {
    assert(choques(c1, Vector(0, 0, 0)) == 1)
  }

  test("choques: asignacion vacia no tiene choques") {
    val cursos = Vector(("A", 0, 4, 10), ("B", 4, 8, 10))
    assert(choques(cursos, Vector(-1, -1)) == 0)
  }

  test("choques: ejemplo 2 asignacion [0,1,0,1] no tiene choques") {
    assert(choques(c2, Vector(0, 1, 0, 1)) == 0)
  }

  test("choques: dos cursos solapados en misma aula produce 1 choque") {
    val cursos = Vector(("A", 0, 6, 10), ("B", 4, 8, 10))
    assert(choques(cursos, Vector(0, 0)) == 1)
  }

  // ─────────────────────────────────────────────────────────────
  // capacidadFallida
  // ─────────────────────────────────────────────────────────────

  test("capacidadFallida: asignacion [0,0,1] no falla capacidad") {
    assert(capacidadFallida(c1, a1, Vector(0, 0, 1)) == 0)
  }

  test("capacidadFallida: F03 con 50 estudiantes falla en S201 cap 45") {
    assert(capacidadFallida(c2, a2, Vector(0, 1, 0, 1)) == 1)
  }

  test("capacidadFallida: todos los cursos en aula insuficiente") {
    val cursos = Vector(("A", 0, 4, 50), ("B", 4, 8, 50))
    val aulas  = Vector(("E0", 10))
    assert(capacidadFallida(cursos, aulas, Vector(0, 0)) == 2)
  }

  test("capacidadFallida: curso no asignado no cuenta como falla") {
    val cursos = Vector(("A", 0, 4, 50))
    val aulas  = Vector(("E0", 10))
    assert(capacidadFallida(cursos, aulas, Vector(-1)) == 0)
  }

  test("capacidadFallida: capacidad exacta no es falla") {
    val cursos = Vector(("A", 0, 4, 30))
    val aulas  = Vector(("E0", 30))
    assert(capacidadFallida(cursos, aulas, Vector(0)) == 0)
  }

  test("capacidadFallida: capacidad mayor no es falla") {
    val cursos = Vector(("A", 0, 4, 20))
    val aulas  = Vector(("E0", 30))
    assert(capacidadFallida(cursos, aulas, Vector(0)) == 0)
  }

  // ─────────────────────────────────────────────────────────────
  // desperdicio
  // ─────────────────────────────────────────────────────────────

  test("desperdicio: asignacion [0,0,1] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E101(30)-M02(30)=0, E102(40)-M03(20)=20 → 25
    assert(desperdicio(c1, a1, Vector(0, 0, 1)) == 25)
  }

  test("desperdicio: asignacion [0,1,0] tiene desperdicio 25") {
    // E101(30)-M01(25)=5, E102(40)-M02(30)=10, E101(30)-M03(20)=10 → 25
    assert(desperdicio(c1, a1, Vector(0, 1, 0)) == 25)
  }

  test("desperdicio: capacidad insuficiente aporta 0 al desperdicio") {
    // F03 tiene 50 estudiantes y S201 tiene 45, aporta 0
    // S201(45)-F01(40)=5, S202(30)-F02(25)=5, 0, S202(30)-F04(15)=15 → 25
    assert(desperdicio(c2, a2, Vector(0, 1, 0, 1)) == 25)
  }

  test("desperdicio: curso no asignado aporta 0") {
    val cursos = Vector(("A", 0, 4, 20))
    val aulas  = Vector(("E0", 30))
    assert(desperdicio(cursos, aulas, Vector(-1)) == 0)
  }

  test("desperdicio: capacidad exacta aporta 0") {
    val cursos = Vector(("A", 0, 4, 30))
    val aulas  = Vector(("E0", 30))
    assert(desperdicio(cursos, aulas, Vector(0)) == 0)
  }

  test("desperdicio: un solo curso con sobra") {
    val cursos = Vector(("A", 0, 4, 10))
    val aulas  = Vector(("E0", 40))
    assert(desperdicio(cursos, aulas, Vector(0)) == 30)
  }

  // ─────────────────────────────────────────────────────────────
  // movilidad
  // ─────────────────────────────────────────────────────────────

  test("movilidad: asignacion [0,1,0] tiene movilidad 6") {
    // orden: M01(ini=4)→aula0, M02(ini=6)→aula1, M03(ini=12)→aula0
    // d[0][1] + d[1][0] = 3 + 3 = 6
    assert(movilidad(c1, a1, d1, Vector(0, 1, 0)) == 6)
  }

  test("movilidad: asignacion [0,0,1] tiene movilidad 3") {
    // orden: M01→aula0, M02→aula0, M03→aula1
    // d[0][0] + d[0][1] = 0 + 3 = 3
    assert(movilidad(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  test("movilidad: un solo curso tiene movilidad 0") {
    val cursos = Vector(("A", 0, 4, 10))
    val aulas  = Vector(("E0", 20))
    val dist   = Vector(Vector(0))
    assert(movilidad(cursos, aulas, dist, Vector(0)) == 0)
  }

  test("movilidad: todos en la misma aula tiene movilidad 0") {
    assert(movilidad(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  test("movilidad: ejemplo 2 asignacion [0,1,0,1] tiene movilidad 15") {
    // d[0][1]+d[1][0]+d[0][1] = 5+5+5 = 15
    assert(movilidad(c2, a2, d2, Vector(0, 1, 0, 1)) == 15)
  }

  test("movilidad: cursos no asignados no cuentan en movilidad") {
    val cursos = Vector(("A", 0, 4, 10), ("B", 4, 8, 10), ("C", 8, 12, 10))
    val aulas  = Vector(("E0", 20), ("E1", 20))
    val dist   = Vector(Vector(0, 5), Vector(5, 0))
    // A→aula0, B no asignado, C→aula1: d[0][1] = 5
    assert(movilidad(cursos, aulas, dist, Vector(0, -1, 1)) == 5)
  }

  // ─────────────────────────────────────────────────────────────
  // costoAsignacion
  // ─────────────────────────────────────────────────────────────

  test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
  }

  test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
  }

  test("costoAsignacion: ejemplo 2 asignacion [0,1,0,1] cuesta 155") {
    assert(costoAsignacion(c2, a2, d2, Vector(0, 1, 0, 1), w) == 155)
  }

  test("costoAsignacion: ejemplo 2 asignacion [0,1,1,0] cuesta 160") {
    assert(costoAsignacion(c2, a2, d2, Vector(0, 1, 1, 0), w) == 160)
  }

  test("costoAsignacion: asignacion con choque tiene costo mayor a 1000") {
    val costo = costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w)
    assert(costo >= 1000)
  }

  // ─────────────────────────────────────────────────────────────
  // generarAsignaciones
  // ─────────────────────────────────────────────────────────────

  test("generarAsignaciones: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignaciones(2, 2).length == 4)
  }

  test("generarAsignaciones: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignaciones(3, 3).length == 27)
  }

  test("generarAsignaciones: 1 curso y 1 aula produce 1 asignacion") {
    assert(generarAsignaciones(1, 1).length == 1)
  }

  test("generarAsignaciones: 0 cursos produce 1 asignacion vacia") {
    assert(generarAsignaciones(0, 2).length == 1)
  }

  test("generarAsignaciones: 4 cursos y 2 aulas produce 16 asignaciones") {
    assert(generarAsignaciones(4, 2).length == 16)
  }

  test("generarAsignaciones: todas las asignaciones tienen longitud n") {
    val asignaciones = generarAsignaciones(3, 2)
    assert(asignaciones.forall(a => a.length == 3))
  }

  // ─────────────────────────────────────────────────────────────
  // asignacionOptima
  // ─────────────────────────────────────────────────────────────

  test("asignacionOptima: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo <= 37)
  }

  test("asignacionOptima: la asignacion optima tiene longitud igual a n") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  test("asignacionOptima: el costo optimo del ejemplo 2 no supera 155") {
    val (_, costo) = asignacionOptima(c2, a2, d2, w)
    assert(costo <= 155)
  }

  test("asignacionOptima: todos los cursos quedan asignados") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0))
  }

  test("asignacionOptima: los indices de aula son validos") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.forall(j => j >= 0 && j < a1.length))
  }
}
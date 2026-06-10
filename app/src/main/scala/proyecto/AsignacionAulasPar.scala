package proyecto

import common._
import AsignacionAulas._

object AsignacionAulasPar {

  /** Versión paralela de choques: divide el vector de cursos en dos mitades. */
  def choquesPar(cursos: Cursos, a: Asignacion): Int = {
    val mid = cursos.length / 2
    val indicesIzq = (0 until mid).toVector
    val indicesDer = (mid until cursos.length).toVector

    val paresIzq = indicesIzq.flatMap { i =>
      (0 until cursos.length).toVector.filter(j => j > i).map(j => (i, j))
    }
    val paresDer = indicesDer.flatMap { i =>
      (0 until cursos.length).toVector.filter(j => j > i).map(j => (i, j))
    }

    val (choquesIzq, choquesDer) = parallel(
      paresIzq.count { case (i, j) =>
        a(i) >= 0 && a(i) == a(j) && solapan(cursos(i), cursos(j))
      },
      paresDer.count { case (i, j) =>
        a(i) >= 0 && a(i) == a(j) && solapan(cursos(i), cursos(j))
      }
    )
    choquesIzq + choquesDer
  }

  /** Versión paralela de desperdicio: divide el vector de cursos en dos mitades. */
  def desperdicioPar(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {
    val mid = cursos.length / 2
    val indicesIzq = (0 until mid).toVector
    val indicesDer = (mid until cursos.length).toVector

    val (desperdicioIzq, desperdicioDer) = parallel(
      indicesIzq.map { i =>
        if (a(i) >= 0 && capAula(aulas(a(i))) >= estCurso(cursos(i)))
          capAula(aulas(a(i))) - estCurso(cursos(i))
        else 0
      }.sum,
      indicesDer.map { i =>
        if (a(i) >= 0 && capAula(aulas(a(i))) >= estCurso(cursos(i)))
          capAula(aulas(a(i))) - estCurso(cursos(i))
        else 0
      }.sum
    )
    desperdicioIzq + desperdicioDer
  }


  /** Versión paralela de movilidad: divide el vector de cursos en dos mitades. */
  def movilidadPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                   a: Asignacion): Int = {
    val asignados = cursos.indices.toVector
      .filter(i => a(i) >= 0)
      .sortBy(i => iniCurso(cursos(i)))
    val mid = asignados.length / 2
    val consIzq = asignados.take(mid).zip(asignados.take(mid).drop(1))
    val consDer = asignados.drop(mid).zip(asignados.drop(mid).drop(1))

    val (movIzq, movDer) = parallel(
      consIzq.map { case (i, j) => d(a(i))(a(j)) }.sum,
      consDer.map { case (i, j) => d(a(i))(a(j)) }.sum
    )
    movIzq + movDer + (
      if (mid > 0 && mid < asignados.length)
        d(a(asignados(mid - 1)))(a(asignados(mid)))
      else 0
      )
  }

  /**
   * Versión paralela de generarAsignaciones:
   * paraleliza la construcción usando parallel sobre los valores del primer curso.
   */
  def generarAsignacionesPar(n: Int, m: Int): Vector[Asignacion] = {
    if (n == 0) Vector(Vector.empty)
    else {
      val mid = m / 2
      val aulasIzq = (0 until mid).toVector
      val aulasDer = (mid until m).toVector

      val (izq, der) = parallel(
        aulasIzq.flatMap { aula =>
          generarAsignaciones(n - 1, m).map(resto => aula +: resto)
        },
        aulasDer.flatMap { aula =>
          generarAsignaciones(n - 1, m).map(resto => aula +: resto)
        }
      )
      izq ++ der
    }
  }

  /**
   * Versión paralela de asignacionOptima:
   * divide el espacio de candidatos en dos mitades y combina los mínimos.
   */
  def asignacionOptimaPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                          w: Pesos): (Asignacion, Int) = {
    val candidatas = generarAsignacionesPar(cursos.length, aulas.length)
    val mid = candidatas.length / 2
    val mitadIzq = candidatas.take(mid)
    val mitadDer = candidatas.drop(mid)

    val (minimoIzq, minimoDer) = parallel(
      mitadIzq.map(a => (a, costoAsignacion(cursos, aulas, d, a, w))).minBy(_._2),
      mitadDer.map(a => (a, costoAsignacion(cursos, aulas, d, a, w))).minBy(_._2)
    )

    if (minimoIzq._2 <= minimoDer._2) minimoIzq else minimoDer
  }
}
